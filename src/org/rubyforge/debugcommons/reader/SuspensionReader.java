/*
 * Copyright (c) 2007-2008, debug-commons team
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
