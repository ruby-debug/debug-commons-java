package org.rubyforge.debugcommons;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import junit.framework.TestCase;

public class TestBase extends TestCase {
    
    protected TestBase(final String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Util.LOGGER.setLevel(Level.ALL);
        Util.LOGGER.setUseParentHandlers(false);
        Util.LOGGER.addHandler(new TestHandler(getName()));
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
