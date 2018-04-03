package Messages;

import Utils.Utils;

import java.util.regex.Matcher;

import static Utils.Utils.byteArrayConcat;

/**
 * Class representing a Putchunk Message
 */
public class PutchunkMsg extends Message implements msgGenerator {

    /**
     * Regex used to parse a String containing a putchunk message
     */
    private final static String REGEX_STRING =
            "\\s*?PUTCHUNK\\s+?(\\d\\.\\d)\\s+?(\\d+?)\\s+(([a-f0-9]){64})\\s+((\\d){1,6})\\s+(\\d)\\s+?\\r\\n\\r\\n";

    /**
     * The chunk number
     */
    private int chunkNum;

    /**
     * The message associated chunk
     */
    private byte[] chunk;

    /**
     * The desired replication degree for the given chunk
     */
    private int repDegree;

    /**
     * Putchunk message constructor
     *
     * @param header The message header
     * @param chunk The message chunk, in bytes
     */
    public PutchunkMsg(String header, byte[] chunk) {
        super(REGEX_STRING);
        Matcher protocolMatch = msgRegex.matcher(header);

        if (! protocolMatch.matches()) {
            Utils.showError("Failed to get a Regex match in received message", this.getClass());
            throw new ExceptionInInitializerError();
        }

        protocolVersion = Float.parseFloat(protocolMatch.group(VERSION_GROUP));
        senderID = Integer.parseInt(protocolMatch.group(SENDER_ID_GROUP));
        fileID = protocolMatch.group(FIELD_ID_GROUP);
        chunkNum = Integer.parseInt(protocolMatch.group(CHUNK_NUM_GROUP));
        repDegree = Integer.parseInt(protocolMatch.group(REP_DEGREE_GROUP));
        this.chunk = chunk;
    }

    /**
     * Putchunk Message constructor
     *
     * @param protocolVersion The communication protocol version
     * @param senderID The peer identifier that will send this message
     * @param fileID The file identifier
     * @param chunkNum The number of the chunk to be sent
     * @param repDegree The replication degree
     * @param chunk The chunk bytes
     */
    public PutchunkMsg(float protocolVersion, int senderID, String fileID, int chunkNum, int repDegree, byte[] chunk) {
        super(protocolVersion, senderID, fileID);
        this.chunkNum = chunkNum;
        this.repDegree = repDegree;
        this.chunk = chunk;
    }

    @Override
    public byte[] genMsg() {
         byte[] header = ("PUTCHUNK" + " " +
                protocolVersion + " " +
                senderID + " " +
                fileID + " " +
                chunkNum + " " +
                repDegree + " " +
                (char) ASCII_CR + (char) ASCII_LF +
                (char) ASCII_CR + (char) ASCII_LF).getBytes();
         return byteArrayConcat(header, chunk);
    }

    /**
     * Getter for the Chunk number
     *
     * @return the chunk number
     */
    public int getChunkNum() {
        return chunkNum;
    }

    /**
     * Getter for the replication degree
     *
     * @return the replication degree
     */
    public int getRepDegree() {
        return repDegree;
    }

    /**
     * Getter for the chunk bytes
     *
     * @return Array containing the chunk bytes
     */
    public byte[] getChunk() {
        return chunk;
    }
}
