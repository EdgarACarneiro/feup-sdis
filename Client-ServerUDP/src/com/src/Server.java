package com.src;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Server {

    public Server(int port) throws IOException {
        DatagramSocket socket = new DatagramSocket(port);
    }
	
}

// Use Hashmap for storing license plates
// java.DatagramScoket //java.DatagramPacket e java.inetAdress