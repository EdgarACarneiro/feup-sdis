package com.src;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Timer;

/**
 * Class responsible for receiving Client's requests and handling the license plate management
 */
public class Server {

    /**
     * Maximum size of a request received by the Server
     */
    private static final int MAX_REQUEST_SIZE = 274;

    /**
     * Position where the Owner name starts in the clients register requests
     */
    private static final int OWNER_NAME_POS = 18;

    /**
     * Time elapsed between Server multicasts
     */
    private static final int TIMER_PERIOD = 1000;

    /**
     * The license plates database
     */
    private HashMap<String, String> database;

    /**
     * The application's socket, used to implement the Client - Server connection
     */
    private DatagramSocket socket = null;

    /**
     * Socket used for the multicast communication (Server with all the Clients)
     */
    private DatagramSocket mcastSocket = null;

    /**
     * Address used in the multicast communication, in format InetAdress
     */
    private InetAddress mcastAddr = null;

    /**
     * Port used in the multicast communication
     */
    private Integer mcastPort = null;

    /**
     * Server that saves all the information regarding the license plates and initializes the database
     *
     * @param socket Socket used to communicate with the clients
     * @param mcastAddr Address used for multicast communication
     * @param mcastPort Port used for multicast communication
     * @throws IOException
     */
    public Server(DatagramSocket socket, String mcastAddr, Integer mcastPort) throws IOException {
        System.out.println("Booting a New Server");

        this.mcastAddr = InetAddress.getByName(mcastAddr);
        this.mcastPort = mcastPort;
        mcastSocket = new DatagramSocket();

        database = new HashMap<>();
        this.socket = socket;

        //Creation of threads, 1 for loop of advertisement, other for cicle receiveMessages -> create advertisement function
        Timer timer  = new Timer();
        timer.scheduleAtFixedRate(null,  0, TIMER_PERIOD);

        //Receive messages
        while(true) { receiveMessages(); }
    }

    /**
     * Receive the messages sent through the given socket
     *
     * @throws IOException
     */
    private void receiveMessages() throws IOException {

        byte[] receiver = new byte[MAX_REQUEST_SIZE];
        DatagramPacket packet = new DatagramPacket(receiver, receiver.length);

        socket.receive(packet);

        String received = new String(packet.getData());
        System.out.println("Server: Received Request: " + received);

        String[] groups=  received.split(" ");
        switch (groups[0]) {
            case "REGISTER":
                sendReply(registerUser(groups[1], received.substring(OWNER_NAME_POS)));
                break;
            case "LOOKUP":
                sendReply(getOwner(groups[1]));
                break;
            default:
                System.err.println("Server Error: Received unknown request.");
        }
        //socket.close();
    }

    /**
     * Getter for the license plate's owner
     *
     * @param licensePlate The given license plate
     * @return The owner if the license plate exists on the database, 'ERROR' otherwise
     */
    private String getOwner(String licensePlate) {
        String result;
        String owner = database.get(licensePlate.trim());

        if (owner == null)
            result = "-1 ";
        else
            result = owner + " ";

        return result + "lookup " + licensePlate;
    }

    /**
     * Add a new license plate to the database
     *
     * @param licensePlate The new license plate
     * @param owner The license plate's owner
     * @return String containing the number of entries on the database if the client insertion succeeded, '-1' otherwise
     */
    private String registerUser(String licensePlate, String owner) {
        String result;
        if (database.containsKey(licensePlate))
            result = "-1 ";
        else {
            database.put(licensePlate, owner);
            result = Integer.toString(database.size()) + " ";
        }
        return result + "register " + licensePlate + " " + owner;
    }

    /**
     * Sends the Client the Server reply to the Client Request
     *
     * @param reply Message to be sent
     * @throws IOException
     */
    private void sendReply(String reply) throws IOException {
        byte[] request = reply.getBytes();
        DatagramPacket packet = new DatagramPacket(request, request.length, InetAddress.getByName(mcastAddr), mcastPort);
        mcastSocket.send(packet);
    }
	
}