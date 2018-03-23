/**
 * A class filled with Utilities methods
 */
public final class Utils {

    /**
     * Method responsible for showing error messages
     *
     * @param errorMsg The error message to e displayed
     * @param callingClass The Class where the error occurred
     */
    public static void showError(String errorMsg, Object callingClass) {
        System.err.println(callingClass.toString() + " error: " + errorMsg);
    }
}
