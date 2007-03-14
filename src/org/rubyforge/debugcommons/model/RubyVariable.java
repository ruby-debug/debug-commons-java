package org.rubyforge.debugcommons.model;

public final class RubyVariable extends RubyEntity {
    
    private final RubyVariableInfo info;
    private final RubyFrame frame;
    
    private final boolean isStatic;
    private final boolean isLocal;
    private final boolean isInstance;
    private final boolean isConstant;
    private final RubyValue value;
    private final RubyVariable parent;
    
    public RubyVariable(RubyFrame frame, RubyVariableInfo info) {
        this(frame, info, null);
    }
    
    public RubyVariable(RubyVariable parent, RubyVariableInfo info) {
        this(parent.getFrame(), info, parent);
    }
    
    private RubyVariable(RubyFrame frame, RubyVariableInfo info, RubyVariable parent) {
        super(frame.getProxy());
        this.frame = frame;
        this.parent = parent;
        this.info = info;
        if (info != RubyVariableInfo.UNKNOWN_IN_CONTEXT) {
            this.isStatic = info.getKind().equals("class");
            this.isLocal = info.getKind().equals("local");
            this.isInstance = info.getKind().equals("instance");
            this.isConstant = info.getKind().equals("constant");
            this.value = new RubyValue(this, info.getValue(), info.getType(), info.hasChildren());
        } else {
            this.isStatic = false;
            this.isLocal = false;
            this.isInstance = false;
            this.isConstant = false;
            this.value = null;
        }
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
    
    public boolean isStatic() {
        return isStatic;
    }
    
    public boolean isConstant() {
        return isConstant;
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
