package org.rubyforge.debugcommons;

import org.rubyforge.debugcommons.DebuggerTestBase.TestBreakpoint;
import org.rubyforge.debugcommons.model.IRubyBreakpoint;

public final class RubyDebuggerProxyTest extends DebuggerTestBase {
    
    public RubyDebuggerProxyTest(String testName) {
        super(testName);
    }
    
    public void testBreakpointsRemoving1() throws Exception {
        setDebuggerType(RubyDebuggerProxy.CLASSIC_DEBUGGER);
        final RubyDebuggerProxy proxy = prepareProxy(
                "3.times do", // 1
                "  b=10",     // 2
                "  b=11",     // 3
                "end");       // 4
        final IRubyBreakpoint[] breakpoints = new IRubyBreakpoint[] {
            new TestBreakpoint("test.rb", 2),
            new TestBreakpoint("test.rb", 3),
        };
        startDebugging(proxy, breakpoints, 1);
        
        // do one cycle
        resumeSuspendedThread(proxy); // 2 -> 3
        resumeSuspendedThread(proxy); // 3 -> 2
        
        IRubyBreakpoint second = breakpoints[1];
        proxy.removeBreakpoint(second);
        
        // finish
        resumeSuspendedThread(proxy); // 2 -> 2
        resumeSuspendedThread(proxy); // 2 -> finish
    }
    
    public void testBreakpointsRemovingAdding() throws Exception {
        setDebuggerType(RubyDebuggerProxy.CLASSIC_DEBUGGER);
        final RubyDebuggerProxy proxy = prepareProxy(
                "3.times do", // 1
                "  b=10",     // 2
                "  b=11",     // 3
                "end");       // 4
        final IRubyBreakpoint[] breakpoints = new IRubyBreakpoint[] {
            new TestBreakpoint("test.rb", 2),
            new TestBreakpoint("test.rb", 3),
        };
        startDebugging(proxy, breakpoints, 1);
        
        // do one cycle
        resumeSuspendedThread(proxy); // 2 -> 3
        resumeSuspendedThread(proxy); // 3 -> 2
        
        final IRubyBreakpoint first = breakpoints[0];
        proxy.removeBreakpoint(first);
        
        // finish
        resumeSuspendedThread(proxy); // 2 -> 3
        resumeSuspendedThread(proxy); // 3 -> 3
        resumeSuspendedThread(proxy); // 3 -> finish
    }
    
    public void testBreakpointsUpdating() throws Exception {
        for (RubyDebuggerProxy.DebuggerType type : RubyDebuggerProxy.DebuggerType.values()) {
            setDebuggerType(type);
            final RubyDebuggerProxy proxy = prepareProxy(
                    "4.times do", // 1
                    "  b=10",     // 2
                    "  b=11",     // 3
                    "end");       // 4
            final TestBreakpoint[] breakpoints = new TestBreakpoint[] {
                new TestBreakpoint("test.rb", 2),
                new TestBreakpoint("test.rb", 3),
            };
            startDebugging(proxy, breakpoints, 1);
            
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
    }
    
}
