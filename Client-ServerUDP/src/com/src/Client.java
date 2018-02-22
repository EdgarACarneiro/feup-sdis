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
     * Maximum size of a reply sent by the Server
     */
    private static final int MAX_REPLY_SIZE = 274;

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
        System.out.println("New Client created!");
        String parsedMsg = buildRequest(msg);

        if (parsedMsg != null) {
            byte[] request = parsedMsg.getBytes();
            InetAddress address = InetAddress.getByName(host);

            DatagramPacket packet = new DatagramPacket(request, request.length, address, port);
            socket.send(packet);

            getServerResult(socket);
        } else {
            showErrorReply(msg);
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
            request += matchLookup.group(2);
            System.out.println(request);
            return request;
        }
    }

    /**
     * Show the User the result of a failed attempt communicate
     *
     * @param sentMsg Message sent to the server
     */
    private void showErrorReply(String sentMsg) {
        System.out.println("Client: Received Reply: " +  sentMsg + ": ERROR");
    }

    /**
     * Show the User the result of the communication with the Server
     *
     * @param reply Server's reply
     * @param args Arguments used in the Server request
     */
    private void showReply(String reply, String args) {
        System.out.println("Client: Received Reply: " + args + ": " + reply);
    }

    /**
     * Get the server's response from the Client's request
     *
     * @param socket socket used for the communication
     * @throws IOException
     */
    private void getServerResult(DatagramSocket socket) throws IOException {
        byte[] serverAnswer = new byte[MAX_REPLY_SIZE];
        DatagramPacket packet = new DatagramPacket(serverAnswer, serverAnswer.length);

        socket.receive(packet);
        String received = new String(packet.getData());

        String[] groups=  received.split(" ");
        String result = groups[0];
        String args = received.substring(result.length() + 1);

        showReply(result, args);
    }
	
}
