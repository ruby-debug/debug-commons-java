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
        List<RubyFrameInfo> frames = new ArrayList<RubyFrameInfo>();
        assert xpp.getName().equals("frames");
        while (!(nextEvent() == XmlPullParser.END_TAG && "frames".equals(xpp.getName()))) {
            assert xpp.getName().equals("frame") : xpp.getName() + " encountered";
            String file = getAttributeValue("file");
            int line = getAttributeIntValue("line");
            int index = getAttributeIntValue("no");
			frames.add(new RubyFrameInfo(file, line, index)) ;			
            ensureEndTag("frame");
        }
        this.frames = frames.toArray(new RubyFrameInfo[frames.size()]);
    }
    
    public static RubyFrameInfo[] readFrames(final XmlPullParser xpp)
            throws IOException, XmlPullParserException {
        FramesReader reader = new FramesReader(xpp);
        reader.parse();
        return reader.frames;
    }
    
}
