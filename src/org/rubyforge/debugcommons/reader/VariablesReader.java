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
            _variables.add(parseVariable());
        }
        this.variables = _variables.toArray(new RubyVariableInfo[_variables.size()]);
    }

    private RubyVariableInfo parseVariable() throws XmlPullParserException, IOException {
        assert xpp.getName().equals("variable") : xpp.getName() + "(type: " + Util.getType(xpp) + ") encountered";
        final String name = getAttributeValue("name");
        String value = getAttributeValue("value");
        final String kind = getAttributeValue("kind");

        if (value == null) {
            ensureEndTag("variable");
            return new RubyVariableInfo(name, kind);
        }

        final String type = getAttributeValue("type");
        final boolean hasChildren = getAttributeBoolValue("hasChildren");
        final String objectId = getAttributeValue("objectId");
        value = readValueFromElement(value);
        ensureAtEndTag(xpp, "variable");

        return new RubyVariableInfo(name, kind, value, type, hasChildren, objectId);
    }

    /**
     * Tries to read value from {@code value} sub-element, if there is no sub-element default value will be returned.
     * Note: xpp moved to the next event after sub-element.
     */
    private String readValueFromElement(final String defaultValue) throws IOException, XmlPullParserException {
        String value = defaultValue;
        final int nextTag = xpp.next();
        if (nextTag == XmlPullParser.START_TAG && "value".equals(xpp.getName())) {
            xpp.next();
            if (xpp.getEventType() == XmlPullParser.TEXT) {
                value = xpp.getText();
                xpp.next();
            }
            ensureAtEndTag(xpp, "value");
            xpp.next();
        }

        return value;
    }

    private void parseProcessingException() throws XmlPullParserException, IOException {
        LOGGER.severe("Processing exception occurred." +
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
