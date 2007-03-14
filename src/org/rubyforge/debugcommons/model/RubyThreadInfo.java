package org.rubyforge.debugcommons.model;

public final class RubyThreadInfo {
    
    private int id;
    private String status;
    
    public RubyThreadInfo(int id, String status) {
        this.id = id;
        this.status = status;
    }
    
    public int getId() {
        return id;
    }
    
    public String getStatus() {
        return status;
    }
    
}
