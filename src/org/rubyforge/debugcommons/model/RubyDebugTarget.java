package org.rubyforge.debugcommons.model;

import java.io.File;
import org.rubyforge.debugcommons.RubyDebugEvent;
import org.rubyforge.debugcommons.RubyDebuggerProxy;
import org.rubyforge.debugcommons.Util;
import org.rubyforge.debugcommons.model.RubyEntity;
import org.rubyforge.debugcommons.model.RubyThread;

public final class RubyDebugTarget extends RubyEntity {
    
    private final Process process;
    private final int port;
    private final String debuggedFile;
    private final String baseDir;
    
    private boolean terminated;
    private RubyThread[] threads;
    
    public RubyDebugTarget(RubyDebuggerProxy proxy, Process process, int port, String debuggedFile) {
        super(proxy);
        this.process = process;
        this.port = port;
        File f = new File(debuggedFile);
        this.debuggedFile = f.getName();
        this.baseDir = f.getParent();
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
    
    public String getBaseDir() {
        return baseDir;
    }
    
    public void updateThreads() {
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
        updateThreads();
        RubyThread thread = getThreadById(suspensionPoint.getThreadId());
        if (thread == null) {
            Util.warning("Thread with id " + suspensionPoint.getThreadId() + " was not found");
            return;
        }
        thread.suspend(suspensionPoint);
    }
    
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
    
    public void terminate() {
        if (terminated) {
            Util.warning("Trying to terminate same process more than once: " + this);
            return;
        }
        RubyDebugEvent ev = new RubyDebugEvent(RubyDebugEvent.Type.TERMINATE);
        getProxy().fireDebugEvent(ev);
        
        getProcess().destroy();
        threads = new RubyThread[0];
        terminated = true;
    }
    
}
