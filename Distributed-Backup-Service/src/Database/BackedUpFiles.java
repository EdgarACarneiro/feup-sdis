package Database;

import Utils.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class representing all the files that were backed up from this Peer
 */
public class BackedUpFiles implements Serializable {

    /**
     * Class used to save information regarding files that were backed up.
     * Saves file name as well as records about the perceived replication degree of each file's chunk in the disk
     */
    public class FilesInfo implements Serializable {

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

        return info.numChunks;
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

    /**
     * Indicates that fail was successfully backed up, and therefore its information should be added to the database
     *
     * @param fileID The file identifier
     * @param realName The real name of the file
     * @param desiredRD The desired replication degree for all the chunks of that file
     * @param numChunks The number of chunks of the file
     * @return True if the false was successfully added to the database, false if the file already existed or could not be added
     */
    public boolean backedFile (String fileID, String realName, Integer desiredRD, Integer numChunks) {
        if (filesInfo.containsKey(fileID)) {
            Utils.showError("Trying to back up a file already backed up. Delete the back up to do another one.", this.getClass());
            return false;
        }
        filesInfo.put(fileID, new FilesInfo(realName, desiredRD, numChunks));
        return true;
    }

    @Override
    public String toString() {
        return "BackedUpFiles{" +
                "filesInfo=" + filesInfo +
                '}' + '\n';
    }
}
