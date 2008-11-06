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
    
    public void resume() {
        this.getThread().resume();
    }
    
    public void suspend() {
        throw new UnsupportedOperationException("not implemented yet");
    }
    
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
