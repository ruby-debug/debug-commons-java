package org.rubyforge.debugcommons;

import org.rubyforge.debugcommons.RubyDebugEvent;

public interface RubyDebugEventListener {

    void onDebugEvent(RubyDebugEvent e);
    
}
