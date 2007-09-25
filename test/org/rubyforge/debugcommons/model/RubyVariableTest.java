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
