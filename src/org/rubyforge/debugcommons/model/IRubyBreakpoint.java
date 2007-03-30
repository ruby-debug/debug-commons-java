package org.rubyforge.debugcommons.model;

public interface IRubyBreakpoint {
    
    boolean isEnabled();
    String getFilePath();
    int getLineNumber();
    
}
