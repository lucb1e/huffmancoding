/*
 DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE 
 TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION 

 0. You just DO WHAT THE FUCK YOU WANT TO.
 */
package Huffman;

/**
 * Contains a character and its frequency count
 */
class CountedCharacter {

    private final long frequency;
    private final int character;
    private CountedCharacter left;
    private CountedCharacter right;

    public CountedCharacter(int character, long frequency) {
        this.frequency = frequency;
        this.character = character;
    }

    public int getCharacter() {
        return character;
    }

    public long getFrequency() {
        return frequency;
    }
}
