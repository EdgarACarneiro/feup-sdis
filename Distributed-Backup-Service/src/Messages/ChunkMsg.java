package Messages;

import Utils.Utils;

import java.util.regex.Matcher;

import static Utils.Utils.byteArrayConcat;

/**
 * Class representing a chunk message
 */
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

    /**
     * The chunk message constructor
     *
     * @param header The header of the message
     * @param chunk The chunk associated to the message, in bytes
     */
    public ChunkMsg(String header, byte[] chunk) {
        super(REGEX_STRING);
        Matcher protocolMatch = msgRegex.matcher(header);

        if (!protocolMatch.matches()) {
            Utils.showError("Failed to get a Regex match in received message", this.getClass());
            throw new ExceptionInInitializerError();
        }

        protocolVersion = Float.parseFloat(protocolMatch.group(VERSION_GROUP));
        senderID = Integer.parseInt(protocolMatch.group(SENDER_ID_GROUP));
        fileID = protocolMatch.group(FIELD_ID_GROUP);
        chunkNum = Integer.parseInt(protocolMatch.group(CHUNK_NUM_GROUP));
        this.chunk = chunk;
    }

    /**
     * Chunk message constructor
     *
     * @param protocolVersion The communication protocol version
     * @param senderID The peer identifier that will send this message
     * @param fileID The file identifier
     * @param chunkNum The number of the chunk to be sent
     * @param chunk The chunk to associate to the chunk message
     */
    public ChunkMsg(float protocolVersion, int senderID, String fileID, int chunkNum, byte[] chunk) {
        super(protocolVersion, senderID, fileID);
        this.chunkNum = chunkNum;
        this.chunk = chunk;
    }

    @Override
    public byte[] genMsg() {
        byte[] header = ("CHUNK" + " " +
                protocolVersion + " " +
                senderID + " " +
                fileID + " " +
                chunkNum + " " +
                (char) ASCII_CR + (char) ASCII_LF +
                (char) ASCII_CR + (char) ASCII_LF).getBytes();
        return byteArrayConcat(header, chunk);
    }

    /**
     * Getter for the chunk number
     *
     * @return the chunk number
     */
    public int getChunkNum() {
        return chunkNum;
    }

    /**
     * Getter for the chunk associated to the message
     *
     * @return the chunk
     */
    public byte[] getChunk() {
        return chunk;
    }
}
