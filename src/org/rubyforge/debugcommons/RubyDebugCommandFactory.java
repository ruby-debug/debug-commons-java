package org.rubyforge.debugcommons;

import org.rubyforge.debugcommons.model.RubyFrame;
import org.rubyforge.debugcommons.model.RubyThread;
import org.rubyforge.debugcommons.model.RubyVariable;

public class RubyDebugCommandFactory implements ICommandFactory {
    
    public String createReadFrames(RubyThread thread) {
        return "w";
    }
    
    public String createReadLocalVariables(RubyFrame frame) {
        return "frame " + frame.getIndex() + "; v l";
    }
    
    public String createReadGlobalVariables() {
        return "v g";
    }
    
    public String createReadInstanceVariable(RubyVariable variable) {
        StringBuilder command = new StringBuilder();
        if (!variable.isGlobal()) {
            command.append("frame " + variable.getFrame().getIndex() + "; ");
        }
        return command.append("v i " + variable.getObjectId()).toString();
    }
    
    public String createStepOver(RubyFrame frame) {
        return "frame " + frame.getIndex() + "; next";
    }
    
    public String createForcedStepOver(RubyFrame frame) {
        return "frame " + frame.getIndex() + "; next+";
    }
    
    public String createStepReturn(RubyFrame frame) {
        return  "frame " + frame.getIndex() + "; finish";
    }
    
    public String createStepInto(RubyFrame frame) {
        return "frame " + frame.getIndex() + "; step";
    }
    
    public String createForcedStepInto(RubyFrame frame) {
        return "frame " + frame.getIndex() + "; step+";
    }
    
    public String createReadThreads() {
        return "th l";
    }
    
    public String createLoad(String filename) {
        return "load " + filename;
    }
    
    public String createInspect(RubyFrame frame, String expression) {
        return "frame " + frame.getIndex() + "; v inspect " + expression.replaceAll(";", "\\;");
    }
    
    public String createResume(RubyThread thread) {
        return "cont";
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
    
    //	public String createCatchOn(IRubyBreakpoint breakpoint) {
    //		return "catch " + ((RubyExceptionBreakpoint) breakpoint).getException();
    //	}
    
    public String createThreadStop(RubyThread thread) {
        return "thread stop " + thread.getId();
    }

    public String createSetCondition(int bpNum, String condition) {
        return "condition " + bpNum + ' ' + condition;
    }

}
