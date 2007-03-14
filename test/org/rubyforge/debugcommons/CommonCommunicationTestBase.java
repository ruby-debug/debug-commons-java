package org.rubyforge.debugcommons;

import java.util.Arrays;
import org.rubyforge.debugcommons.RubyDebuggerProxy;
import org.rubyforge.debugcommons.model.IRubyBreakpoint;
import org.rubyforge.debugcommons.model.RubyFrame;
import org.rubyforge.debugcommons.model.RubyThreadInfo;
import org.rubyforge.debugcommons.model.RubyVariable;

/** Rather functional tests. */
public abstract class CommonCommunicationTestBase extends DebuggerTestBase {
    
    public CommonCommunicationTestBase(final String name) {
        super(name);
    }
    
    //    public void testRubyValue() throws Exception {
    //        final RubyDebuggerProxy proxy = prepareProxy("h={'a' => {'nested1' => 1, 'nested2' => 2}}", "sleep(0.1)");
    //        final IRubyBreakpoint[] breakpoints = new IRubyBreakpoint[] {
    //            new TestBreakpoint("test.rb", 2),
    //        };
    //        startDebugging(proxy, breakpoints);
    //
    //        final RubyThread thread1 = proxy.getDebugTarged().getThreadById(1);
    //        assertNotNull(thread1);
    //        RubyFrame[] frames1 = thread1.getFrames();
    //        assertEquals("one frame", 1, frames1.length);
    //        assertEquals(2, frames1[0].getLine());
    //        RubyVariable[] variables1 = frames1[0].getVariables();
    //        assertEquals("one variable", 1, variables1.length);
    //        assertEquals("h", variables1[0].getName());
    //        RubyValue hValue = variables1[0].getValue();
    //        assertTrue(hValue.hasVariables());
    //        RubyVariable[] hVariables = hValue.getVariables();
    //        assertEquals(1, hVariables.length);
    //        RubyVariable a = hVariables[0];
    //        RubyValue aValue = a.getValue();
    //        assertTrue(aValue.hasVariables());
    //        assertEquals(2, aValue.getVariables().length);
    //        RubyVariable nested2 = aValue.getVariables()[1];
    //        assertEquals("'nested2'", nested2.getName());
    //
    //        waitForEvents(proxy, 1, new Runnable() {
    //            public void run() {
    //                thread1.resume();
    //            }
    //        });
    //    }
    //
    //    public void testStepReturnsFromLastFrame() throws Exception {
    //        final RubyDebuggerProxy proxy = prepareProxy("sleep(0.1)", "sleep(0.1)", "sleep(0.1)");
    //        final IRubyBreakpoint[] breakpoints = new IRubyBreakpoint[] {
    //            new TestBreakpoint("test.rb", 2),
    //        };
    //        startDebugging(proxy, breakpoints);
    //
    //        final RubyThread thread1 = proxy.getDebugTarged().getThreadById(1);
    //        assertNotNull(thread1);
    //        waitForEvents(proxy, 1, new Runnable() {
    //            public void run() {
    //                thread1.stepReturn();
    //                thread1.stepReturn();
    //            }
    //        });
    //
    //    }
    
    //    public void testMinimal() throws Exception {
    //        createSocket("puts 'a'");
    //        sendCont();
    //    }
    //
    //    public void testBreakpointAddAndRemove() throws Exception {
    //        createSocket("1.upto(3) {", "puts 'a'", "puts 'b'", "puts 'c'", "}");
    //        sendRuby("b test.rb:2");
    //        assertEquals(1, readBreakpointNo());
    //        sendRuby("b test.rb:4");
    //        assertEquals(2, readBreakpointNo());
    //        sendCont(); // -> 2
    //        assertTestSuspension(2, true);
    //        sendCont(); // 2 -> 4
    //        assertTestSuspension(4, true);
    //        sendCont(); // 4 -> 2
    //        assertTestSuspension(2, true);
    //        sendRuby("delete -1"); // should be ignored
    //        sendRuby("delete 100");
    //        // XXX assertError();
    //        sendRuby("delete 2");
    //        // XXX assertBreakpointDeleted();
    //        sendCont(); // 2 -> 2
    //        assertTestSuspension(2, true);
    //        sendCont(); // 2 -> finish
    //    }
    //
    //    public void testThreads() throws Exception {
    //        createSocket("Thread.new {", "puts 'a'", "}", "Thread.pass", "puts 'b'");
    //        sendRuby("b test.rb:2");
    //        sendRuby("b test.rb:5");
    //        sendCont();
    //        assertSuspension("test.rb", 5, true, 1);
    //        sendRuby("th l");
    //        assertEquals(2, readersSupport.readThreads().length);
    //        sendRuby("th 2; cont");
    //        sendRuby("th l");
    //        assertEquals(1, readersSupport.readThreads().length);
    //        sendCont();
    //    }
    
}
