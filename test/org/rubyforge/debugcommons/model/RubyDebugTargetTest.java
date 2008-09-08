package org.rubyforge.debugcommons.model;

import org.rubyforge.debugcommons.DebuggerTestBase;
import org.rubyforge.debugcommons.RubyDebuggerProxy;

public class RubyDebugTargetTest extends DebuggerTestBase {
    
    public RubyDebugTargetTest(String testName) {
        super(testName);
    }
    
    public void testFinishWhenSpawnedThreadIsSuspended() throws Exception {
        final RubyDebuggerProxy proxy = prepareProxy(
                "a = Thread.start {",
                "    puts '1'",
                "}");
        final IRubyLineBreakpoint[] breakpoints = new IRubyLineBreakpoint[]{
            new TestBreakpoint("test.rb", 2)
        };
        startDebugging(proxy, breakpoints, 1);
        proxy.finish(true);
    }
    
}
