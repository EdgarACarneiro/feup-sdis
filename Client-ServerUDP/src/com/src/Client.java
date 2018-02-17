package com.src;

import java.io.IOException;
import java.net.*;

public class Client {

    public Client(DatagramSocket socket, String host, int port, String msg) throws IOException {
        System.out.println("Im here son");

        byte[] rbuf = new byte[256];
        DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);
        socket.receive(packet);

        // display response
        String received = new String(packet.getData());
        System.out.println("Echoed Message: " + received);
        //socket.close();
    }
	
}