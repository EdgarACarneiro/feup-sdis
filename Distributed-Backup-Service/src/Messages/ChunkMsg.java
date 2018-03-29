package Messages;

import Utils.Utils;

import java.util.regex.Matcher;

import static Utils.Utils.byteArrayConcat;

public class ChunkMsg extends Message implements msgGenerator {

    /**
     * Regex used to parse a String containing a 'chunk' message
     */
    private final static String REGEX_STRING =
            "\\s*?CHUNK\\s+?(\\d\\.\\d)\\s+?(\\d+?)\\s+(([a-f0-9]){64})\\s+((\\d){1,6})\\s+?\\r\\n\\r\\n";

    /**
     * The chunk number
     */
    private int chunkNum;

    /**
     * The message associated chunk
     */
    private byte[] chunk;


    public ChunkMsg(String receivedMsg) {
        super(REGEX_STRING);
        Matcher protocolMatch = msgRegex.matcher(receivedMsg);

        if (! protocolMatch.find()) {
            Utils.showError("Failed to get a Regex match in received message", this.getClass());
            throw new ExceptionInInitializerError();
        }
        String header = protocolMatch.group();

        protocolVersion = Float.parseFloat(protocolMatch.group(VERSION_GROUP));
        senderID = Integer.parseInt(protocolMatch.group(SENDER_ID_GROUP));
        fileID = protocolMatch.group(FIELD_ID_GROUP);
        chunkNum = Integer.parseInt(protocolMatch.group(CHUNK_NUM_GROUP));
        chunk = receivedMsg.substring(header.length(), receivedMsg.length()).getBytes();
    }

    public ChunkMsg(float protocolVersion, int senderID, String fileID, int chunkNum, byte[] chunk) {
        super(protocolVersion, senderID, fileID);
        this.chunkNum = chunkNum;
        this.chunk = chunk;
    }

    @Override
    public byte[] genMsg() {
        byte[] header = ("PUTCHUNK" + " " +
                protocolVersion + " " +
                senderID + " " +
                fileID + " " +
                chunkNum + " " +
                (char) ASCII_CR + (char) ASCII_LF +
                (char) ASCII_CR + (char) ASCII_LF).getBytes();
        return byteArrayConcat(header, chunk);
    }

    public int getChunkNum() {
        return chunkNum;
    }

    public byte[] getChunk() {
        return chunk;
    }
}
