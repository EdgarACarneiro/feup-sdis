package Action;

import Database.ChunksRecorder;
import Messages.SetTCPIP;
import Utils.FileManager;
import Utils.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
import java.util.ArrayList;

public class SetTCPClientV2 extends Action {

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


    public SetTCPClientV2 (BackedUpFiles backedUpFiles, int peerID, SetTCPIP message) {
        this.peerID = peerID;
        this.message = message;
        this.backedUpFiles = backedUpFiles;
        try {
            this.ipAddress = InetAddress.getByName(message.getIP());
        } catch (UnknownHostException e) {
            Utils.showError("Error parsing IP Address", SetTCPClientV2.class);
        }
        this.port = message.getPort();
    }

    @Override
    public void run() {
        BufferedOutputStream bos;
        OutputStream output;
        DataOutputStream dos;
        int len;
        int smblen; 
        InputStream in;
        boolean flag=true;
        DataInputStream clientData;

        try {
            Utils.log("Gonna receive from" + ipAddress + ":" + port);
            Socket socket = null;
            
            try {
                socket = new Socket(ipAddress, port);
            } catch (IOException ex) {
     
                Utils.log("I/O error: " + ex.getMessage());
            }

            while (true){
                    
                in = socket.getInputStream(); //used  
                clientData = new DataInputStream(in); //use 
                
                Utils.log("Starting...");  
                    
                int fileSize = clientData.read();
                    
                ArrayList<File>files=new ArrayList<File>(fileSize); //store list of filename from client directory
                ArrayList<Integer>sizes = new ArrayList<Integer>(fileSize); //store file size from client
                //Start to accept those filename from server
                for (int count=0;count < fileSize;count ++){
                    File ff=new File(clientData.readUTF());
                    files.add(ff);
                }
                    
                for (int count=0;count < fileSize;count ++){
                        
                    sizes.add(clientData.readInt());
                }
                    
                for (int count =0;count < fileSize ;count ++){  
                    
                    if (fileSize - count == 1){
                        flag =false;
                    }
        
                    len=sizes.get(count);
                            
                Utils.log("File Size =" + len);
                
                output = new FileOutputStream(files.get(count));
                dos = new DataOutputStream(output);
                bos = new BufferedOutputStream(output);
                
                byte[] buffer = new byte[1024];  
                    
                bos.write(buffer, 0, buffer.length); //This line is important
                    
                while (len > 0 && (smblen = clientData.read(buffer)) > 0) { 
                    dos.write(buffer, 0, smblen); 
                        len = len - smblen;
                        dos.flush();
                    }  
                    dos.close();  //It should close to avoid continue deploy by resource under view
                }          
            } 
    
        } catch (IOException e) {
            Utils.showError("Failed to connect!", this.getClass());
        }
    }
}