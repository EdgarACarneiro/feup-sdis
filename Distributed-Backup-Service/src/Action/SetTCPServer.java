package Action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

import Channel.ControlChannel;
import Messages.GetTCPIP;
import Messages.Message;
import Messages.SetTCPIP;
import Utils.Utils;

public class SetTCPServer extends Action {
    
    /**
     * The channel used to communicate with other peers, regarding restore information
     */
    private ControlChannel controlChannel;

    /**
     * The sender peer ID
     */
    private int peerID;

    /**
     * The file identifier for the file to be backed up
     */
    private String fileID;

    /**
     * Protocol Version in the communication
     */
    private float protocolVersion;

    private String ipAddress;

    public SetTCPServer(ControlChannel controlChannel, int peerID, GetTCPIP message) {
        this.controlChannel = controlChannel;
        this.peerID = peerID;
        this.protocolVersion = message.getProtocolVersion();
        this.fileID = message.getFileID();
    }

    @Override
    public void run() {
        try {
            ipAddress = InetAddress.getLocalHost().getHostAddress();            
        } catch (UnknownHostException e) {
            Utils.showError("Invalid IP Address", SetTCPServer.class);
        }

        try (ServerSocket serverSocket = new ServerSocket(0)) {

            int port = serverSocket.getLocalPort();
            
            System.out.println("Server is on IP " + ipAddress);

            System.out.println("Server is listening on port " + port);

            try {
                controlChannel.sendMessage(
                        new SetTCPIP(protocolVersion, peerID, fileID, ipAddress, port).genMsg()
                );
            } catch (ExceptionInInitializerError e) {
                Utils.showWarning("Failed to build message. Proceeding for other messages.", this.getClass());
            }
 
 
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
 
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
 
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
 
 
                String text;
 
                do {
                    text = reader.readLine();
                    String reverseText = new StringBuilder(text).reverse().toString();
                    writer.println("Server: " + reverseText);
 
                } while (!text.equals("bye"));
 
                socket.close();
            }
 
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
