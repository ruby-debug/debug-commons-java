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

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.rubyforge.debugcommons.RubyDebuggerFactory.Descriptor;
import org.rubyforge.debugcommons.model.IRubyLineBreakpoint;

public class RubyDebuggerFactoryTest extends DebuggerTestBase {
    
    public RubyDebuggerFactoryTest(String testName) {
        super(testName);
    }
    
    public void testSpaceAndSemicolonsInPath() throws Exception {
        testFile = writeFile("path spaces semi:colon.rb",
                "b=10", // 1
                "b=11"); // 2
        testFilePath = testFile.getAbsolutePath();
        final RubyDebuggerProxy proxy = startDebugger();
        final IRubyLineBreakpoint[] breakpoints = new IRubyLineBreakpoint[]{
            new TestBreakpoint(testFilePath, 1),
        };
        attach(proxy, breakpoints, 1);
        resumeSuspendedThread(proxy);
    }

    public void testScriptArguments() throws Exception {
        String[] args = {"--used-languages", "Ruby and Java"};
        final RubyDebuggerProxy proxy = prepareProxy(args,
                "exit 1 if ARGV.size != 2",
                "puts 'OK'");
        final IRubyLineBreakpoint[] breakpoints = new IRubyLineBreakpoint[]{
            new TestBreakpoint("test.rb", 2),
        };
        attach(proxy, breakpoints, 1);
        resumeSuspendedThread(proxy);
    }

    public void testBaseDir() throws Exception {
        File baseDir = new File(getWorkDir(), "aaa");
        assertTrue("base directory created", baseDir.mkdir());
        final RubyDebuggerProxy proxy = prepareProxy(baseDir,
                "exit 1 if Dir.pwd[-3, 3] != 'aaa'",
                "puts 'OK'");
        final IRubyLineBreakpoint[] breakpoints = new IRubyLineBreakpoint[]{
            new TestBreakpoint("test.rb", 2),
        };
        attach(proxy, breakpoints, 1);
        resumeSuspendedThread(proxy);
    }

    public void testIncludingPath() throws Exception {
        File baseDir = new File(getWorkDir(), "aaa");
        assertTrue("base directory created", baseDir.mkdir());
        File includeDir = new File(getWorkDir(), "bbb");
        assertTrue("base directory created", includeDir.mkdir());
        testFile = writeFile("bbb" + File.separator + "test.rb", "puts 'OK'");
        Descriptor descriptor = new Descriptor();
        descriptor.useDefaultPort(false);
        descriptor.setVerbose(true);
        descriptor.setAdditionalOptions(Collections.singleton("-I" + includeDir.getAbsolutePath()));
        descriptor.setBaseDirectory(baseDir);
        descriptor.setDebuggeePath(testFile.getAbsolutePath());
        final RubyDebuggerProxy proxy = startDebugger(descriptor);
        final IRubyLineBreakpoint[] breakpoints = new IRubyLineBreakpoint[]{
            new TestBreakpoint("test.rb", 1),
        };
        attach(proxy, breakpoints, 1);
        resumeSuspendedThread(proxy);
    }

    public void testEnvironment() throws Exception {
        Descriptor descriptor = new Descriptor();
        descriptor.useDefaultPort(false);
        descriptor.setVerbose(true);
        descriptor.setEnvironment(Collections.singletonMap("MY_ENV_123_X", "test_123"));
        testFile = writeFile("test.rb", "exit 1 if ENV['MY_ENV_123_X'] != 'test_123'", "puts 'OK'");
        descriptor.setDebuggeePath(testFile.getAbsolutePath());
        final RubyDebuggerProxy proxy = startDebugger(descriptor);
        final IRubyLineBreakpoint[] breakpoints = new IRubyLineBreakpoint[]{
            new TestBreakpoint("test.rb", 2),
        };
        attach(proxy, breakpoints, 1);
        resumeSuspendedThread(proxy);
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

        assertSubstitute("nothing", "nothing", subMap);
        assertSubstitute("${simple}", "simple", subMap);
        assertSubstitute("Not so ${simple}", "Not so simple", subMap);
        assertSubstitute("Not ${simple}r either", "Not simpler either", subMap);
        assertSubstitute("Just ${number}", "Just 1000", subMap);
        assertSubstitute("Is it ${simple} with more? ${number}", "Is it simple with more? 1000", subMap);
        assertSubstitute("Forward slashes ${unix.path}", "Forward slashes /unix/path", subMap);
        assertSubstitute("Backslashes ${windows.path}", "Backslashes c:\\windows\\path", subMap);
        assertSubstitute("Dollars can be ${dollar.sign} finicky", "Dollars can be $dollar$ finicky", subMap);
        assertSubstitute("Dunno about '${spaces}'", "Dunno about 'this phrase has spaces'", subMap);
//        assertSubstitute("Lots of them: ${simple}, ${dollar}, in ${unix.path} plus \"${spaces}\"",
//                "Lots of them: simple, $dollar$, in /unix/path plus \"this phrase has spaces\"", subMap);
        assertSubstitute("System properties too, java.home=${java.home}",
                "System properties too, java.home=" + System.getProperty("java.home"), subMap);
        assertSubstitute("No matching ${subs} here", "No matching ${subs} here", subMap);
        assertSubstitute("Check { curlies } too", "Check { curlies } too", subMap);
        assertSubstitute("Check ${greedy} match too :-}", "Check greedy match too :-}", subMap);
        assertSubstitute("Japanese chars: ${japanese} here", "Japanese chars: 日本語 here", subMap);
    }
    
    private void assertSubstitute(String toSubstitute, String expectedResult, Map<String, String> subMap) {
        String result = RubyDebuggerFactory.substitute(toSubstitute, subMap);
        assertEquals(result, expectedResult);
    }

}
