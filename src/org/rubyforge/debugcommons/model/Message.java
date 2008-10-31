package org.rubyforge.debugcommons.model;

public final class Message {

    private final String text;
    private final boolean debug;

    public Message(final String text, final boolean debug) {
        this.text = text;
        this.debug = debug;
    }

    public String getText() {
        return text;
    }

    public boolean isDebug() {
        return debug;
    }
}
