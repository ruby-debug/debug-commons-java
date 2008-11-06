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
