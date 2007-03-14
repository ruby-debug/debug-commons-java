package org.rubyforge.debugcommons.model;

import org.rubyforge.debugcommons.RubyDebuggerProxy;

abstract class RubyEntity {
    
    private RubyDebuggerProxy proxy;
    
    protected RubyEntity(final RubyDebuggerProxy proxy) {
        this.proxy = proxy;
    }
    
    protected RubyDebuggerProxy getProxy() {
        return proxy;
    }
    
}
