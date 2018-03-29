package Messages;

import Utils.FileManager;
import Utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.regex.Matcher;

public class DeleteMsg extends Message implements msgGenerator {

    /**
     * Regex used to parse a String containing a 'remove' message
     */
    private final static String REGEX_STRING =
            "\\s*?DELETE\\s+?(\\d\\.\\d)\\s+?(\\d+?)\\s+(([a-f0-9]){64})\\s+?\\r\\n\\r\\n";

    public DeleteMsg(String receivedMsg) {
        super(REGEX_STRING);
        Matcher protocolMatch = msgRegex.matcher(receivedMsg);

        if (! protocolMatch.matches()) {
            Utils.showError("Failed to get a Regex match in received message", this.getClass());
            throw new ExceptionInInitializerError();
        }

        protocolVersion = Float.parseFloat(protocolMatch.group(VERSION_GROUP));
        senderID = Integer.parseInt(protocolMatch.group(SENDER_ID_GROUP));
        fileID = protocolMatch.group(FIELD_ID_GROUP);

        File dir = new File(System.getProperty("user.dir"));
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if (child.isDirectory())
                    if (fileID == child.getName())
                        try {
                            Files.deleteIfExists(Paths.get(child.getPath()));
                        } catch (NoSuchFileException e) {
                            System.err.println("No such file/directory exists");
                        } catch (DirectoryNotEmptyException e) {
                            System.err.println("Directory is not empty.");
                        } catch (IOException e) {
                            System.err.println("Invalid permissions.");
                        }
            }
        }
    }

    public DeleteMsg(float protocolVersion, int senderID, String fileID) {
        super(protocolVersion, senderID, fileID);
    }

    @Override
    public byte[] genMsg() {
        return ("DELETE" + " " +
                protocolVersion + " " +
                senderID + " " +
                fileID + " " +
                (char) ASCII_CR + (char) ASCII_LF +
                (char) ASCII_CR + (char) ASCII_LF).getBytes();
    }
}
