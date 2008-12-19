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

package org.rubyforge.debugcommons;

import org.rubyforge.debugcommons.reader.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rubyforge.debugcommons.model.Message;
import org.rubyforge.debugcommons.model.RubyFrameInfo;
import org.rubyforge.debugcommons.model.SuspensionPoint;
import org.rubyforge.debugcommons.model.RubyThreadInfo;
import org.rubyforge.debugcommons.model.RubyVariableInfo;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

final class ReadersSupport {

    private static final Logger LOGGER = Logger.getLogger(ReadersSupport.class.getName());
    
    private static final String BREAKPOINT_ELEMENT = "breakpoint";
    private static final String SUSPENDED_ELEMENT = "suspended";
    private static final String EXCEPTION_ELEMENT = "exception";
    private static final String BREAKPOINT_ADDED_ELEMENT = "breakpointAdded";
    private static final String BREAKPOINT_DELETED_ELEMENT = "breakpointDeleted";
    private static final String CONDITION_SET_ELEMENT = "conditionSet";
    private static final String CATCHPOINT_SET_ELEMENT = "catchpointSet";
    
    private static final String THREADS_ELEMENT = "threads";
    private static final String FRAMES_ELEMENT = "frames";
    private static final String VARIABLES_ELEMENT = "variables";
    
    private static final String PROCESSING_EXCEPTION_ELEMENT = "processingException";
    
    private static final String RUBY_DEBUG_PROMPT = "PROMPT";
    
    /** Message sent by debugger backend when debugger has finished. */
    private static final String FINISHED = "finished";

    /**
     * Reading timeout until giving up when polling information from socket
     * communication.
     */
    private final long timeout;
    
    private final BlockingQueue<RubyThreadInfo[]> threads;
    private final BlockingQueue<RubyFrameInfo[]> frames;
    private final BlockingQueue<RubyVariableInfo[]> variables;
    
    private final BlockingQueue<SuspensionPoint> suspensions;
    private final BlockingQueue<Integer> addedBreakpoints;
    private final BlockingQueue<Integer> removedBreakpoints;
    private final BlockingQueue<Integer> conditionSets;
    private final BlockingQueue<String> catchpointSets;
    
    private boolean finished;
    private boolean unexpectedFail;
    
    /**
     * @param timeout reading timeout until giving up when polling information
     *        from socket communication.
     */
    ReadersSupport(final long timeout) {
        this.timeout = timeout;
        this.threads = new LinkedBlockingQueue<RubyThreadInfo[]>();
        this.frames = new LinkedBlockingQueue<RubyFrameInfo[]>();
        this.variables = new LinkedBlockingQueue<RubyVariableInfo[]>();
        this.suspensions = new LinkedBlockingQueue<SuspensionPoint>();
        this.addedBreakpoints = new LinkedBlockingQueue<Integer>();
        this.removedBreakpoints = new LinkedBlockingQueue<Integer>();
        this.conditionSets = new LinkedBlockingQueue<Integer>();
        this.catchpointSets = new LinkedBlockingQueue<String>();
    }
    
    void startCommandLoop(final InputStream is) throws RubyDebuggerException {
        try {
            new XPPLoop(is, ReadersSupport.class + " command loop").start();
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
                    LOGGER.finest("got ruby-debug prompt message");
                } else {
                    assert false : "Unexpected state: got text \"" + xpp.getText() + '"';
                }
            } else if (eventType == XmlPullParser.START_DOCUMENT) {
                // OK, first cycle, do nothing.
            } else {
                assert false : "Unexpected state: " + eventType;
            }
            if (finished) {
                break;
            }
            eventType = xpp.next();
            Util.logEvent(xpp);
        } while (eventType != XmlPullParser.END_DOCUMENT);
    }
    
    private void processElement(final XmlPullParser xpp) throws IOException, XmlPullParserException {
        String element = xpp.getName();
        if (BREAKPOINT_ADDED_ELEMENT.equals(element)) {
            addedBreakpoints.add(BreakpointAddedReader.readBreakpointNo(xpp));
        } else if (BREAKPOINT_DELETED_ELEMENT.equals(element)) {
            removedBreakpoints.add(BreakpointDeletedReader.readBreakpointNo(xpp));
        } else if (BREAKPOINT_ELEMENT.equals(element) || SUSPENDED_ELEMENT.equals(element) || EXCEPTION_ELEMENT.equals(element)) {
            SuspensionPoint sp = SuspensionReader.readSuspension(xpp);
            suspensions.add(sp);
        } else if (CONDITION_SET_ELEMENT.equals(element)) {
            conditionSets.add(ConditionSetReader.readBreakpointNo(xpp));
        } else if (CATCHPOINT_SET_ELEMENT.equals(element)) {
            catchpointSets.add(CatchpointSetReader.readExceptionClassName(xpp));
        } else if (THREADS_ELEMENT.equals(element)) {
            threads.add(ThreadInfoReader.readThreads(xpp));
        } else if (FRAMES_ELEMENT.equals(element)) {
            frames.add(FramesReader.readFrames(xpp));
        } else if (VARIABLES_ELEMENT.equals(element)) {
            variables.add(VariablesReader.readVariables(xpp));
        } else if (PROCESSING_EXCEPTION_ELEMENT.equals(element)) {
            VariablesReader.logProcessingException(xpp);
            variables.add(new RubyVariableInfo[0]);
        } else {
            Message message = ErrorReader.tryToReadMessageOrError(xpp, element);
            if (message != null) {
                if (message.getText().equals(FINISHED)) {
                    LOGGER.fine("Got 'finished' <message>, text == finished");
                    finished = true;
                }
            } else {
                assert false : "Unexpected element: " + element;
            }
        }
    }

    private <T> T[] readInfo(BlockingQueue<T[]> queue) throws RubyDebuggerException {
        try {
            T[] result = queue.poll(timeout, TimeUnit.SECONDS);
            if (result == null) {
                throw new RubyDebuggerException("Unable to read information in the specified timeout [" + timeout + "s]");
            } else {
                return result;
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RubyDebuggerException("Interruped during reading information " + queue.getClass(), ex);
        }
    }
    
    RubyThreadInfo[] readThreads() throws RubyDebuggerException {
        return readInfo(threads);
    }
    
    RubyFrameInfo[] readFrames() throws RubyDebuggerException {
        return readInfo(frames);
    }
    
    RubyVariableInfo[] readVariables() throws RubyDebuggerException {
        return readInfo(variables);
    }
    
    int readAddedBreakpointNo() throws RubyDebuggerException {
        return poll(addedBreakpoints, "added breakpoint number");
    }
    
    int readConditionSet() throws RubyDebuggerException {
        return poll(conditionSets, "breakpoint number of the set condition");
    }

    String readCatchpointSet() throws RubyDebuggerException {
        return poll(catchpointSets, "catchpoint set");
    }

    int waitForRemovedBreakpoint(int breakpointID) throws RubyDebuggerException {
        int removedID = poll(removedBreakpoints, "breakpoint number of the removed breakpoint (" + breakpointID + ")");
        if (removedID != breakpointID) {
            throw new RubyDebuggerException("Unexpected breakpoint removed. " +
                    "Received id: " + removedID + ", expected: " + breakpointID);
        }
        return removedID;
    }
    
    private <T> T poll(final BlockingQueue<T> queue, final String toRead) throws RubyDebuggerException {
        try {
            T t = queue.poll(timeout, TimeUnit.SECONDS);
            if (t == null) {
                throw new RubyDebuggerException("Unable to read " + toRead + " in the specified timeout [" + timeout + "s]");
            } else {
                return t;
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RubyDebuggerException("Interruped during reading " +
                    toRead + " (timeout: " + timeout + ')', ex);
        }
    }

    SuspensionPoint readSuspension() {
        try {
            return suspensions.take();
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "Interruped during reading suspension point", ex);
            return null;
        }
    }

    boolean isUnexpectedFail() {
        return unexpectedFail;
    }
    
    private static XmlPullParser getXpp(final InputStream is)  throws XmlPullParserException, IOException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance(
                "org.kxml2.io.KXmlParser,org.kxml2.io.KXmlSerializer", null);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new BufferedReader(new InputStreamReader(is)));
        return xpp;
    }

    private class XPPLoop extends Thread {
        
        private final XmlPullParser xpp;
        private final InputStream is;
        
        XPPLoop(final InputStream is, final String loopName) throws XmlPullParserException, IOException {
            super(loopName);
            this.is = is;
            this.xpp = getXpp(is);
        }
        
        @Override
        public void run() {
            try {
                LOGGER.fine("Starting ReadersSupport readloop: " + getName());
                startXPPLoop(xpp);
                LOGGER.fine("ReadersSupport readloop [" + getName() + "] successfully finished.");
            } catch (IOException e) {
                // Debugger is just killed. So this is currently more or less
                // expected behaviour.
                //  - no XmlPullParser.END_DOCUMENT is sent
                //  - incorectly handling finishing of the session in the backends
                LOGGER.fine("SocketException. Loop [" + getName() + "]: " + e.getMessage());
                LOGGER.log(Level.FINE, e.getMessage(), e);
                ReadersSupport.this.unexpectedFail = true;
            } catch (XmlPullParserException e) {
                LOGGER.log(Level.SEVERE, "Exception during ReadersSupport loop [" + getName() + ']', e);
                ReadersSupport.this.unexpectedFail = true;
            } finally {
                suspensions.add(SuspensionPoint.END);
                try {
                    is.close();
                    Thread.sleep(1000); // Avoid Commodification Exceptions
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Cannot close socket's input stream", e);
                } catch (InterruptedException e) {
                    LOGGER.log(Level.SEVERE, "Readers loop interrupted", e);
                }
            }
        }
    }
    
}
