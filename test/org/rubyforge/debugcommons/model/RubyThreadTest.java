package org.rubyforge.debugcommons.model;

import org.rubyforge.debugcommons.DebuggerTestBase;
import org.rubyforge.debugcommons.RubyDebuggerProxy;

public final class RubyThreadTest extends DebuggerTestBase {
    
    public RubyThreadTest(String testName) {
        super(testName);
    }
    
    public void testRunTo() throws Exception {
        for (RubyDebuggerProxy.DebuggerType type : RubyDebuggerProxy.DebuggerType.values()) {
            setDebuggerType(type);
            final RubyDebuggerProxy proxy = prepareProxy(
                    "b=1",  // 1
                    "b=2",  // 2
                    "b=3",  // 2
                    "b=4"); // 3
            final IRubyBreakpoint[] breakpoints = new IRubyBreakpoint[] {
                new TestBreakpoint("test.rb", 1),
            };
            startDebugging(proxy, breakpoints, 1);
            
            waitForEvents(proxy, 1, new Runnable() {
                public void run() {
                    suspendedThread.runTo(testFilePath, 3);
                }
            });
            
            RubyFrame[] frames = suspendedThread.getFrames();
            assertEquals("one frames", 1, frames.length);
            RubyFrame frame = frames[0];
            assertEquals(3, frame.getLine());
            
            // finish
            resumeSuspendedThread(proxy); // 3 -> finish
        }
    }
    
}
