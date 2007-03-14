package org.rubyforge.debugcommons;

import org.rubyforge.debugcommons.RubyDebuggerProxy;
import org.rubyforge.debugcommons.model.IRubyBreakpoint;
import org.rubyforge.debugcommons.model.RubyFrame;
import org.rubyforge.debugcommons.model.RubyThreadInfo;
import org.rubyforge.debugcommons.model.RubyVariable;

/** Rather functional tests. */
public final class RubyDebugCommunicationTest extends CommonCommunicationTestBase {
    
    public RubyDebugCommunicationTest(final String name) {
        super(name);
        setDebuggerType(RubyDebuggerProxy.RUBY_DEBUG);
    }
    
    public void testComprehensive() throws Exception {
        final RubyDebuggerProxy proxy = prepareProxy(
                "s = Thread.new {", // 1
                "  a=5",            // 2
                "  x=6",            // 3
                "  puts 'x'",       // 4
                "}",                // 5
                "s.join",           // 6
                "b=10",             // 7
                "b=11");            // 8
        final IRubyBreakpoint[] breakpoints = new IRubyBreakpoint[] {
            new TestBreakpoint("test.rb", 3),
            new TestBreakpoint("test.rb", 7)
        };
        startDebugging(proxy, breakpoints, 1);
        
        // spawned thread suspended
        RubyThreadInfo[] ti = proxy.readThreadInfo();
        assertEquals("two thread", 2, ti.length);
        assertNotNull(suspendedThread);
        RubyFrame[] frames = suspendedThread.getFrames();
        assertEquals("one frames", 1, frames.length);
        RubyFrame frame = frames[0];
        assertEquals(3, frame.getLine());
        RubyVariable[] variables = frames[0].getVariables();
        assertEquals("self, a, b, s", 4, variables.length);
        assertEquals("self", variables[0].getName());
        assertEquals("a", variables[1].getName());
        assertEquals("b", variables[2].getName());
        assertEquals("s", variables[3].getName());
        //        RubyVariable iv = frame.inspectExpression("a == b");
        waitForEvents(proxy, 1, new Runnable() { // finish spawned thread
            public void run() {
                suspendedThread.resume();
            }
        });
        
        // main thread suspended
        ti = proxy.readThreadInfo();
        assertEquals("two thread", 1, ti.length);
        assertNotNull(suspendedThread);
        frames = suspendedThread.getFrames();
        assertEquals("one frames", 1, frames.length);
        frame = frames[0];
        assertEquals(7, frame.getLine());
        variables = frame.getVariables();
        assertEquals("self, b, s", 3, variables.length);
        assertEquals("self", variables[0].getName());
        assertEquals("b", variables[1].getName());
        assertEquals("s", variables[2].getName());
        // there is a third variable 'x' for ruby 1.8.0
        waitForEvents(proxy, 1, new Runnable() {
            public void run() {
                suspendedThread.stepOver();
            }
        });
        frames = suspendedThread.getFrames();
        assertEquals("one frames", 1, frames.length);
        frame = frames[0];
        variables = frame.getVariables();
        assertEquals("self, b, s", 3, variables.length);
        assertEquals("self", variables[0].getName());
        assertEquals("b", variables[1].getName());
        assertEquals("s", variables[2].getName());
        waitForEvents(proxy, 1, new Runnable() { // finish main thread
            public void run() {
                suspendedThread.resume();
            }
        });
    }
    
}
