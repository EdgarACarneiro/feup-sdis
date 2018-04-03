package Action;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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

public class SetTCPServerV2 extends Action {
    
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

    public SetTCPServerV2(ChunksRecorder record, ControlChannel controlChannel, int peerID, GetTCPIP message) {
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
            Utils.showError("Invalid IP Address", SetTCPServerV2.class);
        }

        try (ServerSocket serverSocket = new ServerSocket(0)) {

            int port = serverSocket.getLocalPort();
            
            Utils.log("Server is on IP " + ipAddress);

            Utils.log("Server is listening on port " + port);

            try {
                controlChannel.sendMessage(
                        new SetTCPIP(protocolVersion, peerID, fileID, ipAddress, port).genMsg()
                );
            } catch (ExceptionInInitializerError e) {
                Utils.showWarning("Failed to build message. Proceeding for other messages.", this.getClass());
            }
            
            Socket socket = null;
            
            try {
                socket = serverSocket.accept();                   
            } catch (IOException ex) {
                Utils.showError("Can't accept client connection. ", this.getClass());
            }
            Utils.log("New client connected");

            File[] backupFiles = FileManager.getPeerBackups(peerID);
            if (backupFiles == null)
                Utils.showError("Failed to get Peer backup files", this.getClass());

            try {    
                OutputStream os = socket.getOutputStream();  
                DataOutputStream dos = new DataOutputStream(os); 
                
                for (File backupFile : backupFiles) {
                    
                    if (backupFile.isDirectory() && backupFile.getName().equals(fileID)) {
                        dos.writeInt(backupFiles.length);
                        
                        for (File chunkFile : backupFile.listFiles()) {
                            dos.writeUTF(chunkFile.getName());
                        }

                        for (File chunkFile : backupFile.listFiles()) {
                            int filesize = (int) chunkFile.length();
                            dos.writeInt(filesize);
                        }

                        for (File chunkFile : backupFile.listFiles()) {
                            int filesize = (int) chunkFile.length();
                            byte[] buffer = new byte [filesize];
                                
                            //FileInputStream fis = new FileInputStream(myFile);  
                            FileInputStream fis = new FileInputStream(chunkFile);  
                            BufferedInputStream bis = new BufferedInputStream(fis);  
                        
                            //Sending file name and file size to the server  
                            bis.read(buffer, 0, buffer.length); //This line is important
                            
                            dos.write(buffer, 0, buffer.length);   
                            dos.flush(); 
                            //dos.close();
                        }
                    }
                }

                socket.close();
                
            } catch (IOException ex) {
                Utils.log("Server exception: " + ex.getMessage());
                ex.printStackTrace();
            } 
        } catch (IOException ex) {
            Utils.log("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
