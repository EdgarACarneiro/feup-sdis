package Action;

import Database.ChunksRecorder;
import Messages.SetTCPIP;
import Utils.FileManager;
import Utils.Utils;

import java.io.BufferedReader;
import java.io.Console;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

    /**
     * The destination IP
     */
    private int port;


    public SetTCPClient (BackedUpFiles backedUpFiles, int peerID, SetTCPIP message) {
        this.peerID = peerID;
        this.message = message;
        this.backedUpFiles = backedUpFiles;
        try {
            this.ipAddress = InetAddress.getByName(message.getIP());
        } catch (UnknownHostException e) {
            Utils.showError("Error parsing IP Address", SetTCPClient.class);
        }
        this.port = message.getPort();
    }

    @Override
    public void run() {
        try {
            System.out.println("Gonna receive from" + ipAddress + ":" + port);
            Socket socket = null;
            try {
                socket = new Socket(ipAddress, port);
            } catch (IOException ex) {
     
                System.out.println("I/O error: " + ex.getMessage());
            }
    
            InputStream input = null;
            OutputStream output = null;
    
            try {
                input = socket.getInputStream();
            } catch (IOException ex) {
                Utils.showError("Can't get socket input stream.", this.getClass());
            }
            
            try {
                output = new FileOutputStream("teste.png");
            } catch (FileNotFoundException ex) {
                Utils.showError("File not found. ", this.getClass());
            }
            
            byte[] bytes = new byte[64*1024];
    
            int count;
            while ((count = input.read(bytes)) > 0) {
                output.write(bytes, 0, count);
            }
    
            socket.close();
        } catch (IOException e) {
            Utils.showError("Failed to connect!", this.getClass());
        }
    }
}