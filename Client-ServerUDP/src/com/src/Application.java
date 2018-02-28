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
	 * The user's input
	 */
	private static Scanner input = new Scanner(System.in);

	/**
	 * Regex's pattern used for analysis of server invocations
	 */
	private static Pattern serverInvoke = Pattern.compile("\\s*?java\\s+?Server\\s+?(\\d{4})\\s+?((\\d+\\.?){1,4})\\s+?(\\d{4})\\s*?$");

	/**
	 * Regex's pattern used for analysis of client invocations
	 */
	private static Pattern clientInvoke = Pattern.compile("\\s*?java\\s+?Client\\s+?((\\d+\\.?){1,4})\\s+?(\\d{4})\\s+?(.+)$");

	/**
	 * Main project function, initiates the Application
	 *
	 * @param args No args are supposed to be used
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		System.out.println("Welcome to the License Plate Manager");
		while(true) { update();	}
	}

	/**
	 * Application update, supposed to run continuously.
	 *
	 * @throws IOException
	 */
	private static void update() throws IOException {

		String msg = input.nextLine();
		Matcher serverMatch = serverInvoke.matcher(msg);
		Matcher clientMatch = clientInvoke.matcher(msg);

		if (serverMatch.matches())
			serverCreation(serverMatch);
		else if (clientMatch.matches())
			clientCreation(clientMatch);
		else
			System.out.println(
					"Server usage:\n " +
					"* 'java Server <srvc_port> <mcast_addr> <mcast_port>'.\n" +
					"Client usage:\n" +
					"* 'java client <mcast_addr> <mcast_port> register <plate number> <owner name>', for register.\n" +
					"* 'java client <mcast_addr> <mcast_port> lookup <plate number>', for lookup."
			);
	}

	/**
	 * Creates the Server used for the Client - Server interaction
	 *
	 * @param match Match with the server Regex
	 * @throws IOException
	 */
	private static void serverCreation(Matcher match) throws IOException {
		int servicePort = Integer.parseInt(match.group(1));
		String mcastAddr = match.group(2);
		int mcastPort = Integer.parseInt(match.group(3));

		new Server(new DatagramSocket(servicePort), mcastAddr, mcastPort);
	}

	/**
	 * Creates a Client for the server Client - Server interaction
	 *
	 * @param match Match with the client Regex.
	 * @throws IOException
	 */
	private static void clientCreation(Matcher match) throws IOException {
		String mcastAddr = match.group(1);
		int mcastPort = Integer.parseInt(match.group(3));
		String clientPurpose = match.group(4);

		new Client(new DatagramSocket(), mcastAddr, mcastPort, clientPurpose);
	}
}