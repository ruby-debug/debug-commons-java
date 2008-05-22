package org.rubyforge.debugcommons;

import org.rubyforge.debugcommons.model.RubyThread;
import org.rubyforge.debugcommons.model.SuspensionPoint;

public final class RubyDebugEvent {

    private final RubyThread rubyThread;
    private final SuspensionPoint sp;
    private boolean isTerminate;
    
    static RubyDebugEvent createTerminateEvent() {
        RubyDebugEvent e = new RubyDebugEvent(null, null);
        e.isTerminate = true;
        return e;
    }
    
    public RubyDebugEvent(final RubyThread rubyThread, final SuspensionPoint sp) {
        if (sp != null) {
            assert rubyThread != null;
        }
        this.sp = sp;
        this.rubyThread = rubyThread;
    }

    public boolean isSuspensionType() {
        return !isTerminate && (sp.isBreakpoint() || sp.isStep());
    }
    
    public boolean isTerminateType() {
        return isTerminate;
    }
    
    public boolean isExceptionType() {
        return !isTerminate && sp.isException();
    }
    
    public RubyThread getRubyThread() {
        return rubyThread;
    }
    
    public String getFilePath() {
        return sp.getFile();
    }
    
    public int getLine() {
        return sp.getLine();
    }

    public boolean isStepping() {
        return sp.isStep();
    }
    
    @Override
    public String toString() {
        if (isTerminate) {
            return "[RubyDebugEvent@" + System.identityHashCode(this) + "> Terminate Event";
        }
        return "[RubyDebugEvent@" + System.identityHashCode(this) +
                "> type: " + sp +
                ", rubyThread: " + rubyThread +
                ", line: " + sp.getLine()  +
                ", filePath: " + sp.getFile() +
                ']';
    }
    
}
