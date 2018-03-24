public class Main {
    /**
     * Main function
     *
     */
    public static void main(String args[]){
        //FileSplitter.splitFile("C:\\Users\\ASUS\\Pictures\\Shade.jpg");
        //(new Message()).parseHeader("PUTCHUNK 1.1 32452 4CA00E99D225CwAFFD7AE27B5CF63EAC44FECB2D1360293A3011E50 23 1");

        // TODO - Serviço tem de se subscrever a rede multicast
        new Peer(false, false, args[0].equals("1"), args[1]);
    }
}