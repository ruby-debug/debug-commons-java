package org.rubyforge.debugcommons.reader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class BreakpointEnabledReader extends XmlStreamReader {

    private int no;

    public BreakpointEnabledReader(XmlPullParser xpp) {
        super(xpp);
    }

    private void parse() throws XmlPullParserException, IOException {
        assert xpp.getName().equals("breakpointEnabled");
        no = getAttributeIntValue("bp_id");
        ensureEndTag("breakpointEnabled");
    }

    public static int readBreakpointNo(final XmlPullParser xpp)
            throws IOException, XmlPullParserException {
        BreakpointEnabledReader reader = new BreakpointEnabledReader(xpp);
        reader.parse();
        return reader.no;
    }
}
