package com.src;

import java.io.IOException;
import java.util.Scanner;
import java.net.*;
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
		//Inicializar a maquina de estados que assegura que o programa:
		// 1 - abre o servidor (1� estado)
		// 2 - chamadas consecutivas de cleinetes ( 2� estado)

		//Depois mudar isto para ele interpretar, para j� fica hardcoded para garantir que funciona
		// send request

		System.out.println("Welcome to the License Plate Manager");

		while(true) {
			update();
		}

		/*DatagramSocket socket = new DatagramSocket(8888);
		byte[] sbuf = "test".getBytes();
		InetAddress address = InetAddress.getByName("129.0");

		DatagramPacket packet = new DatagramPacket(sbuf, sbuf.length, address, 8888);
		socket.send(packet);

		// get response
		byte[] rbuf = new byte[sbuf.length];
		packet = new DatagramPacket(rbuf, rbuf.length);
		socket.receive(packet);

		// display response
		String received = new String(packet.getData());
		System.out.println("Echoed Message: " + received);
		socket.close();*/
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

		if (server == null) {
			server = new Server(port);
			currentState = State.OPENED_SERVER;
		}
	}

	private static void clientCreation(String input) {
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

		new Client(host, port, clientPurpose);
	}
}