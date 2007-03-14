package org.rubyforge.debugcommons;

import java.net.SocketException;
import org.rubyforge.debugcommons.reader.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.rubyforge.debugcommons.RubyDebuggerException;
import org.rubyforge.debugcommons.Util;
import org.rubyforge.debugcommons.model.RubyFrameInfo;
import org.rubyforge.debugcommons.model.SuspensionPoint;
import org.rubyforge.debugcommons.model.RubyThreadInfo;
import org.rubyforge.debugcommons.model.RubyVariableInfo;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

final class ReadersSupport {
    
    private static final String BREAKPOINT_ELEMENT = "breakpoint";
    private static final String SUSPENDED_ELEMENT = "suspended";
    private static final String ERROR_ELEMENT = "error";
    private static final String MESSAGE_ELEMENT = "message";
    private static final String BREAKPOINT_ADDED_ELEMENT = "breakpointAdded";
    private static final String BREAKPOINT_DELETED_ELEMENT = "breakpointDeleted";
    
    private static final String THREADS_ELEMENT = "threads";
    private static final String FRAMES_ELEMENT = "frames";
    private static final String VARIABLES_ELEMENT = "variables";
    
    private static final String PROCESSING_EXCEPTION_ELEMENT = "processingException";
    
    private static final String RUBY_DEBUG_PROMPT = "PROMPT";
    
    private final BlockingQueue<RubyThreadInfo[]> threads;
    private final BlockingQueue<RubyFrameInfo[]> frames;
    private final BlockingQueue<RubyVariableInfo[]> variables;
    
    private final BlockingQueue<SuspensionPoint> suspensions;
    private final BlockingQueue<Integer> addedBreakpoints;
    
    ReadersSupport() throws RubyDebuggerException {
        this.threads = new LinkedBlockingQueue<RubyThreadInfo[]>();
        this.frames = new LinkedBlockingQueue<RubyFrameInfo[]>();
        this.variables = new LinkedBlockingQueue<RubyVariableInfo[]>();
        this.suspensions = new LinkedBlockingQueue<SuspensionPoint>();
        this.addedBreakpoints = new LinkedBlockingQueue<Integer>();
    }
    
    void startControlLoop(final Socket controlSocket) throws RubyDebuggerException {
        startLoop(controlSocket, "control loop");
    }
    
    void startCommandLoop(final Socket commandSocket) throws RubyDebuggerException {
        startLoop(commandSocket, "command loop");
    }
    
    private void startLoop(final Socket socket, final String name) throws RubyDebuggerException {
        try {
            new XPPLoop(getXpp(socket), ReadersSupport.class + " " + name).start();
        } catch (IOException e) {
            throw new RubyDebuggerException(e);
        } catch (XmlPullParserException e) {
            throw new RubyDebuggerException(e);
        }
    }
    
    private void startXPPLoop(final XmlPullParser xpp) throws XmlPullParserException, IOException {
        int eventType = xpp.getEventType();
        do {
            if (eventType == XmlPullParser.START_TAG) {
                processElement(xpp);
            } else if (eventType == XmlPullParser.END_TAG) {
                assert false : "Unexpected state: end tag " + xpp.getName();
            } else if (eventType == XmlPullParser.TEXT) {
                if (xpp.getText().contains(RUBY_DEBUG_PROMPT)) {
                    Util.finest("got ruby-debug prompt message");
                } else {
                    assert false : "Unexpected state: text " + xpp.getText();
                }
            } else if (eventType == XmlPullParser.START_DOCUMENT) {
                // OK, first cycle, do nothing.
            } else {
                assert false : "Unexpected state: " + eventType;
            }
            eventType = xpp.next();
        } while (eventType != XmlPullParser.END_DOCUMENT);
    }
    
    private void processElement(final XmlPullParser xpp) throws IOException, XmlPullParserException {
        String element = xpp.getName();
        if (BREAKPOINT_ADDED_ELEMENT.equals(element)) {
            addedBreakpoints.add(BreakpointAddedReader.readBreakpointNo(xpp));
        } else if (BREAKPOINT_DELETED_ELEMENT.equals(element)) {
            addedBreakpoints.add(BreakpointDeletedReader.readBreakpointNo(xpp));
        } else if (BREAKPOINT_ELEMENT.equals(element)) {
            SuspensionPoint sp = SuspensionReader.readSuspension(xpp);
            suspensions.add(sp);
        } else if (SUSPENDED_ELEMENT.equals(element)) {
            SuspensionPoint sp = SuspensionReader.readSuspension(xpp);
            suspensions.add(sp);
        } else if (ERROR_ELEMENT.equals(element) || MESSAGE_ELEMENT.equals(element)) {
            Util.info(ErrorReader.readMessage(xpp));
        } else if (THREADS_ELEMENT.equals(element)) {
            threads.add(ThreadInfoReader.readThreads(xpp));
        } else if (FRAMES_ELEMENT.equals(element)) {
            frames.add(FramesReader.readFrames(xpp));
        } else if (VARIABLES_ELEMENT.equals(element)) {
            variables.add(VariablesReader.readVariables(xpp));
        } else if (PROCESSING_EXCEPTION_ELEMENT.equals(element)) {
            variables.add(VariablesReader.readProcessingException(xpp));
        } else {
            assert false : "Unexpected element: " + element;
        }
    }
    
    int readBreakpointNo() {
        try {
            return addedBreakpoints.take();
        } catch (InterruptedException ex) {
            Util.severe("Interruped during reading added breakpoint number", ex);
            return -1;
        }
    }
    
    RubyThreadInfo[] readThreads() {
        try {
            return threads.take();
        } catch (InterruptedException ex) {
            Util.severe("Interruped during reading threads information", ex);
            return new RubyThreadInfo[0];
        }
    }
    
    RubyFrameInfo[] readFrames() {
        try {
            return frames.take();
        } catch (InterruptedException ex) {
            Util.severe("Interruped during reading frames information", ex);
            return new RubyFrameInfo[0];
        }
    }
    
    RubyVariableInfo[] readVariables() {
        try {
            return variables.take();
        } catch (InterruptedException ex) {
            Util.severe("Interruped during reading variables information", ex);
            return new RubyVariableInfo[0];
        }
    }
    
    SuspensionPoint readSuspension() {
        try {
            return suspensions.take();
        } catch (InterruptedException ex) {
            Util.severe("Interruped during reading suspension point", ex);
            return null;
        }
    }
    
    private static XmlPullParser getXpp(Socket socket)  throws XmlPullParserException, IOException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance(
                "org.kxml2.io.KXmlParser,org.kxml2.io.KXmlSerializer", null);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new BufferedReader(new InputStreamReader(socket.getInputStream())));
        return xpp;
    }
    
    private class XPPLoop extends Thread {
        
        final XmlPullParser xpp;
        
        XPPLoop(final XmlPullParser xpp, final String loopName) {
            super(loopName);
            this.xpp = xpp;
        }
        
        public void run() {
            try {
                Util.fine("Starting ReadersSupport readloop: " + getName());
                startXPPLoop(xpp);
                Util.fine("ReadersSupport readloop [" + getName() + "] successfully finished.");
            } catch (SocketException e) {
                // XXX: happens with ruby-debug since either:
                //  - no XmlPullParser.END_DOCUMENT is sent
                //  - incorectly handling finishing of the session
                Util.warning("SocketException. Loop [" + getName() + "]: " + e.getMessage());
            } catch (XmlPullParserException e) {
                Util.severe("Exception during ReadersSupport loop [" + getName() + ']', e);
                e.printStackTrace();
            } catch (IOException e) {
                // XXX: seems that classic-debugger does not close correctly
                // connection. When it is fixed, uncomment below exception.
                Util.severe("Exception during ReadersSupport loop [" + getName() + "]: ", e);
            } finally {
                suspensions.add(SuspensionPoint.END);
                try {
                    Thread.sleep(1000); // Avoid Commodification Exceptions
                } catch (InterruptedException e) {
                    Util.severe("Readers loop interrupted", e);
                    e.printStackTrace();
                }
            }
        }
    }
    
}
