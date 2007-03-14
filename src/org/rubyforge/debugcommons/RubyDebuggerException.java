package org.rubyforge.debugcommons;

public class RubyDebuggerException extends Exception {
    
    /**
     * Constructs an instance of <code>RubyDebuggerException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public RubyDebuggerException(String msg) {
        super(msg);
    }
    
    public RubyDebuggerException(String msg, Throwable t) {
        super(msg, t);
    }
    
    public RubyDebuggerException(Throwable t) {
        super(t);
    }
    
}
