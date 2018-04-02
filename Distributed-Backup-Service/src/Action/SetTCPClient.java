package Action;

import Main.ChunksRecorder;
import Messages.SetTCPIP;
import Utils.Utils;

import java.io.BufferedReader;
import java.io.Console;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import Database.BackedUpFiles;

public class SetTCPClient extends Action {

    /**
     * Maximum time waited to trigger the Store Action, exclusively.
     */
    private final static int MAX_TIME_TO_SEND = 4000;

    /**
     * The putchunk message that triggered this action
     */
    private SetTCPIP message;
    
    /**
     * Data Structure where backed up files are located
     */
    private BackedUpFiles backedUpFiles;

    /**
     * The identifier of the Peer associated to this action
     */
    private int peerID;

    /**
     * The destination IP
     */
    private InetAddress ipAddress;


    public SetTCPClient (BackedUpFiles backedUpFiles, int peerID, SetTCPIP message) {
        this.peerID = peerID;
        this.message = message;
        this.backedUpFiles = backedUpFiles;
        try {
            this.ipAddress = InetAddress.getByName(message.getIP());
        } catch (UnknownHostException e) {
            Utils.showError("Error parsing IP Address", SetTCPClient.class);
        }
    }

    @Override
    public void run() {
 
        int port = 9090;
 
        try (Socket socket = new Socket(ipAddress, port)) {
 
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
 
            Console console = System.console();
            String text;
 
            do {
                text = console.readLine("Enter text: ");
 
                writer.println(text);
 
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
 
                String time = reader.readLine();
 
                System.out.println(time);
 
            } while (!text.equals("bye"));
 
            socket.close();
 
        } catch (IOException ex) {
 
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}