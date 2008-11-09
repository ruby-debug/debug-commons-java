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

package org.rubyforge.debugcommons;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rubyforge.debugcommons.DebuggerTestBase.TestBreakpoint;
import org.rubyforge.debugcommons.model.IRubyBreakpoint;
import org.rubyforge.debugcommons.model.IRubyLineBreakpoint;
import org.rubyforge.debugcommons.model.RubyDebugTarget;
import org.rubyforge.debugcommons.model.RubyFrame;

public final class RubyDebuggerProxyTest extends DebuggerTestBase {
    
    private static final Logger LOGGER = Logger.getLogger(RubyDebuggerProxyTest.class.getName());

    public RubyDebuggerProxyTest(String testName) {
        super(testName);
    }

    public void testAttach() throws Exception {
        String[] testContent = {
            "sleep 0.01",
            "sleep 0.01",
            "sleep 0.01",
            "sleep 0.01"};
        File testF = testFile = writeFile("test.rb", testContent);
        int port = 12345;
        Process process = startDebuggerProcess(testF, port);
        RubyDebugTarget debugTarget = null;
        boolean success = false;
        try {
            RubyDebuggerProxy proxy = new RubyDebuggerProxy(RubyDebuggerProxy.RUBY_DEBUG, 6);
            debugTarget = new RubyDebugTarget(proxy, "localhost", port);
            proxy.setDebugTarget(debugTarget);
            final IRubyLineBreakpoint[] breakpoints = new IRubyLineBreakpoint[]{
                new TestBreakpoint("test.rb", 2),
                new TestBreakpoint("test.rb", 3)
            };
            attach(proxy, breakpoints, 1);
            resumeSuspendedThread(proxy); // 2 -> 3
            resumeSuspendedThread(proxy); // 3 -> finish
            assertFalse(Util.isRunning(process));
            success = true;
        } finally {
            if (!success && debugTarget != null) {
                Util.dumpAndDestroyProcess(debugTarget);
            }
        }
    }

    public void testIsFinished() throws Exception {
        final RubyDebuggerProxy proxy = prepareProxy("sleep 0.01");
        assertFalse("proxy not ready yet", proxy.isReady());
        attach(proxy, new IRubyLineBreakpoint[]{}, 0);
        assertTrue("proxy not ready yet", proxy.isReady());
    }
    
    public void testBreakpointsRemoving1() throws Exception {
        final RubyDebuggerProxy proxy = prepareProxy(
                "3.times do", // 1
                "  b=10",     // 2
                "  b=11",     // 3
                "end");       // 4
        final IRubyLineBreakpoint[] breakpoints = new IRubyLineBreakpoint[] {
            new TestBreakpoint("test.rb", 2),
            new TestBreakpoint("test.rb", 3),
        };
        attach(proxy, breakpoints, 1);
        
        // do one cycle
        resumeSuspendedThread(proxy); // 2 -> 3
        resumeSuspendedThread(proxy); // 3 -> 2
        
        IRubyLineBreakpoint second = breakpoints[1];
        proxy.removeBreakpoint(second);
        
        // finish
        resumeSuspendedThread(proxy); // 2 -> 2
        resumeSuspendedThread(proxy); // 2 -> finish
    }
    
    public void testBreakpointsRemovingAdding() throws Exception {
        final RubyDebuggerProxy proxy = prepareProxy(
                "3.times do", // 1
                "  b=10",     // 2
                "  b=11",     // 3
                "end");       // 4
        final IRubyLineBreakpoint[] breakpoints = new IRubyLineBreakpoint[] {
            new TestBreakpoint("test.rb", 2),
            new TestBreakpoint("test.rb", 3),
        };
        attach(proxy, breakpoints, 1);
        
        // do one cycle
        resumeSuspendedThread(proxy); // 2 -> 3
        resumeSuspendedThread(proxy); // 3 -> 2
        
        final IRubyLineBreakpoint first = breakpoints[0];
        proxy.removeBreakpoint(first);
        
        // finish
        resumeSuspendedThread(proxy); // 2 -> 3
        resumeSuspendedThread(proxy); // 3 -> 3
        resumeSuspendedThread(proxy); // 3 -> finish
    }
    
    public void testBreakpointsUpdating() throws Exception {
        final RubyDebuggerProxy proxy = prepareProxy(
                "4.times do", // 1
                "  b=10", // 2
                "  b=11", // 3
                "end");       // 4
        final TestBreakpoint[] breakpoints = new TestBreakpoint[]{
            new TestBreakpoint("test.rb", 2),
            new TestBreakpoint("test.rb", 3),
        };
        attach(proxy, breakpoints, 1);

        // do one cycle
        resumeSuspendedThread(proxy); // 2 -> 3
        resumeSuspendedThread(proxy); // 3 -> 2

        // disable first breakpoint
        final TestBreakpoint first = breakpoints[0];
        first.setEnabled(false);
        proxy.updateBreakpoint(first);

        resumeSuspendedThread(proxy); // 2 -> 3
        resumeSuspendedThread(proxy); // 3 -> 3

        // reenable first breakpoint
        first.setEnabled(true);
        proxy.updateBreakpoint(first);

        resumeSuspendedThread(proxy); // 3 -> 2
        resumeSuspendedThread(proxy); // 2 -> 3
        resumeSuspendedThread(proxy); // 3 -> finish
    }
    
    public void testFinish() throws Exception {
        final RubyDebuggerProxy proxy = prepareProxy(
                "b=1", // 1
                "b=2", // 2
                "b=3"); // 3
        final TestBreakpoint[] breakpoints = new TestBreakpoint[]{
            new TestBreakpoint("test.rb", 2),
        };
        attach(proxy, breakpoints, 1);
        proxy.finish(true);
    }

    public void testStepOver() throws Exception {
        final RubyDebuggerProxy proxy = prepareProxy(
                "10.times do",  // 1
                "  sleep 0.1",  // 2
                "end");         // 3
        final TestBreakpoint[] breakpoints = new TestBreakpoint[] {
            new TestBreakpoint("test.rb", 2),
        };
        attach(proxy, breakpoints, 1);
        resumeSuspendedThread(proxy);
        proxy.removeBreakpoint(breakpoints[0]);
        doStepOver(proxy, false);
        doStepOver(proxy, false);
        doStepOver(proxy, false);
        doStepOver(proxy, true);
    }

    public void testStepReturnOnFrame() throws Exception {
        final RubyDebuggerProxy proxy = prepareProxy(
                "def a",      // 1
                "  sleep 0.1",// 2
                "end",        // 3
                "def b",      // 4
                "  a",        // 5
                "  sleep 0.1",// 6
                "end",        // 7
                "def c",      // 8
                "  b",        // 9
                "  sleep 0.1",// 10
                "end",        // 11
                "c",          // 12
                "sleep 0.1"); // 13
        final TestBreakpoint[] breakpoints = new TestBreakpoint[] {
            new TestBreakpoint("test.rb", 2),
        };
        attach(proxy, breakpoints, 1);
        RubyFrame[] frames = suspendedThread.getFrames();
        assertEquals("four frames", 4, frames.length);
        final RubyFrame frame = frames[2];
        assertEquals("line 9", 9, frame.getLine());
        waitForEvents(proxy, 1,new Runnable() {
            public void run() {
                frame.stepReturn();
                suspendedThread = null;
            }
        });
        assertEquals("one frame", 1, suspendedThread.getFrames().length);
        resumeSuspendedThread(proxy);
    }

    public void testStepReturnOnThread() throws Exception {
        final RubyDebuggerProxy proxy = prepareProxy(
                "def a", // 1
                "  sleep 0.1",// 2
                "end", // 3
                "def b", // 4
                "  a", // 5
                "  sleep 0.1",// 6
                "end", // 7
                "def c", // 8
                "  b", // 9
                "  sleep 0.1",// 10
                "end", // 11
                "c", // 12
                "sleep 0.1"); // 13
        final TestBreakpoint[] breakpoints = new TestBreakpoint[]{
            new TestBreakpoint("test.rb", 2),
        };
        attach(proxy, breakpoints, 1);
        RubyFrame[] frames = suspendedThread.getFrames();
        assertEquals("four frames", 4, frames.length);
        waitForEvents(proxy, 1, new Runnable() {

            public void run() {
                try {
                    suspendedThread.stepReturn();
                    suspendedThread = null;
                } catch (RubyDebuggerException e) {
                    LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
                }
            }
        });
        assertEquals("three frames", 3, suspendedThread.getFrames().length);
        resumeSuspendedThread(proxy);
    }

    public void testCondition() throws Exception {
        final RubyDebuggerProxy proxy = prepareProxy(
                "1.upto(10) do |i|",
                "  sleep 0.01",
                "  sleep 0.01",
                "end");
        final TestBreakpoint[] breakpoints = new TestBreakpoint[]{
            new TestBreakpoint("test.rb", 2, "i>7"),
        };
        attach(proxy, breakpoints, 1); // i == 8

        resumeSuspendedThread(proxy); // i == 9
        resumeSuspendedThread(proxy); // i == 10
        resumeSuspendedThread(proxy); // finish
    }
    
    public void testCatchpoint() throws Exception {
        final RubyDebuggerProxy proxy = prepareProxy(
                "sleep 0.01",
                "5/0",
                "sleep 0.01");
        final IRubyBreakpoint[] breakpoints = new IRubyBreakpoint[] {
            new TestBreakpoint("test.rb", 1),
            new TestExceptionBreakpoint("ZeroDivisionError"),
        };
        attach(proxy, breakpoints, 1); // 1
        doStepOver(proxy, false);
        doStepOver(proxy, false);
        resumeSuspendedThread(proxy); // finish
    }

    public void testCatchpointRemoving() throws Exception {
        final RubyDebuggerProxy proxy = prepareProxy(
                "1.upto(10) do",
                "  sleep 0.1",
                "  abc = 3/0 rescue ZeroDivisionError",
                "end");
        IRubyBreakpoint catchpoint = new TestExceptionBreakpoint("ZeroDivisionError");
        final IRubyBreakpoint[] breakpoints = new IRubyBreakpoint[]{
            catchpoint,
        };
        attach(proxy, breakpoints, 1); // 1
        resumeSuspendedThread(proxy);
        resumeSuspendedThread(proxy);
        proxy.removeBreakpoint(catchpoint);
        resumeSuspendedThread(proxy);
    }

    public void testCatchpointReAdding() throws Exception {
        final RubyDebuggerProxy proxy = prepareProxy(
                "1.upto(10) do",
                "  sleep 0.1",
                "  abc = 3/0 rescue ZeroDivisionError",
                "end");
        IRubyBreakpoint catchpoint = new TestExceptionBreakpoint("ZeroDivisionError");
        TestBreakpoint breakpoint = new TestBreakpoint("test.rb", 2);
        final IRubyBreakpoint[] breakpoints = new IRubyBreakpoint[]{
            breakpoint,
            catchpoint,
        };
        attach(proxy, breakpoints, 1); // 2
        resumeSuspendedThread(proxy); // 2 -> 3
        resumeSuspendedThread(proxy); // 3 -> 2
        proxy.removeBreakpoint(catchpoint);
        resumeSuspendedThread(proxy); // 2 -> 2
        proxy.removeBreakpoint(breakpoint);
        proxy.addBreakpoint(catchpoint);
        resumeSuspendedThread(proxy); // 2 -> 3
        resumeSuspendedThread(proxy); // 3 -> 3
        proxy.removeBreakpoint(catchpoint);
        resumeSuspendedThread(proxy); // 3 -> finish
    }

    public void testRemovingBreakpointFromMultipleThreads() throws Exception {
        final RubyDebuggerProxy proxy = prepareProxy(
                "sleep 0.001", // 1
                "sleep 0.001", // 2
                "sleep 0.001", // 3
                "sleep 0.001", // 4
                "sleep 0.001", // 5
                "sleep 0.001", // 6
                "sleep 0.001", // 7
                "sleep 0.001", // 8
                "sleep 0.001");// 9
        final IRubyLineBreakpoint[] breakpoints = new IRubyLineBreakpoint[9];
        for (int i = 0; i < breakpoints.length; i++) {
            breakpoints[i] = new TestBreakpoint("test.rb", i + 1);
        }
        attach(proxy, breakpoints, 1);

        final CountDownLatch remove = new CountDownLatch(breakpoints.length);
        for (final IRubyLineBreakpoint bp : breakpoints) {
            new Thread(new Runnable() {
                public void run() {
                    proxy.removeBreakpoint(bp);
                    remove.countDown();
                }
            }).start();
        }
        remove.await();

        resumeSuspendedThread(proxy); // finish
    }
}
