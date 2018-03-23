public class Message {

    /**
     * Regex useful for parsing Message Headers
     */
    private final String MESSAGE_HEADER_REGEX =
            "\\s*?(\\w+?)\\s+?(\\d\\.\\d)\\s+?(\\w+?)\\s+(([A-F0-9]){64})\\s+((\\d){1,6})\\s+(\\d)\\s*?";
}
