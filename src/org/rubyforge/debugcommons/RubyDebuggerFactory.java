package org.rubyforge.debugcommons;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.rubyforge.debugcommons.Util;
import org.rubyforge.debugcommons.model.RubyDebugTarget;

public final class RubyDebuggerFactory {
    
    private static final String CLASSIC_DEBUG_NAME = "classic-debug.rb";
    private static final String CLASSIC_VERBOSE_DEBUG_NAME = "classic-debug-verbose.rb";
    
    private static final int DEFAULT_PORT = 1098;
    
    /**
     * Starts classic-debugger session for the given script. Debugger waits on
     * the first script's line.
     *
     * @param descriptor {@link Descriptor} to be used
     * @param pathToClassicDebugDir directory containing classic-debug.rb and
     *        classic-debug-verbose.rb.
     * @param interpreter interpreter to be used
     * @return {@link RubyDebugTarget} instance
     * @throws java.io.IOException
     * @throws org.rubyforge.debugcommons.RubyDebuggerException
     */
    public static RubyDebuggerProxy startClassicDebugger(
            final Descriptor descriptor,
            final String pathToClassicDebugDir,
            final String interpreter)
            throws IOException, RubyDebuggerException {
        List<String> args = new ArrayList<String>();
        args.add(interpreter);
        args.add("-I");
        args.add(pathToClassicDebugDir);
        int port = DEFAULT_PORT;
        if (!descriptor.defaultPortUsed()) {
            port = Util.findFreePort();
            if (port != -1) {
                try {
                    String path = createRemoteDebugPortFile(port);
                    args.add("-r");
                    args.add(path);
                } catch (IOException e) {
                    Util.severe("Could not create 'RemoteDebugPortFile'. Using default port.", e);
                }
            }
        }
        if (descriptor.isSynchronizedOutput()) {
            String path = createIOSynchronizer();
            args.add("-r");
            args.add(path);
        }
        args.add("-r");
        args.add(descriptor.isVerbose() ? CLASSIC_VERBOSE_DEBUG_NAME : CLASSIC_DEBUG_NAME);
        args.addAll(descriptor.getAdditionalArgs());
        args.add(descriptor.getScriptPath());
        Util.fine("Running: \"" + args + "\"");
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(descriptor.getBaseDirectory());
        RubyDebuggerProxy proxy = new RubyDebuggerProxy(RubyDebuggerProxy.CLASSIC_DEBUGGER);
        RubyDebugTarget target = new RubyDebugTarget(proxy, pb.start(), port, descriptor.getScriptPath());
        proxy.connect(target);
        RubyDebuggerProxy.PROXIES.add(proxy);
        return proxy;
    }
    
    /**
     * Starts Kent Sibilev's ruby-debug session for the given script. Debugger
     * waits on the first script's line.
     *
     * @param descriptor {@link Descriptor} to be used
     * @param rdebugExecutable path to rdebug[.cmd]
     * @return {@link RubyDebugTarget} instance
     * @throws java.io.IOException
     * @throws org.rubyforge.debugcommons.RubyDebuggerException
     */
    public static RubyDebuggerProxy startRubyDebug(
            final Descriptor descriptor,
            final String rdebugExecutable)
            throws IOException, RubyDebuggerException {
        List<String> args = new ArrayList<String>();
        args.add(rdebugExecutable);
        args.add("--server");
        args.add("-w"); // wait for client to connect on command port
        args.add("-n"); // do not halt when client connects
        args.add("--port");
        int port = descriptor.defaultPortUsed() ? DEFAULT_PORT : Util.findFreePort();
        args.add(String.valueOf(port));
        args.add("--cport");
        args.add(String.valueOf(port + 1));
        if (descriptor.isVerbose()) {
            args.add("-d");
        }
        args.add("-f");
        args.add("xml");
        args.add(descriptor.getScriptPath());
        Util.fine("Running: \"" + args + "\"");
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(descriptor.getBaseDirectory());
        RubyDebuggerProxy proxy = new RubyDebuggerProxy(RubyDebuggerProxy.RUBY_DEBUG);
        RubyDebugTarget target = new RubyDebugTarget(proxy, pb.start(), port, descriptor.getScriptPath());
        proxy.connect(target);
        RubyDebuggerProxy.PROXIES.add(proxy);
        return proxy;
    }
    
    private static String createRemoteDebugPortFile(final int port) throws IOException {
        File debugParameterFile = File.createTempFile("classic-debug", ".rb");
        debugParameterFile.deleteOnExit();
        FileWriter fWriter = new FileWriter(debugParameterFile);
        new PrintWriter(fWriter).println("$RemoteDebugPort=" + port);
        fWriter.close();
        return debugParameterFile.getAbsolutePath();
    }
    
    private static String createIOSynchronizer() throws IOException {
        File ioSynchronizer = File.createTempFile("io-synchronizer", ".rb");
        ioSynchronizer.deleteOnExit();
        FileWriter fWriter = new FileWriter(ioSynchronizer);
        PrintWriter pwWriter = new PrintWriter(fWriter);
        pwWriter.println("$stdout.sync=true");
        pwWriter.println("$stderr.sync=true");
        fWriter.close();
        return ioSynchronizer.getAbsolutePath();
    }
    
    public static final class Descriptor {
        
        private boolean verbose;
        private boolean useDefaultPort;
        private String scriptPath;
        private boolean synchronizedOutput;
        private Collection<? extends String> additionalArgs;
        
        public boolean isVerbose() {
            return verbose;
        }
        
        public void setVerbose(boolean verbose) {
            this.verbose = verbose;
        }
        
        public boolean defaultPortUsed() {
            return useDefaultPort;
        }
        
        public void useDefaultPort(boolean useDefaultPort) {
            this.useDefaultPort = useDefaultPort;
        }
        
        public String getScriptPath() {
            return scriptPath;
        }
        
        /**
         * @param scriptPath script to be debugged
         */
        public void setScriptPath(String scriptPath) {
            this.scriptPath = scriptPath;
        }
        
        public boolean isSynchronizedOutput() {
            return synchronizedOutput;
        }
        
        public void setSynchronizedOutput(boolean synchronizedOutput) {
            this.synchronizedOutput = synchronizedOutput;
        }
        
        public Collection<? extends String> getAdditionalArgs() {
            return additionalArgs == null ? Collections.<String>emptySet() : additionalArgs;
        }
        
        public void setAdditionalArgs(Collection<? extends String> additionalArgs) {
            this.additionalArgs = additionalArgs;
        }
        
        File getBaseDirectory() {
            return new File(getScriptPath()).getParentFile();
        }
    }
    
}
