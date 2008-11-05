package org.rubyforge.debugcommons;

import org.rubyforge.debugcommons.model.IRubyLineBreakpoint;

public class ReadersSupportTest extends DebuggerTestBase {
    
    public ReadersSupportTest(final String name) {
        super(name);
    }
    
    public void testRubyDebuggerExceptionIsThrown() throws Exception {
        final RubyDebuggerProxy proxy = prepareProxy(
                "b=10",  // 1
                "b=11"); // 2
        final IRubyLineBreakpoint[] breakpoints = new IRubyLineBreakpoint[] {
            new TestBreakpoint("test.rb", 1),
        };
        attach(proxy, breakpoints, 1);
        try {
            proxy.getReadersSupport().readFrames();
            fail("RubyDebuggerException expected");
        } catch (RubyDebuggerException e) {
            // OK - expected
        }
        proxy.getDebugTarged().getThreadById(1).resume();
    }
    
    public void testNPENotThrownWhenReadingAddedBreakpoints() throws Exception {
        final RubyDebuggerProxy proxy = prepareProxy(
                "b=10",  // 1
                "b=11"); // 2
        final IRubyLineBreakpoint[] breakpoints = new IRubyLineBreakpoint[] {
            new TestBreakpoint("test.rb", 1),
        };
        attach(proxy, breakpoints, 1);
        try {
            proxy.getReadersSupport().readAddedBreakpointNo();
            fail("RubyDebuggerException expected");
        } catch (RubyDebuggerException e) {
            // OK - expected
        }
        proxy.getDebugTarged().getThreadById(1).resume();
    }
    
}
