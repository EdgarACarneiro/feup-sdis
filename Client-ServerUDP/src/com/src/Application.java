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
	private static Pattern serverCall = Pattern.compile("\\s*?java\\s*?Server\\s*?(\\d{4})");

	public static void main(String[] args) throws IOException {
		//Inicializar a maquina de estados que assegura que o programa:
		// 1 - abre o servidor (1� estado)
		// 2 - chamadas consecutivas de cleinetes ( 2� estado)

		//Depois mudar isto para ele interpretar, para j� fica hardcoded para garantir que funciona
		// send request

		while(true) {
			update();
		}

		DatagramSocket socket = new DatagramSocket(8888);
		byte[] sbuf = "test".getBytes();
		InetAddress address = InetAddress.getByName("127.0.0.1");

		DatagramPacket packet = new DatagramPacket(sbuf, sbuf.length, address, 8888);
		socket.send(packet);

		// get response
		byte[] rbuf = new byte[sbuf.length];
		packet = new DatagramPacket(rbuf, rbuf.length);
		socket.receive(packet);

		// display response
		String received = new String(packet.getData());
		System.out.println("Echoed Message: " + received);
		socket.close();
	}

	private static void update() {

		String msg = input.nextLine();

		switch(currentState) {
			case START:
				serverCreation(msg);
				break;
			case OPENED_SERVER:

				break;
			default:
				System.err.println("Error: Unknown Application State.");
		}
	}

	private static void serverCreation(String input) {
		Matcher match = serverCall.matcher(input);

		if (! match.matches()) {
			System.out.println("To create a Server use: 'java Server <port>'");
			return;
		}
		int port = Integer.parseInt(match.group(1));

		if (server == null) {
			server = new Server(port);
			currentState = State.OPENED_SERVER;
		}
	}
}