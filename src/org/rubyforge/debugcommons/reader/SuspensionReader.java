package org.rubyforge.debugcommons.reader;

import java.io.IOException;
import org.rubyforge.debugcommons.model.BreakpointSuspensionPoint;
import org.rubyforge.debugcommons.model.ExceptionSuspensionPoint;
import org.rubyforge.debugcommons.model.StepSuspensionPoint;
import org.rubyforge.debugcommons.model.SuspensionPoint;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class SuspensionReader extends XmlStreamReader {
    
    private SuspensionPoint suspensionPoint;
    
    public SuspensionReader(XmlPullParser xpp) {
        super(xpp);
    }
    
    private void parse() throws XmlPullParserException, IOException {
        String name = xpp.getName();
        assert name.equals("breakpoint") || name.equals("suspended") || name.equals("exception");
        if (name.equals("breakpoint")) {
            suspensionPoint = new BreakpointSuspensionPoint();
        } else if (name.equals("exception")) {
            ExceptionSuspensionPoint exceptionPoint = new ExceptionSuspensionPoint();
            exceptionPoint.setExceptionMessage(getAttributeValue("message"));
            exceptionPoint.setExceptionType(getAttributeValue("type"));
            suspensionPoint = exceptionPoint;
        } else if (name.equals("suspended")) {
            StepSuspensionPoint stepPoint = new StepSuspensionPoint();
            String frameNoAttribute = getAttributeValue("frames");
            try {
                stepPoint.setFramesNumber(Integer.parseInt(frameNoAttribute));
                suspensionPoint = stepPoint;
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException(
                        "Could not parse: " + frameNoAttribute + ", " + xpp.getText());
            }
        } else {
            throw new IllegalStateException("Unexpected element: " + name);
        }
        suspensionPoint.setLine(getAttributeIntValue("line"));
        suspensionPoint.setFile(getAttributeValue("file"));
        suspensionPoint.setThreadId(getAttributeIntValue("threadId"));
        ensureEndTag(name);
    }
    
    public static SuspensionPoint readSuspension(final XmlPullParser xpp)
            throws IOException, XmlPullParserException {
        SuspensionReader reader = new SuspensionReader(xpp);
        reader.parse();
        return reader.suspensionPoint;
    }
    
}
