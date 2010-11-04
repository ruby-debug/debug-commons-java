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

import org.rubyforge.debugcommons.model.IRubyExceptionBreakpoint;
import org.rubyforge.debugcommons.model.RubyFrame;
import org.rubyforge.debugcommons.model.RubyThread;
import org.rubyforge.debugcommons.model.RubyVariable;

public interface ICommandFactory {
    
    String createReadFrames(RubyThread thread);
    
    String createReadLocalVariables(RubyFrame frame);
    
    String createReadGlobalVariables();
    
    String createReadInstanceVariable(RubyVariable variable);
    
    String createStepOver(RubyFrame frame);
    
    String createForcedStepOver(RubyFrame frame);
    
    String createStepReturn(RubyFrame frame);
    
    String createStepInto(RubyFrame frame);
    
    String createForcedStepInto(RubyFrame frame);
    
    String createReadThreads();
    
    String createInspect(RubyFrame frame, String expression);
    
    String createResume(RubyThread thread);
    
    String createSetCondition(int bpNum, String condition);
    
    String createAddBreakpoint(String file, int line);
    
    String createRemoveBreakpoint(int index);

    String createCatchOn(IRubyExceptionBreakpoint breakpoint);

    String createCatchOff();
    
    String createLoad(String filename);

    String createEnableBreakpoint(int index);

    String createDisableBreakpoint(int index);
}
