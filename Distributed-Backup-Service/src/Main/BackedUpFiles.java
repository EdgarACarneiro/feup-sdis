package Main;

import Utils.Utils;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class BackedUpFiles {

    /**
     * Class used to save information regarding files that were backed up.
     * Saves file name as well as records about the perceived replication degree of each file's chunk in the disk
     */
    public class FilesInfo {

        public ArrayList<Integer> chunksRD;

        public String fileName;

        public FilesInfo(String fileName, ArrayList<Integer> chunksRD) {
            this.fileName = fileName;
            this.chunksRD = chunksRD;
        }
    }

    private ConcurrentHashMap<String, FilesInfo> filesInfo = new ConcurrentHashMap<>();


    public BackedUpFiles() {}

    public void backedFile (String fileID, String fileName, ArrayList<Integer> chunksRD) {

        if (filesInfo.containsKey(fileID)) {
            Utils.showWarning("File is already backed up.", this.getClass());
            return;
        }

        filesInfo.put(fileID, new FilesInfo(fileName, chunksRD));
    }

    public void decRepDegree(String fileID, Integer chunkNum) {
        updateRepDegree(fileID, chunkNum, -1);
    }

    public void incRepDegree(String fileID, Integer chunkNum) {
        updateRepDegree(fileID, chunkNum, 1);
    }

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

    public int getNumChunks(String fileID) {
        FilesInfo info = filesInfo.get(fileID);

        if (info == null)
            return -1;

        return info.chunksRD.size();
    }

    public String getFileName(String fileID) {
        FilesInfo info = filesInfo.get(fileID);

        if (info == null)
            return null;

        return info.fileName;
    }

    public boolean hasFileBackedUp(String fileID) {
        return filesInfo.containsKey(fileID);
    }
}
