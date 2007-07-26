package org.rubyforge.debugcommons;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;
import org.rubyforge.debugcommons.RubyDebuggerFactory.Descriptor;
import org.rubyforge.debugcommons.RubyDebuggerProxy.DebuggerType;
import org.rubyforge.debugcommons.model.IRubyBreakpoint;
import org.rubyforge.debugcommons.model.RubyDebugTarget;
import org.rubyforge.debugcommons.model.RubyThread;

public abstract class DebuggerTestBase extends TestBase {
    
    // XXX cannot be hardcoded. Use configuration files or property or ...
    private static final String PATH_TO_CLASSIC_DEBUG_DIR =
            "/space/ruby/sources/rubyforge.org/debug-commons/trunk/lib";
    private static final String PATH_TO_REMOTE_DEBUG_DIR =
            "/space/ruby/gemrepo/bin";
    
    protected RubyThread suspendedThread;
    
    //    private Socket socket;
    //    private PrintWriter socketWritter;
    protected File testFile;
    protected String testFilePath;
    private RubyDebugTarget debugTarget;
    private DebuggerType debuggerType;
    
    protected ReadersSupport readersSupport;
    
    protected OutputRedirectorThread rubyStdoutRedirectorThread;
    protected OutputRedirectorThread rubyStderrRedirectorThread;
    
    public DebuggerTestBase(String name) {
        super(name);
    }
    
    @Override
    protected void tearDown() throws Exception {
        String name = this.getName();
        System.out.println("Waiting for the server process to finish...");
        for (int i = 0; i < 8 && debugTarget.isRunning(); i++) {
            Thread.sleep(250);
            System.out.println(i + " (" + name + ")");
        }
        assertFalse("server process did not finish", debugTarget.isRunning());
    }
    
    protected void setDebuggerType(final DebuggerType debuggerType) {
        this.debuggerType = debuggerType;
    }
    
    //    public void sendCont() {
    //        sendRuby("cont");
    //    }
    
    //    protected void sendRuby(String debuggerCommand) {
    //        if (debugTarget.isRunning()) {
    //            Util.info("Sending: " + debuggerCommand);
    //            socketWritter.println(debuggerCommand);
    //        } else {
    //            throw new RuntimeException("Ruby debugger has finished prematurely.");
    //        }
    //    }
    
    protected File writeFile(String name, String... content) throws FileNotFoundException {
        File file = new File(getWorkDir(),  name);
        PrintWriter writer = new PrintWriter(file);
        try {
            for (String line : content) {
                writer.println(line);
            }
        } finally {
            writer.close();
        }
        return  file;
    }
    
    protected RubyDebuggerProxy prepareProxy(String... lines) throws IOException, RubyDebuggerException {
        return prepareProxy(null, null, lines);
    }
    
    
    protected RubyDebuggerProxy prepareProxy(String[] args, String... lines) throws IOException, RubyDebuggerException {
        return prepareProxy(null, args, lines);
    }
    
    protected RubyDebuggerProxy prepareProxy(File baseDir, String... lines) throws IOException, RubyDebuggerException {
        return prepareProxy(baseDir, null, lines);
    }
    
    protected RubyDebuggerProxy prepareProxy(File baseDir, String[] arguments, String... lines) throws IOException, RubyDebuggerException {
        testFile = writeFile("test.rb", lines);
        testFilePath = testFile.getAbsolutePath();
        return startDebugger(baseDir, arguments);
    }
    
    public RubyDebuggerProxy startDebugger() throws IOException, RubyDebuggerException {
        return startDebugger(null, null);
    }
    
    public RubyDebuggerProxy startDebugger(final File baseDir, final String[] scriptArguments) throws IOException, RubyDebuggerException {
        Descriptor descriptor = new Descriptor();
        descriptor.useDefaultPort(false);
        descriptor.setVerbose(true);
        descriptor.setScriptPath(testFilePath);
        descriptor.setBaseDirectory(baseDir);
        descriptor.setScriptArguments(scriptArguments);
        return startDebugger(descriptor);
    }
    
    public RubyDebuggerProxy startDebugger(final Descriptor descriptor) throws IOException, RubyDebuggerException {
        RubyDebuggerProxy proxy;
        switch(debuggerType) {
        case CLASSIC_DEBUGGER:
            proxy = RubyDebuggerFactory.startClassicDebugger(descriptor,
                    PATH_TO_CLASSIC_DEBUG_DIR, "ruby");
            break;
        case RUBY_DEBUG:
            File rdebug = new File(PATH_TO_REMOTE_DEBUG_DIR, "rdebug-ide");
            assertTrue("rdebug-ide file exists", rdebug.isFile());
            proxy = RubyDebuggerFactory.startRubyDebug(descriptor, rdebug.getAbsolutePath(), 10);
            break;
        default:
            throw new IllegalStateException("Unhandled debugger type: " + debuggerType);
        }
        debugTarget = proxy.getDebugTarged();
        rubyStderrRedirectorThread = new OutputRedirectorThread(debugTarget.getProcess().getErrorStream());
        rubyStderrRedirectorThread.start();
        rubyStdoutRedirectorThread = new OutputRedirectorThread(debugTarget.getProcess().getInputStream());
        rubyStdoutRedirectorThread.start();
        return proxy;
    }
    
    //    private Socket connect(int port) throws IOException {
    //        for (int tryCount = 8, i = 0; i < tryCount && socket == null; i++) {
    //            try {
    //                socket = new Socket("localhost", port + 1);
    //            } catch (ConnectException e) {
    //                if (i == tryCount - 1) {
    //                    throw new RuntimeException(
    //                            "Ruby process finished prematurely. Last line in stderr: "
    //                            + rubyStderrRedirectorThread.getLastLine(), e);
    //                }
    //                try {
    //                    System.err.println("Cannot connect to localhost:" + port + ". Trying again...(" + (tryCount - i - 1) + ')');
    //                    Thread.sleep(500);
    //                } catch (InterruptedException e1) {
    //                    e1.printStackTrace();
    //                    Thread.currentThread().interrupt();
    //                }
    //            }
    //        }
    //        return socket;
    //    }
    //
    //    protected int readBreakpointNo() {
    //        return readersSupport.readBreakpointNo();
    //    }
    //
    //    protected int readThreads() {
    //        return readersSupport.readThreads();
    //    }
    //
    //    protected void assertSuspension(String file, int line, boolean isBreakpoint, int threadId) throws Exception {
    //        SuspensionPoint suspension = readersSupport.readSuspension();
    //        assertEquals(file, suspension.getFile());
    //        assertEquals(line, suspension.getLine());
    //        assertEquals(isBreakpoint, suspension.isBreakpoint());
    //    }
    //
    //    protected void assertSuspension(String file, int line, boolean isBreakpoint) throws Exception {
    //        assertSuspension(file, line, isBreakpoint, 1);
    //    }
    //
    //    protected void assertTestSuspension(int line, boolean isBreakpoint) throws Exception {
    //        assertSuspension("test.rb", line, isBreakpoint, 1);
    //    }
    
    protected void startDebugging(
            final RubyDebuggerProxy proxy,
            final IRubyBreakpoint[] breakpoints,
            final int nOfEvents) throws InterruptedException {
        waitForEvents(proxy, nOfEvents, new Runnable() {
            public void run() {
                try {
                    proxy.startDebugging(breakpoints);
                    assertTrue("proxy connected", proxy.checkConnection());
                } catch (RubyDebuggerException e) {
                    fail("Cannot start debugger: " + e);
                }
            }
        });
    }
    
    protected void resumeSuspendedThread(final RubyDebuggerProxy proxy) throws InterruptedException {
        waitForEvents(proxy, 1, new Runnable() {
            public void run() {
                synchronized(DebuggerTestBase.this) {
                    suspendedThread.resume();
                    suspendedThread = null;
                }
            }
        });
    }
    
    protected void doStepOver(final RubyDebuggerProxy proxy, final boolean forceNewLine) throws InterruptedException {
        waitForEvents(proxy, 1,new Runnable() {
            public void run() {
                synchronized(DebuggerTestBase.this) {
                    try {
                        suspendedThread.stepOver(forceNewLine);
                        suspendedThread = null;
                    } catch (RubyDebuggerException e) {
                        fail("Unable do step: " + e);
                    }
                }
            }
        });
    }
    
    protected void waitForEvents(RubyDebuggerProxy proxy, int n, Runnable block) throws InterruptedException {
        final CountDownLatch events = new CountDownLatch(n);
        RubyDebugEventListener listener = new RubyDebugEventListener() {
            public void onDebugEvent(RubyDebugEvent e) {
                if (e.isSuspensionType()) {
                    synchronized(DebuggerTestBase.this) {
                        DebuggerTestBase.this.suspendedThread = e.getRubyThread();
                    }
                }
                Util.finest("Received event: " + e);
                events.countDown();
            }
        };
        proxy.addRubyDebugEventListener(listener);
        block.run();
        events.await();
        proxy.removeRubyDebugEventListener(listener);
    }
    
    protected static final class TestBreakpoint implements IRubyBreakpoint {
        
        private String file;
        private int line;
        private int index;
        private boolean enabled;
        
        public TestBreakpoint(String file, int line) {
            this.file = file;
            this.line = line;
            this.enabled = true;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public String getFilePath() {
            return file;
        }
        
        public int getLineNumber() {
            return line;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public void setIndex(int index) {
            this.index = index;
        }
        
        public int getIndex() {
            return index;
        }
        
    }
    
}
