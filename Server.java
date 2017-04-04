import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
/**
 * Name:Elise Chen
 * Set lost rate as 0.3
 */
public class Server {
    public static void main(String[] args) throws Exception {
        DatagramSocket serverSocket = new DatagramSocket(8888);
        int endReceive = 0;
        int missedCtr = 0;

        while (true){
            byte[] data = new byte[100];
            DatagramPacket receivePacket = new DatagramPacket(data, data.length);
            serverSocket.receive(receivePacket);
            String in = new String(receivePacket.getData()).trim();
            int seq = Integer.parseInt(in.substring(0, in.length() - 3));
           // System.out.println("seq"+seq);
            if (seq == 0) {
                System.out.println("Restarting");
                endReceive = 0;
                missedCtr = 0;
            }
           // if (seq == 0 || Math.random()<=0.999){
                if (seq == endReceive){
                    //receive message，and send ack;
                    endReceive++;
                    byte[] ackData = new String("ack"+endReceive).getBytes();
                    InetAddress clientAddress = receivePacket.getAddress();
                    int clientPort = receivePacket.getPort();
                    DatagramPacket sendPacket = new DatagramPacket(ackData, ackData.length, clientAddress, clientPort);
                    serverSocket.send(sendPacket);
                   // System.out.println("Server send ack= " + seq);
                }else if (endReceive != 0){
                    if (seq == endReceive + 1) {
                        System.out.println("Packet lost or disordered on way to server: " + endReceive);
                        missedCtr++;
                        System.out.println("Missed: " + missedCtr);
                    }

                    byte[] ackData = new String("ack"+endReceive).getBytes();
                    InetAddress clientAddress = receivePacket.getAddress();
                    int clientPort = receivePacket.getPort();
                    DatagramPacket sendPacket = new DatagramPacket(ackData, ackData.length, clientAddress, clientPort);
                    serverSocket.send(sendPacket);
                   // System.out.println("Server send ack= " + endReceive);
                }

           // } else {
           //     System.out.println("Drop packet " + seq);
           // }
        }
    }
}
