package Main;

import Utils.Utils;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class representing all the files that were backed up from this Peer
 */
public class BackedUpFiles {

    /**
     * Class used to save information regarding files that were backed up.
     * Saves file name as well as records about the perceived replication degree of each file's chunk in the disk
     */
    public class FilesInfo {

        /**
         * The real file name of the file
         */
        public String fileName;

        /**
         * The desired replication degree for each of the chunks
         */
        public int desiredRD;

        /**
         * List containing the replication degree associated to each file's' chunk
         */
        public ArrayList<Integer> chunksRD;

        /**
         * The FilesInfo backed up.
         * It initializes all of the files related information
         *
         * @param fileName The file's real name
         * @param desiredRD The desired replication degree for each of the chunks
         * @param chunksRD A list containing the replication degree associated to each chunk
         */
        public FilesInfo(String fileName, int desiredRD, ArrayList<Integer> chunksRD) {
            this.fileName = fileName;
            this.desiredRD = desiredRD;
            this.chunksRD = chunksRD;
        }
    }

    /**
     * Concurrent Container used to keep de FilesInfo information associated to each file
     */
    private ConcurrentHashMap<String, FilesInfo> filesInfo = new ConcurrentHashMap<>();

    /**
     * Default BackedUpFiles constructor
     */
    public BackedUpFiles() {}

    /**
     * Indicate that a file was successfully backed up and therefore add its information to the records
     *
     * @param fileID The file identifier
     * @param fileName The file real name
     * @param desiredRD  The desired replication degree for each of the chunks
     * @param chunksRD The file associated
     */
    public void backedFile (String fileID, String fileName, int desiredRD, ArrayList<Integer> chunksRD) {

        if (filesInfo.containsKey(fileID)) {
            Utils.showWarning("File is already backed up.", this.getClass());
            return;
        }

        filesInfo.put(fileID, new FilesInfo(fileName, desiredRD, chunksRD));
    }

    /**
     * Decrement the perceived replication degree of a given chunk
     *
     * @param fileID The file identifier of the chunk
     * @param chunkNum The number of the chunk to be updated
     */
    public void decRepDegree(String fileID, Integer chunkNum) {
        updateRepDegree(fileID, chunkNum, -1);
    }

    /**
     * Increment the perceived replication degree of a given chunk
     *
     * @param fileID The file identifier of the chunk
     * @param chunkNum The number of the chunk to be updated
     */
    public void incRepDegree(String fileID, Integer chunkNum) {
        updateRepDegree(fileID, chunkNum, 1);
    }

    /**
     * Update the replication degree of a certain chunk, with the given change
     *
     * @param fileID The file identifier of the chunk
     * @param chunkNum The number of the chunk to be updated
     * @param change The value to be added to the previous replication degree
     */
    private void updateRepDegree(String fileID, Integer chunkNum, Integer change) {

        if (! filesInfo.containsKey(fileID)) {
            Utils.showError("Failed to update Replication Degree of chunk. " +
                    "Given file is not backed up", this.getClass());
            return;
        }

        ArrayList<Integer> chunks = filesInfo.get(fileID).chunksRD;
        if (chunks.size() <= chunkNum) {
            Utils.showError("Non-existent chunk requested.", this.getClass());
            return;
        }

        chunks.set(chunkNum, chunks.get(chunkNum) + change);
    }

    /**
     * Indicates if the replication degree balance of a given chunk is positive (balanced)
     *
     * @param fileID The file identifier of the chunks
     * @param chunkNum The chunk that is desired to know the replication degree balance
     * @return Concurrent Hash map were the key is the chunk number and the object is the count of how much the chunk's replication degree s below the desired RD
     */
    public boolean isRDBalanced(String fileID, Integer chunkNum) {
        FilesInfo info = filesInfo.get(fileID);

        return info != null && info.chunksRD.get(chunkNum) >= info.desiredRD;
    }

    /**
     * Getter for the chunks whose replication degree is below the desired replication degree
     *
     * @param fileID The file identifier of the chunks
     * @return Concurrent Hash map were the key is the chunk number and the object is the count of how much the chunk's replication degree s below the desired RD
     */
    public ConcurrentHashMap<Integer, Integer> getRDBalance(String fileID) {
        FilesInfo info = filesInfo.get(fileID);

        if (info == null)
            return null;

        ConcurrentHashMap<Integer, Integer> result = new ConcurrentHashMap<>();
        for (int i = 0; i < info.chunksRD.size(); ++i) {
            if (info.chunksRD.get(i) < info.desiredRD)
                result.put(i, info.chunksRD.get(i) - info.desiredRD);
        }

        return result;
    }

    /**
     * Getter for the number of chunks backed up
     *
     * @param fileID The file identifier
     * @return Number of chunks backed up
     */
    public int getNumChunks(String fileID) {
        FilesInfo info = filesInfo.get(fileID);

        if (info == null)
            return -1;

        return info.chunksRD.size();
    }

    /**
     * Getter for the real file name of  a file identifier
     *
     * @param fileID The file identifier
     * @return The file real name
     */
    public String getFileName(String fileID) {
        FilesInfo info = filesInfo.get(fileID);

        if (info == null)
            return null;

        return info.fileName;
    }

    /**
     * Check if the given file has chunks backed up
     *
     * @param fileID The file identifier
     * @return True if the file is backed up, false otherwise
     */
    public boolean hasFileBackedUp(String fileID) {
        return filesInfo.containsKey(fileID);
    }
}
