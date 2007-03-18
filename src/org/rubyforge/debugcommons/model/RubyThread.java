package org.rubyforge.debugcommons.model;

import org.rubyforge.debugcommons.RubyDebugEvent;
import org.rubyforge.debugcommons.RubyDebuggerException;
import org.rubyforge.debugcommons.model.SuspensionPoint;

public final class RubyThread extends RubyEntity {
    
    private final int id;
    private String name;
    private RubyFrame[] frames;
    
    private boolean isSuspended;
    //    private boolean isTerminated;
    //    private boolean isStepping;
    
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
                frames = new RubyFrame[] {} ;
            }
        }
        return frames;
    }
    
    
    //	public int getFramesSize() {
    //		return frames.length;
    //	}
    //
    //	public boolean hasFrames() {
    //		return isSuspended ; //TODO: change getFrames().length > 0;
    //	}
    
    public RubyFrame getTopFrame() throws RubyDebuggerException {
        RubyFrame[] frames = getFrames();
        assert frames.length > 0;
        return frames[0];
    }
    
    //	public boolean canResume() {
    //		return isSuspended;
    //	}
    //
    //	public boolean canSuspend() {
    //		return !isSuspended;
    //	}
    
    public boolean isSuspended() {
        return isSuspended;
    }
    
    //	protected void setSuspended(boolean isSuspended) {
    //		this.isSuspended = isSuspended;
    //	}
    
    /*
     * Call after user wants to resume or step.
     */
    protected void resume(boolean isStep) {
        //        isStepping = isStep;
        isSuspended = false;
        this.updateName();
        //        this.frames = new RubyFrame[] {};
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
        frames = null;
        isSuspended = true;
        //        isStepping = false;
        this.updateName(suspensionPoint);
        RubyDebugEvent ev = new RubyDebugEvent(this, suspensionPoint.getFile(), suspensionPoint.getLine());
        getProxy().fireDebugEvent(ev);
    }
    
    //	public void suspend() {
    //		frames = null ;
    //		isStepping = false;
    //		isSuspended = true;
    //		// TODO: send suspension command to ruby debugger
    //	}
    //
    //	public boolean canStepInto() {
    //		return isSuspended && this.hasFrames();
    //	}
    //
    //	public boolean canStepOver() {
    //		return isSuspended && this.hasFrames();
    //	}
    //
    //	public boolean canStepReturn() {
    //		return false;
    //	}
    //
    //	public boolean isStepping() {
    //		return isStepping;
    //	}
    
    public void stepInto() throws RubyDebuggerException {
        //        isStepping = true;
        this.updateName();
        getTopFrame().stepInto();
    }
    
    public void stepOver() throws RubyDebuggerException {
        getTopFrame().stepOver();
    }
    
    public void stepReturn() throws RubyDebuggerException {
        RubyFrame[] frames = getFrames();
        assert frames.length > 0;
        frames[frames.length > 1 ? 1 : 0].stepReturn();
    }
    
    //    public boolean canTerminate() {
    //		return !isTerminated;
    //	}
    //
    //	public boolean isTerminated() {
    //		return isTerminated;
    //	}
    //
    //	public void terminate() {
    //		RubyDebugEvent ev = new RubyDebugEvent(this, RubyDebugEvent.Type.TERMINATE);
    //		getRubyDebuggerProxy().fireDebugEvent(ev);
    //		this.getDebugTarget().terminate();
    //		isTerminated = true;
    //		this.frames = null;
    //	}
    //
    //	public void setFrames(RubyFrame[] frames) {
    //		this.frames = frames;
    //	}
    //
    //	public String getName() {
    //		return name;
    //	}
    //
    //	public void setName(String name) {
    //		this.name = name;
    //	}
    
    //		return !isTerminated;
    //	}
    //
    //	public boolean isTerminated() {
    //		return isTerminated;
    //	}
    //
    //	public void terminate() {
    //		RubyDebugEvent ev = new RubyDebugEvent(this, RubyDebugEvent.Type.TERMINATE);
    //		getRubyDebuggerProxy().fireDebugEvent(ev);
    //		this.getDebugTarget().terminate();
    //		isTerminated = true;
    //		this.frames = null;
    //	}
    //
    //	public RubyDebuggerProxy getRubyDebuggerProxy() {
    //		return ((RubyDebugTarget) this.getDebugTarget()).getRubyDebuggerProxy();
    //	}
    //
    //	public void setFrames(RubyFrame[] frames) {
    //		this.frames = frames;
    //	}
    //
    //	public String getName() {
    //		return name;
    //	}
    //
    //	public void setName(String name) {
    //		this.name = name;
    //	}
    
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
    
    //	public void setId(int id) {
    //		this.id = id;
    //	}
    //
    //    @Override
    //    public int hashCode() {
    //        return getId();
    //    }
    //
    //    @Override
    //    public boolean equals(Object other) {
    //        return (other instanceof RubyThread) && getId() == ((RubyThread) other).getId();
    //    }
    //
    //    @Override
    //	public String toString() {
    //		return "[RubyThread@" + System.identityHashCode(this) +
    //				"> id: " + id +
    //				", name: \"" + name + '"' +
    //				']';
    //	}
    //
}
