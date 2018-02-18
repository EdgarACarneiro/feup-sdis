package com.src;

import java.io.IOException;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class responsible for sending a given message to the Server
 */
public class Client {

    /**
     * Regex's pattern used for analysis of client register requests
     */
    private static Pattern registerRequest = Pattern.compile("\\s*?((register)\\s+?((([A-Z]|[0-9]){2}-?){3})\\s+?(((\\w|\\s)+?){1,256}))$");

    /**
     * Regex's pattern used for analysis of client lookup requests
     */
    private static Pattern lookupRequest = Pattern.compile("\\s*?(lookup)\\s+?((([A-Z]|[0-9]){2}-?){3})\\s*?");

    /**
     * Client constructor. Sends the created request to the Server.
     *
     * @param socket The socket to send the request to
     * @param host The host to communicate with the Server
     * @param port The port to communicate with the Server
     * @param msg The request to send to the Server
     * @throws IOException
     */
    public Client(DatagramSocket socket, String host, int port, String msg) throws IOException {
        String parsedMsg = buildRequest(msg);

        if (parsedMsg != null) {
            byte[] request = parsedMsg.getBytes();
            InetAddress address = InetAddress.getByName(host);

            DatagramPacket packet = new DatagramPacket(request, request.length, address, port);
            socket.send(packet);

            showReply(parsedMsg, getServerResult(socket));

        } else {
            showReply(msg, "ERROR");
        }

    }

    /**
     * Build the String request to be sent to the Server
     *
     * @param msg String to build the Client request
     * @return String request to be sent to the Server
     */
    private String buildRequest(String msg) {
        Matcher matchRegister = registerRequest.matcher(msg);
        Matcher matchLookup = lookupRequest.matcher(msg);

        if ((! matchRegister.matches()) && (! matchLookup.matches())) {
            System.out.println("Client request should be of type:\n" +
                    " * 'register XX-XX-XX <owner name>', for register;\n" +
                    " * 'lookup XX-XX-XX', for lookup;'\n" +
                    "where XX-XX-XX is the license plate.");
            return null;
        }

        String request;
        if (matchRegister.matches()) {
            if (matchRegister.group(6).length() > 256) {
                System.out.println("Error: Owner name must not have more than 256 characters.");
                return null;
            }
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

    private void showReply(String sentMsg, String answer) {
        System.out.println(sentMsg + ": " + answer);
    }

    private String getServerResult(DatagramSocket socket) throws IOException {
        byte[] serverAnswer = new byte[100];
        DatagramPacket packet = new DatagramPacket(serverAnswer, serverAnswer.length);
        socket.receive(packet);
        
        //TODO: PArsing of server result with REGEX here maybe
        return new String(packet.getData());
    }
	
}