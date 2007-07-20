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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RubyFrameInfo)) {
            return false;
        }
        final RubyFrameInfo other = (RubyFrameInfo) obj;
        return (file == null ? other.file == null : file.equals(other.file))
                && (line == other.line) && (index == other.index);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (file != null ? file.hashCode() : 0);
        hash = 59 * hash + line;
        hash = 59 * hash + index;
        return hash;
    }
}
