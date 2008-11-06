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
