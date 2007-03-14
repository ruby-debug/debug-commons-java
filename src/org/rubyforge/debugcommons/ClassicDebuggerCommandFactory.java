package org.rubyforge.debugcommons;

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
    
    public String createReadInstanceVariable(RubyVariable variable) {
        return "th " + variable.getFrame().getThread().getId() + "; v i " + variable.getFrame().getIndex() + " " + variable.getObjectId();
    }
    
    public String createStepOver(RubyFrame frame) {
        return "th " + frame.getThread().getId() + "; next";
    }
    
    public String createStepReturn(RubyFrame frame) {
        return "th " + frame.getThread().getId() + "; next " + (frame.getLine() + 1);
    }
    
    public String createStepInto(RubyFrame frame) {
        return "th " + frame.getThread().getId() + "; step";
    }
    
    public String createReadThreads() {
        return "th l";
    }
    
    public String createLoad(String filename) {
        return "load " + filename;
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
    
    public String createCatchOff() {
        return "catch off";
    }
    
}
