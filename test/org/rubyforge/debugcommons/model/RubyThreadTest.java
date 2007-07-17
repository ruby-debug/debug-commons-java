package org.rubyforge.debugcommons.model;

import org.rubyforge.debugcommons.DebuggerTestBase;
import org.rubyforge.debugcommons.RubyDebuggerException;
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
                    try {
                        suspendedThread.runTo(testFilePath, 3);
                    } catch (RubyDebuggerException e) {
                        throw new RuntimeException(e);
                    }
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
    
    public void testSuspendedThread() throws Exception {
        for (RubyDebuggerProxy.DebuggerType type : RubyDebuggerProxy.DebuggerType.values()) {
            setDebuggerType(type);
            final RubyDebuggerProxy proxy = prepareProxy(
                    "loop do",
                    "  a = 2",
                    "  sleep 1",
                    "  puts a",
                    "end");
            TestBreakpoint bp2 = new TestBreakpoint("test.rb", 2);
            final IRubyBreakpoint[] breakpoints = new IRubyBreakpoint[] {
                bp2,
            };
            startDebugging(proxy, breakpoints, 1);
            resumeSuspendedThread(proxy);
            assertSuspensionLine(2);
            
            bp2.setEnabled(false);
            proxy.updateBreakpoint(bp2);
            
            suspendedThread.resume();
            
            assertNull("not top stack frame", suspendedThread.getTopFrame());
            proxy.finish(true);
        }
    }
    
    public void testThreadsSwitching() throws Exception {
        setDebuggerType(RubyDebuggerProxy.CLASSIC_DEBUGGER);
        final RubyDebuggerProxy proxy = prepareProxy(
                "a = Thread.new do",
                "  sleep 0.1",
                "  sleep 0.1",
                "end",
                "b = Thread.new do",
                "  sleep 0.1",
                "  sleep 0.1",
                "end",
                "a.join",
                "b.join");
        TestBreakpoint bp2 = new TestBreakpoint("test.rb", 2);
        TestBreakpoint bp6 = new TestBreakpoint("test.rb", 6);
        final IRubyBreakpoint[] breakpoints = new IRubyBreakpoint[] {
            bp2, bp6
        };
        startDebugging(proxy, breakpoints, 2);
        RubyThread t2 = proxy.getDebugTarged().getThreadById(2);
        RubyThread t3 = proxy.getDebugTarged().getThreadById(3);
        assertNotNull("thread 2 is not null", t2);
        assertNotNull("thread 3 is not null", t3);
        assertTrue(t2.canStepOver());
        assertTrue(t3.canStepOver());
        t2.resume();
        t3.resume();
    }
    
    private void assertSuspensionLine(int line) throws RubyDebuggerException {
        RubyFrame[] frames = suspendedThread.getFrames();
        RubyFrame frame = frames[0];
        assertEquals(line, frame.getLine());
    }
    
}
