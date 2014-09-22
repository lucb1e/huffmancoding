/*
 DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE 
 TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION 

 0. You just DO WHAT THE FUCK YOU WANT TO.
 */
package Huffman;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A layer that we can write bits to, which will then be compiled into bytes and
 * fed into an OutputStream. Don't forget to call flush() at the end!
 */
final class ByteAssembler {

    /**
     * The OutputStream to write our final data to
     */
    private final OutputStream output;

    /**
     * Temporary value as the byte is assembled
     */
    private int currentbyte;

    /**
     * How many bits of this byte we already wrote
     */
    private int currentbytesize;

    /**
     * Assembles whole bytes from individual bits.
     *
     * @param output Where the assembled bytes are written to.
     */
    public ByteAssembler(OutputStream output) {
        this.output = new BufferedOutputStream(output);
        currentbyte = 0;
        currentbytesize = 0;
    }

    /**
     * Write bits that should, in the end, form whole bytes
     *
     * @param bitarray The bits to assemble into bytes
     * @throws IOException If we fail to write to the OutputStream
     */
    public void writeBits(int[] bitarray) throws IOException {
        for (int bit : bitarray) {
            if (bit == 1) {
                currentbyte += Math.pow(2, 8 - currentbytesize - 1);
            }
            currentbytesize++;

            if (currentbytesize == 8) {
                output.write(currentbyte);
                currentbyte = 0;
                currentbytesize = 0;
            }
        }
    }

    /**
     * Flushes any residue to the outputstream, padding zeros if necessary. Does
     * not close the OutputStream.
     *
     * @throws IOException If we fail to write to the OutputStream
     * @return The number of bits written (excess zeros = 8-returnvalue)
     */
    public int flush() throws IOException {
        if (currentbytesize == 0) {
            output.flush();
            return 0;
        }

        output.write(currentbyte);
        int tmp = currentbytesize;
        currentbytesize = 0;
        output.flush();
        return tmp;
    }
}
