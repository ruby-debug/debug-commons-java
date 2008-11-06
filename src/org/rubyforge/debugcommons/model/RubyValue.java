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

public class RubyValue extends RubyEntity {
    
    private String value;
    private String referenceTypeName;
    private boolean hasChildren;
    private RubyVariable owner;
    private RubyVariable[] variables;
    
    public RubyValue(RubyVariable owner, String value, String type, boolean hasChildren) {
        super(owner.getProxy());
        this.value = value;
        this.owner = owner;
        this.hasChildren = hasChildren;
        this.referenceTypeName = type;
    }
    
    public String getReferenceTypeName()  {
        return referenceTypeName;
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
    
    public @Override String toString() {
        return getValueString();
    }
    
}
