package org.rubyforge.debugcommons.model;

public interface IRubyLineBreakpoint extends IRubyBreakpoint {
    
    String getFilePath();
    
    int getLineNumber();
    
    String getCondition();
    
}
