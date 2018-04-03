package Messages;

/**
 * Interface that forces message to generate a string from fields
 */
public interface msgGenerator {

    /**
     * Generate the Messages. Message from private fields
     *
     * @return The byte array containing the message
     */
    byte[] genMsg();
}
