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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;
import org.rubyforge.debugcommons.RubyDebuggerFactory.Descriptor;
import org.rubyforge.debugcommons.RubyDebuggerProxy.DebuggerType;
import org.rubyforge.debugcommons.model.IRubyBreakpoint;
import org.rubyforge.debugcommons.model.IRubyExceptionBreakpoint;
import org.rubyforge.debugcommons.model.IRubyLineBreakpoint;
import org.rubyforge.debugcommons.model.RubyDebugTarget;
import org.rubyforge.debugcommons.model.RubyThread;

public abstract class DebuggerTestBase extends TestBase {
    
    private static final Logger LOGGER = Logger.getLogger(DebuggerTestBase.class.getName());

    private static final String PATH_TO_CLASSIC_DEBUG_DIR;
    private static final String PATH_TO_RDEBUG_IDE;
    private static final String GEM_HOME;
    private static final String GEM_PATH;
    
    static {
        InputStream stream = null;
        Properties config = new Properties();
        try {
            String resourceName = "/org/rubyforge/debugcommons/testconfig.properties";
            stream = DebuggerTestBase.class.getResourceAsStream(resourceName);
            if (stream == null) {
                throw new RuntimeException("Resource not found: " + resourceName);
            }
            config.load(stream);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // silently ignore
                }
            }
        }
        
        PATH_TO_CLASSIC_DEBUG_DIR = config.getProperty("classic.debug.lib.dir");
        PATH_TO_RDEBUG_IDE = config.getProperty("rdebug.executable");
        GEM_HOME = config.getProperty("gem.home");
        GEM_PATH = config.getProperty("gem.path");
    }

    protected RubyThread suspendedThread;
    
    protected File testFile;
    protected String testFilePath;
    private RubyDebugTarget debugTarget;
    private DebuggerType debuggerType;
    private int timeout = 10; // 10s by default
    
    ReadersSupport readersSupport;
    
    protected OutputRedirectorThread rubyStdoutRedirectorThread;
    protected OutputRedirectorThread rubyStderrRedirectorThread;
    
    public DebuggerTestBase(String name) {
        super(name);
        setDebuggerType(RubyDebuggerProxy.RUBY_DEBUG);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        assertTrue("Correctly set testconfig.properties#classic.debug.lib.dir",
                new File(PATH_TO_CLASSIC_DEBUG_DIR, "classic-debug.rb").isFile());
        assertTrue("Correctly set testconfig.properties#rdebug.executable",
                new File(PATH_TO_RDEBUG_IDE).isFile());
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        String name = this.getName();
        System.out.println("Waiting for the server process to finish...");
        if (debugTarget != null) {
            for (int i = 0; i < 8 && debugTarget.isAvailable(); i++) {
                Thread.sleep(250);
                System.out.println(i + " (" + name + ")");
            }
            assertFalse("server process did not finish", debugTarget.isRunning());
        }
    }
    
    protected void setDebuggerType(final DebuggerType debuggerType) {
        this.debuggerType = debuggerType;
    }
    
    protected void setTimeout(final int timeout) {
        this.timeout = timeout;
    }
    
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
        descriptor.setDebuggeePath(testFilePath);
        descriptor.setBaseDirectory(baseDir);
        descriptor.setScriptArguments(scriptArguments);
        return startDebugger(descriptor);
    }
    
    public RubyDebuggerProxy startDebugger(final Descriptor descriptor) throws IOException, RubyDebuggerException {
        RubyDebuggerProxy proxy;
        switch (debuggerType) {
            case CLASSIC_DEBUGGER:
                proxy = RubyDebuggerFactory.startClassicDebugger(descriptor,
                        PATH_TO_CLASSIC_DEBUG_DIR, "ruby", timeout);
                break;
            case RUBY_DEBUG:
                File rdebug = new File(PATH_TO_RDEBUG_IDE);
                assertTrue("rdebug-ide file exists", rdebug.isFile());
                descriptor.setEnvironment(tweakTestProcessEnvironment(descriptor.getEnvironment()));
                proxy = RubyDebuggerFactory.startRubyDebug(descriptor, rdebug.getAbsolutePath(), "ruby", timeout);
                break;
            default:
                throw new IllegalStateException("Unhandled debugger type: " + debuggerType);
        }
        debugTarget = proxy.getDebugTarget();
        rubyStderrRedirectorThread = new OutputRedirectorThread(debugTarget.getProcess().getErrorStream());
        rubyStderrRedirectorThread.start();
        rubyStdoutRedirectorThread = new OutputRedirectorThread(debugTarget.getProcess().getInputStream());
        rubyStdoutRedirectorThread.start();
        return proxy;
    }

    /** Start debuggee process without attaching to it. */
    protected Process startDebuggerProcess(
            final File toTest,
            final int port) throws IOException {
        List<String> args = Arrays.asList(PATH_TO_RDEBUG_IDE, "-p", "" + port,
                "--xml-debug", "--", toTest.getAbsolutePath());
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(toTest.getParentFile());
        pb.environment().putAll(tweakTestProcessEnvironment(pb.environment()));
        LOGGER.fine("Running [basedir: " + toTest.getParentFile().getPath() +
                "]: \"" + Util.getProcessAsString(args) + "\"");
        return pb.start();
    }

    protected void attach(
            final RubyDebuggerProxy proxy,
            final IRubyBreakpoint[] breakpoints,
            final int nOfEvents) throws InterruptedException {
        waitForEvents(proxy, nOfEvents, new Runnable() {
            public void run() {
                try {
                    proxy.attach(breakpoints);
                    assertTrue("proxy alive", proxy.isReady());
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
                if (e.isSuspensionType() || e.isExceptionType()) {
                    synchronized(DebuggerTestBase.this) {
                        DebuggerTestBase.this.suspendedThread = e.getRubyThread();
                    }
                }
                LOGGER.finest("Received event: " + e);
                events.countDown();
                LOGGER.finest("Current events count: " + events.getCount());
            }
        };
        proxy.addRubyDebugEventListener(listener);
        block.run();
        events.await();
        proxy.removeRubyDebugEventListener(listener);
    }

    private Map<? extends String, ? extends String> tweakTestProcessEnvironment(
            final Map<? extends String, ? extends String> origEnv) {
        Map<String, String> env;
        if (origEnv == null) {
            env = new HashMap<String, String>();
        } else {
            env = new HashMap<String, String>(origEnv);
        }
        if (GEM_HOME != null) {
            env.put("GEM_HOME", GEM_HOME);
        }
        if (GEM_PATH != null) {
            env.put("GEM_PATH", GEM_PATH);
        }
        return env;
    }
    
    protected static final class TestExceptionBreakpoint implements IRubyExceptionBreakpoint {
        
        private String exception;

        public TestExceptionBreakpoint(String exception) {
            this.exception = exception;
        }
        
        public boolean isEnabled() {
            return true;
        }

        public String getException() {
            return exception;
        }
        
    }
    
    protected static final class TestBreakpoint implements IRubyLineBreakpoint {
        
        private String file;
        private int line;
        private int index;
        private boolean enabled;
        private String condition;
        
        public TestBreakpoint(String file, int line) {
            this(file, line, null);
        }
        
        public TestBreakpoint(String file, int line, String condition) {
            this.file = file;
            this.line = line;
            this.condition = condition;
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

        public String getCondition() {
            return condition;
        }
        
    }
    
}
