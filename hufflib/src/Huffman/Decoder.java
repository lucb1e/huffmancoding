/*
 DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE 
 TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION 

 0. You just DO WHAT THE FUCK YOU WANT TO.
 */
package Huffman;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Decodes a stream encoded by this encoder. Expects a header
 */
public class Decoder {

    /**
     * Contains the character frequencies
     */
    private final long[] frequencies;

    /**
     * Contains the tree with characters and frequency counts
     */
    private PriorityQueue<CountedCharacter> countedCharacters;

    /**
     * The inputstream to encode the data from
     */
    private final InputStream input;

    /**
     * Whether we should print debugging info to stderr along the way
     */
    private final boolean debug;

    private long filesize;

    /**
     * Decodes Huffman coded data
     *
     * @param input Where to read the data
     * @param output Where to write the decoded data to
     * @param debug whether to output debug information
     * @throws IOException When we have trouble reading from or writing to your
     * input- or outputstream
     */
    public Decoder(InputStream input, OutputStream output, boolean debug) throws IOException {
        this.debug = debug;
        this.input = input;
        filesize = 0;
        frequencies = new long[256];

        parseHeader();

        buildTree();

        performDecoding(new BufferedOutputStream(output));
    }

    private void parseHeader() throws IOException {
        DataInputStream dis = new DataInputStream(input);
        do {
            int character = dis.read();
            long count = dis.readLong();
            if (debug) {
                System.err.println("Read count " + count + " for char " + character);
            }
            if (count == 0) {
                if (character != 0) {
                    throw new IllegalArgumentException("Invalid header data (zero count for a non-zero byte index)");
                }
                if (debug) {
                    System.err.println("Zero-count character detected - that means we've reached the end of the header");
                }
                break; // End of header is denoted by a 0-count character
            }
            frequencies[character] = count;
            filesize += count;
        } while (true);
    }

    /**
     * Builds the Huffman tree from the frequency count
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
    }

    private void performDecoding(BufferedOutputStream bof) throws IOException {
        long outputtedCharacters = 0;
        int buffsize = 2048;
        byte[] readbuffer = new byte[buffsize];
        CountedCharacter root = countedCharacters.peek();
        CountedCharacter currentCC = root;
        do {
            int readbytes = input.read(readbuffer, 0, buffsize);
            if (readbytes == -1) {
                throw new IllegalArgumentException("End of byte stream before end of file");
            }

            for (int i = 0; i < readbytes; i++) {
                for (int bitpos = 7; bitpos >= 0; bitpos--) {
                    if (((readbuffer[i] >> bitpos) & 1) != 1) {
                        currentCC = currentCC.getLeft();
                    } else {
                        currentCC = currentCC.getRight();
                    }
                    if (currentCC.hasCharacter()) {
                        bof.write(currentCC.getCharacter());
                        currentCC = root;
                        outputtedCharacters++;
                        if (outputtedCharacters >= filesize) {
                            break;
                        }
                    }
                }
            }
        } while (outputtedCharacters < filesize);
        bof.flush();
    }

}
