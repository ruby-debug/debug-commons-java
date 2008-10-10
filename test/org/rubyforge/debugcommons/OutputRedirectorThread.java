package org.rubyforge.debugcommons;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

public class OutputRedirectorThread extends Thread {
    
    private static final Logger LOGGER = Logger.getLogger(OutputRedirectorThread.class.getName());
    
    private InputStream inputStream;
    private String lastLine = "No output.";
    
    public OutputRedirectorThread(InputStream aInputStream) {
        inputStream = aInputStream;
    }
    
    public @Override void run() {
        LOGGER.info("OutputRedirectorThread started.");
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = br.readLine()) != null) {
                LOGGER.info("RUBY: " + line);
                lastLine = line;
            }
        } catch (IOException e) {
            // XXX: seems that classic-debugger does not close correctly
            // connection. When it is fixed, uncomment below exception.
            LOGGER.severe("XXX: IOException in OutputRedirectorThread: " + e.getMessage() /*, e*/);
        }
        LOGGER.info("OutputRedirectorThread stopped.");
    }
    
    public String getLastLine() {
        return lastLine;
    }
    
}
