package Huffman;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * The main encoder class; used to encode data in Huffman coding
 */
public class Encoder {

    /**
     * Contains the character frequencies
     */
    private final long[] frequencies;

    /**
     * Contains the tree with characters and frequency counts
     */
    private PriorityQueue<CountedCharacter> countedCharacters;

    /**
     * A lookup table for the data. Retrieve an element with
     * lookup[byte-to-encode]; this will give you a byte array of which each
     * byte represents one bit (since you cannot actually store bits in RAM and
     * since booleans are actually treated as integers by the jvm).
     */
    private final int[][] lookup;

    /**
     * The inputstream to encode the data from
     */
    private InputStream input;

    /**
     * Since Java can't seek to the beginning of a regular file (WTF?) using a
     * normal InputStream... :/
     */
    private InputWorkaround inputWorkaround;

    /**
     * Whether we should print debugging info to stderr along the way
     */
    private final boolean debug;

    /**
     * Performs the Huffman coding. This is a two-pass system: one for frequency
     * counting, one for actual encoding. The data format of the output is:
     * header length (1 byte) || header || data || number of excess zero bits (1
     * byte). You can prepend magic bytes (better known as magic mime) yourself
     * simply by writing the bytes to the outputstream before calling this
     * method.
     *
     * @param input byte stream to Huffman code. Note that reset() will be used,
     * so it is not advised to use the inputstream's mark() method.
     * @param output Where the encoded data is written to
     * @param debug Whether to write debug information to stderr
     * @throws IOException When we can't read/write from/to your
     * Input/OutputStream
     */
    public Encoder(InputWorkaround input, OutputStream output, boolean debug) throws IOException {
        this.input = new BufferedInputStream(input.getInput());
        this.inputWorkaround = input;
        this.debug = debug;
        if (debug) {
            System.err.println("Allocating a byte array and a long array, each of 256 items, to store character frequencies and a lookup table.");
        }
        frequencies = new long[256];
        lookup = new int[256][];

        countFrequencies(this.input);

        this.input = new BufferedInputStream(inputWorkaround.reset());

        buildTree();

        // We use a DataOutputStream for this because it can write longs as 8-byte values instead of their string representation
        DataOutputStream dos = new DataOutputStream(output);
        outputHeader(dos, countedCharacters.peek());

        // Write a byte with count 0 as a means of denoting end-of-header
        // Too bad this takes 8 bytes, but hey no variable-length numbers in Java..!
        dos.write(0);
        dos.writeLong(0);

        dos.flush();

        performEncoding(output);
    }

    /**
     * Outputs our custom Huffman header to the outputstream. Recursive
     * function.
     *
     * @param dos Where to write the header to
     * @param character The root character (or the one to go down from)
     * @throws IOException when we fail to write to the output
     */
    private void outputHeader(DataOutputStream dos, CountedCharacter character) throws IOException {
        if (character.hasCharacter()) {
            dos.write(character.getCharacter());
            dos.writeLong(character.getFrequency()); // Since Java has no obvious way of doing variable-size numbers and I don't feel like coding it myself right now...
        } else {
            outputHeader(dos, character.getLeft());
            outputHeader(dos, character.getRight());
        }
    }

    /**
     * Builds the Huffman tree, storing a final version in the lookup table for
     * O(1) lookups
     */
    private void buildTree() {
        countedCharacters = new PriorityQueue<>(new Comparator<CountedCharacter>() {
            @Override
            public int compare(CountedCharacter o1, CountedCharacter o2) {
                if (o1.getFrequency() > o2.getFrequency()) {
                    return 1;
                } else if (o1.getFrequency() < o2.getFrequency()) {
                    return -1;
                }
                return 0;
            }
        });

        for (int i = 0; i < 256; i++) {
            if (frequencies[i] > 0) {
                countedCharacters.add(new CountedCharacter(i, frequencies[i]));
            }
        }

        if (debug) {
            System.err.println("Building the tree...");
        }

        while (countedCharacters.size() > 1) {
            countedCharacters.add(new CountedCharacter(countedCharacters.poll(), countedCharacters.poll()));
        }

        if (debug) {
            System.err.println("Tree built; generating lookup table");
        }

        rundown(countedCharacters.peek(), "");

        if (debug) {
            for (int i = 0; i < 256; i++) {
                if (lookup[i] != null) {
                    System.err.print(i + " = ");
                    for (int j = 0; j < lookup[i].length; j++) {
                        System.err.print(lookup[i][j]);
                    }
                    System.err.println();
                }
            }
        }
    }

    /**
     * Run down the character tree, exhausting all possibilities, adding them to
     * the lookup table. Recursive function.
     *
     * @param character the character where to run down from (treated as root)
     */
    private void rundown(CountedCharacter character, String prefix) {
        if (character.hasCharacter()) {
            int c = character.getCharacter();
            lookup[c] = new int[prefix.length()];
            char[] chars = prefix.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                lookup[c][i] = chars[i] == '0' ? 0 : 1;
            }
        } else {
            rundown(character.getLeft(), prefix + "0");
            rundown(character.getRight(), prefix + "1");
        }
    }

    /**
     * Counts the character frequencies
     *
     * @param input The data to count the frequencies from
     */
    private void countFrequencies(InputStream input) throws IOException {
        if (debug) {
            System.err.println("Counting character frequencies...");
        }

        long length = 0;
        int b;
        do {
            b = input.read();
            if (b == -1) {
                break;
            }
            length++;
            frequencies[b]++;
        } while (true);

        if (length == 0) {
            throw new IllegalArgumentException("InputStream returned no data");
        }

        if (debug) {
            long m = length / 1024 / 1024;
            System.err.println("Finished counting frequencies; input length is " + length + " bytes (" + m + " MB)");
        }
    }

    /**
     * Performs the actual encoding (lookup table must be filled). TODO:
     * optimize by reading more than one byte at a time.
     *
     * @param output Where to write our output to
     * @throws IOException If we fail to write to the output
     */
    private void performEncoding(OutputStream output) throws IOException {
        if (debug) {
            System.err.println("Now starting to encode...");
        }

        ByteAssembler assembler = new ByteAssembler(output);
        int b;
        do {
            b = input.read();
            if (b == -1) {
                break;
            }
            assembler.writeBits(lookup[b]);
        } while (true);

        assembler.flush();

        if (debug) {
            System.err.println("Finished encoding (also flushed assembler)!");
        }
    }
}
