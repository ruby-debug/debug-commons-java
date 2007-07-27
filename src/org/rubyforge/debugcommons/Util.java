package org.rubyforge.debugcommons;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Util {
    
    public static final Logger LOGGER = Logger.getLogger(Util.class.getName());
    
    public static void finest(String message) {
        LOGGER.finest(message);
    }
    
    public static void finer(String message) {
        LOGGER.finer(message);
    }
    
    public static void fine(String message) {
        LOGGER.fine(message);
    }
    
    public static void info(String message) {
        LOGGER.info(message);
    }
    
    public static void warning(String message) {
        LOGGER.warning(message);
    }
    
    public static void severe(String failure) {
        LOGGER.log(Level.SEVERE, failure);
    }
    
    public static void severe(Throwable t) {
        LOGGER.log(Level.SEVERE, t.getMessage(), t);
    }
    
    public static void severe(String message, Throwable t) {
        LOGGER.log(Level.SEVERE, message, t);
    }
    
    /**
     * Returns a free port number on localhost, or -1 if unable to find a free
     * port.
     *
     * @return a free port number on localhost, or -1 if unable to find a free
     *         port
     */
    public static int findFreePort() {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(0);
            return socket.getLocalPort();
        } catch (IOException e) {
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    Util.severe("Cannot close socket.", e);
                }
            }
        }
        return -1;
    }

    public static boolean isRunning(final Process process) {
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
