package org.rubyforge.debugcommons.reader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class BreakpointDisabledReader extends XmlStreamReader {

    private int no;

    public BreakpointDisabledReader(XmlPullParser xpp) {
        super(xpp);
    }

    private void parse() throws XmlPullParserException, IOException {
        assert xpp.getName().equals("breakpointDisabled");
        no = getAttributeIntValue("bp_id");
        ensureEndTag("breakpointDisabled");
    }

    public static int readBreakpointNo(final XmlPullParser xpp)
            throws IOException, XmlPullParserException {
        BreakpointDisabledReader reader = new BreakpointDisabledReader(xpp);
        reader.parse();
        return reader.no;
    }
}
