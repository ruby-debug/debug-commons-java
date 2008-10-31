package org.rubyforge.debugcommons.reader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.rubyforge.debugcommons.model.RubyThreadInfo;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class ThreadInfoReader extends XmlStreamReader {

    private RubyThreadInfo[] threads;

    public ThreadInfoReader(XmlPullParser xpp) {
        super(xpp);
    }

    private void parse() throws XmlPullParserException, IOException {
        List<RubyThreadInfo> _threads = new ArrayList<RubyThreadInfo>();
        assert xpp.getName().equals("threads");
        while (!(nextEvent() == XmlPullParser.END_TAG && "threads".equals(xpp.getName()))) {
            ErrorReader.flushPossibleMessage(xpp);
            assert xpp.getName().equals("thread") : xpp.getName() + " encountered";
            int id = getAttributeIntValue("id");
            String status = getAttributeValue("status");
            RubyThreadInfo info = new RubyThreadInfo(id, status);
            _threads.add(info);
            ensureEndTag("thread");
        }
        this.threads = _threads.toArray(new RubyThreadInfo[_threads.size()]);
    }

    public static RubyThreadInfo[] readThreads(final XmlPullParser xpp)
            throws IOException, XmlPullParserException {
        ThreadInfoReader reader = new ThreadInfoReader(xpp);
        reader.parse();
        return reader.threads;
    }
    
}
