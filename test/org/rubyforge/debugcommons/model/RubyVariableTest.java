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

import junit.framework.TestCase;

public class RubyVariableTest extends TestCase {

    public RubyVariableTest(String testName) {
        super(testName);
    }

    public void testEqualsAndHashCode() {
        RubyVariableInfo info1 = new RubyVariableInfo(null, "class", "hello", null, false, "0x1234");
        RubyVariable v1a = new RubyVariable(null, info1);
        RubyVariable v1b = new RubyVariable(null, info1);
        assertTrue("v1a and v1b equals", v1a.equals(v1b));
        assertEquals("same hashcode for v1a and v1b", v1a.hashCode(), v1b.hashCode());

        RubyVariableInfo info2 = new RubyVariableInfo(null, "class", "cau", null, false, "0x4321");
        RubyVariable v2 = new RubyVariable(null, info2);
        assertFalse("v1a and v2 not equals", v1a.equals(v2));
        assertFalse("v1b and v2 not equals", v1b.equals(v2));
        assertFalse("different hashcode for v2 and v1a", v2.hashCode() == v1a.hashCode());
        assertFalse("different hashcode for v2 and v1b", v2.hashCode() == v1b.hashCode());

        RubyVariableInfo nilInfo = new RubyVariableInfo(null, "class", null, null, false, null);
        RubyVariable nil = new RubyVariable(null, nilInfo);
        assertFalse("v1a and nil not equals", nil.equals(v1a));
        assertFalse("v1b and nil not equals", v1b.equals(nil));
        assertFalse("different hashcode for nil and v1a", nil.hashCode() == v1a.hashCode());
        assertFalse("different hashcode for nil and v1b", nil.hashCode() == v1b.hashCode());
    }
}
