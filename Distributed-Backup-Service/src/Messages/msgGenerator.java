package Messages;

public interface msgGenerator {

    /**
     * Generate the Messages. Message from private fields
     *
     * @return The byte array containing the message
     */
    public byte[] genMsg();
}
