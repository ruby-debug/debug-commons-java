package org.rubyforge.debugcommons;

import java.io.File;
import java.util.Collections;
import org.rubyforge.debugcommons.RubyDebuggerFactory.Descriptor;
import org.rubyforge.debugcommons.model.IRubyBreakpoint;

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
            final IRubyBreakpoint[] breakpoints = new IRubyBreakpoint[] {
                new TestBreakpoint(testFilePath, 1),
            };
            startDebugging(proxy, breakpoints, 1);
            waitForEvents(proxy, 1, new Runnable() { // finish spawned thread
                public void run() {
                    proxy.getDebugTarged().getThreadById(1).resume();
                }
            });
        }
    }
    
    public void testScriptArguments() throws Exception {
        for (RubyDebuggerProxy.DebuggerType debuggerType : RubyDebuggerProxy.DebuggerType.values()) {
            setDebuggerType(debuggerType);
            String[] args = { "--used-languages", "Ruby and Java" };
            final RubyDebuggerProxy proxy = prepareProxy(args,
                    "exit 1 if ARGV.size != 2",
                    "puts 'OK'");
            final IRubyBreakpoint[] breakpoints = new IRubyBreakpoint[] {
                new TestBreakpoint("test.rb", 2),
            };
            startDebugging(proxy, breakpoints, 1);
            waitForEvents(proxy, 1, new Runnable() { // finish spawned thread
                public void run() {
                    proxy.getDebugTarged().getThreadById(1).resume();
                }
            });
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
            final IRubyBreakpoint[] breakpoints = new IRubyBreakpoint[] {
                new TestBreakpoint("test.rb", 2),
            };
            startDebugging(proxy, breakpoints, 1);
            waitForEvents(proxy, 1, new Runnable() { // finish spawned thread
                public void run() {
                    proxy.getDebugTarged().getThreadById(1).resume();
                }
            });
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
            final IRubyBreakpoint[] breakpoints = new IRubyBreakpoint[] {
                new TestBreakpoint("test.rb", 1),
            };
            startDebugging(proxy, breakpoints, 1);
            waitForEvents(proxy, 1, new Runnable() { // finish spawned thread
                public void run() {
                    proxy.getDebugTarged().getThreadById(1).resume();
                }
            });
        }
    }
    
}
