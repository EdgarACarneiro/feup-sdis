package com.src;

import java.io.IOException;
import java.net.*;

public class Server {

    public Server(DatagramSocket socket) throws IOException {

        byte[] sbuf = "test".getBytes();
        InetAddress address = InetAddress.getByName("127.0.0.1");

        DatagramPacket packet = new DatagramPacket(sbuf, sbuf.length, address, 8080);
        socket.send(packet);
    }
	
}

// Use Hashmap for storing license plates
// java.DatagramScoket //java.DatagramPacket e java.inetAdress