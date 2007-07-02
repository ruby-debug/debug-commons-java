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
        if (info != RubyVariableInfo.UNKNOWN_IN_CONTEXT) {
            this.isClass = info.getKind().equals("class");
            this.isLocal = info.getKind().equals("local");
            this.isInstance = info.getKind().equals("instance");
            this.isConstant = info.getKind().equals("constant");
            this.isGlobal = info.getKind().equals("global");
            this.value = new RubyValue(this, info.getValue(), info.getType(), info.hasChildren());
        } else {
            this.isClass = false;
            this.isLocal = false;
            this.isInstance = false;
            this.isConstant = false;
            this.isGlobal = false;
            this.value = null;
        }
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
    
    //    public void setValue(String expression) {
    //        throw new UnsupportedOperationException("not implemented yet");
    //    }
    //
    //    public void setValue(RubyValue value) {
    //        throw new UnsupportedOperationException("not implemented yet");
    //    }
    //
    //    public boolean supportsValueModification() {
    //        return false;
    //    }
    //
    //    public boolean verifyValue(String expression) {
    //        return false;
    //    }
    //
    //    public boolean verifyValue(RubyValue value) {
    //        return false;
    //    }
    //
    
    //    public void setFrame(RubyFrame frame) {
    //        this.frame = frame;
    //        //        setProxy(frame.getProxy());
    //    }
    
    //    public void setParent(RubyVariable parent) {
    //        this.parent = parent;
    //        //        this.frame = parent.getFrame();
    //    }
    
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
    
    public String toString() {
        if (this.isHashValue()) {
            return this.getName() + " => " + this.getValue();
        }
        return this.getName() + " = " + this.getValue();
        
    }
    
}
