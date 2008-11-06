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

import org.rubyforge.debugcommons.model.IRubyLineBreakpoint;
import org.rubyforge.debugcommons.model.RubyFrame;
import org.rubyforge.debugcommons.model.RubyThreadInfo;
import org.rubyforge.debugcommons.model.RubyVariable;

/** Rather functional tests. */
public final class ClassicDebuggerCommunicationTest extends CommonCommunicationTestBase {
    
    public ClassicDebuggerCommunicationTest(final String name) {
        super(name);
        setDebuggerType(RubyDebuggerProxy.CLASSIC_DEBUGGER);
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
        final IRubyLineBreakpoint[] breakpoints = new IRubyLineBreakpoint[] {
            new TestBreakpoint("test.rb", 3),
            new TestBreakpoint("test.rb", 7)
        };
        attach(proxy, breakpoints, 1);
        
        // spawned thread suspended
        RubyThreadInfo[] ti = proxy.readThreadInfo();
        assertEquals("two thread", 2, ti.length);
        assertNotNull(suspendedThread);
        RubyFrame[] frames = suspendedThread.getFrames();
        assertEquals("one frames", 1, frames.length);
        RubyFrame frame = frames[0];
        assertEquals(3, frame.getLine());
        RubyVariable[] variables = frames[0].getVariables();
        assertEquals("a, b, s", 3, variables.length);
        assertEquals("a", variables[0].getName());
        assertEquals("b", variables[1].getName());
        assertEquals("s", variables[2].getName());
        RubyVariable inspected = frame.inspectExpression("a == b");
        assertEquals("a == b", inspected.getName());
        assertEquals("false", inspected.getValue().getValueString());
        assertEquals("FalseClass", inspected.getValue().getReferenceTypeName());
        RubyVariable unknown = frame.inspectExpression("unknown_in_context");
        resumeSuspendedThread(proxy); // finish spawned thread
        
        // main thread suspended
        ti = proxy.readThreadInfo();
        assertEquals("two thread", 1, ti.length);
        assertNotNull(suspendedThread);
        frames = suspendedThread.getFrames();
        assertEquals("one frames", 1, frames.length);
        frame = frames[0];
        assertEquals(7, frame.getLine());
        variables = frame.getVariables();
        assertEquals("b, s", 2, variables.length);
        assertEquals("b", variables[0].getName());
        assertEquals("s", variables[1].getName());
        // there is a third variable 'x' for ruby 1.8.0
        waitForEvents(proxy, 1, new Runnable() {
            public void run() {
                try {
                    suspendedThread.stepOver();
                } catch (RubyDebuggerException e) {
                    throw new RuntimeException("Cannot stepOver", e);
                }
            }
        });
        frames = suspendedThread.getFrames();
        assertEquals("one frames", 1, frames.length);
        frame = frames[0];
        variables = frame.getVariables();
        assertEquals("b, s", 2, variables.length);
        assertEquals("b", variables[0].getName());
        assertEquals("s", variables[1].getName());
        resumeSuspendedThread(proxy); // finish main thread
    }
    
    // TODO: failing test due to backend wrong synchronization
//    public void testSynchronization() throws Exception {
//        setTimeout(30);
//        final RubyDebuggerProxy proxy = prepareProxy(
//                "require 'thread'",
//                "m = Mutex.new",
//                "i = 0",
//                "(1..2).each do",
//                "  Thread.new do",
//                "    (1..5).each do",
//                "      Thread.new do",
//                "        (1..20).each do",
//                "          Thread.new do",
//                "            sleep 0.01",
//                "            m.synchronize do",
//                "              i += 1",
//                "            end",
//                "          end",
//                "        end",
//                "      end",
//                "    end",
//                "  end",
//                "end",
//                "while i != 200",
//                "  puts \"i: #{i}\"",
//                "  sleep 1",
//                "end",
//                "puts 'main thread'");
//        final IRubyLineBreakpoint[] breakpoints = new IRubyLineBreakpoint[] {
//            new TestBreakpoint("test.rb", 10),
//        };
//        attach(proxy, breakpoints, 200);
//        System.out.println("MK> OK");
//        proxy.finish(true);
//    }

}
