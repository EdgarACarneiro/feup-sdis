package Utils;

/**
 * A class filled with Utility methods
 */
public final class Utils {

    /**
     * Method responsible for showing error messages
     *
     * @param errorMsg The error message to be displayed
     * @param callingClass The Class where the error occurred
     */
    public static void showError(String errorMsg, Object callingClass) {
        System.err.println(callingClass.toString() + " error: " + errorMsg);
    }

    /**
     * Method responsible for showing warning messages
     *
     * @param warningMsg The warning message to be displayed
     * @param callingClass The Class where the error occurred
     */
    public static void showWarning(String warningMsg, Object callingClass) {
        System.out.println(callingClass.toString() + " warning: " + warningMsg);
    }
}
