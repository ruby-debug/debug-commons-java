package org.rubyforge.debugcommons.reader;

import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class BreakpointDeletedReader extends XmlStreamReader {
    
    private int no;
    
    public BreakpointDeletedReader(XmlPullParser xpp) {
        super(xpp);
    }
    
    private void parse() throws XmlPullParserException, IOException {
        assert xpp.getName().equals("breakpointDeleted");
        no = getAttributeIntValue("no");
        ensureEndTag("breakpointDeleted");
    }
    
    public static int readBreakpointNo(final XmlPullParser xpp)
            throws IOException, XmlPullParserException {
        BreakpointDeletedReader reader = new BreakpointDeletedReader(xpp);
        reader.parse();
        return reader.no;
    }
    
}
