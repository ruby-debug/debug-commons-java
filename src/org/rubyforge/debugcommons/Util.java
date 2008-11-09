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

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.rubyforge.debugcommons.model.Message;
import org.rubyforge.debugcommons.model.RubyDebugTarget;
import org.rubyforge.debugcommons.reader.VariablesReader;
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

    /** Just helper method for logging. */
    static String getProcessAsString(List<? extends String> process) {
        StringBuilder sb = new StringBuilder();
        for (String arg : process) {
            sb.append(arg).append(' ');
        }
        return sb.toString().trim();
    }

    public static String dumpAndDestroyProcess(final RubyDebugTarget target) {
        final StringBuilder info = new StringBuilder();
        if (target.isRemote()) {
            info.append("Remote process");
        } else {
            boolean running = target.isRunning();
            if (running) {
                info.append("Dumping and destroying process, when the debuggee process is running." +
                        " You might try to increase the timeout. Killing...\n\n");
            }
            Process process = target.getProcess();
            info.append(dumpStream(process.getInputStream(), Level.INFO, "Standard Output: ", running));
            info.append(dumpStream(process.getErrorStream(), Level.SEVERE, "Error Output: ", running));
            if (running) {
                process.destroy();
            }
        }
        return info.toString();
    }

    private static String dumpStream(final InputStream stream, final Level level, final String msgPrefix, final boolean asynch) {
        final StringBuilder output = new StringBuilder();
        if (asynch) {
            Thread collector = new Thread(new Runnable() {

                public void run() {
                    collect(stream, output);
                }
            });
            collector.start();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
            collector.interrupt();
        } else {
            collect(stream, output);
        }
        if (output.length() > 0) {
            LOGGER.log(level, msgPrefix);
            String outputS = output.toString();
            LOGGER.log(level, outputS);
            return msgPrefix + '\n' + outputS;
        } else {
            return "";
        }
    }

    private static void collect(final InputStream stream, final StringBuilder output) {
        try {
            int c;
            while ((c = stream.read()) != -1) {
                output.append((char) c);
            }
        } catch (IOException e) {
            LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
        }
    }

    public static void logEvent(final XmlPullParser xpp) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            try {
                int eventType = xpp.getEventType();
                if (eventType == XmlPullParser.END_DOCUMENT) {
                    LOGGER.finest("Received: END_DOCUMENT event");
                    return;
                }
                if ("message".equals(xpp.getName())) { // message is handled specially
                    return;
                }
                if (xpp.getName() == null) {
                    LOGGER.warning("Unexpected type: (" + Util.getType(xpp) + ") encountered in logEvent");
                    return;
                }
                StringBuilder toXml = new StringBuilder();
                if (eventType == XmlPullParser.TEXT) {
                    return;
                }
                toXml.append("<");
                if (eventType == XmlPullParser.END_TAG) {
                    toXml.append('/');
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
            } catch (XmlPullParserException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }
    }

    public static void logMessage(final Message message) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            StringBuilder messageXml = new StringBuilder("<message");
            if (message.isDebug()) {
                messageXml.append(" debug='true'");
            }
            messageXml.append('>');
            messageXml.append(message.getText());
            messageXml.append("</message>");
            LOGGER.finest("Received message: " + messageXml.toString());
        }
    }

    public static String getType(final XmlPullParser xpp) {
        try {
            if (xpp.getEventType() == XmlPullParser.END_TAG) {
                return "END_TAG";
            } else if (xpp.getEventType() == XmlPullParser.START_TAG) {
                return "START_TAG";
            } else if (xpp.getEventType() == XmlPullParser.TEXT) {
                return "TEXT";
            } else if (xpp.getEventType() == XmlPullParser.START_DOCUMENT) {
                return "START_DOCUMENT";
            } else if (xpp.getEventType() == XmlPullParser.END_DOCUMENT) {
                return "END_DOCUMENT";
            } else {
                return "UNKNOWN: " + xpp.getEventType();
            }
        } catch (XmlPullParserException e) {
            Logger.getLogger(VariablesReader.class.getName()).log(Level.SEVERE, null, e);
            return "<Unable to find a type>";
        }
    }
}
