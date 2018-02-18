package com.src;

import java.io.IOException;
import java.net.*;

/**
 * Class responsible for receiving Client's requests and handling the license plate management
 */
public class Server {

    /**
     * Maximum size of a request received by the Server
     */
    private static final int MAX_REQUEST_SIZE = 274;


    public Server(DatagramSocket socket) throws IOException {

        byte[] receiver = new byte[MAX_REQUEST_SIZE];
        DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);
        socket.receive(packet);

        // display response
        String received = new String(packet.getData());
        System.out.println("Echoed Message: " + received);
        //socket.close();
    }

    private void receiveMessages() {
        //socket.receive(packet);
    }
	
}