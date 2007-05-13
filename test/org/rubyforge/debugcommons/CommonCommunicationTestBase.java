package org.rubyforge.debugcommons;

import org.rubyforge.debugcommons.model.IRubyBreakpoint;
import org.rubyforge.debugcommons.model.RubyVariable;

public abstract class CommonCommunicationTestBase extends DebuggerTestBase {
    
    public CommonCommunicationTestBase(final String name) {
        super(name);
    }
    
    public void testGlobalVariables() throws Exception {
        final RubyDebuggerProxy proxy = prepareProxy("sleep 0.1");
        final IRubyBreakpoint[] breakpoints = new IRubyBreakpoint[] {
            new TestBreakpoint("test.rb", 1),
        };
        startDebugging(proxy, breakpoints, 1);
        
        assertNotNull(suspendedThread);
        RubyVariable[] variables = proxy.readGlobalVariables();
        assertTrue("global variables read", variables.length > 0);
        RubyVariable loadPath = null;
        for (RubyVariable rubyVariable : variables) {
            if (rubyVariable.getName().equals("$:")) {
                loadPath = rubyVariable;
                break;
            }
        }
        assertNotNull("$: found", loadPath);
        assertTrue("$: is global", loadPath.isGlobal());
        assertTrue("$: has children", loadPath.getValue().getVariables().length > 0);
        resumeSuspendedThread(proxy); // finish spawned thread
    }
    
}
