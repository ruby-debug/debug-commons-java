package org.rubyforge.debugcommons;

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
    
    String createAddBreakpoint(String file, int line);
    
    String createRemoveBreakpoint(int index);
    
    String createCatchOff();
    
    String createLoad(String filename);
    
}
