package Messages;

import Utils.Utils;

import java.util.regex.Matcher;

public class CommonMsg extends Message {

    /**
     * The chunk number
     */
    protected int chunkNum;

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

    protected CommonMsg(float protocolVersion, int senderID, String fileID, int chunkNum) {
        super(protocolVersion, senderID, fileID);
        this.chunkNum = chunkNum;
    }

    public int getChunkNum() {
        return chunkNum;
    }
}
