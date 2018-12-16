import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * Reads and contains in memory the map and specifications of the game.
 *
 * @author Jiri Swen
 * @version 3.0
 * @release 15/12/2017
 * @see {@link GameLogic.java}
 */
public class Map {

	// Representation of the map
	private char[][] map;
	
	// Map name
	private String mapName;
	
	// Gold required for the human player to win
	private int goldRequired;
	
	/**
	 * Default constructor, creates the default map "Very small Labyrinth of doom".
	 */
	public Map() {
		mapName = "Very small Labyrinth of Doom";
		goldRequired = 2;
		map = new char[][] {
		{'#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#'},
		{'#','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','#'},
		{'#','.','.','.','.','.','.','G','.','.','.','.','.','.','.','.','.','E','.','#'},
		{'#','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','#'},
		{'#','.','.','E','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','#'},
		{'#','.','.','.','.','.','.','.','.','.','.','.','G','.','.','.','.','.','.','#'},
		{'#','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','#'},
		{'#','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','#'},
		{'#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#'}
		};
	}

	/**
	 * Constructor that accepts a map to read in from.
	 *
	 * @param The filename of the map file.
	 * @throws Exceptions thrown by {@link readMap}
	 */
	public Map(String fileName) throws Exception {
		readMap(fileName);
	}

/**
   * Reads the map from a file using a BufferedReader and sets the appropriate parameters.
   *
   * @param The filename of the map file.
   * @throws IOException : The usual IOException from a BufferedReader.
   * @throws Exception : Thrown if map is not rectangular.
   */
  protected void readMap(String fileName) throws IOException, Exception {
		BufferedReader r = new BufferedReader(new FileReader(fileName));
		// First reads in the map name and amount of required gold
		mapName = r.readLine().replace("name ", "");
		goldRequired = Integer.parseInt(r.readLine().replace("win ", ""));
		int lineNumber = countLines(fileName) - 2;
		map = new char[lineNumber][];

		for (int i = 0; i < lineNumber; ++i) {
			map[i] = r.readLine().toCharArray();
			// Checks if all lines have the same length
			if (map[i].length != map[0].length) {
				throw new Exception("\nYour chosen map appears to not be rectangular.");
			}
		}
		r.close();
	}

	/**
   * Super quick line counter by martinus:
   * https://stackoverflow.com/questions/453018/number-of-lines-in-a-file-in-java
   *
   * @param The filename of the map file to count the lines of.
   * @return The number of lines.
   * @throws The usual IOException from a BufferedReader.
   */
	protected int countLines(String fileName) throws IOException {
    InputStream is = new BufferedInputStream(new FileInputStream(fileName));
    try {
      byte[] c = new byte[1024];
      int count = 1; // I changed this from 0 to 1 because it was counting one too few lines for some reason
      int readChars = 0;
      boolean empty = true;
      while ((readChars = is.read(c)) != -1) {
        empty = false;
        for (int i = 0; i < readChars; ++i) {
          if (c[i] == '\n') {
            ++count;
          }
        }
      }
      return (count == 0 && !empty) ? 1 : count;
    } 
    finally {
      is.close();
    }
	}

  /** 
   * @return Gold required to exit the current map.
   */
  protected int getGoldRequired() {
    return goldRequired;
  }

  /**
   * @return The map as stored in memory.
   */
  protected char[][] getMap() {
    return map;
  }

  /**
   * @return The name of the current map.
   */
  protected String getMapName() {
    return mapName;
  }

  /**
   * @param The coordinates to get the character at as stored in the map.
   * @return The item stored at specified coordinates.
   */
	protected char getItemAtCoordinate(int[] coordinates) {
		return map[coordinates[0]][coordinates[1]];
	}

	/**
	 * Removes item stored at specified coordinates.
	 * 
	 * @param Coordinates to replace with an 'empty' slot.
	 */
	protected void removeItemAtCoordinate(int[] coordinates) {
		map[coordinates[0]][coordinates[1]] = '.';
	}

	/**
	 * @return The height and width of the map stored together in an array.
	 */
	protected int[] getMapSize() {
		return new int[] {map.length, map[0].length};
	}
}