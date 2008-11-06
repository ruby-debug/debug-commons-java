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

package org.rubyforge.debugcommons.model;

import org.rubyforge.debugcommons.DebuggerTestBase;
import org.rubyforge.debugcommons.RubyDebuggerException;
import org.rubyforge.debugcommons.RubyDebuggerProxy;

public final class RubyThreadTest extends DebuggerTestBase {
    
    public RubyThreadTest(String testName) {
        super(testName);
    }
    
    public void testRunTo() throws Exception {
        final RubyDebuggerProxy proxy = prepareProxy(
                "b=1", // 1
                "b=2", // 2
                "b=3", // 2
                "b=4"); // 3
        final IRubyLineBreakpoint[] breakpoints = new IRubyLineBreakpoint[]{
            new TestBreakpoint("test.rb", 1),
        };
        attach(proxy, breakpoints, 1);

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
    
    public void testSuspendedThread() throws Exception {
        final RubyDebuggerProxy proxy = prepareProxy(
                "loop do",
                "  a = 2",
                "  sleep 1",
                "  puts a",
                "end");
        TestBreakpoint bp2 = new TestBreakpoint("test.rb", 2);
        final IRubyLineBreakpoint[] breakpoints = new IRubyLineBreakpoint[]{
            bp2,
        };
        attach(proxy, breakpoints, 1);
        resumeSuspendedThread(proxy);
        assertSuspensionLine(2);

        bp2.setEnabled(false);
        proxy.updateBreakpoint(bp2);

        suspendedThread.resume();

        assertNull("not top stack frame", suspendedThread.getTopFrame());
        proxy.finish(true);
    }

    // XXX check and enable
//    public void testThreadsSwitching() throws Exception {
//        final RubyDebuggerProxy proxy = prepareProxy(
//                "a = Thread.new do",
//                "  sleep 0.1",
//                "  sleep 0.1",
//                "end",
//                "b = Thread.new do",
//                "  sleep 0.1",
//                "  sleep 0.1",
//                "end",
//                "a.join",
//                "b.join");
//        TestBreakpoint bp2 = new TestBreakpoint("test.rb", 2);
//        TestBreakpoint bp6 = new TestBreakpoint("test.rb", 6);
//        final IRubyLineBreakpoint[] breakpoints = new IRubyLineBreakpoint[] {
//            bp2, bp6
//        };
//        attach(proxy, breakpoints, 2);
//        RubyThread t2 = proxy.getDebugTarget().getThreadById(2);
//        RubyThread t3 = proxy.getDebugTarget().getThreadById(3);
//        assertNotNull("thread 2 is not null", t2);
//        assertNotNull("thread 3 is not null", t3);
//        assertTrue(t2.canStepOver());
//        assertTrue(t3.canStepOver());
//        t2.resume();
//        t3.resume();
//    }
    
    private void assertSuspensionLine(int line) throws RubyDebuggerException {
        RubyFrame[] frames = suspendedThread.getFrames();
        RubyFrame frame = frames[0];
        assertEquals(line, frame.getLine());
    }
    
}
