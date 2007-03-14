package org.rubyforge.debugcommons.model;

public final class RubyFrameInfo {
    
    private String file;
    private int line;
    private int index;
    
    public RubyFrameInfo(String file, int line, int index) {
        this.file = file;
        this.line = line;
        this.index = index;
    }
    
    public int getIndex() {
        return index;
    }
    
    public int getLine() {
        return line;
    }
    
    public String getFile() {
        return file;
    }
    
}
