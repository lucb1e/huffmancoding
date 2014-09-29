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

    public CountedCharacter(CountedCharacter c1, CountedCharacter c2) {
        if (c1.getFrequency() < c2.getFrequency()) {
            left = c1;
            right = c2;
        } else {
            left = c2;
            right = c1;
        }
        frequency = c1.getFrequency() + c2.getFrequency();
        character = -1; // Indicate that we're a node (yeah maybe I should have used a boolean)
    }

    public int getCharacter() {
        return character;
    }

    public long getFrequency() {
        return frequency;
    }
    
    public CountedCharacter getLeft() {
        return left;
    }
    
    public CountedCharacter getRight() {
        return right;
    }
    
    public boolean hasCharacter() {
        return character > -1;
    }
}
