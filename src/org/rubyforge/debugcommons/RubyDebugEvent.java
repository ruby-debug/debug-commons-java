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
