package Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class responsible for splitting a file in chunks
 */
public final class FileManager {

    /**
     * Chunks' size in bytes : 64KBytes (chunk's Body)
     */
    private static final int CHUNKS_SIZE = 64000;

    public static ArrayList<byte[]> splitFile(String filePath) {

        Path path = getPath(filePath);
        if (path == null)
            return null;

        ArrayList<byte[]> chunks = new ArrayList<>();
        try {

            byte[] fileData = Files.readAllBytes(path);
            int fileSize = fileData.length;
            int writtenBytes = 0;

            while (writtenBytes < fileSize) {

                // To remove this from here to later create a folder only for backups -< new class or some shit TODO
                //if(! new File("backup-files").mkdirs())
                //    Utils.showError("Failed to create directory for backup files", FileManager.class);

                //FileOutputStream out = new FileOutputStream("backup-files/" + Integer.toString(writtenBytes));
                //out.write(fileData, writtenBytes,
                //        ((fileSize - writtenBytes)< CHUNKS_SIZE? (fileSize - writtenBytes) : CHUNKS_SIZE)
                //);
                System.out.println(fileSize + " " + writtenBytes);
                chunks.add(Arrays.copyOfRange(fileData, writtenBytes,
                        ((fileSize - writtenBytes) < CHUNKS_SIZE?
                                (writtenBytes += (fileSize - writtenBytes)):
                                (writtenBytes += CHUNKS_SIZE))
                ));
            }

        } catch (java.io.IOException e) {
            Utils.showError("Unable to handle file bytes", FileManager.class);
        }

        return chunks;
    }

    /**
     * Generates the fileId of a given file, using sha256 over some file peculiarity
     *
     * @param filePath The path of the file to be processed
     * @return The resultant fileID or empty string if  something failed
     */
    public static String genFileID(String filePath) {

        Path path = getPath(filePath);
        if (path == null)
            return "";

        File file = path.toFile();

        try {
            MessageDigest hasher = MessageDigest.getInstance("SHA-256");
            byte[] hashedMessage =  hasher.digest(
                    (file.getName() + file.lastModified()).getBytes("UTF-8")
            );

            // Convert byte[] to String
            StringBuffer hashAsString = new StringBuffer();
            for (byte hashByte : hashedMessage) {
                hashAsString.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
            }
            return hashAsString.toString();

        } catch (java.security.NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            Utils.showError("failed to apply sha256", FileManager.class);
        }
        return "";
    }

    /**
     * Verifies if the file correspondent to a given fileName exists
     *
     * @param filePath The filepath
     * @return returns a Path object if the file exists, null otherwise
     */
    private static Path getPath(String filePath) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            Utils.showError("Given File does not exist", FileManager.class);
            return null;
        }
        return path;
    }

    // Function to merge files here
}