package org.rubyforge.debugcommons.reader;

import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class ConditionSetReader extends XmlStreamReader {
    
    private int bpNum;
    
    public ConditionSetReader(XmlPullParser xpp) {
        super(xpp);
    }
    
    private void parse() throws XmlPullParserException, IOException {
        assert xpp.getName().equals("conditionSet");
        bpNum = getAttributeIntValue("bp_id");
        ensureEndTag("conditionSet");
    }
    
    public static int readBreakpointNo(final XmlPullParser xpp)
            throws IOException, XmlPullParserException {
        ConditionSetReader reader = new ConditionSetReader(xpp);
        reader.parse();
        return reader.bpNum;
    }
    
}
