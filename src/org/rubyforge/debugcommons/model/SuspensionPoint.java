package org.rubyforge.debugcommons.model;

public abstract class SuspensionPoint {
    
    public static final SuspensionPoint END = new EndSuspensionPoint();
    
    private String file;
    private int line;
    private int threadId = -1;
    
    public SuspensionPoint() {}
    
    public SuspensionPoint(String file, int line) {
        this.file = file;
        this.line = line;
    }
    
    public abstract @Override String toString();
    
    public abstract boolean isException();
    public abstract boolean isStep();
    public abstract boolean isBreakpoint();
    
    public String getFile() {
        return file;
    }
    
    public int getLine() {
        return line;
    }
    
    public void setFile(String file) {
        this.file = file;
    }
    
    public void setLine(int line) {
        this.line = line;
    }
    
    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }
    
    public String getPosition() {
        return getFile() + ":" + getLine();
    }
    
    public int getThreadId() {
        return threadId;
    }
    
    private static final class EndSuspensionPoint extends SuspensionPoint {
        
        public @Override String toString() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public @Override boolean isException() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public @Override boolean isStep() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public @Override boolean isBreakpoint() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    
}
