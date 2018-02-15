package com.src;

import java.io.IOException;
import java.util.Scanner;
import java.net.*;


/**
 * Class responsible for interpreting the console input messages and then delegate them to either the Server or the Clients
 */
public class Application {

	/**
	 * The application's server
	 */
	private static Server server;

	/**
	 * Possible States the Application may be in
	 */
	private enum State {
		START, OPENED_SERVER
	}

	/**
	 * Current State the application is in
	 */
	private static State currentState;

	public static void main(String[] args) {
		//Inicializar a maquina de estados que assegura que o programa:
		// 1 - abre o servidor (1� estado)
		// 2 - chamadas consecutivas de cleinetes ( 2� estado)

		//Depois mudar isto para ele interpretar, para j� fica hardcoded para garantir que funciona
		// send request

		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		byte[] sbuf = "test".getBytes();
		InetAddress address = null;
		try {
			address = InetAddress.getByName("128.92.232.2");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		DatagramPacket packet = new DatagramPacket(sbuf, sbuf.length, address, 4445);
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// get response
		byte[] rbuf = new byte[sbuf.length];
		packet = new DatagramPacket(rbuf, rbuf.length);
		try {
			socket.receive(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// display response
		String received = new String(packet.getData());
		System.out.println("Echoed Message: " + received);
		socket.close();

	}
}