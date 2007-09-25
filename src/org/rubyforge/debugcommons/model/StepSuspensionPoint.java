package org.rubyforge.debugcommons.model;

/**
 * @author Markus
 */
public class StepSuspensionPoint extends SuspensionPoint {
    
    private int framesNumber;
    
    public boolean isBreakpoint() {
        return false;
    }
    
    public boolean isException() {
        return false;
    }
    
    public boolean isStep() {
        return true;
    }
    
    public int getFramesNumber() {
        return framesNumber;
    }
    
    public void setFramesNumber(int framesNumber) {
        this.framesNumber = framesNumber;
    }
    
    public @Override String toString() {
        return "Step end at " + this.getPosition() + ", threadId: " + this.getThreadId();
    }
    
}
