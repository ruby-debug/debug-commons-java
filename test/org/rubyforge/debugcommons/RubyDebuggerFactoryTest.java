package org.rubyforge.debugcommons;

import org.rubyforge.debugcommons.model.IRubyBreakpoint;

public class RubyDebuggerFactoryTest extends DebuggerTestBase {
    
    public RubyDebuggerFactoryTest(String testName) {
        super(testName);
    }
    
    public void testSpaceAndSemicolonsInPath() throws Exception {
        for (RubyDebuggerProxy.DebuggerType debuggerType : RubyDebuggerProxy.DebuggerType.values()) {
            setDebuggerType(RubyDebuggerProxy.CLASSIC_DEBUGGER);
            testFile = writeFile("path spaces semi:colon.rb",
                    "b=10",  // 1
                    "b=11"); // 2
            testFilePath = testFile.getAbsolutePath();
            final RubyDebuggerProxy proxy = startDebugger();
            final IRubyBreakpoint[] breakpoints = new IRubyBreakpoint[] {
                new TestBreakpoint("path spaces semi:colon.rb", 1),
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
            final RubyDebuggerProxy proxy = prepareProxyWithArguments(args,
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
    
}
