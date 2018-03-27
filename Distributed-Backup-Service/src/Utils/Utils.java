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
}
