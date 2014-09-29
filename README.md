# Huffman coding library

(And a test project and test files/script and fancy benchmarks with charts.)

Note that this is not intended to be used in real world systems, though you are
free to. The design is fairly okay, though I'm sure improvements could be made.

License: CC-BY-SA 4.0 International. Attribution to: lucb1e

## Contents

In hufflib/ you can find my library for Huffman coding; in HuffmanCodingDemo/
you can find a demo project that uses the library. Both can be opened with
Netbeans. They use JDK8 by default, but I'm pretty sure it would work on Java
SE 6.

If you want to explore the algorithm, start with HuffmanCodingDemo and then
read through the encoding process.

## Benchmarks

Hardware: Intel Core i7 3rd gen and an SSD doing ~3500mbps r/w.

Encoding (two-pass system) first has to obtain character frequencies, which
runs at roughly 500mbps, then goes on to encode data at around 100mbps.

Decoding (one pass system) works at about 160 mbps.

Tests were performed with a 150MB chunk of a virtual machine hard disk. For
both encoding and decoding, memory usage of the Java process is around 10MB
(+/- 0.5MB).

Compression ratio of 250 lines of Java code is about 65% (the encoded version
is 65% the size of the original). The data size was 8KB.

Compression ratio of the first paragraph of Wikipedia's Huffman coding article,
4 times repeated, is about 65%. The data length was 4KB.

## Todo

- The InputWorkaround system is just ugly (hence the name).
- Check whether concatenation of bits isn't quicker than the current method in
  ByteAssembler.writeBits().
- Optimize the header futher (at least use a variable-length number instead of
  a long, and perhaps a byte for the header length?).

Hint: I probably won't actually ever do these things.

## Header format

I've thought about how to do this for a while. In the end I ended up with this:

For each character that occurs in the original file, write one byte for the
character and 8 bytes (long) for its frequency. Denote end with a zero-count
character (since this would never occur). Example:

    B00000001A00000003N00000002000000000
    ^----------------------------------- character (B)
	 ^^^^^^^^--------------------------- count (1)
	         ^-------------------------- character (A)
	          ^^^^^^^^------------------ count (3)
			          ^----------------- character (N)
					   ^^^^^^^^--------- count (2)
					           ^-------- character (NULL)
							    ^^^^^^^^ count (0, denoting header end)

This format supports my principle of supporting practically infinite filesizes
(2^63 bytes) but, on the downside, wastes a lot of space. A variable-length
number would be better (e.g. use 1 byte for numbers <=63, 2 bytes for numbers
<=8191, etc.). Another improvement is prepending a byte to indicate how long
the header will actually be (i.e. how many elements it contains). This is
probably still suboptimal, more bit packing could be done, but at least makes
it feel as if less space is wasted!

Then there is the other problem of how to know when the file ends. At first I
used a trailer byte, a byte that was appended to each file and contained the
count of padded zeros. If an encoded stream is 14 bits in length, it would
(excluding the header) be 3 bytes on disk: 14 bits, 2 bits padding (because you
can't write half bytes) and a final byte with the value '2' to indicate that we
padded two zero bits.

This worked, but afterwards I thought of a better solution: if you simply add
up all frequencies from the header, you end up with the original file length.
Therefore we know how many bytes we must output before we have reached the end
of the file, without even knowing how many bits the stream contains. After
having outputted that many bits, the rest of the data stream is simply ignored.
Hint: this means you can append arbitrary data (PKZIP polygots anyone?).

----------

Project keywords: JCF JCF41 Huffman Java Collections Framework

