import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/**
 * Contains the main logic part of the game, as it processes.
 *
 * @author Jiri Swen
 * @version 4.0
 * @release 15/12/2017
 * @see {@link Map.java}
 * @see {@link HumanPlayer.java}
 * @see {@link Bot.java}
 */
public class GameLogic {
	
	// Objects for the map, human player and bot to be used in the game
	private Map map;
	private Player humanPlayer;
	private Player botPlayer;

	// Keeps track of whether the game is running
	private boolean gameRunning = false;

	// Keeps track of how much gold the player owns
	private int goldOwned;
	
	// These keep track of the coordinates of the bot and player to compare against things
	// COORDINATES ARE ALWAYS STORED LIKE THIS: [ROW][COLUMN] / [line][characterInLine] so can be considered [y][x]
	private int[] botCoords;
	private int[] playerCoords;
	// Used for temporary storage of new coordinates to test agains old ones / to subject to if something is possible
	private int[] newCoords; 
	
	/**
	 * Default constructor for the game
	 */
	public GameLogic() {
		humanPlayer = new HumanPlayer();
		botPlayer = new Bot();
		getCustomMap();
	}

	/**
	 * Has the player type in the map they want to play, then constructs {@link Map} based on this.
	 *
	 * @exception Just uses the default map in case e.g. the map name was not recognised.
	 */
	protected void getCustomMap() {
		System.out.println("\nPlease input the name of the map you want to play, or type nothing to play the default map.\n");
		String fileName = humanPlayer.getNextCommand();
		if (fileName.isEmpty()) {
			map = new Map();
			System.out.println("\nDefault map created: \"" + map.getMapName() + "\"\nGold required to leave dungeon: 2\n");
		}
		else {
			try {
				// Coded so that you can input the file name with or without '.txt'
				if (! fileName.contains(".txt")) {
					map = new Map(fileName + ".txt");
					System.out.println("\nMap \"" + fileName + "\" created:");
				}
				else {
					map = new Map(fileName);
					System.out.println("\nMap \"" + fileName.replace(".txt", "") + "\" created:");
				}
				System.out.print("\"" + map.getMapName() + "\"\nGold required to leave dungeon: " + map.getGoldRequired() + "\n");
			}
			catch (Exception e) {
				System.err.println("\nSomething went wrong in the initialisation of the map, so the default map has been used (gold required to win: 2).\nPlease check the validity of your chosen map file.");
				if (! e.getMessage().isEmpty()) {
					System.err.println("Error message: \"" + e.getMessage() + "\"\n");
				}
				map = new Map();
			}
		}

		System.out.println("See what commands you can use by typing \"COMMANDS\".\n");
	}

/*
______                      _                  _    _                                            
| ___ \                    (_)                | |  | |                                           
| |_/ /_   _  _ __   _ __   _  _ __    __ _   | |_ | |__    ___     __ _   __ _  _ __ ___    ___ 
|    /| | | || '_ \ | '_ \ | || '_ \  / _` |  | __|| '_ \  / _ \   / _` | / _` || '_ ` _ \  / _ \
| |\ \| |_| || | | || | | || || | | || (_| |  | |_ | | | ||  __/  | (_| || (_| || | | | | ||  __/
\_| \_|\__,_||_| |_||_| |_||_||_| |_| \__, |   \__||_| |_| \___|   \__, | \__,_||_| |_| |_| \___|
                                       __/ |                        __/ |                        
                                      |___/                        |___/                         
*/

	/**
	 * Sets some variables relevant to running the game,
	 * then keeps asking the player and bot what they want to do in turns until the game ends.
	 */
	protected void runGame() {
		gameRunning = true;
		goldOwned = 0;
		playerCoords = new int[2];
		botCoords = new int[2];

		playerCoords = spawnPlayerCoords(humanPlayer);
		botCoords = spawnPlayerCoords(botPlayer);

		String input;
		while (true) {
			input = humanPlayer.getNextAction();
			switch (input) {
				case "HELLO":
					humanPlayer.passResult(hello());
					break;
				case "LOOK":
					humanPlayer.passArray(lookArray(playerCoords));
					break;
				case "PICKUP":
					humanPlayer.passResult(pickup());
					break;
				case "QUIT":
					quitGame();
					break;
				case "PASS":
				case "COMMANDS":
					break;
			}
			// Always tries to move and the movePlayer() method assesses whether it's relevant
			movePlayer(input, humanPlayer);

			input = botPlayer.getNextAction();
			if (input == "LOOK") {
				botPlayer.passArray(lookArray(botCoords));
			}
			movePlayer(input, botPlayer);
			// Useful to turn this on if you want to see what happens behind the scenes every turn!!
			//printWholeMap();
		}
	}


	/**
	 * Tries to move a {@link Player} based on their command and processes it to be used by move().
	 * 
	 * @param input : The raw input from the player.
	 * @param movingPlayer : The player whose turn it currently is.
	 */
	protected void movePlayer(String input, Player movingPlayer) {
		if (input.contains("MOVE ")) {
			String[] direction = input.split("MOVE ");
			char directionInput = direction[1].charAt(0);
			movingPlayer.passResult(move(directionInput, movingPlayer));
		}
	}

	/**
	 * @return If the game is running.
	 */
	protected boolean gameRunning() {
		return gameRunning;
	}

	/**
	 * Generates random coordinates within the map to spawn a {@link Player} at.
	 * 
	 * @param Player to spawn.
	 * @return Coordinates that the player will be spawned at.
	 */
	protected int[] spawnPlayerCoords(Player player) {
		while (true) {
			// Y and X coordinates are calculated independently based on the height/width of the map
			newCoords = new int[] {(int) (Math.random() * map.getMapSize()[0]), (int) (Math.random() * map.getMapSize()[1])};
			// Spawning must not happen in a wall, so it keeps trying until this doesn't happen
			if (map.getItemAtCoordinate(newCoords) != '#') {
				if (! (player instanceof HumanPlayer)) {
					return newCoords;
				}
				// Human player can't spawn on Gold tile
				else if (map.getItemAtCoordinate(newCoords) != 'G') {
					return newCoords;
				}
			}
		}
	}

 /**
	 * @return Returns back gold player requires to exit the Dungeon.
	 */
	protected String hello() {
		return "Gold to win: " + (map.getGoldRequired() - goldOwned);
	}

	/**
   * Converts the map from a 2D char array to a single string.
   * NOT USED as for a {@link Bot} it is a lot more useful to receive the grid as a 2D char array.
   *
   * @return A String representation of the game map.
   */
  protected String look() {
  	return null;
  }


 /**
	 * Checks if movement is legal and updates player's location on the map.
	 * NOT USED because code needs to check for the type of player that is moving 
	 * in order to update the appropriate coordinates.
	 *
	 * @param The direction of the movement.
	 * @return Protocol if success or not.
	 */
	protected String move(char direction) {
		return null;
	}

 /**
	 * Checks if movement is legal and updates {@link Player} location on the map.
	 * Also checks whether the conditions to end the game have been met.
	 *
	 * @param direction : The direction of the movement.
	 * @param player : Which player instance is currently moving.
	 * @return Protocol if success or not.
	 * @exception ArrayIndexOutOfBoundsException : If player tries to move outside of the map.
	 * @exception RuntimeException : If somehow a player that is not in the game tries to move.
	 */
	protected String move(char direction, Player player) {
		int[] oldCoords;
		if (player instanceof HumanPlayer) {
			oldCoords = playerCoords;
		}
		else if (player instanceof Bot) {
			oldCoords = botCoords;
		}
		else {
			throw new RuntimeException();
		}
		switch (direction) {
			case 'N':
				newCoords = new int[] {oldCoords[0] - 1, oldCoords[1]};
				break;
			case 'S':
				newCoords = new int[] {oldCoords[0] + 1, oldCoords[1]};
				break;
			case 'E':
				newCoords = new int[] {oldCoords[0], oldCoords[1] + 1};
				break;
			case 'W':
				newCoords = new int[] {oldCoords[0], oldCoords[1] - 1};
				break;
		}
		try {
			if (map.getItemAtCoordinate(newCoords) == 'E' && goldOwned >= map.getGoldRequired() && player instanceof HumanPlayer) {
			endGameSuccess();
			return "MOVE_SUCCESS_ENDGAME";
			}
		}
		// If tile player tries to move to is outside of the map (e.g. if edge is not hashed)
		catch (ArrayIndexOutOfBoundsException e) {
			return "MOVE_FAIL";
		}
		if (map.getItemAtCoordinate(newCoords) == '#') {
			return "MOVE_FAIL";
		}
		else {
			if (player instanceof HumanPlayer) {
				playerCoords = newCoords;
			}
			else if (player instanceof Bot) {
				botCoords = newCoords;
			}
			else {
				throw new RuntimeException();
			}
			// You die when you're on the same spot as the bot
			if (Arrays.equals(playerCoords, botCoords)) {
				endGameFail();
				return "MOVE_FAIL";
			}
			return "MOVE_SUCCESS";
		}
	}

  /**
   * Improved version of look(): look at the area around a player
   * Includes indication of the player when the bot is looking 
   * and indication of both player and bot when the player is looking.
   * 
   * @param Coordinates to use as centre for the area to look at.
   * @return A 5x5 array for the {@link Player} to process.
   * @exception ArrayIndexOutOfBoundsException for the tiles that are outside of the map that the player tries to look at
   */
	protected char[][] lookArray(int[] centreCoords) {
		char[][] printMap = new char[5][5];
		char[][] largeMap = map.getMap();
		for (int i = 0; i < 5; ++i) {
			for (int j = 0; j < 5; ++j) {
				try {
					//Prints a 'P' where the player is (i.e. in the centre or not in the centre when the bot is looking)
					if ((i == 2 && j == 2 && centreCoords == playerCoords) || (centreCoords[0] - 2 + i == playerCoords[0] && centreCoords[1] - 2 + j == playerCoords[1])) {
						printMap[i][j] = 'P';
					}
					// Prints a 'B' where the bot is if it's not the bot looking
					else if ((centreCoords[0] - 2 + i == botCoords[0] && centreCoords[1] - 2 + j == botCoords[1]) && centreCoords != botCoords) {
						printMap[i][j] = 'B';
					}
					else {
						// Because the array square is 5x5 it goes from centreCoords - 2 to centreCoords + 2
						printMap[i][j] = largeMap[centreCoords[0] - 2 + i][centreCoords[1] - 2 + j];
					}
				}
				// Hash the space outside the map
				catch (ArrayIndexOutOfBoundsException e) {
					printMap[i][j] = '#';
				}
			}
		}
		return printMap;
	}

 /**
	 * Processes the player's pickup command, updating the map and the player's gold amount.
	 *
	 * @return If the player successfully picked-up gold or not.
	 */
	protected String pickup() {
		if (map.getItemAtCoordinate(playerCoords) == 'G') {
			goldOwned ++;
			map.removeItemAtCoordinate(playerCoords);
			return "SUCCESS. Gold owned: " + goldOwned + ".";
		}
		else {
			return "FAIL. Gold owned: " + goldOwned + ".";
		}
	}

 /**
	 * Quits the game, shutting down the application.
	 */
	protected void quitGame() {
		System.out.println("QUITTING GAME");
		System.exit(0);
	}

	/**
	 * Quits the game when the player has exited the dungeon, printing a victory message.
	 */
	protected void endGameSuccess() {
		humanPlayer.passResult("Congratulations! You've exited the dungeon with enough treasure to last you a lifetime!");
		System.exit(0);
	}

	/**
	 * Quits the game when player was caught by the bot, displaying a game-over message.
	 */
	protected void endGameFail() {
		humanPlayer.passResult("Too bad, you got horribly ripped to death by the bot of terror.");
		System.exit(0);
	}

	public static void main(String[] args) {
		GameLogic g = new GameLogic();
		g.runGame();
	}

/*
 _____           _    _                                   _    _                 _      
|_   _|         | |  (_)                                 | |  | |               | |     
  | |  ___  ___ | |_  _  _ __    __ _    _ __ ___    ___ | |_ | |__    ___    __| | ___ 
  | | / _ \/ __|| __|| || '_ \  / _` |  | '_ ` _ \  / _ \| __|| '_ \  / _ \  / _` |/ __|
  | ||  __/\__ \| |_ | || | | || (_| |  | | | | | ||  __/| |_ | | | || (_) || (_| |\__ \
  \_/ \___||___/ \__||_||_| |_| \__, |  |_| |_| |_| \___| \__||_| |_| \___/  \__,_||___/
                                 __/ |                                                  
                                |___/                                                   
 These methods were used in the testing of the code and I thought it would be useful to keep them in in case needed for future testing. */

 	/**
 	 * Prints the whole stored map with indications of where the player and bot are at.
 	 */
  protected void printWholeMap() {
  	char[][] printMap = map.getMap();
  	for (int i = 0; i < printMap.length; ++i) {
  		for (int j = 0; j < printMap[0].length; ++j) {
  			if (i == playerCoords[0] && j == playerCoords[1]) {
  				System.out.print('P');
  			}
  			else if (i == botCoords[0] && j == botCoords[1]) {
  				System.out.print('B');
  			}
  			else {
  				System.out.print(printMap[i][j]);
  			}
  			if (j == printMap[0].length - 1) {
  				System.out.println("");
  			}
  		}
  	}
  }

  /**
   * Prints a 2D char array.
   * 
   * @param The array to print
   */
  protected void printArray(char[][] array) {
  	for (int i = 0; i < array.length; ++i) {
  		for (int j = 0; j < array.length; ++j) {
  				System.out.print(array[i][j]);
  			if (j == array.length - 1) {
  				System.out.println("");
  			}
  		}
  	}
  }
}