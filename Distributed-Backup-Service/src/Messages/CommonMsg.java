package Messages;

import Utils.Utils;

import java.util.regex.Matcher;

/**
 * Class representing the mos common type of messages sent / received
 */
public class CommonMsg extends Message {

    /**
     * The number of the chunk in the message
     */
    protected int chunkNum;

    /**
     * Common message constructor
     *
     * @param receivedMsg The message received
     * @param regex The regex to be used to parse the message
     */
    protected CommonMsg(String receivedMsg, String regex) {
        super(regex);
        Matcher protocolMatch = msgRegex.matcher(receivedMsg);

        if (! protocolMatch.matches()) {
            Utils.showError("Failed to get a Regex match in received message", this.getClass());
            throw new ExceptionInInitializerError();
        }

        protocolVersion = Float.parseFloat(protocolMatch.group(VERSION_GROUP));
        senderID = Integer.parseInt(protocolMatch.group(SENDER_ID_GROUP));
        fileID = protocolMatch.group(FIELD_ID_GROUP);
        chunkNum = Integer.parseInt(protocolMatch.group(CHUNK_NUM_GROUP));
    }

    /**
     * Common Message constructor
     *
     * @param protocolVersion The communication protocol version
     * @param senderID The peer identifier that will send this message
     * @param fileID The file identifier
     * @param chunkNum The number of the chunk to be sent
     */
    protected CommonMsg(float protocolVersion, int senderID, String fileID, int chunkNum) {
        super(protocolVersion, senderID, fileID);
        this.chunkNum = chunkNum;
    }

    /**
     * Getter for the number of the chunk associated to the message
     *
     * @return The chunk's number
     */
    public int getChunkNum() {
        return chunkNum;
    }
}
