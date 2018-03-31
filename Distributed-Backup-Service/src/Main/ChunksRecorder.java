package Main;

import java.util.ArrayList;
import java.util.HashMap;

public class ChunksRecorder {

    /**
     * The hashMap used for keeping information about the chunks that were saved for each file (fileID)
     */
    private HashMap<String, ArrayList<Integer>> chunksRecord = new HashMap<>();

    public ChunksRecorder() {}

    public void updateChunks(String fileID, int chunkNum) {
        ArrayList<Integer> chunks = chunksRecord.get(fileID);

        if (chunks != null) {
            if (chunks.contains(chunkNum))
                return;
            else
                chunksRecord.remove(fileID);
        }
        else
            chunks = new ArrayList<>();

        chunks.add(chunkNum);
        chunksRecord.put(fileID, chunks);
    }

    public boolean hasChunk(String fileID, Integer chunkNum) {
        ArrayList<Integer> storedChunks = chunksRecord.get(fileID);

        return (storedChunks != null && (storedChunks.contains(chunkNum)) );
    }


}
