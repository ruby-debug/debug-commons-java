package org.rubyforge.debugcommons.model;

import org.rubyforge.debugcommons.DebuggerTestBase;
import org.rubyforge.debugcommons.RubyDebuggerProxy;

public class RubyDebugTargetTest extends DebuggerTestBase {
    
    public RubyDebugTargetTest(String testName) {
        super(testName);
    }
    
    public void testFinishWhenSpawnedThreadIsSuspended() throws Exception {
        for (RubyDebuggerProxy.DebuggerType type : RubyDebuggerProxy.DebuggerType.values()) {
            setDebuggerType(type);
            final RubyDebuggerProxy proxy = prepareProxy(
                    "a = Thread.start {",
                    "    puts '1'",
                    "}");
            final IRubyBreakpoint[] breakpoints = new IRubyBreakpoint[] {
                new TestBreakpoint("test.rb", 2)
            };
            startDebugging(proxy, breakpoints, 1);
            proxy.finish();
        }
    }
    
}
