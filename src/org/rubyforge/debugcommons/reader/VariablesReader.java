package org.rubyforge.debugcommons.reader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.rubyforge.debugcommons.Util;
import org.rubyforge.debugcommons.model.RubyVariableInfo;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class VariablesReader extends XmlStreamReader {

    private static final Logger LOGGER = Logger.getLogger(VariablesReader.class.getName());

    private RubyVariableInfo[] variables;

    public VariablesReader(XmlPullParser xpp) {
        super(xpp);
    }

    private void parse() throws XmlPullParserException, IOException {
        String element = xpp.getName();
        assert element.equals("variables") || element.equals("processingException");
        if (element.equals("variables")) {
            parseVariables();
        } else if (element.equals("processingException")) {
            parseProcessingException();
        } else {
            assert false : "Unexpected element: " + element;
        }
    }

    private void parseVariables() throws XmlPullParserException, IOException {
        List<RubyVariableInfo> _variables = new ArrayList<RubyVariableInfo>();
        while (!(nextEvent() == XmlPullParser.END_TAG && "variables".equals(xpp.getName()))) {
            // Seems to happen on this place from time to time.
            if (xpp.getName() == null) {
                throw new XmlPullParserException("xpp.getName() returned 'null'. " +
                        "Segmentation fault. Bug in the Ruby interpreter/VM. " +
                        "Please provide possibly exact steps to reproduce " +
                        "and file a bug against Ruby or debug-commons tracker.");
            }
            ErrorReader.flushPossibleMessage(xpp);
            assert xpp.getName().equals("variable") : xpp.getName() + "(type: " + Util.getType(xpp) + ") encountered";
            String name = getAttributeValue("name");
            String value = getAttributeValue("value");
            String kind = getAttributeValue("kind");
            RubyVariableInfo newVariable;
            if (value == null) {
                newVariable = new RubyVariableInfo(name, kind);
            } else {
                String type = getAttributeValue("type");
                boolean hasChildren = getAttributeBoolValue("hasChildren");
                String objectId = getAttributeValue("objectId");
                newVariable = new RubyVariableInfo(name, kind, value, type, hasChildren, objectId);
            }
            _variables.add(newVariable);
            ensureEndTag("variable");
        }
        this.variables = _variables.toArray(new RubyVariableInfo[_variables.size()]);
    }

    private void parseProcessingException() throws XmlPullParserException, IOException {
        LOGGER.severe("Processing exception occured." +
                " exceptionMessage: " + getAttributeValue("message") +
                ", exceptionType: " + getAttributeValue("type"));
        ensureEndTag("processingException");
    }

    public static RubyVariableInfo[] readVariables(final XmlPullParser xpp)
            throws IOException, XmlPullParserException {
        VariablesReader reader = new VariablesReader(xpp);
        reader.parse();
        return reader.variables;
    }

    public static void logProcessingException(final XmlPullParser xpp)
            throws IOException, XmlPullParserException {
        VariablesReader reader = new VariablesReader(xpp);
        reader.parse();
    }

}
