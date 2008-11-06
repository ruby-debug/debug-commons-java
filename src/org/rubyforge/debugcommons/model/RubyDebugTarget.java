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

package org.rubyforge.debugcommons.model;

import java.io.File;
import java.util.logging.Logger;
import org.rubyforge.debugcommons.RubyDebuggerException;
import org.rubyforge.debugcommons.RubyDebuggerProxy;
import org.rubyforge.debugcommons.Util;

public final class RubyDebugTarget extends RubyEntity {
    
    private static final Logger LOGGER = Logger.getLogger(RubyDebugTarget.class.getName());

    private final Process process;
    private final String host;
    private final int port;
    private final String debuggedFile;
    private final File baseDir;
    
    private RubyThread[] threads;
    
    public RubyDebugTarget(RubyDebuggerProxy proxy, String host, int port) {
        this(proxy, host, port, null, null, null);
    }

    public RubyDebugTarget(RubyDebuggerProxy proxy, String host, int port, Process process,
            String debuggedFile, File baseDir) {
        super(proxy);
        this.process = process;
        this.host = host;
        this.port = port;
        this.debuggedFile = debuggedFile;
        this.baseDir = baseDir;
        this.threads = new RubyThread[0];
    }
    
    public Process getProcess() {
        return process;
    }

    public String getHost() {
        return host;
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
    
    private void updateThreads() throws RubyDebuggerException {
        // preconditions:
        // 1) both threadInfos and updatedThreads are sorted by their id attribute
        // 2) once a thread has died its id is never reused for new threads again.
        //    Instead each new thread gets an id which is the currently highest id + 1.
        LOGGER.fine("udpating threads");
        RubyThreadInfo[] threadInfos = getProxy().readThreadInfo();
        RubyThread[] updatedThreads = new RubyThread[threadInfos.length];
        int threadIndex = 0;
        synchronized (this) {
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
    }
    
    public void suspensionOccurred(SuspensionPoint suspensionPoint) {
        try {
            updateThreads();
        } catch (RubyDebuggerException e) {
            if (!getProxy().isReady()) {
                throw new RuntimeException("Cannot update threads. Proxy is not ready.", e);
            } else {
                LOGGER.fine("Session has finished. Ignoring unsuccessful thread update.");
                return;
            }
        }
        RubyThread thread = getThreadById(suspensionPoint.getThreadId());
        if (thread == null) {
            LOGGER.warning("Thread with id " + suspensionPoint.getThreadId() + " was not found");
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
    public synchronized RubyThread getThreadById(int id) {
        for (RubyThread thread : threads) {
            if (thread.getId() == id) {
                return thread;
            }
        }
        return null;
    }

    private boolean isRemote() {
        return process == null;
    }

    public boolean isRunning() {
        return isRemote() || Util.isRunning(process);
    }

    @Override
    public String toString() {
        return "RubyDebugTarget@" + System.identityHashCode(this) + '[' +
                "baseDir: " + getBaseDir() +
                ", debuggedFile: " + getDebuggedFile() +
                ", port: " + getPort() + ']';
    }

}
