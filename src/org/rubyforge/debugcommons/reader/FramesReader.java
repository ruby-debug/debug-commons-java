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
import java.util.ArrayList;
import java.util.List;
import org.rubyforge.debugcommons.model.RubyFrameInfo;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class FramesReader extends XmlStreamReader {

    private RubyFrameInfo[] frames;

    public FramesReader(XmlPullParser xpp) {
        super(xpp);
    }

    private void parse() throws XmlPullParserException, IOException {
        List<RubyFrameInfo> _frames = new ArrayList<RubyFrameInfo>();
        assert xpp.getName().equals("frames");
        while (!(nextEvent() == XmlPullParser.END_TAG && "frames".equals(xpp.getName()))) {
            ErrorReader.flushPossibleMessage(xpp);
            assert xpp.getName().equals("frame") : xpp.getName() + " encountered";
            String file = getAttributeValue("file");
            int line = getAttributeIntValue("line");
            int index = getAttributeIntValue("no");
            _frames.add(new RubyFrameInfo(file, line, index));
            ensureEndTag("frame");
        }
        this.frames = _frames.toArray(new RubyFrameInfo[_frames.size()]);
    }

    public static RubyFrameInfo[] readFrames(final XmlPullParser xpp)
            throws IOException, XmlPullParserException {
        FramesReader reader = new FramesReader(xpp);
        reader.parse();
        return reader.frames;
    }
    
}
