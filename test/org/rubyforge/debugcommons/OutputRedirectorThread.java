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
