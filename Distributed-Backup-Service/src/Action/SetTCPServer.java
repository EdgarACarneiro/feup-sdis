package Action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import java.util.ArrayList;

import Channel.ControlChannel;
import Database.ChunksRecorder;
import Messages.GetTCPIP;
import Messages.Message;
import Messages.SetTCPIP;
import Utils.FileManager;
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

    /**
     * Data Structure to get update after eliminating chunks, referent to the Peer stored files' chunks
     */
    private ChunksRecorder record;

    public SetTCPServer(ChunksRecorder record, ControlChannel controlChannel, int peerID, GetTCPIP message) {
        this.controlChannel = controlChannel;
        this.peerID = peerID;
        this.protocolVersion = message.getProtocolVersion();
        this.fileID = message.getFileID();
        this.record = record;
    }

    @Override
    public void run() {

        ArrayList<Integer> chunks = record.getChunksList(fileID);

        if (chunks == null)
            return;

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
                Socket socket = null;

                try {
                    socket = serverSocket.accept();                   
                } catch (IOException ex) {
                    Utils.showError("Can't accept client connection. ", this.getClass());
                }
                System.out.println("New client connected");

                File[] backupFiles = FileManager.getPeerBackups(peerID);
                if (backupFiles == null)
                    Utils.showError("Failed to get Peer backup files", this.getClass());

                InputStream input = null;
                OutputStream output = null;

                try {
                    output = socket.getOutputStream();
                } catch (FileNotFoundException e) {
                    Utils.showError("Server exception: " + e.getMessage(), this.getClass());
                }
  
                for (File backupFile : backupFiles) {
                    
                    if (backupFile.isDirectory() && backupFile.getName().equals(fileID)) {
                        
                        for (File chunkFile : backupFile.listFiles()) {

                            try {
                                input = new FileInputStream(chunkFile); 
                            } catch (IOException ex) {
                                Utils.showError("Can't get socket input stream.", this.getClass());
                            }

                            byte[] bytes = new byte[64*1024];

                            int count;
                            while ((count = input.read(bytes)) > 0) {
                                output.write(bytes, 0, count);
                            }
                        }
                    }
                } 

                socket.close();
            }
 
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
