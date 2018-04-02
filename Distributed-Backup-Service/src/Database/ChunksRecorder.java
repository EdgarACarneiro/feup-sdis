package Database;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Class that holds the records for all the chunks stored in this Peer
 */
public class ChunksRecorder {

    public class ChunkInfo {

        public int repDegree = 1;

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

    /**
     * Default ChunksRecorder Constructor
     */
    public ChunksRecorder() {
        maxDiskSpace.set(INFINITE_SPACE);
        usedDiskSpace.set(0);
    }

    public boolean addChunkRecord(String fileID, Integer chunkNum, Integer chunkSize) {
        ConcurrentHashMap<Integer, ChunkInfo> record = chunksRecord.get(fileID);

        ChunkInfo chunk = new ChunkInfo(chunkSize);

        if (record == null) {

            // No disk space to back up
            if (maxDiskSpace.longValue() != INFINITE_SPACE && (usedDiskSpace.get() + chunkSize) > maxDiskSpace.longValue())
                return false;

            System.out.println(usedDiskSpace.longValue() + "  " + chunkSize + "  " + chunkNum);
            usedDiskSpace.set(usedDiskSpace.longValue() + chunkSize);

            ConcurrentHashMap<Integer, ChunkInfo> newEntry = new ConcurrentHashMap<>();
            newEntry.put(chunkNum, chunk);
            chunksRecord.put(fileID, newEntry);
        }
        else if (! record.containsKey(chunkNum)) {
            record.put(chunkNum, chunk);
            System.out.println(usedDiskSpace.longValue() + "  " + chunkSize + "  " + chunkNum);
            usedDiskSpace.set(usedDiskSpace.longValue() + chunkSize);
        }

        return true;
    }

    public void updateChunkRecord(String fileID, Integer chunkNum, Integer senderID) {
        ConcurrentHashMap<Integer, ChunkInfo> record = chunksRecord.get(fileID);

        if (record != null && record.containsKey(chunkNum)) {

            ChunkInfo chunk = record.get(chunkNum);
            if (! chunk.peersStored.contains(senderID)) {
                chunk.repDegree += 1;
                chunk.peersStored.add(senderID);
            }
        }
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
        if (chunksRecord.containsKey(fileID))
            chunksRecord.remove(fileID);
    }

    /**
     * Update the maximum disk space to the given value
     *
     * @param maxDiskSpace maximum disk space
     */
    public void updateMaxSpace(long maxDiskSpace) {
        this.maxDiskSpace.set(maxDiskSpace);
    }

    /**
     * Getter for the used disk space by the chunks / file storage
     *
     * @return The space used
     */
    public long getUsedDiskSpace() {
        return usedDiskSpace.longValue();
    }
}
