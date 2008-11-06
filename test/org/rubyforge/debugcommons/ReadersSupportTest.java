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
        proxy.getDebugTarget().getThreadById(1).resume();
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
        proxy.getDebugTarget().getThreadById(1).resume();
    }
    
}
