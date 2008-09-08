package org.rubyforge.debugcommons.reader;

import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class CatchpointSetReader extends XmlStreamReader {
    
    private String exception;
    
    public CatchpointSetReader(XmlPullParser xpp) {
        super(xpp);
    }
    
    private void parse() throws XmlPullParserException, IOException {
        assert xpp.getName().equals("catchpointSet");
        exception = getAttributeValue("exception");
        ensureEndTag("catchpointSet");
    }
    
    public static String readExceptionClassName(final XmlPullParser xpp)
            throws IOException, XmlPullParserException {
        CatchpointSetReader reader = new CatchpointSetReader(xpp);
        reader.parse();
        return reader.exception;
    }
    
}
