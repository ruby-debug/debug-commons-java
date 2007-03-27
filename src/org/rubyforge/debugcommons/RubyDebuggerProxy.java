package org.rubyforge.debugcommons;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.rubyforge.debugcommons.model.IRubyBreakpoint;
import org.rubyforge.debugcommons.model.SuspensionPoint;
import org.rubyforge.debugcommons.model.RubyThreadInfo;
import org.rubyforge.debugcommons.ReadersSupport;
import org.rubyforge.debugcommons.RubyDebugEventListener;
import org.rubyforge.debugcommons.RubyDebugEvent;
import org.rubyforge.debugcommons.model.RubyDebugTarget;
import org.rubyforge.debugcommons.model.RubyFrame;
import org.rubyforge.debugcommons.model.RubyFrameInfo;
import org.rubyforge.debugcommons.model.RubyThread;
import org.rubyforge.debugcommons.model.RubyVariable;
import org.rubyforge.debugcommons.model.RubyVariableInfo;

public final class RubyDebuggerProxy {
    
    static enum DebuggerType { CLASSIC_DEBUGGER, RUBY_DEBUG }
    
    static final DebuggerType CLASSIC_DEBUGGER = DebuggerType.CLASSIC_DEBUGGER;
    static final DebuggerType RUBY_DEBUG = DebuggerType.RUBY_DEBUG;
    
    public static final List<RubyDebuggerProxy> PROXIES = new CopyOnWriteArrayList<RubyDebuggerProxy>();
    
    private List<RubyDebugEventListener> listeners;
    
    private final DebuggerType debuggerType;
    private RubyDebugTarget debugTarged;
    private Socket commandSocket;
    private Socket controlSocket;
    private boolean connected;
    
    private PrintWriter commandWriter;
    private PrintWriter controlWriter;
    private RubyLoop rubyLoop;
    private ICommandFactory commandFactory;
    private ReadersSupport readersSupport;
    
    public RubyDebuggerProxy(DebuggerType debuggerType) {
        this.debuggerType = debuggerType;
        this.listeners = new CopyOnWriteArrayList<RubyDebugEventListener>();
    }
    
    public void connect(RubyDebugTarget debugTarged) throws IOException, RubyDebuggerException {
        this.debugTarged = debugTarged;
        this.readersSupport = new ReadersSupport(10); // default reading timeout 10s
    }
    
    public RubyDebugTarget getDebugTarged() {
        return debugTarged;
    }
    
    /** <b>Package-private</b> for unit tests only. */
    ReadersSupport getReadersSupport() throws RubyDebuggerException {
        return readersSupport;
    }
    
    /**
     * Set initial breakpoints and start the debugging process stopping (and
     * firing event to the {@link #addRubyDebugEventListener}) on the first
     * breakpoint.
     *
     * @param initialBreakpoints initial set of breakpoints to be set before
     *        triggering the debugging
     */
    public void startDebugging(final IRubyBreakpoint[] initialBreakpoints) throws RubyDebuggerException {
        try {
            switch(debuggerType) {
                case CLASSIC_DEBUGGER:
                    startClassicDebugger(initialBreakpoints);
                    break;
                case RUBY_DEBUG:
                    startRubyDebug(initialBreakpoints);
                    break;
                default:
                    throw new IllegalStateException("Unhandled debugger type: " + debuggerType);
            }
        } catch (RubyDebuggerException e) {
            PROXIES.remove(this);
            throw e;
        }
        startRubyLoop();
    }
    
    public boolean checkConnection() {
        return connected;
    }
    
    private void startClassicDebugger(final IRubyBreakpoint[] initialBreakpoints) throws RubyDebuggerException {
        commandFactory = new ClassicDebuggerCommandFactory();
        readersSupport.startCommandLoop(getCommandSocket());
        setBreakpoints(initialBreakpoints);
    }
    
    private void startRubyDebug(final IRubyBreakpoint[] initialBreakpoints) throws RubyDebuggerException {
        commandFactory = new RubyDebugCommandFactory();
        controlSocket = attach(debugTarged.getPort() + 1);
        readersSupport.startControlLoop(controlSocket);
        setBreakpoints(initialBreakpoints);
        readersSupport.startCommandLoop(getCommandSocket());
    }
    
    public void fireDebugEvent(final RubyDebugEvent e) {
        for (RubyDebugEventListener listener : listeners) {
            listener.onDebugEvent(e);
        }
    }
    
    public void addRubyDebugEventListener(final RubyDebugEventListener listener) {
        listeners.add(listener);
    }
    
    public void removeRubyDebugEventListener(final RubyDebugEventListener listener) {
        listeners.remove(listener);
    }
    
    private PrintWriter getControlWriter() throws RubyDebuggerException {
        if (controlWriter == null) {
            switch(debuggerType) {
                case CLASSIC_DEBUGGER:
                    // same writer for commands and control in the case of Classic Debugger
                    controlWriter = getCommandWriter();
                    break;
                case RUBY_DEBUG:
                    try {
                        controlWriter = new PrintWriter(getControlSocket().getOutputStream(), true);
                    } catch (IOException e) {
                        throw new RubyDebuggerException(e);
                    }
                    break;
                default:
                    throw new IllegalStateException("Unhandled debugger type: " + debuggerType);
            }
            connected = true;
        }
        return controlWriter;
    }
    
    private PrintWriter getCommandWriter() throws RubyDebuggerException {
        if (commandWriter == null) {
            try {
                commandWriter = new PrintWriter(getCommandSocket().getOutputStream(), true);
            } catch (IOException e) {
                throw new RubyDebuggerException(e);
            }
        }
        return commandWriter;
    }
    
    protected void setBreakpoints(final IRubyBreakpoint[] breakpoints) throws RubyDebuggerException {
        for (IRubyBreakpoint breakpoint: breakpoints) {
            addBreakpoint(breakpoint);
        }
    }
    
    public void addBreakpoint(final IRubyBreakpoint breakpoint) {
        try {
            if (breakpoint.isEnabled()) {
                String command = commandFactory.createAddBreakpoint(breakpoint.getFilePath(), breakpoint.getLineNumber());
                sendControlCommand(command);
                int index = getReadersSupport().readBreakpointNo();
                breakpoint.setIndex(index);
            }
        } catch (RubyDebuggerException e) {
            Util.severe("Exception during adding breakpoint.", e);
        }
    }
    
    public void removeBreakpoint(final IRubyBreakpoint breakpoint) {
        try {
            if (breakpoint.getIndex() != -1) {
                String command = commandFactory.createRemoveBreakpoint(breakpoint.getIndex());
                sendControlCommand(command);
                breakpoint.setIndex(-1);
            }
        } catch (RubyDebuggerException e) {
            Util.severe("Exception during removing breakpoint.", e);
        }
    }
    
    private void startRubyLoop() {
        rubyLoop = new RubyLoop();
        rubyLoop.start();
    }
    
    public Socket getCommandSocket() throws RubyDebuggerException {
        if (commandSocket == null) {
            commandSocket = attach(debugTarged.getPort());
        }
        return commandSocket;
    }
    
    public Socket getControlSocket() throws RubyDebuggerException {
        assert debuggerType == DebuggerType.RUBY_DEBUG : "control is used only in ruby-debug";
        if (controlSocket == null) {
            controlSocket = attach(debugTarged.getPort() + 1);
        }
        return controlSocket;
    }
    
    public void resume(final RubyThread thread) {
        try {
            sendCommand(commandFactory.createResume(thread));
        } catch (RubyDebuggerException e) {
            Util.severe("resuming of " + thread.getId() + " failed", e);
        }
    }
    
    private void sendControlCommand(final String s) throws RubyDebuggerException {
        Util.fine("Sending control command debugger: " + s);
        if (!debugTarged.isRunning()) {
            throw new RubyDebuggerException("Trying to send a control command [" + s + "] to terminated process");
        }
        getControlWriter().println(s);
    }
    
    private void sendCommand(final String s) throws RubyDebuggerException {
        Util.fine("Sending command debugger: " + s);
        if (!debugTarged.isRunning()) {
            throw new RubyDebuggerException("Trying to send a command [" + s + "] to terminated process");
        }
        getCommandWriter().println(s);
    }
    
    
    public void sendStepOverEnd(RubyFrame frame) {
        try {
            sendCommand(commandFactory.createStepOver(frame));
        } catch (RubyDebuggerException e) {
            Util.severe("Stepping failed", e);
        }
    }
    
    public void sendStepReturnEnd(RubyFrame frame) {
        try {
            sendCommand(commandFactory.createStepReturn(frame));
        } catch (RubyDebuggerException e) {
            Util.severe("Stepping failed", e);
        }
    }
    
    public void sendStepIntoEnd(RubyFrame frame) {
        try {
            sendCommand(commandFactory.createStepInto(frame));
        } catch (RubyDebuggerException e) {
            Util.severe("Stepping failed", e);
        }
    }
    
    public RubyThreadInfo[] readThreadInfo() throws RubyDebuggerException {
        sendCommand(commandFactory.createReadThreads());
        return getReadersSupport().readThreads();
    }
    
    public RubyFrame[] readFrames(RubyThread thread) throws RubyDebuggerException {
        sendCommand(commandFactory.createReadFrames(thread));
        RubyFrameInfo[] infos = getReadersSupport().readFrames();
        RubyFrame[] frames = new RubyFrame[infos.length];
        for (int i = 0; i < infos.length; i++) {
            RubyFrameInfo info = infos[i];
            frames[i] = new RubyFrame(thread, info);
        }
        return frames;
    }
    
    public RubyVariable[] readVariables(RubyFrame frame) throws RubyDebuggerException {
        sendCommand(commandFactory.createReadLocalVariables(frame));
        RubyVariableInfo[] infos = getReadersSupport().readVariables();
        RubyVariable[] variables= new RubyVariable[infos.length];
        for (int i = 0; i < infos.length; i++) {
            RubyVariableInfo info = infos[i];
            variables[i] = new RubyVariable(frame, info);
        }
        return variables;
    }
    
    public RubyVariable[] readInstanceVariables(final RubyVariable variable) throws RubyDebuggerException {
        sendCommand(commandFactory.createReadInstanceVariable(variable));
        RubyVariableInfo[] infos = getReadersSupport().readVariables();
        RubyVariable[] variables= new RubyVariable[infos.length];
        for (int i = 0; i < infos.length; i++) {
            RubyVariableInfo info = infos[i];
            variables[i] = new RubyVariable(variable, info);
        }
        return variables;
    }
    
    public RubyVariable inspectExpression(RubyFrame frame, String expression) throws RubyDebuggerException {
        sendCommand(commandFactory.createInspect(frame, expression));
        RubyVariableInfo[] infos = getReadersSupport().readVariables();
        return infos.length == 0 ? null : new RubyVariable(frame, infos[0]);
    }
    
    private void closeConnections() throws RubyDebuggerException, IOException {
        connected = false;
        if (commandSocket != null) {
            commandSocket.close();
        }
        if (controlSocket != null) {
            if (debugTarged.isRunning()) {
                sendControlCommand("exit");
            }
            controlSocket.close();
        }
    }
    
    private static Socket attach(int port) throws RubyDebuggerException {
        Socket socket = null;
        for (int tryCount = 20, i = 0; i < tryCount && socket == null; i++) {
            try {
                socket = new Socket("localhost", port);
            } catch (ConnectException e) {
                if (i == tryCount - 1) {
                    throw new RubyDebuggerException("Ruby process finished prematurely", e);
                }
                try {
                    Util.finest("Cannot connect to localhost:" + port + ". Trying again...(" + (tryCount - i - 1) + ')');
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    Util.severe("Interrupted during attaching.", e);
                    Thread.currentThread().interrupt();
                }
            } catch (IOException e) {
                throw new RubyDebuggerException(e);
            }
        }
        return socket;
    }
    
    private class RubyLoop extends Thread {
        
        RubyLoop() {
            this.setName("RubyDebuggerLoop [" + System.currentTimeMillis() + ']');
        }
        
        public void suspensionOccurred(final SuspensionPoint hit) {
            new Thread() {
                public void run() {
                    debugTarged.suspensionOccurred(hit);
                }
            }.start();
        }
        
        public void run() {
            try {
                if (debuggerType == CLASSIC_DEBUGGER) {
                    sendCommand("cont");
                }
                Util.finest("Waiting for breakpoints.");
                while (true) {
                    SuspensionPoint hit = getReadersSupport().readSuspension();
                    if (hit == SuspensionPoint.END) {
                        break;
                    }
                    Util.finest(hit.toString());
                    RubyLoop.this.suspensionOccurred(hit);
                }
            } catch (RubyDebuggerException e) {
                Util.severe("Exception in socket reader loop.", e);
            } finally {
                PROXIES.remove(RubyDebuggerProxy.this);
                debugTarged.terminate();
                try {
                    closeConnections();
                } catch (RubyDebuggerException e) {
                    Util.severe("Exception during closing connection", e);
                } catch (IOException e) {
                    Util.severe("Exception during closing connection", e);
                }
                Util.finest("Socket reader loop finished.");
            }
        }
    }
    
}
