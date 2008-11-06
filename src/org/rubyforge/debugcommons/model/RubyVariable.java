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

import org.rubyforge.debugcommons.RubyDebuggerProxy;

public final class RubyVariable extends RubyEntity {
    
    private final RubyVariableInfo info;
    private final RubyFrame frame;
    
    private final boolean isClass;
    private final boolean isLocal;
    private final boolean isInstance;
    private final boolean isConstant;
    private final boolean isGlobal;
    private final RubyValue value;
    private final RubyVariable parent;
    
    private RubyVariable(RubyDebuggerProxy proxy, RubyVariableInfo info, RubyFrame frame, RubyVariable parent) {
        super(proxy);
        this.info = info;
        this.isClass = info.getKind().equals("class");
        this.isLocal = info.getKind().equals("local");
        this.isInstance = info.getKind().equals("instance");
        this.isConstant = info.getKind().equals("constant");
        this.isGlobal = info.getKind().equals("global");
        this.value = new RubyValue(this, info.getValue(), info.getType(), info.hasChildren());
        this.frame = frame;
        this.parent = parent;
    }
    
    /**
     * Helper constructor for global variables which have neither frame nor
     * parent.
     */
    public RubyVariable(RubyDebuggerProxy proxy, RubyVariableInfo info) {
        this(proxy, info, null, null);
    }
    
    public RubyVariable(RubyVariableInfo info, RubyFrame frame) {
        this(info, frame, null);
    }
    
    public RubyVariable(RubyVariableInfo info, RubyVariable parent) {
        this(parent.getProxy(), info, parent.getFrame(), parent);
    }
    
    private RubyVariable(RubyVariableInfo info, RubyFrame frame, RubyVariable parent) {
        this(frame.getProxy(), info, frame, parent);
    }
    
    public String getName() {
        return info.getName();
    }
    
    public String getObjectId() {
        return info.getObjectId();
    }
    
    public RubyValue getValue() {
        return value;
    }
    
    public String getReferenceTypeName() {
        return "RefTypeName";
    }
    
    public boolean hasValueChanged() {
        return false;
    }
    
    public RubyFrame getFrame() {
        return frame;
    }
    
    public RubyVariable getParent() {
        return parent;
    }
    
    public String getQualifiedName() {
        if (parent == null) {
            return this.getName();
        }
        if (this.isHashValue()) {
            if (this.getValue().getReferenceTypeName().equals("String")) {
                return parent.getQualifiedName() + "[" + this.getName() + "]";
            }
            return "[ObjectSpace._id2ref(" + this.getObjectId() + ")]";
        }
        if (this.getName().startsWith("[")) { // Array
            return parent.getQualifiedName() + this.getName();
        }
        return parent.getQualifiedName() + "." + this.getName();
    }
    
    public boolean isInstance() {
        return isInstance;
    }
    
    public boolean isLocal() {
        return isLocal;
    }
    
    public boolean isClass() {
        return isClass;
    }
    
    public boolean isConstant() {
        return isConstant;
    }
    
    public boolean isGlobal() {
        return isGlobal;
    }
    
    public boolean isHashValue() {
        return parent != null && parent.getValue().getReferenceTypeName().equals("Hash");
    }
    
    private boolean isNil() {
        return info.isNil();
    }

    public @Override boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RubyVariable other = (RubyVariable) obj;
        if (isNil()) {
            return other.isNil();
        }
        return getObjectId().equals(other.getObjectId());
    }

    public @Override int hashCode() {
        int hash = 7;
        if (isNil()) {
            hash = 89 * hash + (isNil() ? 1 : 0);
        } else {
            hash = 89 * hash + (info.getObjectId() != null ? info.getObjectId().hashCode() : 0);
        }
        return hash;
    }

    public @Override String toString() {
        String sep = isHashValue() ? " => " : " = ";
        return getName() + sep + getValue() + ", INFO: (" + info + ')';
    }
    
}
