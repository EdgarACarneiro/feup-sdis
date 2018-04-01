package Messages;

import Utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;

import static Utils.Utils.byteArrayConcat;

public class SetTCPIP extends Message implements msgGenerator {

    /**
     * Regex used to parse a String containing a 'chunk' message
     */
    private final static String REGEX_STRING =
            "\\s*?SETTCPIP\\s+?(\\d\\.\\d)\\s+?(\\d+?)\\s+(([a-f0-9]){64})\\s+((\\d){1,6})\\s+?\\r\\n\\r\\n";

    /**
     * The machine's IP address
     */
    private String ipAddress;

    public SetTCPIP(String header, byte[] chunk) {
        super(REGEX_STRING);
        Matcher protocolMatch = msgRegex.matcher(header);

        if (!protocolMatch.matches()) {
            Utils.showError("Failed to get a Regex match in received message", this.getClass());
            throw new ExceptionInInitializerError();
        }
        URL whatismyip; 
        BufferedReader in;
        try {
            whatismyip = new URL("http://checkip.amazonaws.com");
            in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
            ipAddress = in.readLine(); 
        } catch (MalformedURLException e) {
            Utils.showError("Failed to get IP Address", SetTCPIP.class);
        } catch (IOException e) {
            Utils.showError("Failed to get IP Address", SetTCPIP.class);
        }

        System.out.println(ipAddress);
    }

    public SetTCPIP(float protocolVersion, int senderID, String fileID) {
        super(protocolVersion, senderID, fileID);
    }

    @Override
    public byte[] genMsg() {
        return ("SetTCPIP" + 
                ipAddress + 
                (char) ASCII_CR + (char) ASCII_LF +
                (char) ASCII_CR + (char) ASCII_LF).getBytes();
    }
}
