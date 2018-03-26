import Utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;

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
            Utils.showError("Given File does not exist", FileSplitter.class);
            return false;
        }

        try {

            byte[] fileData = Files.readAllBytes(path);
            int fileSize = fileData.length;
            int writtenBytes = 0;

            while (writtenBytes <= fileSize) {

                // To remove this from here to later create a folder only for backups -< new class or some shit TODO
                if(! new File("backup-files").mkdirs())
                    Utils.showError("Failed to create directory for backup files", FileSplitter.class);

                FileOutputStream out = new FileOutputStream("backup-files/" + Integer.toString(writtenBytes));
                out.write(fileData, writtenBytes,
                        ((fileSize - writtenBytes)< CHUNKS_SIZE? (fileSize - writtenBytes) : CHUNKS_SIZE)
                );
                writtenBytes += CHUNKS_SIZE;
            }

        } catch (java.io.IOException e) {
            Utils.showError("Unable to handle file bytes", FileSplitter.class);
        }

        return true;
    }

    /**
     * Generates the fileId of a given file, using sha256 over some file peculiarity
     *
     * @param file The file to be processed
     * @return The resultant fileID or empty string if  something failed
     */
    public static String genFileID(File file) {
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
            Utils.showError("failed to apply sha256", FileSplitter.class);
        }
        return "";
    }

    // Function to merge files here
}