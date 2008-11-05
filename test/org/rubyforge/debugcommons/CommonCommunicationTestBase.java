package org.rubyforge.debugcommons;

import java.util.Arrays;
import org.rubyforge.debugcommons.model.IRubyLineBreakpoint;
import org.rubyforge.debugcommons.model.RubyFrame;
import org.rubyforge.debugcommons.model.RubyVariable;

public abstract class CommonCommunicationTestBase extends DebuggerTestBase {
    
    public CommonCommunicationTestBase(final String name) {
        super(name);
    }
    
    public void testGlobalVariables() throws Exception {
        final RubyDebuggerProxy proxy = prepareProxy("sleep 0.1");
        final IRubyLineBreakpoint[] breakpoints = new IRubyLineBreakpoint[] {
            new TestBreakpoint("test.rb", 1),
        };
        attach(proxy, breakpoints, 1);
        
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
    
    public void testClassVariables() throws Exception {
        final RubyDebuggerProxy proxy = prepareProxy(
                "class A",
                "  @x=1",
                "  @@y=2",
                "  def initialize",
                "    @z=3",
                "  end",
                "end",
                "a = A.new",
                "sleep 0.1");

        final IRubyLineBreakpoint[] breakpoints = new IRubyLineBreakpoint[] {
            new TestBreakpoint("test.rb", 9),
        };
        attach(proxy, breakpoints, 1);
        assertNotNull(suspendedThread);
        RubyFrame[] frames = suspendedThread.getFrames();
        assertEquals("one frames", 1, frames.length);
        RubyVariable[] variables = frames[0].getVariables();
        assertEquals("a", 1, variables.length);
        RubyVariable aVar = variables[0];
        assertEquals("a", aVar.getName());
        RubyVariable[] vars = aVar.getValue().getVariables();
        assertEquals("two vars: @@y, @z (was: " + Arrays.asList(vars) + ")", 2, vars.length);
        resumeSuspendedThread(proxy); // finish spawned thread
    }
    
}
