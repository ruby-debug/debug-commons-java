package org.rubyforge.debugcommons.model;

/**
 * @author Markus
 */
public class BreakpointSuspensionPoint extends SuspensionPoint {
    
    public boolean isBreakpoint() {
        return true;
    }
    
    public boolean isException() {
        return false;
    }
    
    public boolean isStep() {
        return false;
    }
    
    public @Override String toString() {
        return "Breakpoint at " + getPosition() + ", threadId: " + this.getThreadId();
    }
    
}
