package com.src;

import java.io.IOException;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {

    /**
     * Regex's pattern used for analysis of client register requests
     */
    private static Pattern registerRequest = Pattern.compile("\\s*?((register)\\s+?((([A-Z]|[0-9]){2}-?){3})\\s+?(((\\w|\\s)+?){1,256}))$");

    private static Pattern lookupRequest = Pattern.compile("\\s*?(lookup)\\s+?((([A-Z]|[0-9]){2}-?){3})\\s*?");

    public Client(DatagramSocket socket, String host, int port, String msg) throws IOException {
        String request = buildRequest(msg);

        byte[] rbuf = new byte[256];
        DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);
        socket.receive(packet);

        // display response
        String received = new String(packet.getData());
        System.out.println("Echoed Message: " + received);
        //socket.close();
    }

    /**
     * Build the String request to be sent to the Server
     *
     * @param msg String to build the Client request
     * @return String request to be sent to the Server
     */
    private String buildRequest(String msg) {
        System.out.print(msg);

        Matcher matchRegister = registerRequest.matcher(msg);
        Matcher matchLookup = lookupRequest.matcher(msg);

        if ((! matchRegister.matches()) && (! matchLookup.matches())) {
            System.out.println("Client request should be of type:\n" +
                    " * 'register XX-XX-XX <owner name>', for register;\n" +
                    " * 'lookup XX-XX-XX', for lookup;'\n" +
                    "where XX-XX-XX is the license plate.");
            return "";
        }

        String request;
        if (matchRegister.matches()) {
            request = "REGISTER ";
            request += matchRegister.group(3) + " " + matchRegister.group(6);
            System.out.println(request);
            return request;
        } else {
            request = "LOOKUP ";
            request += matchLookup.group(10);
            System.out.println(request);
            return request;
        }
    }
	
}