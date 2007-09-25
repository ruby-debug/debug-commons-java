package org.rubyforge.debugcommons.model;

public final class RubyVariableInfo {
    
    private static final String NIL_VALUE = "nil";
    private static final String NIL_CLASS = "NilClass";
    
    private final String name;
    private final String kind;
    private final String value;
    private final String type;
    private final boolean hasChildren;
    private final String objectId;
    
    public RubyVariableInfo(String name, String kind) {
        this(name, kind, null, null, false, null);
    }
    
    public RubyVariableInfo(String name, String kind, String value, String type, boolean hasChildren, String objectId) {
        this.name = name;
        this.kind = kind;
        this.value = value == null ? NIL_VALUE : value;
        this.type = type == null ? NIL_CLASS: type;
        this.hasChildren = hasChildren;
        this.objectId = objectId;
    }
    
    public String getObjectId() {
        return objectId;
    }
    
    public boolean hasChildren() {
        return hasChildren;
    }
    
    public String getType() {
        return type;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getKind() {
        return kind;
    }
    
    public String getName() {
        return name;
    }

    boolean isNil() {
        return RubyVariableInfo.NIL_VALUE.equals(getValue());
    }

    @Override
    public String toString() {
        return "[" + getClass().getName() + '@' + System.identityHashCode(this) + 
                "] kind = " + kind + 
                ", value = " + value + 
                ", type = " + type + 
                ", hasChildren = " + hasChildren + 
                ", objectId = " + objectId; 
    }
    
}
