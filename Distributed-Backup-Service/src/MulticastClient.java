import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

public class MulticastClient {

    public MulticastClient(MulticastChannel channel, boolean test, String test_name) {

        byte[] buf = new byte[256];

        // Create a new Multicast socket (that will allow other sockets/programs to join it as well.
        try {
            MulticastSocket clientSocket = new MulticastSocket(channel.getPort());

            //Joint the Multicast group
            clientSocket.joinGroup(channel.getInetAddr());

            if(test){
                while (true) {
                    // Receive the information and print it.
                    DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
                    clientSocket.receive(msgPacket);

                    String msg = new String(buf, 0, buf.length);
                    System.out.println("Socket " + test_name + "  received msg: " + msg);
                }
            } else {
                for (int i = 0; i < 5; i++) {
                    String msg = "Sent message no " + i;

                    // Create a packet that will contain the data
                    // (in the form of bytes) and send it.
                    DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(),
                            msg.getBytes().length, channel.getInetAddr(), channel.getPort());
                    clientSocket.send(msgPacket);

                    System.out.println("Socket " + test_name + " sent packet with msg: " + msg);
                    Thread.sleep(500);
                }
            }
        } catch (IOException | java.lang.InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
