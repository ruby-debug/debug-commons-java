package org.rubyforge.debugcommons;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.rubyforge.debugcommons.RubyDebuggerFactory.Descriptor;
import org.rubyforge.debugcommons.model.IRubyLineBreakpoint;

public class RubyDebuggerFactoryTest extends DebuggerTestBase {
    
    public RubyDebuggerFactoryTest(String testName) {
        super(testName);
    }
    
    public void testSpaceAndSemicolonsInPath() throws Exception {
        for (RubyDebuggerProxy.DebuggerType debuggerType : RubyDebuggerProxy.DebuggerType.values()) {
            setDebuggerType(debuggerType);
            testFile = writeFile("path spaces semi:colon.rb",
                    "b=10",  // 1
                    "b=11"); // 2
            testFilePath = testFile.getAbsolutePath();
            final RubyDebuggerProxy proxy = startDebugger();
            final IRubyLineBreakpoint[] breakpoints = new IRubyLineBreakpoint[] {
                new TestBreakpoint(testFilePath, 1),
            };
            startDebugging(proxy, breakpoints, 1);
            resumeSuspendedThread(proxy);
        }
    }

    public void testScriptArguments() throws Exception {
        for (RubyDebuggerProxy.DebuggerType debuggerType : RubyDebuggerProxy.DebuggerType.values()) {
            setDebuggerType(debuggerType);
            String[] args = { "--used-languages", "Ruby and Java" };
            final RubyDebuggerProxy proxy = prepareProxy(args,
                    "exit 1 if ARGV.size != 2",
                    "puts 'OK'");
            final IRubyLineBreakpoint[] breakpoints = new IRubyLineBreakpoint[] {
                new TestBreakpoint("test.rb", 2),
            };
            startDebugging(proxy, breakpoints, 1);
            resumeSuspendedThread(proxy);
        }
    }

    public void testBaseDir() throws Exception {
        File baseDir = new File(getWorkDir(), "aaa");
        assertTrue("base directory created", baseDir.mkdir());
        for (RubyDebuggerProxy.DebuggerType debuggerType : RubyDebuggerProxy.DebuggerType.values()) {
            setDebuggerType(debuggerType);
            final RubyDebuggerProxy proxy = prepareProxy(baseDir,
                    "exit 1 if Dir.pwd[-3, 3] != 'aaa'",
                    "puts 'OK'");
            final IRubyLineBreakpoint[] breakpoints = new IRubyLineBreakpoint[] {
                new TestBreakpoint("test.rb", 2),
            };
            startDebugging(proxy, breakpoints, 1);
            resumeSuspendedThread(proxy);
        }
    }

    public void testIncludingPath() throws Exception {
        File baseDir = new File(getWorkDir(), "aaa");
        assertTrue("base directory created", baseDir.mkdir());
        File includeDir = new File(getWorkDir(), "bbb");
        assertTrue("base directory created", includeDir.mkdir());
        testFile = writeFile("bbb" + File.separator + "test.rb", "puts 'OK'");
        for (RubyDebuggerProxy.DebuggerType debuggerType : RubyDebuggerProxy.DebuggerType.values()) {
            setDebuggerType(debuggerType);
            Descriptor descriptor = new Descriptor();
            descriptor.useDefaultPort(false);
            descriptor.setVerbose(true);
            descriptor.setAdditionalOptions(Collections.singleton("-I" + includeDir.getAbsolutePath()));
            descriptor.setBaseDirectory(baseDir);
            descriptor.setScriptPath(testFile.getAbsolutePath());
            final RubyDebuggerProxy proxy = startDebugger(descriptor);
            final IRubyLineBreakpoint[] breakpoints = new IRubyLineBreakpoint[] {
                new TestBreakpoint("test.rb", 1),
            };
            startDebugging(proxy, breakpoints, 1);
            resumeSuspendedThread(proxy);
        }
    }

    public void testEnvironment() throws Exception {
        for (RubyDebuggerProxy.DebuggerType debuggerType : RubyDebuggerProxy.DebuggerType.values()) {
            setDebuggerType(debuggerType);
            Descriptor descriptor = new Descriptor();
            descriptor.useDefaultPort(false);
            descriptor.setVerbose(true);
            descriptor.setEnvironment(Collections.singletonMap("MY_ENV_123_X", "test_123"));
            testFile = writeFile("test.rb", "exit 1 if ENV['MY_ENV_123_X'] != 'test_123'", "puts 'OK'");
            descriptor.setScriptPath(testFile.getAbsolutePath());
            final RubyDebuggerProxy proxy = startDebugger(descriptor);
            final IRubyLineBreakpoint[] breakpoints = new IRubyLineBreakpoint[] {
                new TestBreakpoint("test.rb", 2),
            };
            startDebugging(proxy, breakpoints, 1);
            resumeSuspendedThread(proxy);
        }
    }

    public void testSubstitute() throws Exception {
        Map<String, String> subMap = new HashMap<String, String>();
        subMap.put("simple", "simple");
        subMap.put("greedy", "greedy");
        subMap.put("number", "1000");
        subMap.put("unix.path", "/unix/path");
        subMap.put("windows.path", "c:\\windows\\path");
        subMap.put("dollar.sign", "$dollar$");
        subMap.put("spaces", "this phrase has spaces");
        subMap.put("japanese", "日本語");

        // map as cheap version of Tuple<String, String>
        Map<String, String> testData = new LinkedHashMap<String, String>();
        testData.put("nothing", "nothing");
        testData.put("${simple}", "simple");
        testData.put("Not so ${simple}", "Not so simple");
        testData.put("Not ${simple}r either", "Not simpler either");
        testData.put("Just ${number}", "Just 1000");
        testData.put("Is it ${simple} with more? ${number}", "Is it simple with more? 1000");
        testData.put("Forward slashes ${unix.path}", "Forward slashes /unix/path");
        testData.put("Backslashes ${windows.path}", "Backslashes c:\\windows\\path");
        testData.put("Dollars can be ${dollar.sign} finicky", "Dollars can be $dollar$ finicky");
        testData.put("Dunno about '${spaces}'", "Dunno about 'this phrase has spaces'");
        testData.put("Lots of them: ${simple}, ${dollar}, in ${unix.path} plus \"${spaces}\"",
                "Lots of them: simple, $dollar$, in /unix/path plus \"this phrase has spaces\"");
        testData.put("System properties too, java.home=${java.home}",
                "System properties too, java.home=" + System.getProperty("java.home"));
        testData.put("No matching ${subs} here", "No matching ${subs} here");
        testData.put("Check { curlies } too", "Check { curlies } too");
        testData.put("Check ${greedy} match too :-}", "Check greedy match too :-}");
        testData.put("Japanese chars: ${japanese} here", "Japanese chars: 日本語 here");

        for (Map.Entry<String, String> entry : testData.entrySet()) {
            String result = RubyDebuggerFactory.substitute(entry.getKey(), subMap);
            assertEquals(entry.getValue(), entry.getValue(), result);
        }
    }

}
