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

package org.rubyforge.debugcommons.model;

public final class RubyFrameInfo {
    
    private String file;
    private int line;
    private int index;
    
    public RubyFrameInfo(String file, int line, int index) {
        this.file = file;
        this.line = line;
        this.index = index;
    }
    
    public int getIndex() {
        return index;
    }
    
    public int getLine() {
        return line;
    }
    
    public String getFile() {
        return file;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RubyFrameInfo)) {
            return false;
        }
        final RubyFrameInfo other = (RubyFrameInfo) obj;
        return (file == null ? other.file == null : file.equals(other.file))
                && (line == other.line) && (index == other.index);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (file != null ? file.hashCode() : 0);
        hash = 59 * hash + line;
        hash = 59 * hash + index;
        return hash;
    }
}
