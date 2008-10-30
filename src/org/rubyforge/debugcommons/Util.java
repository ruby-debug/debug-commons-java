package org.rubyforge.debugcommons;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

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

    public static void logEvent(final XmlPullParser xpp) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            if ("message".equals(xpp.getName())) { // message is handled specially
                return;
            }
            StringBuilder toXml = new StringBuilder();
            toXml.append("<");
            try {
                if (xpp.getEventType() == XmlPullParser.END_TAG) {
                    toXml.append('/');
                }
            } catch (XmlPullParserException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
            toXml.append(xpp.getName());
            for (int i = 0; i < xpp.getAttributeCount(); i++) {
                toXml.append(' ').
                        append(xpp.getAttributeName(i)).
                        append("='").
                        append(xpp.getAttributeValue(i)).
                        append("'");
            }
            toXml.append('>');
            LOGGER.finest("Received: " + toXml.toString());
        }
    }

    public static void logMessage(final String message, final boolean debug) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            StringBuilder messageXml = new StringBuilder("<message");
            if (debug) {
                messageXml.append(" debug='true'");
            }
            messageXml.append('>');
            messageXml.append(message);
            messageXml.append("</message>");
            LOGGER.finest("Received: " + messageXml.toString());
        }
    }
}
