package Database;

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
         * The number of chunks that make this file
         */
        public int numChunks;

        /**
         * HashMap containing the replication degree associated to each file's' chunk. Key - file chunk, Value - replication degree
         */
        public ConcurrentHashMap<Integer, Integer> chunksRD = new ConcurrentHashMap<>();

        /**
         * HashMap containing the list of peers that have replicated each file's' chunk. Key - file chunk, Value - list of files
         */
        public ConcurrentHashMap<Integer, ArrayList<Integer> > chunksRDPeers = new ConcurrentHashMap<>();

        /**
         * The FilesInfo constructor.
         * It initializes all of the files related information
         *
         * @param fileName The file's real name
         * @param desiredRD The desired replication degree for each of the chunks
         */
        public FilesInfo(String fileName, int desiredRD, int numChunks) {
            this.fileName = fileName;
            this.desiredRD = desiredRD;
            this.numChunks = numChunks;
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
     * Indicate that a chunk was successfully backed up and therefore add its information to the records
     *
     * @param fileID The file identifier
     * @param chunkNum The file associated
     * @param peerID The peer executing the replication
     */
    public void backedChunk(String fileID, Integer chunkNum, Integer peerID) {
        FilesInfo file = filesInfo.get(fileID);

        if (file == null)
            return;

        ConcurrentHashMap<Integer, Integer> chunksRD = file.chunksRD;
        ConcurrentHashMap<Integer, ArrayList<Integer> > chunksRDPeers = file.chunksRDPeers;
        ArrayList<Integer> peersList;

        if (chunksRD.containsKey(chunkNum)) {

            peersList = chunksRDPeers.get(chunkNum);
            if (! peersList.contains(peerID)) {
                int oldValue = chunksRD.get(chunkNum);
                chunksRD.replace(chunkNum, oldValue + 1);

                peersList.add(peerID);
                chunksRDPeers.replace(chunkNum, peersList);
            }
        }
        else {
            chunksRD.put(chunkNum, 1);

            peersList = new ArrayList<>();
            peersList.add(peerID);
            chunksRDPeers.put(chunkNum, peersList);
        }
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

        ConcurrentHashMap<Integer, Integer> chunksRD = filesInfo.get(fileID).chunksRD;
        if (! chunksRD.containsKey(chunkNum)) {
            Utils.showError("Non-existent chunk requested.", this.getClass());
            return;
        }

        int oldValue = chunksRD.get(chunkNum);
        chunksRD.replace(chunkNum, oldValue + change);
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

        return info != null && info.chunksRD.containsKey(chunkNum) && (info.chunksRD.get(chunkNum) >= info.desiredRD);
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

        ConcurrentHashMap<Integer, Integer> chunksRD = info.chunksRD;
        ConcurrentHashMap<Integer, Integer> result = new ConcurrentHashMap<>();

        ArrayList<Integer> keys = new ArrayList<>();
        keys.addAll(info.chunksRD.keySet());

        for (int key : keys) {
            if (chunksRD.get(key) < info.desiredRD)
                result.put(key, chunksRD.get(key) - info.desiredRD);
        }

        return result;
    }

    /**
     * Getter for the chunks who still have not replication degree bigger than the desired, or do not yet exist
     *
     * @param fileID The file identifier
     * @return List containing the number of the chunks that still do not meet the replication degree condition
     */
    public ArrayList<Integer> checkAllRD(String fileID) {
        FilesInfo info = filesInfo.get(fileID);

        if (info == null) {
            Utils.showWarning("checkAllRD function: File has not been backed up.", this.getClass());
            return null;
        }

        ConcurrentHashMap<Integer, Integer> chunksRD = info.chunksRD;
        ArrayList<Integer> values = new ArrayList<>();

        for (int i = 0; i < info.numChunks; ++i) {
            if (chunksRD.get(i) == null || chunksRD.get(i) < info.desiredRD)
                values.add(i);
        }

        return values;
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

    public void backedFile (String fileID, String realName, Integer desiredRD, Integer numChunks) {
        if (filesInfo.containsKey(fileID)) {
            Utils.showError("Trying to back up a file already backed up.", this.getClass());
            return;
        }
        filesInfo.put(fileID, new FilesInfo(realName, desiredRD, numChunks));
    }
}
