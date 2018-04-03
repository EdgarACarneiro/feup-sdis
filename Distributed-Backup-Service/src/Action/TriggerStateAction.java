package Action;

import Database.BackedUpFiles;
import Database.ChunksRecorder;
import Messages.ChunkMsg;
import Channel.ControlChannel;
import Messages.Message;
import Utils.*;

import java.util.ArrayList;

public class TriggerStateAction extends Action {

    private String result;

    private ChunksRecorder record;

    private BackedUpFiles ownBackedUpFile;

    public TriggerStateAction(ChunksRecorder record, BackedUpFiles ownBackedUpFile) {
        this.record = record;
        this.ownBackedUpFile = ownBackedUpFile;
    }

    @Override
    public void run() {
        result = "EUREKA" + (record.toString() + ownBackedUpFile.toString());
    }

    public String getResult() {
        return (record.toString() + ownBackedUpFile.toString());
    }
}