package com.src;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Class responsible for interpreting the console input messages and then delegate them to either the Server or the Clients
 */
public class Application {

	/**
	 * The application's server
	 */
	private static Server server = null;

	/**
	 * The socket associated to the application
	 */
	private static DatagramSocket socket = null;

	/**
	 * The user's input
	 */
	private static Scanner input = new Scanner(System.in);

	/**
	 * Possible States the Application may be in
	 */
	private enum State {
		START, OPENED_SERVER
	}

	/**
	 * Current State the application is in
	 */
	private static State currentState = State.START;

	/**
	 * Regex's pattern used for analysis of server invocations
	 */
	private static Pattern serverInvoke = Pattern.compile("\\s*?java\\s+?Server\\s+?(\\d{4})\\s*?");

	/**
	 * Regex's pattern used for analysis of client invocations
	 */
	private static Pattern clientInvoke = Pattern.compile("\\s*?java\\s+?Client\\s+?((\\d+\\.?){1,4})\\s+?(\\d{4})\\s+?(.+)$");


	public static void main(String[] args) throws IOException {

		System.out.println("Welcome to the License Plate Manager");
		while(true) { update();	}
	}

	private static void update() throws IOException {

		String msg = input.nextLine();

		switch(currentState) {
			case START:
				serverCreation(msg);
				break;
			case OPENED_SERVER:
				clientCreation(msg);
				break;
			default:
				System.err.println("Error: Unknown Application State.");
		}
	}

	private static void serverCreation(String input) throws IOException {
		Matcher match = serverInvoke.matcher(input);

		if (! match.matches()) {
			System.out.println("To create a Server use: 'java Server <port>'.");
			return;
		}
		int port = Integer.parseInt(match.group(1));

		if (server == null && socket == null) {
			socket = new DatagramSocket(port);
			server = new Server(socket);
			currentState = State.OPENED_SERVER;
		}
	}

	private static void clientCreation(String input) throws IOException {
		Matcher match = clientInvoke.matcher(input);

		if (! match.matches()) {
			System.out.println("Client usage:\n" +
					"* 'java Client <host> <port> register <plate number> <owner name>', for register;\n" +
					"* 'java Client <host> <port> lookup <plate number>', for lookup;");
			return;
		}
		String host = match.group(1);
		int port = Integer.parseInt(match.group(3));
		String clientPurpose = match.group(4);

		new Client(socket, host, port, clientPurpose);
	}
}