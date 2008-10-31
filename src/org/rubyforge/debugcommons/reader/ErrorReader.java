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
