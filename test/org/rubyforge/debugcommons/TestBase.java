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
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.TestCase;

public class TestBase extends TestCase {

    private TestHandler testHandler;

    protected TestBase(final String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
        Logger logger = Logger.getLogger("org.rubyforge.debugcommons");
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
        testHandler = new TestHandler(getName());
        logger.addHandler(testHandler);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        Logger.getLogger("org.rubyforge.debugcommons").removeHandler(testHandler);
    }

    /**
     * Tries to create directory in the system <code>tmp</code> directory and
     * then returns it.
     */
    protected File getWorkDir() {
        String workDir = System.getProperty("java.io.tmpdir") +
                File.separatorChar + "ruby-debugging-tests" + File.separatorChar +
                getClass().getName() + File.separatorChar + getName();
        File workDirF = new File(workDir);
        workDirF.mkdirs();
        return workDirF;
    }
    
    protected void clearWorkDir() throws IOException {
        deleteFile(getWorkDir());
    }
    
    /**
     * Deletes given file or directory. In the case of directory also all its
     * subdirectories/files are deleted recursively.
     */
    private void deleteFile(final File file) throws IOException {
        if (file.isDirectory()) {
            // file is a directory - delete sub files first
            File files[] = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteFile(files[i]);
            }
            
        }
        // delete file or an empty directory
        boolean result = file.delete();
        if (!result) {
            // a problem has appeared
            throw new IOException("Cannot delete file, file = " + file.getPath());
        }
    }
    
    private static class TestHandler extends Handler {
        
        private final String name;
        
        TestHandler(final String name) {
            this.name = name;
        }
        
        public void publish(LogRecord rec) {
            PrintStream os = rec.getLevel().intValue() >= Level.WARNING.intValue() ? System.err : System.out;
            os.println("[" + System.currentTimeMillis() + "::" + name + "::" + rec.getLevel().getName() + "]: " + rec.getMessage());
            Throwable th = rec.getThrown();
            if (th != null) {
                th.printStackTrace(os);
            }
            os.flush();
        }
        
        public void flush() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void close() throws SecurityException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
}
