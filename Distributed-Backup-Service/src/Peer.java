public class Peer {

    private ControlChannel controlChannel;

    private BackupChannel backupChannel;

    private RestoreChannel restoreChannel;

    Peer(boolean usesMDB, boolean usesMDR, boolean test, String test_name) {
        controlChannel = new ControlChannel();
        if (usesMDB)
            backupChannel = new BackupChannel();
        if (usesMDR)
            restoreChannel = new RestoreChannel();

        // TODO- Abrir a comunicação usado o coidog da main acima e lançando para cada um um função que gere o channel, através de threads. Para já fazer com controlchannel so
        MulticastClient tretas = new MulticastClient(controlChannel, test, test_name);

    }
}
