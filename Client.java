import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Name:Elise Chen
 * Timer=3 , if time out, resend message
 */
public class Client {
    public static int windowSize;
    public static int start=0, end, num;
    public static long timerTime = 0;

    public static void main(String[] args) throws Exception {
        InetAddress serverAddress = InetAddress.getByName("158.69.208.150");
        DatagramSocket clientSocket = new DatagramSocket(9999);
        byte[] sendData;
        Timer timer = new Timer();
        timer.schedule(new DelayActionListener(clientSocket), 0L, 500L);

        Scanner scanner = new Scanner(System.in);
       // System.out.println("The lost rate set as 0.3 ");
        System.out.println("Timer is 1s");
        System.out.println("Please enter number of packets: ");
        num = scanner.nextInt();
        System.out.println("Please enter slide window size: ");
        windowSize = scanner.nextInt();
        System.out.println("\n\n");
        end = start + windowSize;
        Timer[] timers = new Timer[num];
        long startTime = System.currentTimeMillis();

        for (int i=start;i<=end;i++){
            sendData = (i + "seq").getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, 8888);
            clientSocket.send(sendPacket);
           // System.out.println("client send a packet. Number: " + i);
        }

        while (true){
            byte[] recvData = new byte[100];
            DatagramPacket recvPacket = new DatagramPacket(recvData, recvData.length);
            clientSocket.receive(recvPacket);
            String in = new String(recvPacket.getData()).trim();
            int ack_seq = Integer.parseInt(in.substring(3, in.length()));
            System.out.println("Client received acknowledgement " + ack_seq);
            if (ack_seq >= start && ack_seq <= end){
                int oldEnd = end;
                start = ack_seq;
                end = ack_seq + windowSize;
                if (end > num)
                    end = num;

                for (int i = oldEnd; i < end; i++) {
                    if (i % 500 == 0) {
                        System.out.println("Sending packet " + i + "/" + num);
                    }

                    sendData = (i + "seq").getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, 8888);
                    clientSocket.send(sendPacket);
                    System.out.println("Client sent packet " + i);
                }

                timerTime = System.currentTimeMillis();
            }
            if (ack_seq == end)
                timerTime = 0;
            if (ack_seq == num){
                timer.cancel();
                System.out.println("Success sent all packets");
                System.out.println("Time to send " + num + " packets successfully was " + (System.currentTimeMillis() - startTime) + "ms");
                return;
            }
        }
    }
}

class DelayActionListener extends TimerTask {

    DatagramSocket clientSocket;
    public DelayActionListener(DatagramSocket clientSocket){
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        if (Client.timerTime != 0 && Client.timerTime < System.currentTimeMillis() - 1000) {
            int end = Client.end;
            int start = Client.start;
            System.out.println("\nClient will resend packets " + start + " - " + end);
            for (int i = start; i <= end; i++) {
                byte[] sendData;
                InetAddress serverAddress = null;
                try {
                    serverAddress = InetAddress.getByName("158.69.208.150");
                    sendData = (i + "seq").getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, 8888);
                    clientSocket.send(sendPacket);
                    System.out.println("Client sent packet " + i);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
