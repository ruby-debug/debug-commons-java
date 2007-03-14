package org.rubyforge.debugcommons;

import org.rubyforge.debugcommons.model.RubyThread;

public final class RubyDebugEvent {
    
    /** Types of the event. */
    public enum Type { SUSPEND, TERMINATE };
    
    private final Type type;
    private final RubyThread rubyThread;
    
    private final String filePath;
    private final int line;
    
    public RubyDebugEvent(final Type type) {
        this(null, type);
    }
    
    public RubyDebugEvent(final RubyThread rubyThread, final Type type) {
        this(rubyThread, type, null, -1);
    }
    
    public RubyDebugEvent(final RubyThread rubyThread,
            final String filePath,
            final int line) {
        this(rubyThread, Type.SUSPEND, filePath, line);
    }
    
    public RubyDebugEvent(final RubyThread rubyThread,
            final Type type,
            final String filePath,
            final int line) {
        if (type == Type.SUSPEND) {
            assert rubyThread != null;
        }
        this.rubyThread = rubyThread;
        this.type = type;
        this.filePath = filePath;
        this.line = line;
    }
    
    public boolean isSuspensionType() {
        return this.type == Type.SUSPEND;
    }
    
    public boolean isTerminateType() {
        return this.type == Type.TERMINATE;
    }
    
    public RubyThread getRubyThread() {
        return rubyThread;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public int getLine() {
        return line;
    }
    
    @Override
    public String toString() {
        return "[RubyDebugEvent@" + System.identityHashCode(this) +
                "> type: " + type +
                ", rubyThread: " + rubyThread +
                ", line: " + line  +
                ", filePath: " + filePath +
                ']';
    }
    
}
