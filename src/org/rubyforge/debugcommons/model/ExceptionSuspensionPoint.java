package org.rubyforge.debugcommons.model;

/**
 * @author Markus
 */
public class ExceptionSuspensionPoint extends SuspensionPoint {
    
    private String exceptionMessage;
    private String exceptionType;
    
    public boolean isBreakpoint() {
        return false;
    }
    
    public boolean isException() {
        return true;
    }
    
    public boolean isStep() {
        return false;
    }
    
    public String getExceptionMessage() {
        return exceptionMessage;
    }
    
    public String getExceptionType() {
        return exceptionType;
    }
    
    public void setExceptionMessage(String exceptionMessage) {
        exceptionMessage = exceptionMessage;
    }
    
    public void setExceptionType(String exceptionType) {
        exceptionType = exceptionType;
    }
    
    public String toString() {
        return getExceptionType() + " occurred: " + getExceptionMessage() + ", threadId: " + this.getThreadId();
    }
    
}
