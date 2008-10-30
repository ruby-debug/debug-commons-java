package org.rubyforge.debugcommons.reader;

import java.io.IOException;
import org.rubyforge.debugcommons.Util;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class ErrorReader extends XmlStreamReader {
    
    private String message;
    
    public ErrorReader(XmlPullParser xpp) {
        super(xpp);
    }
    
    private void parse() throws XmlPullParserException, IOException {
        assert xpp.getName().equals("error") || xpp.getName().equals("message");
        boolean debug = getAttributeBoolValue("debug");
        message = xpp.nextText();
        Util.logMessage(message, debug);
    }
    
    public static String readMessage(final XmlPullParser xpp)
            throws IOException, XmlPullParserException {
        ErrorReader reader = new ErrorReader(xpp);
        reader.parse();
        return reader.message;
    }
    
}
