/**
* Defines the properties and behaviour of a car.
*
* @author Jiri Swen
* @version 1.0
* @release 15/12/2017
* @see {@link HumanPlayer.java}
* @see {@link Bot.java}
*/

public interface Player {
	
	/**
	 * Ask the Player entity for the thing to do on the next turn.
	 *
	 * @return A String containing the command to be processed by {@link GameLogic}.
	 */
	public String getNextAction();

	/**
	 * An extra method to use in case you need to get raw input that is not processed (i.e. uppercase, trim).
	 *
	 * @return A String containing the command to be processed by {@link GameLogic}.
	 */
	public String getNextCommand();

	/**
	 * This is used to send information back to the Player entity to process for it's own purposes.
	 *
	 * @param The result coming from {@link GameLogic} to process.
	 */
	public void passResult(String result);

	/**
	 * A designated method to pass a section of a map as an array to the Player entity (probably using {@link GameLogic.look()}) to process.
	 *
	 * @param The 2D character array that is the map.
	 */
	public void passArray(char[][] array);
}