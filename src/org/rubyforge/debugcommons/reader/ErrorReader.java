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
import java.util.logging.Logger;
import org.rubyforge.debugcommons.Util;
import org.rubyforge.debugcommons.model.Message;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class ErrorReader extends XmlStreamReader {

    private static final Logger LOGGER = Logger.getLogger(ErrorReader.class.getName());
    
    private static final String ERROR_ELEMENT = "error";
    private static final String MESSAGE_ELEMENT = "message";

    private Message message;
    
    public ErrorReader(XmlPullParser xpp) {
        super(xpp);
    }
    
    private void parse() throws XmlPullParserException, IOException {
        String name = xpp.getName();
        assert name.equals("error") || name.equals("message") : "message expected, got: " + name;
        boolean debug = getAttributeBoolValue("debug");
        int eventType = xpp.next();
        assert eventType == XmlPullParser.TEXT : "text event expected";
        String text = xpp.getText();
        assert text != null : "message has text";
        message = new Message(text, debug);
        ensureEndTag(name);
        Util.logMessage(message);
    }
    
    public static Message readMessage(final XmlPullParser xpp)
            throws IOException, XmlPullParserException {
        ErrorReader reader = new ErrorReader(xpp);
        reader.parse();
        return reader.message;
    }

    public static Message tryToReadMessageOrError(final XmlPullParser xpp, final String element) throws IOException, XmlPullParserException {
        Message message = null;
        if (ERROR_ELEMENT.equals(element)) {
            message = ErrorReader.readMessage(xpp);
            LOGGER.warning("Error occured: " + message.getText());
        } else if (MESSAGE_ELEMENT.equals(element)) {
            message = ErrorReader.readMessage(xpp);
        }
        return message;
    }

    static void flushPossibleMessage(final XmlPullParser xpp) throws XmlPullParserException, IOException {
        while (ErrorReader.tryToReadMessageOrError(xpp, xpp.getName()) != null) {
            xpp.next();
        }
    }
}
