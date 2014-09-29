/*
 DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE 
 TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION 

 0. You just DO WHAT THE FUCK YOU WANT TO.
 */
package huffmancodingdemo;

import Huffman.InputWorkaround;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Demo of hufflib (Huffman coding library)
 */
public class HuffmanCodingDemo {

    /**
     * @param args the command line arguments
     *
     * @throws FileNotFoundException When the file is, you know, not found.
     * @throws IOException When we can't write to the file you specified.
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        if (args.length != 3 && args.length != 4) {
            usage();
            return;
        }
        boolean debug = false;
        if (args.length == 4) {
            debug = true;
        }
        FileInputStream in = new FileInputStream(args[1]);
        OutputStream out = new FileOutputStream(args[2]);
        if (args[0].equalsIgnoreCase("encode")) {
            InputWorkaround inw = new InputWorkaround(in);
            new Huffman.Encoder(inw, out, debug);
        } else {
            new Huffman.Decoder(in, out, debug);
        }
    }

    private static void usage() {
        System.out.println("Huffman Coding Demo - Usage");
        System.out.println("  ./HuffmanCodingDemo.jar decode inputfile outputfile");
        System.out.println("  ./HuffmanCodingDemo.jar encode inputfile outputfile -v");
        System.out.println("Argument order is important (-v MUST be last).");
        System.out.println("");
        System.out.println("Hint: inputfile can be /dev/stdin on Linux (and possibly CON for 'console' on Windows, but I'm not sure).");
    }

}
