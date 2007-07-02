package org.rubyforge.debugcommons.model;

import org.rubyforge.debugcommons.RubyDebuggerException;

public class RubyValue extends RubyEntity {
    
    private String value;
    private String referenceTypeName;
    private boolean hasChildren;
    private RubyVariable owner;
    private RubyVariable[] variables;
    
    public  RubyValue(RubyVariable owner, String value, String type, boolean hasChildren) {
        super(owner.getProxy());
        this.value = value;
        this.owner = owner;
        this.hasChildren = hasChildren;
        this.referenceTypeName = type;
    }
    
    public String getReferenceTypeName()  {
        return this.referenceTypeName;
    }
    
    public String getValueString() {
        return value;
    }
    
    public boolean isAllocated() {
        return false;
    }
    
    /**
     * Returns <em>instance</em> variables of the object represented by this
     * value plus <em>class</em> variables of the class of the object.
     *
     * @see RubyVariable#isClass
     * @see RubyVariable#isInstance
     */
    public RubyVariable[] getVariables() throws RubyDebuggerException {
        if (!hasChildren) {
            return new RubyVariable[0];
        }
        if (variables == null) {
            variables = getProxy().readInstanceVariables(owner);
        }
        return variables;
    }
    
    public boolean hasVariables() {
        return hasChildren;
    }
    
    public String toString() {
        if (this.getReferenceTypeName() == null) {
            return this.getValueString();
        }
        return this.getValueString();
    }
    
}
