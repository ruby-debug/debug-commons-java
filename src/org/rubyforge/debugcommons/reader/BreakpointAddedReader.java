package org.rubyforge.debugcommons.reader;

import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class BreakpointAddedReader extends XmlStreamReader {
    
    private int no;
    
    public BreakpointAddedReader(XmlPullParser xpp) {
        super(xpp);
    }
    
    private void parse() throws XmlPullParserException, IOException {
        assert xpp.getName().equals("breakpointAdded");
        no = getAttributeIntValue("no");
        ensureEndTag("breakpointAdded");
    }
    
    public static int readBreakpointNo(final XmlPullParser xpp)
            throws IOException, XmlPullParserException {
        BreakpointAddedReader reader = new BreakpointAddedReader(xpp);
        reader.parse();
        return reader.no;
    }
    
}
