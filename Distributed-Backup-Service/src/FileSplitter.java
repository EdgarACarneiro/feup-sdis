import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Class responsible for splitting a file in chunks
 */
public final class FileSplitter {

    /**
     * Chunks' size in bytes : 64KBytes
     */
    private static final int CHUNKS_SIZE = 64000;


    public static boolean splitFile(String filePath) {

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            System.err.println("FileSplitter Error: Given File does not exist");
            return false;
        }

        try {

            byte[] fileData = Files.readAllBytes(path);
            int fileSize = fileData.length;
            int writtenBytes = 0;

            while (writtenBytes <= fileSize) {

                // To remove this from here to later create a folder only for backups -< new class or some shit TODO
                new File("backup-files").mkdirs();

                FileOutputStream out = new FileOutputStream("backup-files/" + Integer.toString(writtenBytes));
                out.write(fileData, writtenBytes,
                        ((fileSize - writtenBytes)< CHUNKS_SIZE? (fileSize - writtenBytes) : CHUNKS_SIZE)
                );
                writtenBytes += CHUNKS_SIZE;
            }

        } catch (java.io.IOException e) {
            System.err.println("FileSplitter Error: Unable to handle file bytes");
        }

        return true;
    }

    // Function to merge files here
}