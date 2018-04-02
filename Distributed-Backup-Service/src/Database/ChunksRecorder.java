package Database;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class that holds the records for all the chunks stored in this Peer
 */
public class ChunksRecorder {

    /**
     * The hashMap used for keeping information about the chunks that were saved for each file (fileID).
     * Maps a file identifier into a chunk number, that than is mapped into a perceived replication degree.
     */
    private ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>> chunksRecord = new ConcurrentHashMap<>();

    /**
     * Default ChunksRecorder Constructor
     */
    public ChunksRecorder() {}

    // TODO - Ver se retorna falso quando o file size da pasta ja esta cheia
    public boolean updateChunkRecord(String fileID, Integer chunkNum) {
        ConcurrentHashMap<Integer, Integer> chunks = chunksRecord.get(fileID);

        if (chunks == null) {
            ConcurrentHashMap<Integer, Integer> newEntry = new ConcurrentHashMap<>();
            newEntry.put(chunkNum, 1);
            chunksRecord.put(fileID, newEntry);
        }
        else {
            if (chunks.containsKey(chunkNum)) {
                int oldValue = chunks.get(chunkNum);
                chunks.replace(chunkNum, oldValue + 1);
            }
            else
                chunks.put(chunkNum, 1);
        }

        return true;
    }

    /**
     * Getter for all the chunks stored from a given file
     *
     * @param fileID The file identifier
     * @return List containing the numeration of the chunks stored in the disk
     */
    public ArrayList<Integer> getChunksList(String fileID) {
        ConcurrentHashMap<Integer, Integer> storedChunks= chunksRecord.get(fileID);

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
        ConcurrentHashMap<Integer, Integer> storedChunks = chunksRecord.get(fileID);

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


}
