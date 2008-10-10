package org.rubyforge.debugcommons;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Util {
    
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)(-\\S+)?"); // NOI18N

    private static final Logger LOGGER = Logger.getLogger(Util.class.getName());
    
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
                    LOGGER.log(Level.SEVERE, "Cannot close socket.", e);
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

    /**
     * Return &gt; 0 if <code>version1</code> is greater than
     * <code>version2</code>, 0 if equal and -1 otherwise.
     * <p>
     * Based on NetBeans' Util class from ruby.platform module.
     */
    public static int compareVersions(String version1, String version2) {
        if (version1.equals(version2)) {
            return 0;
        }

        Matcher matcher1 = VERSION_PATTERN.matcher(version1);

        if (matcher1.matches()) {
            int major1 = Integer.parseInt(matcher1.group(1));
            int minor1 = Integer.parseInt(matcher1.group(2));
            int micro1 = Integer.parseInt(matcher1.group(3));

            Matcher matcher2 = VERSION_PATTERN.matcher(version2);

            if (matcher2.matches()) {
                int major2 = Integer.parseInt(matcher2.group(1));
                int minor2 = Integer.parseInt(matcher2.group(2));
                int micro2 = Integer.parseInt(matcher2.group(3));

                if (major1 != major2) {
                    return major1 - major2;
                }

                if (minor1 != minor2) {
                    return minor1 - minor2;
                }

                if (micro1 != micro2) {
                    return micro1 - micro2;
                }
            } else {
                //assert false : "no version match on " + version2;
            }
        } else {
            // TODO assert false : "no version match on " + version1;
        }

        // Just do silly alphabetical comparison
        return version1.compareTo(version2);
    }
}
