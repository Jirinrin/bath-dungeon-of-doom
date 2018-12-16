import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Implements a bot that walks around the map in the game and tries to hunt down the human player.
 * Contains artificial intelligence to decide what to do
 * and where to move whether or not it knows where the human player is.
 * 
 * @author Jiri Swen
 * @version 2.0
 * @release 15/12/2017
 * @see {@link Player.java}
 * @see {@link GameLogic.java}
 * @see {@link DijkstraOperations.java}
 */
public class Bot implements Player {

	// The latest 5x5 grid that the bot remembers from the last time it looked
	private char[][] mapMemory;
	// The coordinates that the bot is at on the 5x5 grid from mapMemory
	private int[] botCoordinates;
	// Stores the coordinates where the bot has last seen the human player
	private int[] memoryPlayerCoordinates;

	// Is toggled to remind the bot the next turn that it needs to look
	private boolean needToLook;
	// Keeps track of how many turns the bot has not looked consequently
	private int lookCounter = 0;
	// Is toggled to know whether the bot should move towards the stored player coordinates
	private boolean remembersPlayer = false;
	
	// Stores the direction that the bot is moving in, in order to use when received back a fail or success protocol from GameLogic
	private char movingInDirection;
	// Used to know if the bot wants to e.g. not move backwards
	private char cantMoveInDirection = 'X';
	// Remembers the last direction the bot was moving in so as to stay accurate even when it turns out after looking that it can't move where it was intending to go
	private char lastMovingDirection;
	
	// Used for picking a random direction when wished
	private char[] directionsArchive = new char[] {'N', 'S', 'E', 'W'};
	
	// Made use of for moving towards the player in the most efficient manner
	private DijkstraOperations dijkstra;

	/**
	 * Default constructor
	 */
	public Bot() {
		mapMemory = new char[5][5];
		botCoordinates = new int[] {2, 2};
		movingInDirection = directionsArchive[(int) (Math.random() * 4)];
		lastMovingDirection = movingInDirection;
		needToLook = true;
		dijkstra = new DijkstraOperations();
	}

	/**
	 * Assesses whether to look or move when asked for the next action.
	 * 
	 * @return The command to be further processed by {@link GameLogic}
	 */
	public String getNextAction() {
		// Also returns "LOOK" if bot has not looked for 5 turns in a row: otherwise it might not see a player passing by whilst moving in its own remembered square
		if (needToLook || lookCounter >= 5) {
			lookCounter = 0;
			return "LOOK";
		}
		else {
			lookCounter ++;
			return "MOVE " + getMoveDirection();
		}
	}

	/**
	 * NOT IMPLEMENTED because for a bot there is no difference
	 * between the 'raw' and 'processed' input.
	 */
	public String getNextCommand() {
		return null;
	}

/*
___  ___              _               
|  \/  |             (_)              
| .  . |  ___ __   __ _  _ __    __ _ 
| |\/| | / _ \\ \ / /| || '_ \  / _` |
| |  | || (_) |\ V / | || | | || (_| |
\_|  |_/ \___/  \_/  |_||_| |_| \__, |
                                 __/ |
                                |___/ 
 */

  /**
   * Assesses whether bot should move towards the player coordinates (i.e. when it has seen a player),
   * continue in the direction it was already going in (i.e. when there is no obstacles and the next tile is known)
   * or move in a new random direction (i.e. in case of an obstacle or edge of known map).
   * 
   * @return The direction to move in.
   */
	protected char getMoveDirection() {
		if (remembersPlayer) {
			movingInDirection = moveTowardsPlayer();
		}
		else {
			while (true) {
				if (! checkIfCanMove(movingInDirection, true)) {
					setNewRandomDirection(false);
				}
				else {
					break;
				}
			}
		}
		return movingInDirection;
	}

	/**
	 * Translates the next tile outputted from getNextTile() to the direction for the bot to move in based on the bot's own coordinates.
	 * 
	 * @return The direction to move in.
	 */
	protected char moveTowardsPlayer() {
		int[] nextTile = new int[2];
		nextTile = getNextTile();
		int[] translation = new int[] {botCoordinates[0] - nextTile[0], botCoordinates[1] - nextTile[1]};
		if (translation[0] == 1) {
			return 'N';
		}
		if (translation[0] == -1) {
			return 'S';
		}
		if (translation[1] == -1) {
			return 'E';
		}
		if (translation[1] == 1) {
			return 'W';
		}
		throw new RuntimeException();
	}

	/**
	 * Has Dijkstra's algorithm run the calculations to give the path to the human player's coordinates 
	 * and picks the first next space of that path.
	 * <p>
	 * (Reason for generating the path and updating the network anew every single turn:
	 * to have it be more adaptable to change of the program should you e.g. 
	 * change the code to have two moves every turn or something: the player's location 
	 * constantly changes and so does the path as a result...)
	 * 
	 * @return The coordinates for the next tile to move to.
	 */
	protected int[] getNextTile() {
		updateNetwork();
		dijkstra.executeDijkstra(botCoordinates);
		LinkedList<Vertex> path = dijkstra.getShortestPath(memoryPlayerCoordinates);
		return DijkstraOperations.nodeIDToMapCoordinates(DijkstraOperations.vertexToNodeID(path.get(1)));
	}

	/**
	 * Arranges that {@link Dijkstra} stays up to date on where the walls (i.e. obstacles) are.
	 */
	protected void updateNetwork() {
		List<Integer[]> wallList = new ArrayList<Integer[]>();
		wallList = findAllWalls();
		dijkstra.updateNetwork(wallList);
	}

	/**
	 * Searches the entire 5x5 grid for walls (#).
	 *
	 * @return A list of the coordinates of all found walls.
	 */
	protected List<Integer[]> findAllWalls() {
		List<Integer[]> coordinateList = new ArrayList<Integer[]>();
		for (int i = 0; i < 5; ++i) {
			for (int j = 0; j < 5; ++j) {
				if (mapMemory[i][j] == '#') {
					coordinateList.add(new Integer[] {i, j});
				}
			}
		}
		return coordinateList;
	}
	
/*
 _    _  _                                           _         _                   __                 _  _                   _    
| |  | || |                                         (_)       (_)                 / _|               | || |                 | |   
| |  | || |__    ___  _ __     _ __  ___   ___  ___  _ __   __ _  _ __    __ _   | |_  ___   ___   __| || |__    __ _   ___ | | __
| |/\| || '_ \  / _ \| '_ \   | '__|/ _ \ / __|/ _ \| |\ \ / /| || '_ \  / _` |  |  _|/ _ \ / _ \ / _` || '_ \  / _` | / __|| |/ /
\  /\  /| | | ||  __/| | | |  | |  |  __/| (__|  __/| | \ V / | || | | || (_| |  | | |  __/|  __/| (_| || |_) || (_| || (__ |   < 
 \/  \/ |_| |_| \___||_| |_|  |_|   \___| \___|\___||_|  \_/  |_||_| |_| \__, |  |_|  \___| \___| \__,_||_.__/  \__,_| \___||_|\_\
                                                                          __/ |                                                   
                                                                         |___/                                                    
 */

  /**
   * Processes feedback of the action sent in getNextAction().
   * @param The feedback from {@link GameLogic} to process.
   */
	public void passResult(String result) {
		switch (result) {
			case "MOVE_SUCCESS":
				updateBotCoords();
				break;
			case "MOVE_FAIL":
				setNewRandomDirection(false);
				break;
		}
	}

	/**
	 * Changes the bot's coordinates in its 5x5 grid,
	 * still remembering movingInDirection from when it sent the direction to {@link GameLogic}.
	 * Also decides on a new random direction for the next MOVE if it has 
	 * arrived at where it last remembered the player was at
	 * or if the tile it was going to move to is unknown.
	 */
	protected void updateBotCoords()  {
		botCoordinates = giveCoordinatesInDirection(botCoordinates, movingInDirection);
		if (Arrays.equals(botCoordinates, memoryPlayerCoordinates)) {
			remembersPlayer = false;
			// Look immediately the next turn because the player probably isn't far away
			needToLook = true;
			/* Because the direction changes constantly when going after the player, here it's not relevant to 'not want to go backwards' (hence the "true")
			   Obviously the random direction is only used if the player is NOT found the next turn using the LOOK command */
			setNewRandomDirection(true);
		}
		else if (! remembersPlayer && ! checkIfTileIsKnown(movingInDirection)) {
			setNewRandomDirection(false);
		}
	}

	/**
	 * Updates the 5x5 grid map in the memory of the bot after LOOK command has been issued.
	 * 
	 * @param The array received from {@link GameLogic} to process.
	 */
	public void passArray(char[][] array) {
		mapMemory = new char[5][5];
		mapMemory = array;
		// Set the bot's coordinates back to the middle of the map
		botCoordinates = new int[] {2, 2};
		needToLook = false;
		// Makes it so it wil go after the player's currently remembered coordinates the next turn
		if (findPlayer()) {
			remembersPlayer = true;
		}
		/* Before the LOOK command the direction for the next MOVE had already been set,
		   but now it's reevaluating if this move is still possible and otherwise changing it */
		if (! checkIfCanMove(movingInDirection, true)) {
			/* This reverts the moving direction the last used direction because of the fact
			   that the random method tries to not have the bot move backwards:
			   this wouldn't work if it still held the direction it failed to go in... */
			movingInDirection = lastMovingDirection;
			setNewRandomDirection(false);
		}
	}

/*
 _____                           _                           _    _                 _      
|  __ \                         (_)                         | |  | |               | |     
| |  \/  ___  _ __    ___  _ __  _   ___    _ __ ___    ___ | |_ | |__    ___    __| | ___ 
| | __  / _ \| '_ \  / _ \| '__|| | / __|  | '_ ` _ \  / _ \| __|| '_ \  / _ \  / _` |/ __|
| |_\ \|  __/| | | ||  __/| |   | || (__   | | | | | ||  __/| |_ | | | || (_) || (_| |\__ \
 \____/ \___||_| |_| \___||_|   |_| \___|  |_| |_| |_| \___| \__||_| |_| \___/  \__,_||___/
                                                                                           
 */

	/**
	 * Gives the coordinates of the tile in a direction respective to another tile.
	 * 
	 * @param  oldCoordinates : Tile to base the direction off of.
	 * @param  direction : Direction to use.
	 * @return The coordinates of the adjacent tile.
	 */
	protected static int[] giveCoordinatesInDirection(int[] oldCoordinates, char direction) {
		// The {-1, -1} is necessary for the compiler because "The direction might not be N/S/E/W"
		int[] newCoordinates = new int[] {-1, -1};
		switch (direction) { // I got this method right from GameLogic inside the move() method
			case 'N':
				newCoordinates = new int[] {oldCoordinates[0] - 1, oldCoordinates[1]};
				break;
			case 'S':
				newCoordinates = new int[] {oldCoordinates[0] + 1, oldCoordinates[1]};
				break;
			case 'E':
				newCoordinates = new int[] {oldCoordinates[0], oldCoordinates[1] + 1};
				break;
			case 'W':
				newCoordinates = new int[] {oldCoordinates[0], oldCoordinates[1] - 1};
				break;
		}
		return newCoordinates;
	}

	/**
	 * @param The coordinates to find the character at.
	 * @return The character stored at specified coordinates.
	 * @throws ArrayIndexOutOfBoundsException if the coordinates are outside of the bot's 5x5 grid.
	 */
	protected char getTileAtCoordinate(int[] coordinates) throws ArrayIndexOutOfBoundsException {
		return mapMemory[coordinates[0]][coordinates[1]];
	}

	/**
	 * Says whether or not the tile in a direction is on the known map
	 * by trying and catching getTileAtCoordinate().
	 * 
	 * @param The direction to check for an unknown tile.
	 * @return Whether the tile in specified direction is known.
	 */
	protected boolean checkIfTileIsKnown(char direction) {
		try {
			getTileAtCoordinate(giveCoordinatesInDirection(botCoordinates, direction));
		}
		catch (ArrayIndexOutOfBoundsException a) {
			return false;
		}
		return true;
	}

	/**
	 * @param The direction to get the opposite of.
	 * @return The opposite direction of specified direction.
	 * @exception Just in case some unknown character gets put in.
	 */
	protected static char getOppositeDirection(char currentDirection) {
		switch (currentDirection) {
			case 'N':
				return 'S';
			case 'S':
				return 'N';
			case 'E':
				return 'W';
			case 'W':
				return 'E';
		}
		throw new RuntimeException();
	}

	/**
	 * Searches the known 5x5 grid from the last time looked for the human player.
	 * Updates the stored player coordinates while it's at it.
	 * 
	 * @return Whether the player was found or not.
	 */
	protected boolean findPlayer() {
		for (int i = 0; i < 5; ++i) {
			for (int j = 0; j < 5; ++j) {
				if (mapMemory[i][j] == 'P') {
					memoryPlayerCoordinates = new int[] {i, j};
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Sets the direction for the next MOVE to something random.
	 * 
	 * @param Whether you want the bot moving backwards respective it's last direction to be a possibilty.
	 */
	protected void setNewRandomDirection(boolean backwardsAllowed) {
		cantMoveInDirection = getOppositeDirection(movingInDirection);
		char nextRandomDirection = getNextRandomDirection(backwardsAllowed);
			if (! checkIfTileIsKnown(nextRandomDirection)) {
				needToLook = true;
			}
		lastMovingDirection = movingInDirection;
		movingInDirection = nextRandomDirection;
	}

	/**
	 * Gets a random direction that the bot CAN move in,
	 * therefore already checking whether a direction is possible until it finds a solution.
	 * 
	 * @param Whether you want the bot moving backwards respective it's last direction to be a possibilty.
	 * @return The direction to move in.
	 */
	protected char getNextRandomDirection(boolean backwardsAllowed) {
		int timesRun = 0;
		char direction;
		while (true) {
			direction = directionsArchive[(int) (Math.random() * 4)];
			if (checkIfCanMove(direction, backwardsAllowed)) {
				return direction;
			}
			timesRun ++;
			// In case the bot has worked itself into a dead end, it will go backwards if necessary
			if (timesRun > 99) {
				if (checkIfCanMove(direction, true)) {
					return direction;
				}
			}
		}
	}

	/**
	 * Assesses whether the bot can move in a certain direction.
	 * @param direction : Direction to try moving in.
	 * @param backwardsAllowed : Whether the bot may move backwards to its current direction.`
	 * @return Whether or not the bot can move in specified direction.
	 */
	protected boolean checkIfCanMove(char direction, boolean backwardsAllowed) {
		try {
			if (getTileAtCoordinate(giveCoordinatesInDirection(botCoordinates, direction)) != '#' && cantMoveInDirection != direction) {
				return true;
			}
			else if (getTileAtCoordinate(giveCoordinatesInDirection(botCoordinates, direction)) != '#' && backwardsAllowed) {
				return true;
			}
			else {
				return false;
			}
		}
		/* Also returns that it can move in the direction if that tile is unknown:
		   the bot will look in the next turn and reevaluate then. */
		catch (ArrayIndexOutOfBoundsException a) {
			return true;
		}
	}
}