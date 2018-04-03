package Database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Class that holds the records for all the chunks stored in this Peer
 */
public class ChunksRecorder implements Serializable {

    /**
     * Class representing all the info stored regarding a chunk
     */
    public class ChunkInfo implements Serializable {

        /**
         * The initial replication degree
         */
        public int repDegree = 0;

        /**
         * The size of the chunk
         */
        public int chunkSize;

        /**
         * The peers that have this chunk also stored, not including the self
         */
        private ArrayList<Integer> peersStored = new ArrayList<>();

        /**
         * Chunk Info constructor
         *
         * @param chunkSize the chunk's size, in bytes
         */
        public ChunkInfo(Integer chunkSize) {
            this.chunkSize = chunkSize;
        }
    }

    /**
     * Maximum disk space a peer can have. When there is no limit.
     */
    private final int INFINITE_SPACE = -1;

    /**
     * Maximum Disk Space used. If equal to -1 means there is no limit
     */
    private AtomicLong maxDiskSpace = new AtomicLong();

    /**
     * Disk Space being used so far to store all the peers
     */
    private AtomicLong usedDiskSpace = new AtomicLong();

    /**
     * The hashMap used for keeping information about the chunks that were saved for each file (fileID).
     * Maps a file identifier into a chunk number, that than is mapped into a perceived replication degree.
     */
    private ConcurrentHashMap<String, ConcurrentHashMap<Integer, ChunkInfo> > chunksRecord = new ConcurrentHashMap<>();

    /**
     * The List used for keeping information about which files were deleted
     */
    private CopyOnWriteArrayList<String> deletedFiles = new CopyOnWriteArrayList<>();

    /**
     * A hashMap indicating for each file its desired replication degree
     */
    private ConcurrentHashMap<String, Integer> filesDesiredRD = new ConcurrentHashMap<>();

    /**
     * Default ChunksRecorder Constructor
     */
    public ChunksRecorder() {
        maxDiskSpace.set(INFINITE_SPACE);
        usedDiskSpace.set(0);
    }

    /**
     * Add a chunk record (meaning a chunk was stored) to the database, with replication degree already as 1
     *
     * @param fileID The file identifier
     * @param chunkNum The chunk numeration
     * @param chunkSize The chunk size
     * @param desiredRD The desired replication degree
     * @return True if the file was successfully added
     */
    public boolean addChunkRecord(String fileID, Integer chunkNum, Integer chunkSize, Integer desiredRD) {
        return (initChunkRecord(fileID, chunkNum, chunkSize, desiredRD) && setExistingChunk(fileID, chunkNum));
    }

    /**
     * Initialize a chunk record in the database, with replication degree as 0
     *
     * @param fileID The file identifier
     * @param chunkNum The chunk numeration
     * @param chunkSize The chunk size
     * @param desiredRD The desired replication degree
     * @return True if the file was successfully added
     */
    public boolean initChunkRecord(String fileID, Integer chunkNum, Integer chunkSize, Integer desiredRD) {
        ConcurrentHashMap<Integer, ChunkInfo> record = chunksRecord.get(fileID);
        ChunkInfo chunk = new ChunkInfo(chunkSize);

        if (record == null) {

            // No disk space to back up
            if (maxDiskSpace.longValue() != INFINITE_SPACE && (usedDiskSpace.get() + chunkSize) > maxDiskSpace.longValue())
                return false;

            usedDiskSpace.set(usedDiskSpace.longValue() + chunkSize);

            ConcurrentHashMap<Integer, ChunkInfo> newEntry = new ConcurrentHashMap<>();
            newEntry.put(chunkNum, chunk);
            chunksRecord.put(fileID, newEntry);
        }
        else if (! record.containsKey(chunkNum)) {
            record.put(chunkNum, chunk);
            usedDiskSpace.set(usedDiskSpace.longValue() + chunkSize);
        }

        return true;
    }

    /**
     * Sets the value of the replication degree of an existing chunk to 1
     *
     * @param fileID The file identifier
     * @param chunkNum the chunk numeration
     * @return True if the operation succeeded
     */
    private boolean setExistingChunk(String fileID, Integer chunkNum) {
        ChunkInfo chunk = chunksRecord.get(fileID).get(chunkNum);

        if (chunk == null)
            return false;

        chunk.repDegree = 1;
        return true;
    }

    /**
     * Increment the replication degree of a given chunk
     *
     * @param fileID The file identifier
     * @param chunkNum The chunk numeration
     * @param senderID The identifier of the peer who stored the chunk
     * @return True if the replication degree was updated correctly
     */
    public boolean incChunkRecord(String fileID, Integer chunkNum, Integer senderID) {
        return updateChunkRecord(fileID, chunkNum, senderID, 1);
    }

    /**
     * Decrement the replication degree of a given chunk
     *
     * @param fileID The file identifier
     * @param chunkNum The chunk numeration
     * @param senderID The identifier of the peer who removed the the chunk
     * @return True if the replication degree was updated correctly
     */
    public boolean decChunkRecord(String fileID, Integer chunkNum, Integer senderID) {
        return updateChunkRecord(fileID, chunkNum, senderID, -1);
    }

    /**
     * Update the replication degree of a given chunk
     *
     * @param fileID The file identifier
     * @param chunkNum The chunk numeration
     * @param senderID The identifier of the peer who stored/removed the file
     * @param change The change to be added to the replication degree tp update it
     * @return True if the replication degree was updated correctly
     */
    private boolean updateChunkRecord(String fileID, Integer chunkNum, Integer senderID, Integer change) {
        ConcurrentHashMap<Integer, ChunkInfo> record = chunksRecord.get(fileID);

        if (record != null && record.containsKey(chunkNum)) {
            ChunkInfo chunk = record.get(chunkNum);
            boolean hasPeer = chunk.peersStored.contains(senderID);

            if (! hasPeer && (change > 0)) {
                chunk.repDegree += change;
                chunk.peersStored.add(senderID);
            }

            if (hasPeer && (change < 0)) {
                chunk.repDegree += change;
                chunk.peersStored.remove(senderID);
            }
            return true;
        }
        return false;
    }

    /**
     * Check whether the replication degree of a given chunk is acceptable (balanced)
     *
     * @param fileID the file identifier
     * @param chunkNum The chunk numeration
     * @return True if it is balanced
     */
    public boolean isRDBalanced(String fileID, int chunkNum) {
        ConcurrentHashMap<Integer, ChunkInfo> record = chunksRecord.get(fileID);

        if (record != null && record.containsKey(chunkNum)) {
            if (record.get(chunkNum).repDegree < filesDesiredRD.get(fileID))
                return false;
        }
        return true;
    }

    /**
     * Getter for the desired replication degree of a given file
     *
     * @param fileID The file identifier
     * @return The desired replication degree
     */
    public Integer getFileDesiredRD(String fileID) {
        return filesDesiredRD.get(fileID);
    }

    /**
     * Getter for the perceived replication degree of a given chunk in a given file
     *
     * @param fileID The file identifier
     * @param chunkNum The chunk numeration
     * @return Return the chunk replication degree if it exists, otherwise returns 0
     */
    public Integer getChunkRD(String fileID, int chunkNum) {
        ConcurrentHashMap<Integer, ChunkInfo> storedChunks= chunksRecord.get(fileID);

        if (storedChunks == null)
            return 0;

        return (storedChunks.containsKey(chunkNum) ? storedChunks.get(chunkNum).repDegree : 0);
    }

    /**
     * Getter for all the chunks stored from a given file
     *
     * @param fileID The file identifier
     * @return List containing the numeration of the chunks stored in the disk
     */
    public ArrayList<Integer> getChunksList(String fileID) {
        ConcurrentHashMap<Integer, ChunkInfo> storedChunks= chunksRecord.get(fileID);

        if (storedChunks == null)
            return null;

        ArrayList<Integer> results = new ArrayList<>();
        results.addAll(storedChunks.keySet());
        return results;
    }

    /**
     * Verifies if the given chunk, from the given file, is part of the database regarding the stored chunks in the disk
     *
     * @param fileID The file identifier
     * @param chunkNum The chunk number
     * @return True if the file is stored, false otherwise
     */
    public boolean hasChunk(String fileID, Integer chunkNum) {
        ConcurrentHashMap<Integer, ChunkInfo> storedChunks = chunksRecord.get(fileID);

        return (storedChunks != null && (storedChunks.containsKey(chunkNum)) );
    }

    /**
     * Remove the the information from the chunks belonging to the hash table from the file
     *
     * @param fileID The file identifier
     */
    public void removeFile(String fileID) {
        if (chunksRecord.containsKey(fileID)) {
            chunksRecord.remove(fileID);
            deletedFiles.add(fileID);
        }
    }

    /**
     * Checks if a file was deleted from the database
     *
     * @param fileID The file identifier
     * @return True if the file is no longer on the database
     */
    public boolean wasDeleted(String fileID) {
        return deletedFiles.contains(fileID);
    }

    /**
     * Update the maximum disk space to the given values
     *
     * @param maxDiskSpace maximum disk space
     */
    public void updateMaxSpace(long maxDiskSpace) {
        this.maxDiskSpace.set(maxDiskSpace);
    }

    /**
     * Remove the given chunk from the database
     *
     * @param fileID The file identifier
     * @param chunkNum The chunk numeration
     */
    public void removeChunk(String fileID, Integer chunkNum) {
        ConcurrentHashMap<Integer, ChunkInfo> storedChunks = chunksRecord.get(fileID);

        if (storedChunks == null)
            return;

        storedChunks.remove(chunkNum);
    }

    /**
     * Getter for the size of a given chunk
     *
     * @param fileID The file identifier
     * @param chunkNum the chunk numeration
     * @return The chunk's size
     */
    public long getChunkSize(String fileID, Integer chunkNum) {
        ConcurrentHashMap<Integer, ChunkInfo> storedChunks = chunksRecord.get(fileID);
        if (storedChunks == null)
            return -1;

        ChunkInfo info = storedChunks.get(chunkNum);
        if (info == null)
            return -1;

        return info.chunkSize;
    }

    /**
     * Getter for the used disk space by the chunks / file storage
     *
     * @return The space used
     */
    public long getUsedDiskSpace() {
        return usedDiskSpace.longValue();
    }

    @Override
    public String toString() {
        return "ChunksRecorder{" +
                " maxDiskSpace=" + maxDiskSpace +
                ", usedDiskSpace=" + usedDiskSpace +
                ", chunksRecord=" + chunksRecord +
                ", deletedFiles=" + deletedFiles +
                ", filesDesiredRD=" + filesDesiredRD +
                '}' + '\n';
    }
}
