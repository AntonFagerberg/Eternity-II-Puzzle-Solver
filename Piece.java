import java.util.HashMap;

public class Piece {

    String value;
    String[] oneSide = new String[4];
    String[] twoSide = new String[4];
    String[] threeSide = new String[4];
    String[] fourSide = new String[4];
    int rotation;

    /**
     * Create a new piece.
     *
     * @param value Pattern of piece.
     */
    public Piece(String value) {
	this.value = value;
	rotation = 0;

	oneSide[0] = value.substring(0, 1);
	oneSide[1] = value.substring(1, 2);
	oneSide[2] = value.substring(2, 3);
	oneSide[3] = value.substring(3, 4);

	String valueTwice = value + value;
	twoSide[0] = valueTwice.substring(0, 2);
	twoSide[1] = valueTwice.substring(1, 3);
	twoSide[2] = valueTwice.substring(2, 4);
	twoSide[3] = valueTwice.substring(3, 5);

	threeSide[0] = valueTwice.substring(0, 3);
	threeSide[1] = valueTwice.substring(1, 4);
	threeSide[2] = valueTwice.substring(2, 5);
	threeSide[3] = valueTwice.substring(3, 6);

	fourSide[0] = valueTwice.substring(0, 4);
	fourSide[1] = valueTwice.substring(1, 5);
	fourSide[2] = valueTwice.substring(2, 6);
	fourSide[3] = valueTwice.substring(3, 7);
    }

    /**
     * Search for pattern.
     *
     * @param value Pattern to search for.
     * @return Possible valid rotations matching pattern.
     */
    public HashMap<Integer, String> find(String value) {
	String[] searchArray;

	switch (value.length()) {
	    default:
	    case 1:
		searchArray = oneSide;
		break;
	    case 2:
		searchArray = twoSide;
		break;
	    case 3:
		searchArray = threeSide;
		break;
	    case 4:
		searchArray = fourSide;
		break;
	}

	HashMap<Integer, String> resultMap = new HashMap<Integer, String>();

	for (int i = 0; i < 4; i++) {
	    if (searchArray[i].equals(value)) {
		resultMap.put(i, searchArray[i]);
	    }
	}

	return resultMap;
    }

    /**
     * Get pattern of piece in given rotation.
     *
     * @param direction Rotation of piece.
     * @return Pattern string (one character).
     */
    public String get(int direction) {
	if (direction - rotation < 0) {
	    return oneSide[4 + direction - rotation];
	} else {
	    return oneSide[direction - rotation];
	}
    }

    /**
     * Set rotation of piece when placing it on the board.
     *
     * @param rotation Rotation in steps clockwise.
     */
    public void setRotation(int rotation) {
	this.rotation = rotation % 4;
    }
}
