package org.rubyforge.debugcommons.model;

import org.rubyforge.debugcommons.RubyDebuggerException;

public final class RubyFrame extends RubyEntity {
    
    private final RubyFrameInfo info;
    private final RubyThread thread;
    
    private RubyVariable[] variables;
    
    public RubyFrame(RubyThread thread, RubyFrameInfo info) {
        super(thread.getProxy());
        this.info = info;
        this.thread = thread;
    }
    
    public String getFile() {
        return info.getFile();
    }
    
    public int getLine() {
        return info.getLine();
    }
    
    public int getIndex() {
        return info.getIndex();
    }
    
    public RubyThread getThread() {
        return thread;
    }
    
    public RubyVariable[] getVariables() throws RubyDebuggerException {
        if (variables == null) {
            variables = getProxy().readVariables(this);
        }
        return variables;
    }
    
    public RubyVariable inspectExpression(final String expression) throws RubyDebuggerException {
        return getProxy().inspectExpression(this, expression);
    }
    
    public boolean hasVariables() throws RubyDebuggerException {
        return getVariables().length > 0;
    }
    
    public String getName() {
        return getFile() + ':' + getLine();
    }
    
    //    public boolean canStepInto() {
    //        return canResume();
    //    }
    //
    //    public boolean canStepOver() {
    //        return canResume();
    //    }
    //
    //    public boolean canStepReturn() {
    //        return canResume();
    //    }
    
    public boolean isStepping() {
        return false;
    }
    
    public void stepInto(boolean forceNewLine) {
        thread.resume(true /*isstep*/);
        getProxy().sendStepIntoEnd(this, forceNewLine);
    }
    
    public void stepInto() {
        stepInto(false);
    }
    
    public void stepOver(boolean forceNewLine) {
        thread.resume(true /*isstep*/);
        getProxy().sendStepOver(this, forceNewLine);
    }
    
    public void stepOver() {
        stepOver(false);
    }
    
    public void stepReturn() {
        thread.resume(true /*isstep*/);
        getProxy().sendStepReturnEnd(this);
    }
    
    //    public boolean canResume() {
    //        return this.getThread().canResume();
    //    }
    //
    //    public boolean canSuspend() {
    //        return this.getThread().canSuspend();
    //    }
    //
    //    public boolean isSuspended() {
    //        return this.getThread().isSuspended();
    //    }
    
    public void resume() {
        this.getThread().resume();
    }
    
    public void suspend() {
        throw new UnsupportedOperationException("not implemented yet");
    }
    
    //    public boolean canTerminate() {
    //        return this.getThread().canTerminate();
    //    }
    //
    //    public boolean isTerminated() {
    //        return this.getThread().isTerminated();
    //    }
    //
    //    public void terminate() {
    //        this.getThread().terminate() ;
    //    }
    
    @Override
    public String toString() {
        return getName();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof RubyFrame)) {
            return false;
        }
        final RubyFrame other = (RubyFrame) obj;
        return info == null ? other.info == null : info.equals(other.info);
    }

    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (this.info != null ? this.info.hashCode() : 0);
        return hash;
    }
    
}
