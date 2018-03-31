package Main;

public class BackedupFile {

    private int numChunks;

    private String fileName;

    public  BackedupFile (int numChunks, String fileName) {
        this.numChunks = numChunks;
        this.fileName = fileName;
    }

    public int getNumChunks() {
        return numChunks;
    }

    public String getFileName() {
        return fileName;
    }
}
