import java.util.Scanner;
import java.util.List;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * Runs the game with a human player and contains code needed to read inputs and display results.
 * 
 * @author Jiri Swen
 * @version 2.0
 * @release 15/12/2017
 * @see {@link Player.java}
 * @see {@link GameLogic.java}
 */
public class HumanPlayer implements Player {

	// Stores commands considered 'valid' to check against
	private List<String> validCommands = Arrays.asList("HELLO", "LOOK", "MOVE N", "MOVE S", "MOVE E", "MOVE W", "PICKUP", "QUIT", "PASS", "COMMANDS");
	// Used to read commands from command line to be processed
	private BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

	/**
	 * Default constructor.
	 */
	public HumanPlayer() {}

	/**
	 * Reads the next command from the command line, 
	 * then processes it, checks if it is valid and returns it for {@link GameLogic} to use.
	 * 
	 * @return The processed command.
	 */
	public String getNextAction() {
		while (true) {
			String command = getNextCommand();
			// This makes it so that it doesn't matter if you type in (partly) lowercase or put spaces at the beginning or end
			command = command.trim().toUpperCase();
			if (command.equals("COMMANDS")) {
				printAvailableCommands();
			}
			// Lets your turn go by without anything happening
			else if (command.equals("PASS")) {
				System.out.println("");
			}
			if (validCommands.contains(command)) {
				return command;
			}
			else {
				System.out.println("Invalid");
			}
		}
	}

	/**
	 * Prints all possible commands to type when a player asks for it.
	 */
	protected void printAvailableCommands() {
		System.out.println("\nAvailable commands:");
		for (int i = 0; i < validCommands.size(); ++i) {
			System.out.println("\"" + validCommands.get(i) + "\"");
		}
		System.out.println("");
	}

	/**
	 * Processes the command. It should return a reply in form of a String, as the protocol dictates.
	 * Otherwise it should return the string "Invalid".
	 * NOT USED because taking a string as argument does not
	 * make as much sense as reading directly from the command line.
	 *
	 * @param Input entered by the user.
	 * @return Processed output or Invalid if the @param command is wrong.
	 */
	protected String getNextAction(String command) {
    return null;
  }

  /**
   * Gets raw input from the console for getNextInput() to use 
   * or for {@link GameLogic} to use when needed.
   * Also cuts to closing the system when e.g. Ctrl+D is pressed.
   * 
   * @return The raw input from the command line.
   */
	public String getNextCommand() {
		String command = getInputFromConsole();
		if (command == null) {
			closeReader();
			System.exit(0);
		}
		return command;
	}

	/**
	 * Reads player's input from the console.
	 * 
	 * @return A string containing the input the player entered.
	 * @exception The usual IOException from a BufferedReader.
	 */
	protected String getInputFromConsole() {
		try {
			return br.readLine();
		}
		catch (IOException e) {
			System.err.println("\nIOException!");
		}
		return "0";
	}

	/**
	 * Closes the reader (when the game ends).
	 *
	 * @exception The usual IOException from a BufferedReader.
	 */
	protected void closeReader() {
		try {
			br.close();
		}
		catch (IOException e) {
			System.err.println("\nIOException!");
		}
	}

	/**
	 * Accepts input to print the (processed) result.
	 * 
	 * @param The result coming from {@link GameLogic}
	 */
	public void passResult(String result) {
	  switch (result) {
	 		// Move success and failure is 'encoded' as that can be useful for the bot to read as feedback
	 		case "MOVE_SUCCESS":
	 			System.out.println("\nSUCCESS\n");
	 			return;
	 		case "MOVE_FAIL":
	 			System.out.println("\nFAIL\n");
	 			return;
		}

		// Most results are just printed directly as there's no use in 'encoding' them
		System.out.println("\n" + result + "\n");
	}

	/**
	 * Prints out an array when received for the player to read.
	 * 
	 * @param The array coming from {@link GameLogic} to print.
	 */
	public void passArray(char[][] array) {
		System.out.println("");
		for (int i = 0; i < array.length; ++i) {
			for (int j = 0; j < array[i].length; ++j) {
				System.out.print(array[i][j]);
				// Goes to the next line when end of line is reached
				if (array[i].length - j <= 1) {
					System.out.println("");
				}
			}	
		}
		System.out.println("");
	}
}