package org.rubyforge.debugcommons.model;

public interface IRubyExceptionBreakpoint extends IRubyBreakpoint {

    /**
     * Returns exception class name.
     * 
     * @return exception class name
     */
    String getException();
}
