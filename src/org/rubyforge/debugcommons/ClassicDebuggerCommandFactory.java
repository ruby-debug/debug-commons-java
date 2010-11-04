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

public final class ClassicDebuggerCommandFactory implements ICommandFactory {
    
    public String createReadFrames(RubyThread thread) {
        return "th " + thread.getId() + "; w";
    }
    
    public String createReadLocalVariables(RubyFrame frame) {
        return "th " + frame.getThread().getId() + "; frame " + frame.getIndex() + "; v l";
    }
    
    public String createReadGlobalVariables() {
        return "v g";
    }
    
    public String createReadInstanceVariable(RubyVariable variable) {
        StringBuilder command = new StringBuilder();
        if (!variable.isGlobal()) {
            command.append("th " + variable.getFrame().getThread().getId() + "; ");
        }
        command.append("v i ");
        if (!variable.isGlobal()) {
            command.append(variable.getFrame().getIndex() + " ");
        }
        return command.append(variable.getObjectId()).toString();
    }
    
    public String createStepOver(RubyFrame frame) {
        return "th " + frame.getThread().getId() + "; next";
    }
    
    public String createForcedStepOver(RubyFrame frame) {
        // not supported by Classic Debugger
        return createStepOver(frame);
    }
    
    public String createStepReturn(RubyFrame frame) {
        return "th " + frame.getThread().getId() + "; next " + (frame.getLine() + 1);
    }
    
    public String createStepInto(RubyFrame frame) {
        return "th " + frame.getThread().getId() + "; step";
    }
    
    public String createForcedStepInto(RubyFrame frame) {
        // not supported by Classic Debugger
        return createStepInto(frame);
    }
    
    public String createReadThreads() {
        return "th l";
    }
    
    public String createLoad(String filename) {
        return "load " + filename;
    }

    public String createEnableBreakpoint(int index) {
        return null;
    }

    public String createDisableBreakpoint(int index) {
        return null;
    }

    public String createInspect(RubyFrame frame, String expression) {
        return "th " + frame.getThread().getId() + "; v inspect " + frame.getIndex() + " " + expression;
    }
    
    public String createResume(RubyThread thread) {
        return "th " + thread.getId() + "; cont";
    }
    
    public String createAddBreakpoint(String file, int line) {
        StringBuffer setBreakPointCommand = new StringBuffer();
        setBreakPointCommand.append("b ");
        setBreakPointCommand.append(file);
        setBreakPointCommand.append(":");
        setBreakPointCommand.append(line);
        return setBreakPointCommand.toString();
    }
    
    public String createRemoveBreakpoint(int index) {
        return "delete " + index;
    }
    
    public String createCatchOn(IRubyExceptionBreakpoint breakpoint) {
        return "catch " + breakpoint.getException();
    }
    
    public String createCatchOff() {
        return "catch off";
    }

    public String createSetCondition(int bpNum, String condition) {
        return null;
    }

}
