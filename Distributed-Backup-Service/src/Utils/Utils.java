package Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A class filled with Utility methods
 */
public final class Utils {

    private static File logFile = new File("logFile.txt");

	public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_YELLOW = "\u001B[33m";


    /**
     * Method responsible for showing error messages
     *
     * @param errorMsg The error message to be displayed
     * @param callingClass The Class where the error occurred
     */
    public static void showError(String errorMsg, Object callingClass) {
        System.err.println(ANSI_RED + callingClass.toString() + " error: " + ANSI_RESET + errorMsg);
    }

    /**
     * Method responsible for showing warning messages
     *
     * @param warningMsg The warning message to be displayed
     * @param callingClass The Class where the error occurred
     */
    public static void showWarning(String warningMsg, Object callingClass) {
        System.out.println(ANSI_YELLOW + callingClass.toString() + " warning: " + ANSI_RESET + warningMsg);
    }

    /**
     * Method responsible for logging Error and Warning messages
     *
     * @param toLog Message to be saved
     */
    public synchronized static void log(String toLog) {
		FileOutputStream logStream;
		try {
			logStream = new FileOutputStream(logFile, true);
		} catch (FileNotFoundException e1) {
			System.err.println(ANSI_RED + "LOGGER: Could not log." + ANSI_RESET);
			return;
		}

		String timeStamp = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss").format(new Date(System.currentTimeMillis()));
		try {
			logStream.write((timeStamp + " - " + toLog + "\n").getBytes());
			logStream.close();
		} catch (IOException e) {
			System.err.println(ANSI_RED + "LOGGER: Could not write to log." + ANSI_RESET);
		}
	}

	/**
	 * Concatenates two byte arrays into one
	 *
	 * @param array1 One of the arrays
	 * @param array2 The other array
	 * @return Array containing the concatenation of the two given arrays
	 */
	public static byte[] byteArrayConcat(byte[] array1, byte[] array2) {
		byte[] array1and2 = new byte[array1.length + array2.length];
		System.arraycopy(array1, 0, array1and2, 0, array1.length);
		System.arraycopy(array2, 0, array1and2, array1.length, array2.length);
		return array1and2;
    }
    
    /**
	 * Deletes a Folder with files in it
	 *
	 * @param folder folder to be deleted
	 * @return Boolean with success or not 
	 */
    public static boolean deleteFolder(File folder){
        File[] directoryListing = folder.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                child.delete();
            }
        }
        return folder.delete();
    }
}
