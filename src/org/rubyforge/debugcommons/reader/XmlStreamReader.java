package org.rubyforge.debugcommons.reader;

import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public abstract class XmlStreamReader {
    
    protected final XmlPullParser xpp;
    
    public XmlStreamReader(XmlPullParser xpp) {
        this.xpp = xpp;
    }
    
    protected void ensureEndTag(final String name) throws XmlPullParserException, IOException {
        int nextTag = xpp.nextTag();
        if (nextTag != XmlPullParser.END_TAG && !name.equals(xpp.getName())) {
            throw new IllegalStateException(
                    "Unexpected event. Expecting " + name + " end tag." + xpp.getName());
        }
    }
    
    /**
     * Works like {@link XmlPullParser#next} but skips all {@link
     * XmlPullParser#TEXT} events.
     */
    protected int nextEvent() throws XmlPullParserException, IOException {
        int eventType = -1;
        while ((eventType = xpp.next()) == XmlPullParser.TEXT) {
            // skip
            // Util.finest("Skipping text event");
        }
        return eventType;
    }
    
    protected String getAttributeValue(final String attrName) {
        return xpp.getAttributeValue("", attrName);
    }
    
    protected int getAttributeIntValue(final String attrName) {
        return Integer.parseInt(getAttributeValue(attrName));
    }
    
    protected boolean getAttributeBoolValue(final String attrName) {
        return getAttributeValue(attrName).equals("true");
    }
    
}
