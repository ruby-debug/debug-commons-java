package org.rubyforge.debugcommons;

import org.rubyforge.debugcommons.RubyDebuggerProxy;
import org.rubyforge.debugcommons.model.IRubyBreakpoint;

public class ReadersSupportTest extends DebuggerTestBase {
    
    public ReadersSupportTest(final String name) {
        super(name);
    }
    
    public void testRubyDebuggerExceptionIsThrown() throws Exception {
        setDebuggerType(RubyDebuggerProxy.RUBY_DEBUG);
        final RubyDebuggerProxy proxy = prepareProxy(
                "b=10",  // 1
                "b=11"); // 2
        final IRubyBreakpoint[] breakpoints = new IRubyBreakpoint[] {
            new TestBreakpoint("test.rb", 1),
        };
        startDebugging(proxy, breakpoints, 1);
        try {
            proxy.getReadersSupport().readFrames();
            fail("RubyDebuggerException expected");
        } catch (RubyDebuggerException e) {
            // OK - expected
        }
        proxy.getDebugTarged().getThreadById(1).resume();
    }
    
    public void testNPENotThrownWhenReadingAddedBreakpoints() throws Exception {
        setDebuggerType(RubyDebuggerProxy.RUBY_DEBUG);
        final RubyDebuggerProxy proxy = prepareProxy(
                "b=10",  // 1
                "b=11"); // 2
        final IRubyBreakpoint[] breakpoints = new IRubyBreakpoint[] {
            new TestBreakpoint("test.rb", 1),
        };
        startDebugging(proxy, breakpoints, 1);
        try {
            proxy.getReadersSupport().readAddedBreakpointNo();
            fail("RubyDebuggerException expected");
        } catch (RubyDebuggerException e) {
            // OK - expected
        }
        proxy.getDebugTarged().getThreadById(1).resume();
    }
    
}
