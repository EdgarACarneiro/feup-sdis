import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Class responsible for splitting a file in chunks
 */
public class FileSplitter {

    /**
     * Chunks' size in bytes : 64KBytes
     */
    private final int CHUNKS_SIZE = 64000;

    public FileSplitter(){}

    public ArrayList<Integer> splitFile(String filePath) {

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            System.err.println("FileSplitter Error: Given File does not exist");
            return null;
        }


        try {

            byte[] fileData = Files.readAllBytes(path);
            int fileSize = fileData.length;
            int writtenBytes = 0;

            while (writtenBytes <= fileSize) {
                FileOutputStream out = new FileOutputStream("test/" + Integer.toString(writtenBytes));
                out.write(fileData, writtenBytes,
                        ((fileSize - writtenBytes)< CHUNKS_SIZE? (fileSize - writtenBytes) : CHUNKS_SIZE)
                );
                writtenBytes += CHUNKS_SIZE;
            }

        } catch (java.io.IOException e) {
            System.err.println("FileSplitter Error: Unable to handle file bytes");
        }

        return null;
    }

    // Function to merge files here
}