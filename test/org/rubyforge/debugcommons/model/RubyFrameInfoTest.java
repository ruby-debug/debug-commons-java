package org.rubyforge.debugcommons.model;

import junit.framework.TestCase;

public class RubyFrameInfoTest extends TestCase {

    public RubyFrameInfoTest(String testName) {
        super(testName);
    }

    public void testEquality() {
        assertEquals(new RubyFrameInfo("a", 1, 1), new RubyFrameInfo("a", 1, 1));
        assertNotSame(new RubyFrameInfo("a", 1, 1), new RubyFrameInfo("b", 1, 1));
        assertNotSame(new RubyFrameInfo(null, 1, 1), new RubyFrameInfo("b", 1, 1));
        assertNotSame(new RubyFrameInfo("a", 1, 1), new RubyFrameInfo(null, 1, 1));
        assertNotSame(new RubyFrameInfo("a", 0, 1), new RubyFrameInfo("a", 1, 1));
        assertNotSame(new RubyFrameInfo("a", 1, 1), new RubyFrameInfo("a", 1, 0));
    }
}
