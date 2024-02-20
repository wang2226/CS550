import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class SendNeighbor implements Runnable {
    Socket socket;
    String IP;
    int port;
    String message;

    SendNeighbor(String IP, int port, String message) {
        this.IP = IP;
        this.port = port;
        this.message = message;
    }

    @Override
    public void run() {
        DataOutputStream outputStream = null;

        try {
            socket = new Socket(IP, port);
            // Create stream writer and reader
            outputStream = new DataOutputStream(socket.getOutputStream());

            // Send message to server
            // System.out.println("SendNeighbor, before send: IP =" + IP + " message=" + message);
            outputStream.writeUTF(message);
            outputStream.flush();

        } catch (IOException e) {
            //outputStream.close();
            //socket.close();
            throw new RuntimeException(e);
        }
    }
}
