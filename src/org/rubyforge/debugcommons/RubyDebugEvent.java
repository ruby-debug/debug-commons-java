package org.rubyforge.debugcommons;

import org.rubyforge.debugcommons.model.RubyThread;
import org.rubyforge.debugcommons.model.SuspensionPoint;

public final class RubyDebugEvent {
    
    /** Types of the event. */
    public enum Type { SUSPEND, TERMINATE };
    
    private final Type type;
    private final RubyThread rubyThread;
    
    private final String filePath;
    private final int line;
    private final boolean stepping;
    
    public RubyDebugEvent(final Type type) {
        this(null, type, null, -1, false);
    }
    
    public RubyDebugEvent(final RubyThread rubyThread, final SuspensionPoint sp) {
        this(rubyThread, Type.SUSPEND, sp.getFile(), sp.getLine(), sp.isStep());
    }

    public RubyDebugEvent(final RubyThread rubyThread,
            final Type type,
            final String filePath,
            final int line,
            final boolean stepping) {
        if (type == Type.SUSPEND) {
            assert rubyThread != null;
        }
        this.rubyThread = rubyThread;
        this.type = type;
        this.filePath = filePath;
        this.line = line;
        this.stepping = stepping;
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

    public boolean isStepping() {
        return stepping;
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
