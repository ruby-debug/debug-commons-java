package org.rubyforge.debugcommons.model;

import java.io.File;
import org.rubyforge.debugcommons.RubyDebuggerException;
import org.rubyforge.debugcommons.RubyDebuggerProxy;
import org.rubyforge.debugcommons.Util;
import org.rubyforge.debugcommons.model.RubyEntity;
import org.rubyforge.debugcommons.model.RubyThread;

public final class RubyDebugTarget extends RubyEntity {
    
    private final Process process;
    private final int port;
    private final String debuggedFile;
    private final File baseDir;
    
    private boolean terminated;
    private RubyThread[] threads;
    
    public RubyDebugTarget(RubyDebuggerProxy proxy, Process process, int port,
            String debuggedFile, File baseDir) {
        super(proxy);
        this.process = process;
        this.port = port;
        this.debuggedFile = new File(debuggedFile).getName();
        this.baseDir = baseDir;
        this.threads = new RubyThread[0];
    }
    
    public Process getProcess() {
        return process;
    }
    
    public int getPort() {
        return port;
    }
    
    public String getDebuggedFile() {
        return debuggedFile;
    }
    
    public File getBaseDir() {
        return baseDir;
    }
    
    public void updateThreads() throws RubyDebuggerException {
        // preconditions:
        // 1) both threadInfos and updatedThreads are sorted by their id attribute
        // 2) once a thread has died its id is never reused for new threads again.
        //    Instead each new thread gets an id which is the currently highest id + 1.
        
        Util.fine("udpating threads");
        RubyThreadInfo[] threadInfos = getProxy().readThreadInfo();
        RubyThread[] updatedThreads = new RubyThread[threadInfos.length];
        int threadIndex = 0;
        for (int i = 0; i < threadInfos.length; i++) {
            while (threadIndex < threads.length && threadInfos[i].getId() != threads[threadIndex].getId()) {
                // step over dead threads, which do not occur in threadInfos anymore
                threadIndex += 1;
            }
            if (threadIndex == threads.length) {
                updatedThreads[i] = new RubyThread(this, threadInfos[i].getId());
            } else {
                updatedThreads[i] = threads[threadIndex];
            }
        }
        threads = updatedThreads;
    }
    
    public void suspensionOccurred(SuspensionPoint suspensionPoint) {
        try {
            updateThreads();
        } catch (RubyDebuggerException e) {
            throw new RuntimeException("Cannot update threads", e);
        }
        RubyThread thread = getThreadById(suspensionPoint.getThreadId());
        if (thread == null) {
            Util.warning("Thread with id " + suspensionPoint.getThreadId() + " was not found");
            return;
        }
        thread.suspend(suspensionPoint);
    }
    
    /**
     * Look up Ruby thread corresponding to the given id.
     *
     * @return {@link RubyThread} instance or <code>null</code> if no thread if
     *          found.
     */
    public RubyThread getThreadById(int id) {
        for (RubyThread thread : threads) {
            if (thread.getId() == id) {
                return thread;
            }
        }
        return null;
    }
    
    public boolean isRunning() {
        try {
            process.exitValue();
            return false;
        } catch (IllegalThreadStateException ex) {
            // not yet finished, normal behaviour why does java.lang.Process
            // does not have a function like isRunning()?
            return true;
        }
    }
    
}
