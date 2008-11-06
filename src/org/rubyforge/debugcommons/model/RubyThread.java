/*
 * Copyright (c) 2007-2008, debug-commons team
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.rubyforge.debugcommons.model;

import java.util.logging.Logger;
import org.rubyforge.debugcommons.RubyDebugEvent;
import org.rubyforge.debugcommons.RubyDebuggerException;

public final class RubyThread extends RubyEntity {
    
    private static final Logger LOGGER = Logger.getLogger(RubyThread.class.getName());
    
    private final int id;
    private String name;
    private RubyFrame[] frames;
    
    private boolean isSuspended;
    
    /** Used by {@link #runTo} method. */
    private IRubyLineBreakpoint temporaryBreakpoint;
    
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
            LOGGER.fine("stepInto failed, not top stack frame (thread is not suspended?)");
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
            LOGGER.fine("stepOver failed, not top stack frame (thread is not suspended?)");
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
            LOGGER.fine("stepReturn failed, empty frame stack (thread is not suspended?)");
        } else {
            frame.stepReturn();
        }
    }
    
    public void runTo(final String path, final int line) throws RubyDebuggerException {
        temporaryBreakpoint = new IRubyLineBreakpoint() {
            public boolean isEnabled() { return true; }
            public String getFilePath() { return path; }
            public int getLineNumber() { return line; }
            public String getCondition() { return null; }
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
