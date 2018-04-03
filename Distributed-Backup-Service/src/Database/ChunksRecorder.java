package Database;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Class that holds the records for all the chunks stored in this Peer
 */
public class ChunksRecorder implements Serializable {

    public class ChunkInfo implements Serializable {

        public int repDegree = 0;

        public int chunkSize;

        // Size will always be inferior in 1 to the repDegree, because it does not count with the self
        private ArrayList<Integer> peersStored = new ArrayList<>();

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

    private CopyOnWriteArrayList<String> deletedFiles = new CopyOnWriteArrayList<>();

    private ConcurrentHashMap<String, Integer> filesDesiredRD = new ConcurrentHashMap<>();

    /**
     * Default ChunksRecorder Constructor
     */
    public ChunksRecorder() {
        maxDiskSpace.set(INFINITE_SPACE);
        usedDiskSpace.set(0);
    }

    public boolean addChunkRecord(String fileID, Integer chunkNum, Integer chunkSize, Integer desiredRD) {
        return (initChunkRecord(fileID, chunkNum, chunkSize, desiredRD) && setExistingChunk(fileID, chunkNum));
    }

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

            Utils.log(fileID, desiredRD);
        }
        else if (! record.containsKey(chunkNum)) {
            record.put(chunkNum, chunk);
            usedDiskSpace.set(usedDiskSpace.longValue() + chunkSize);
        }

        return true;
    }

    private boolean setExistingChunk(String fileID, Integer chunkNum) {
        ChunkInfo chunk = chunksRecord.get(fileID).get(chunkNum);

        if (chunk == null)
            return false;

        chunk.repDegree = 1;
        return true;
    }

    public boolean incChunkRecord(String fileID, Integer chunkNum, Integer senderID) {
        return updateChunkRecord(fileID, chunkNum, senderID, 1);
    }

    public boolean decChunkRecord(String fileID, Integer chunkNum, Integer senderID) {
        return updateChunkRecord(fileID, chunkNum, senderID, -1);
    }

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

    public boolean isRDBalanced(String fileID, int chunkNum) {
        ConcurrentHashMap<Integer, ChunkInfo> record = chunksRecord.get(fileID);

        if (record != null && record.containsKey(chunkNum)) {
            if (record.get(chunkNum).repDegree < filesDesiredRD.get(fileID))
                return false;
        }
        return true;
    }

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

    public void removeChunk(String fileID, Integer chunkNum) {
        ConcurrentHashMap<Integer, ChunkInfo> storedChunks = chunksRecord.get(fileID);

        if (storedChunks == null)
            return;

        storedChunks.remove(chunkNum);
    }

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
