package org.rubyforge.debugcommons;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.rubyforge.debugcommons.RubyDebuggerProxy.DebuggerType;
import org.rubyforge.debugcommons.model.RubyDebugTarget;

import static org.rubyforge.debugcommons.RubyDebuggerProxy.CLASSIC_DEBUGGER;
import static org.rubyforge.debugcommons.RubyDebuggerProxy.RUBY_DEBUG;

public final class RubyDebuggerFactory {

    private static final Logger LOGGER = Logger.getLogger(RubyDebuggerFactory.class.getName());

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
            final String interpreter,
            final int timeout)
            throws IOException, RubyDebuggerException {
        descriptor.setType(CLASSIC_DEBUGGER);
        List<String> args = new ArrayList<String>();
        args.add(interpreter);
        if (descriptor.isJRuby()) {
            adjustForJRuby(args);
        }
        args.addAll(descriptor.getAddtionalOptions());
        args.add("-I");
        args.add(pathToClassicDebugDir);
        if (!descriptor.defaultPortUsed()) {
            if (descriptor.getPort() != -1) {
                try {
                    String path = createRemoteDebugPortFile(descriptor.getPort());
                    args.add("-r");
                    args.add(path);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Could not create 'RemoteDebugPortFile'. Using default port.", e);
                }
            }
        }
        appendIOSynchronizer(args, descriptor);
        args.add("-r");
        args.add(descriptor.isVerbose() ? CLASSIC_VERBOSE_DEBUG_NAME : CLASSIC_DEBUG_NAME);
        args.add(descriptor.getScriptPath());
        if (descriptor.getScriptArguments() != null) {
            args.addAll(Arrays.asList(descriptor.getScriptArguments()));
        }
        return startDebugger(descriptor, args, timeout);
    }

    /**
     * The same as {@link #startClassicDebugger(Descriptor, String, String, int)}.
     */
    public static RubyDebuggerProxy startClassicDebugger(
            final Descriptor descriptor,
            final String pathToClassicDebugDir,
            final String interpreter) throws IOException, RubyDebuggerException {
        return startClassicDebugger(descriptor, pathToClassicDebugDir, interpreter, 10);
    }

    /**
     * Starts <tt>ruby-debug-ide</tt> session for the given script. Debugger
     * waits on the first script's line.
     *
     * @param descriptor {@link Descriptor} to be used
     * @param rdebugExecutable path to rdebug-ide
     * @param interpreter interpreter to be used for running
     *        <code>rdebugExecutable</code>. If <code>null</code>
     *        <code>rdebugExecutable</code> will be run directly using default
     *        system interpreter.
     * @return {@link RubyDebugTarget} instance
     * @throws java.io.IOException
     * @throws org.rubyforge.debugcommons.RubyDebuggerException
     */
    public static RubyDebuggerProxy startRubyDebug(
            final Descriptor descriptor,
            final String rdebugExecutable,
            final String interpreter,
            final int timeout) throws IOException, RubyDebuggerException {
        descriptor.setType(RUBY_DEBUG);
        List<String> args = new ArrayList<String>();
        if (interpreter != null) {
            args.add(interpreter);
        }
        if (descriptor.isJRuby()) {
            adjustForJRuby(args);
        }
        args.addAll(descriptor.getAddtionalOptions());
        if (interpreter != null) {
            appendIOSynchronizer(args, descriptor);
        }
        args.add(rdebugExecutable);
        String version = descriptor.getRubyDebugIDEVersion();
        if (version != null) { // invoke appropriate rdebug-ide version when set
            args.add('_' + version + '_');
        }
        args.add("-p");
        args.add(String.valueOf(descriptor.getPort()));
        if (descriptor.isVerbose()) {
            args.add("-d");
        }
        args.add("--");
        args.add(descriptor.getScriptPath());
        if (descriptor.getScriptArguments() != null) {
            args.addAll(Arrays.asList(descriptor.getScriptArguments()));
        }
        return startDebugger(descriptor, args, timeout);
    }

    /**
     * Starts <tt>ruby-debug-ide</tt> session for the given script. Debugger
     * waits on the first script's line.
     * <p>
     * This method takes a preconstructed command line and inserts the computed
     * rdebug values into the appropriate <tt>${ }</tt> bracketed variables
     * contained therein. Supported variables are:
     * <p>
     * <tt>${rdebug.path}</tt> = fully qualified to rdebug script<br/>
     * <tt>${rdebug.port}</tt> = port rdebug should use<br/>
     * <tt>${rdebug.version}</tt> = underscore bracketed version, or blank if unknown.<br/>
     * <tt>${rdebug.iosynch}</tt> = fully qualified path to io-synch script for rdebug.<br/>
     *
     * @param descriptor {@link Descriptor} to be used
     * @param args
     * @param rdebugExecutable fully qualified path to rdebug-ide
     * @param timeout time to wait before assuming failure
     * @return {@link RubyDebugTarget} instance
     * @throws java.io.IOException
     * @throws org.rubyforge.debugcommons.RubyDebuggerException
     */
    public static RubyDebuggerProxy startRubyDebug(
            final Descriptor descriptor,
            final List<String> args,
            final String rdebugExecutable,
            final int timeout) throws IOException, RubyDebuggerException {
        descriptor.setType(RUBY_DEBUG);
        
        Map<String, String> varMap = new HashMap<String, String>();
        varMap.put("rdebug.path", rdebugExecutable);
        varMap.put("rdebug.port", Integer.toString(descriptor.getPort()));
        String version = descriptor.getRubyDebugIDEVersion();
        varMap.put("rdebug.version", version != null ? ('_' + version + '_') : "");
        // would be nice to use a continuation here -- if the replacement was
        // never needed, we wouldn't even create the file.
        varMap.put("rdebug.iosynch", createIOSynchronizer());

        int size = args.size();
        for(int i = 0; i < size; i++) {
            args.set(i, substitute(args.get(i), varMap));
        }

        return startDebugger(descriptor, args, timeout);
    }
    
    private static void adjustForJRuby(List<String> args) {
        args.add("-J-Djruby.reflection=true");
        args.add("-J-Djruby.compile.mode=OFF");
    }

    private static RubyDebuggerProxy startDebugger(final Descriptor desc, final List<String> args, final int timeout)
            throws IOException, RubyDebuggerException {
        LOGGER.fine("Running [basedir: " + desc.getBaseDirectory() + "]: \"" + getProcessAsString(args) + "\"");
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(desc.getBaseDirectory());
        if (desc.getEnvironment() != null) {
            pb.environment().putAll(desc.getEnvironment());
        }
        LOGGER.fine("Environment: " + pb.environment());
        RubyDebuggerProxy proxy = new RubyDebuggerProxy(desc.getType(), timeout);
        
        // set whether backend support condition on breakpoints
        String rdebugIDEVer = desc.getRubyDebugIDEVersion();
        boolean suitableVersion = rdebugIDEVer == null || Util.compareVersions(rdebugIDEVer, "0.1.10") > 0;
        boolean supportsCondition = desc.getType() == RUBY_DEBUG && suitableVersion;
        proxy.setConditionSupport(supportsCondition);
        
        RubyDebugTarget target = new RubyDebugTarget(proxy, pb.start(),
                desc.getPort(), desc.getScriptPath(), desc.getBaseDirectory());
        proxy.setDebugTarget(target);
        RubyDebuggerProxy.PROXIES.add(proxy);
        return proxy;
    }
    
    private static String createRemoteDebugPortFile(final int port) throws IOException {
        File debugParameterFile = File.createTempFile("classic-debug", ".rb");
        debugParameterFile.deleteOnExit();
        FileWriter fWriter = new FileWriter(debugParameterFile);
        new PrintWriter(fWriter).println("$RemoteDebugPort=" + port);
        fWriter.close();
        return debugParameterFile.getCanonicalPath();
    }
    
    private static String createIOSynchronizer() throws IOException {
        File ioSynchronizer = File.createTempFile("io-synchronizer", ".rb");
        ioSynchronizer.deleteOnExit();
        FileWriter fWriter = new FileWriter(ioSynchronizer);
        PrintWriter pwWriter = new PrintWriter(fWriter);
        pwWriter.println("$stdout.sync=true");
        pwWriter.println("$stderr.sync=true");
        fWriter.close();
        return ioSynchronizer.getCanonicalPath();
    }

    private static void appendIOSynchronizer(final List<? super String> args, final Descriptor descriptor) throws IOException {
        if (descriptor.isSynchronizedOutput()) {
            String path = createIOSynchronizer();
            args.add("-r");
            args.add(path);
        }
    }

    /** Describes a debugger session. */
    public static final class Descriptor {
        
        private int coputedPort = -1;
        
        private DebuggerType type;
        private boolean verbose;
        private boolean useDefaultPort;
        private String scriptPath;
        private File baseDir;
        private String[] scriptArguments;
        private Map<String, String> environment;
        private boolean synchronizedOutput;
        private Collection<? extends String> additionalOptions;
        private boolean jruby;
        private String rubyDebugIDEVersion;
        
        public DebuggerType getType() {
            return type;
        }

        public void setType(DebuggerType type) {
            this.type = type;
        }
        
        public boolean isVerbose() {
            return verbose || Boolean.getBoolean("org.rubyforge.debugcommons.verbose");
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
        
        /** @see #getBaseDirectory */
        public void setBaseDirectory(File baseDir) {
            this.baseDir = baseDir;
        }
        
        /**
         * Returns directory to be used as a base directory of the process. If
         * it was not {@link #setBaseDirectory set} or is <code>null</code>,
         * script's parent directory is used.
         */
        public File getBaseDirectory() {
            return baseDir != null ? baseDir : new File(getScriptPath()).getParentFile();
        }
        
        public String[] getScriptArguments() {
            return scriptArguments;
        }
        
        /**
         * @param scriptArguments scriptArguments arguments for the debugged script.
         */
        public void setScriptArguments(String[] scriptArguments) {
            this.scriptArguments = scriptArguments;
        }
        
        public Map<String, String> getEnvironment() {
            return environment;
        }

        public void setEnvironment(final Map<String, String> environment) {
            this.environment = environment;
        }
        
        public boolean isSynchronizedOutput() {
            return synchronizedOutput;
        }
        
        public void setSynchronizedOutput(boolean synchronizedOutput) {
            this.synchronizedOutput = synchronizedOutput;
        }
        
        public Collection<? extends String> getAddtionalOptions() {
            return additionalOptions == null ? Collections.<String>emptySet() : additionalOptions;
        }
        
        public void setAdditionalOptions(Collection<? extends String> additionalOptions) {
            this.additionalOptions = additionalOptions;
        }
        
        int getPort() {
            if (coputedPort == -1) {
                coputedPort = defaultPortUsed() ? DEFAULT_PORT : Util.findFreePort();
            }
            return coputedPort;
        }

        public void setJRuby(boolean jruby) {
            this.jruby = jruby;
        }

        boolean isJRuby() {
            return jruby;
        }

        /** Used for RUBY_DEBUG type. */
        public void setRubyDebugIDEVersion(String rubyDebugIDEVersion) {
            this.rubyDebugIDEVersion = rubyDebugIDEVersion;
        }

        /** Used for RUBY_DEBUG type. */
        private String getRubyDebugIDEVersion() {
            return rubyDebugIDEVersion;
        }
    }

    /** Just helper method for logging. */
    private static String getProcessAsString(List<? extends String> process) {
        StringBuilder sb = new StringBuilder();
        for (String arg : process) {
            sb.append(arg).append(' ');
        }
        return sb.toString().trim();
    }
    
    private static Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");

    static String substitute(String value, Map<String, String> varMap) {
        try {
            Matcher matcher = pattern.matcher(value);
            boolean result = matcher.find();
            if (result) {
                StringBuffer sb = new StringBuffer();
                do {
                    String key = matcher.group(1);
                    String replacement = varMap.get(key);
                    if (replacement == null) {
                        replacement = System.getProperty(key);
                    }
                    if (replacement != null) {
                        replacement = replacement.replace("\\", "\\\\").replace("$", "\\$");
                    } else {
                        replacement = "\\$\\{" + key + "\\}";
                    }
                    matcher.appendReplacement(sb, replacement);
                    result = matcher.find();
                } while (result);
                matcher.appendTail(sb);
                value = sb.toString();
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
        return value;
    }
}
