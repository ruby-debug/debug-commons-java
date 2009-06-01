/*
 * Copyright (c) 2007-2008, debug-commons team
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
            /*
             * Check for empty <variables>, e.g.:
             * <variables>
             * <message>
             * </variables>
             */
            if (Util.isEndTag(xpp, "variables")) {
                break;
            }
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
