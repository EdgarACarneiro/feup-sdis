package com.src;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;

/**
 * Class responsible for receiving Client's requests and handling the license plate management
 */
public class Server {

    /**
     * Maximum size of a request received by the Server
     */
    private static final int MAX_REQUEST_SIZE = 274;

    /**
     * The license plates database
     */
    private HashMap<String, String> database;

    /**
     * Server that saves all the information regarding the license plates and initializes the database
     *
     * @param socket Socket used to communicate with the clients
     * @throws IOException
     */
    public Server(DatagramSocket socket) throws IOException {
        database = new HashMap<>();

        //Receive messages
        while(true) { receiveMessages(socket); }
    }

    /**
     * Receive the messages sent through the given socket
     *
     * @param socket Socket used to communicate
     * @throws IOException
     */
    private void receiveMessages(DatagramSocket socket) throws IOException {

        byte[] receiver = new byte[MAX_REQUEST_SIZE];
        DatagramPacket packet = new DatagramPacket(receiver, receiver.length);
        socket.receive(packet);

        //conforme o request, dar update a database ou devolver o user pedido

        // display response -> do template forncedio -> TODO
        String received = new String(packet.getData());
        System.out.println("Echoed Message: " + received);
        //socket.close();
    }

    /**
     * Getter for the license plate's owner
     *
     * @param licensePlate The given license plate
     * @return The license plate's owner, or null if the license plate does not belong to the database
     */
    private String getOwner(String licensePlate) {
        return database.get(licensePlate);
    }

    /**
     * Add a new license plate to the database
     *
     * @param licensePlate The new license plate
     * @param owner The license plate's owner
     * @return If true, success upon adding the new User to the database, failed otherwise.
     */
    private boolean registerUser(String licensePlate, String owner) {
        if (database.containsKey(licensePlate))
            return false;
        else {
            database.put(licensePlate, owner);
            return true;
        }
    }
	
}