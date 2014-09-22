/*
 DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE 
 TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION 

 0. You just DO WHAT THE FUCK YOU WANT TO.
 */
package Huffman;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Since FileInputStream.markSupported is false for no apparent reason...
 */
public class InputWorkaround {
    enum Mode {
        File, InputStream, FileInputStream
    }
    private Mode mode;
    private InputStream in1;
    private InputStream in2;
    private InputStream in;
    private FileInputStream fis;
    
    private boolean alreadyreset;
    
    public InputWorkaround(FileInputStream fis) {
        mode = Mode.FileInputStream;
        this.fis = fis;
        alreadyreset = false;
    }
    
    public InputWorkaround(InputStream in1, InputStream in2) {
        mode = Mode.File;
        this.in1 = in1;
        this.in2 = in2;
        alreadyreset = false;
    }
    
    public InputWorkaround(InputStream in) {
        mode = Mode.InputStream;
        this.in = in;
        alreadyreset = false;
    }
    
    public InputStream getInput() {
        if (mode.equals(Mode.FileInputStream))
            return fis;
        
        if (mode.equals(Mode.File)) {
            return in1;
        }
        return in;
    }
    
    public InputStream reset() throws IOException {
        if (alreadyreset) {
            throw new IllegalStateException("You already used reset.");
        }
        alreadyreset = true;
        if (mode.equals(Mode.File)) {
            in1.close();
            return in2;
        }
        if (mode.equals(Mode.FileInputStream)) {
            fis.getChannel().position(0);
            return fis;
        }
        in.reset(); // Let's hope this works!
        return in;
    }
}
