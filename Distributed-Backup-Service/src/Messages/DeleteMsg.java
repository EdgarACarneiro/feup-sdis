package Messages;

public class DeleteMsg extends Message {

    public DeleteMsg(String receivedMsg) {

    }

    public DeleteMsg(float protocolVersion, int senderID, String fileID) {
        super(protocolVersion, senderID, fileID);
    }
}
