package org.rubyforge.debugcommons.model;

import org.rubyforge.debugcommons.RubyDebugEvent;
import org.rubyforge.debugcommons.RubyDebuggerException;
import org.rubyforge.debugcommons.Util;

public final class RubyThread extends RubyEntity {
    
    private final int id;
    private String name;
    private RubyFrame[] frames;
    
    private boolean isSuspended;
    
    /** Used by {@link #runTo} method. */
    private IRubyBreakpoint temporaryBreakpoint;
    
    public RubyThread(RubyDebugTarget target, int id) {
        super(target.getProxy());
        this.id = id;
        this.updateName();
    }
    
    public RubyFrame[] getFrames() throws RubyDebuggerException {
        if (frames == null) {
            if (isSuspended()) {
                frames = getProxy().readFrames(this);
            } else {
                frames = new RubyFrame[] {};
            }
        }
        return frames;
    }
    
    /**
     * Returns top stack frame for this thread. Might be <code>null</code> if
     * thread is not {@link #isSuspended() suspended}.
     */
    public RubyFrame getTopFrame() throws RubyDebuggerException {
        RubyFrame[] frames = getFrames();
        return frames.length == 0 ? null : frames[0];
    }
    
    public boolean isSuspended() {
        return isSuspended;
    }
    
    /*
     * Call after user wants to resume or step.
     */
    protected void resume(boolean isStep) {
        //        isStepping = isStep;
        isSuspended = false;
        frames = null;
        updateName();
    }
    
    public void resume() {
        resume(false /* isStep*/);
        getProxy().resume(this);
        // TODO: resume event should be sent from ruby debugger
    }
    
    /**
     * Called when suspension event was sent from ruby debugger.
     *
     * @param suspensionPoint point of suspension
     */
    public void suspend(final SuspensionPoint suspensionPoint) {
        if (temporaryBreakpoint != null) {
            getProxy().removeBreakpoint(temporaryBreakpoint);
            temporaryBreakpoint = null;
        }
        frames = null;
        isSuspended = true;
        this.updateName(suspensionPoint);
        RubyDebugEvent ev = new RubyDebugEvent(this, suspensionPoint);
        getProxy().fireDebugEvent(ev);
    }
    
    public boolean canStepInto() throws RubyDebuggerException {
        return isSuspended && getFrames().length > 0;
    }
    
    public boolean canStepOver() throws RubyDebuggerException {
        return isSuspended && getFrames().length > 0;
    }
    
    public void stepInto(boolean forceNewLine) throws RubyDebuggerException {
        this.updateName();
        RubyFrame frame = getTopFrame();
        if (frame == null) {
            Util.fine("stepInto failed, not top stack frame (thread is not suspended?)");
        } else {
            frame.stepInto(forceNewLine);
        }
    }
    
    public void stepInto() throws RubyDebuggerException {
        stepInto(false);
    }
    
    public void stepOver(boolean forceNewLine) throws RubyDebuggerException {
        RubyFrame frame = getTopFrame();
        if (frame == null) {
            Util.fine("stepOver failed, not top stack frame (thread is not suspended?)");
        } else {
            frame.stepOver(forceNewLine);
        }
    }
    
    public void stepOver() throws RubyDebuggerException {
        stepOver(false);
    }
    
    public void stepReturn() throws RubyDebuggerException {
        RubyFrame frame = getTopFrame();
        if (frame == null) {
            Util.fine("stepReturn failed, empty frame stack (thread is not suspended?)");
        } else {
            frame.stepReturn();
        }
    }
    
    public void runTo(final String path, final int line) throws RubyDebuggerException {
        temporaryBreakpoint = new IRubyBreakpoint() {
            public boolean isEnabled() { return true; }
            public String getFilePath() { return path; }
            public int getLineNumber() { return line; }
        };
        getProxy().addBreakpoint(temporaryBreakpoint);
        resume();
    }
    
    private void updateName() {
        this.updateName(null);
    }
    
    private void updateName(SuspensionPoint suspensionPoint) {
        this.name = "Ruby Thread - " + getId();
        if (suspensionPoint != null) {
            this.name += " (" + suspensionPoint + ")";
        }
    }
    
    public int getId() {
        return id;
    }
    
}
