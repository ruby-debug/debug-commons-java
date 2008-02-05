package org.rubyforge.debugcommons;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class OutputRedirectorThread extends Thread {
    
    private InputStream inputStream;
    private String lastLine = "No output.";
    
    public OutputRedirectorThread(InputStream aInputStream) {
        inputStream = aInputStream;
    }
    
    public @Override void run() {
        Util.info("OutputRedirectorThread started.");
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = br.readLine()) != null) {
                Util.info("RUBY: " + line);
                lastLine = line;
            }
        } catch (IOException e) {
            // XXX: seems that classic-debugger does not close correctly
            // connection. When it is fixed, uncomment below exception.
            Util.severe("XXX: IOException in OutputRedirectorThread: " + e.getMessage() /*, e*/);
        }
        Util.info("OutputRedirectorThread stopped.");
    }
    
    public String getLastLine() {
        return lastLine;
    }
    
}
