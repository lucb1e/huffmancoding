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
                    throw new IllegalArgumentException("Invalid header data");
                }
                if (debug) {
                    System.err.println("Zero-count character detected - that means we've reached the end of the header");
                }
                break; // End of header is denoted by a 0-count character
            }
            frequencies[character] = count;
        } while (true);
    }

    /**
     * Builds the Huffman tree (temporary copy, should be replaced with a
     * general function)
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
        int buffsize = 4096;
        int readblock = 1024;
        byte[] readbuffer = new byte[readblock];
        boolean[] bitbuffer = new boolean[buffsize * 8];
        int bitbufferwriteptr = 0;
        int bitbufferreadptr = 0;
        int bitsavailable = 0;
        int lastbyte = 0;
        int keepbits = 256; // How many bits to keep available
        CountedCharacter currentCC = null;
        String debugbitbuffer = "";
        do {
            int readbytes = input.read(readbuffer, 0, readblock);
            if (readbytes == -1) {
                bitsavailable -= lastbyte + 8; // The last byte tells us how many bits are padding
                keepbits = 0;
                if (debug) {
                    System.err.println("Found end of stream! Excess bits: " + lastbyte);
                    System.err.println("Bits remaining: " + bitsavailable);
                }
            } else {
                bitsavailable += readbytes * 8;
                lastbyte = readbuffer[readbytes - 1];
                for (int i = 0; i < readbytes; i++) {
                    for (int bitpos = 7; bitpos >= 0; bitpos--) {
                        bitbuffer[bitbufferwriteptr] = ((readbuffer[i] >> bitpos) & 1) == 1;
                        bitbufferwriteptr = (bitbufferwriteptr + 1) % (buffsize * 8);
                    }
                }
            }
            while (bitsavailable > keepbits) {
                if (currentCC == null) {
                    currentCC = countedCharacters.peek();
                }
                if (!bitbuffer[bitbufferreadptr]) {
                    if (debug) {
                        debugbitbuffer += "1";
                    }
                    currentCC = currentCC.getLeft();
                } else {
                    if (debug) {
                        debugbitbuffer += "0";
                    }
                    currentCC = currentCC.getRight();
                }
                if (currentCC.hasCharacter()) {
                    bof.write(currentCC.getCharacter());
                    if (debug) {
                        System.err.println("Found character " + currentCC.getCharacter() + " after reading " + debugbitbuffer);
                        debugbitbuffer = "";
                    }
                    currentCC = null;
                }
                bitsavailable--;
                bitbufferreadptr = (bitbufferreadptr + 1) % (buffsize * 8);
            }
            if (bitsavailable == 0) {
                break;
            }
        } while (true);
        bof.flush();
    }

}
