package com.src;

import java.io.IOException;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {

    /**
     * Regex's pattern used for analysis of client requests
     */
    private static Pattern requestMsg = Pattern.compile("\\s*?(register\\s+?(([A-Z]|[0-9]){2}-?){3}\\s+?(\\w+?){1,256}$|lookup\\s+?(([A-Z]|[0-9]){2}-?){3}\\s*?)");


    public Client(DatagramSocket socket, String host, int port, String msg) throws IOException {
        Matcher match = requestMsg.matcher(msg);

        if (! match.matches()) {
            System.out.println("Client request should be of type:\n" +
                    " * 'register XX-XX-XX <owner name>', for register;" +
                    " * 'lookup XX-XX-XX', for lookup;'" +
                    "where XX-XX-XX is the license plate.");
            return;
        }
        if (match.group(2).equals("register")) {

        } else if (match.group(6).equals("lookup")) {

        } else {
            System.out.println("Error in regex, unexpected group matched.");
            return;
        }

        byte[] rbuf = new byte[256];
        DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);
        socket.receive(packet);

        // display response
        String received = new String(packet.getData());
        System.out.println("Echoed Message: " + received);
        //socket.close();
    }
	
}