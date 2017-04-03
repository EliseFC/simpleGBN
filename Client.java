import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
import javax.swing.Timer;
/**
 * Name:Elise Chen
 * Timer=3 , if time out, resend message
 */
public class Client {
    public static int windowSize;
    public static int start=0, end, num;
    public static void main(String[] args) throws Exception {
        InetAddress serverAddress = InetAddress.getByName("localhost");
        DatagramSocket clientSocket = new DatagramSocket(9999);
        byte[] sendData;
        Timer[] timers = new Timer[20];

        Scanner scanner = new Scanner(System.in);
        System.out.println("The lost rate set as 0.3 ");
        System.out.println("Timer is 3s");
        System.out.println("Please enter number of packets: ");
        num = scanner.nextInt();
        System.out.println("Please enter slide window size: ");
        windowSize = scanner.nextInt();
        System.out.println("\n\n");
        end = start + windowSize - 1;

        for (int i=start;i<=end;i++){
            sendData = (i + "seq").getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, 8888);
            clientSocket.send(sendPacket);
            timers[i] = new Timer(3000, new DelayActionListener(clientSocket, i, timers));
            timers[i].start();
            System.out.println("client send a packet. Number: " + i);
        }

        while (true){
            byte[] recvData = new byte[100];
            DatagramPacket recvPacket = new DatagramPacket(recvData, recvData.length);
            clientSocket.receive(recvPacket);
            int ack_seq = new String(recvPacket.getData()).charAt(3) -'0';
            System.out.println("Client receive ack=" + ack_seq);
            timers[ack_seq].stop();
            if (ack_seq == start){
                start++;
                end++;
                if (end > num - 1)
                    end = num - 1;
                sendData = (end + "seq").getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, 8888);
                clientSocket.send(sendPacket);
                timers[end] = new Timer(3000, new DelayActionListener(clientSocket, start, timers));
                timers[end].start();
                System.out.println("Client send packet. Number " + end);
            }
            if (ack_seq == num - 1){
                System.out.println("Success send all packets");
                return;
            }
        }
    }
}

class DelayActionListener implements ActionListener{

    DatagramSocket clientSocket;
    int end_ack;
    Timer[] timers;
    public DelayActionListener(DatagramSocket clientSocket, int end_ack, Timer[] timers){
        this.clientSocket = clientSocket;
        this.end_ack = end_ack;
        this.timers = timers;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int end = Client.end;
        System.out.println("\nClient will resend packet " + end_ack +" - " + end);
        for (int i=end_ack;i<=end;i++){
            byte[] sendData;
            InetAddress serverAddress = null;
            try {
                serverAddress = InetAddress.getByName("localhost");
                sendData = (i + "seq").getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, 8888);
                clientSocket.send(sendPacket);
                System.out.println("Client send packet, num " + i);
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            timers[i].stop();
            timers[i].start();
        }
    }
}
