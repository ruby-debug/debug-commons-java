package org.rubyforge.debugcommons;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.rubyforge.debugcommons.Util;
import org.rubyforge.debugcommons.model.RubyDebugTarget;

public final class RubyDebugTargetFactory {
    
    private static final String CLASSIC_DEBUG_NAME = "classic-debug.rb";
    private static final String CLASSIC_VERBOSE_DEBUG_NAME = "classic-debug-verbose.rb";
    
    private static final int DEFAULT_PORT = 1098;
    
    /**
     * Starts classic-debugger session for the given script. Debugger waits on
     * the first script's line.
     *
     * @param pathToClassicDebugDir directory containing classic-debug.rb and
     *        classic-debug-verbose.rb.
     * @param scriptPath script to be debugged
     * @param defaultPort whether to use default port or not
     * @param interpreter interpreter to be used
     * @param verbose verbose or not
     * @return {@link RubyDebugTarget} instance
     * @throws java.io.IOException
     * @throws org.rubyforge.debugcommons.RubyDebuggerException
     */
    public static RubyDebuggerProxy startClassicDebugger(
            final String pathToClassicDebugDir,
            final String scriptPath,
            final boolean defaultPort,
            final String interpreter,
            final boolean verbose)
            throws IOException, RubyDebuggerException {
        List<String> params = new ArrayList<String>();
        params.add(interpreter);
        params.add("-I");
        params.add(pathToClassicDebugDir);
        int port = DEFAULT_PORT;
        if (!defaultPort) {
            port = Util.findFreePort();
            if (port != -1) {
                try {
                    String path = createRemoteDebugPortFile(port);
                    params.add("-r");
                    params.add(path);
                } catch (IOException e) {
                    Util.severe("Could not create 'RemoteDebugPortFile'. Using default port.", e);
                }
            }
        }
        params.add("-r");
        params.add(verbose ? CLASSIC_VERBOSE_DEBUG_NAME : CLASSIC_DEBUG_NAME);
        params.add(scriptPath);
        Util.fine("Running: \"" + params + "\"");
        ProcessBuilder pb = new ProcessBuilder(params);
        File baseDir = new File(scriptPath).getParentFile();
        pb.directory(baseDir);
        RubyDebuggerProxy proxy = new RubyDebuggerProxy(RubyDebuggerProxy.CLASSIC_DEBUGGER);
        RubyDebugTarget target = new RubyDebugTarget(proxy, pb.start(), port, scriptPath);
        proxy.connect(target);
        RubyDebuggerProxy.PROXIES.add(proxy);
        return proxy;
    }
    
    /**
     * Starts Kent Sibilev's ruby-debug session for the given script. Debugger
     * waits on the first script's line.
     *
     * @param rdebugExecutable path to rdebug[.cmd]
     * @param scriptPath script to be debugged
     * @param defaultPort whether to use default port or not
     * @param verbose verbose or not
     * @return {@link RubyDebugTarget} instance
     * @throws java.io.IOException
     * @throws org.rubyforge.debugcommons.RubyDebuggerException
     */
    public static RubyDebuggerProxy startRubyDebug(
            final String rdebugExecutable,
            final String scriptPath,
            final boolean defaultPort,
            final boolean verbose)
            throws IOException, RubyDebuggerException {
        List<String> params = new ArrayList<String>();
        params.add(rdebugExecutable);
        params.add("--server");
        params.add("-w"); // wait for client to connect on command port
        params.add("-n"); // do not halt when client connects
        params.add("--port");
        int port = defaultPort ? DEFAULT_PORT : Util.findFreePort();
        params.add(String.valueOf(port));
        params.add("--cport");
        params.add(String.valueOf(port + 1));
        if (verbose) {
            params.add("-d");
        }
        params.add("-f");
        params.add("xml");
        params.add(scriptPath);
        Util.fine("Running: \"" + params + "\"");
        ProcessBuilder pb = new ProcessBuilder(params);
        File baseDir = new File(scriptPath).getParentFile();
        pb.directory(baseDir);
        RubyDebuggerProxy proxy = new RubyDebuggerProxy(RubyDebuggerProxy.RUBY_DEBUG);
        RubyDebugTarget target = new RubyDebugTarget(proxy, pb.start(), port, scriptPath);
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
    
}
