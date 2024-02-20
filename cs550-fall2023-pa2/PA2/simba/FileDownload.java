import java.io.*;
import java.net.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileDownload extends Gnutella {

    // Download function -  invoked by a peer to download a file from another peer
    //Support up to 4GB in size
    public static void download(String ownerId, String fileName) {
        DataOutputStream outputStream = null;
        DataInputStream inputStream = null;

        String downPath = ReadProperties.getByName("Shared_Directory");

        //Check if the folder exists
        //Create if it doesn't exist
        File directory = new File(downPath);
        if (!directory.exists()) {
            System.out.println("Creating a new folder named: ");
            if (directory.mkdir()) {
                System.out.println("The file will be found at: " + downPath);
            }
        }

        //Make a connection with server to get file from
        String peerIp = ReadProperties.getByName(ownerId + "_IP");
        if (peerIp.equals("error")) {
            System.out.println("FileDownload: wrong file owner IP, please note case sensitive");
            return;
        }
        String property = ReadProperties.getByName(ownerId + "_Server_Port");
        if (property.equals("error")) {
            System.out.println("FileDownload: wrong file owner port, please note case sensitive");
            return;
        }
        int peerPort = Integer.parseInt(property);
        String filePath = downPath + "/" + fileName;

        System.out.println("Downloading File Please wait ...");

        try (Socket peerSocket = new Socket(peerIp, peerPort)) {
            inputStream = new DataInputStream(peerSocket.getInputStream());
            outputStream = new DataOutputStream(peerSocket.getOutputStream());

            String message = "download" + "," + fileName;
            outputStream.writeUTF(message);

            String status = inputStream.readUTF();
            if (status.equalsIgnoreCase("ready")) {
                receiveFile(inputStream, filePath);
            } else if (status.equalsIgnoreCase("NotFound")) {
                System.out.println(fileName + " not found on " + ownerId + ".");
                return;
            }

            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.out.println("Downloaded Successfully");
    }

    private static void receiveFile(DataInputStream in, String fileName) throws Exception {
        int bytes = 0;
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);

        long size = in.readLong();     // read file size
        byte[] buffer = new byte[4 * 1024];
        while (size > 0 && (bytes = in.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
            fileOutputStream.write(buffer, 0, bytes);
            size -= bytes;      // read upto file size
        }
        fileOutputStream.close();
    }

    public static boolean isSocketAlive(InetAddress hostIP, int port) {
        boolean isAlive = false;

        // Creates a socket address from a hostname and a port number
        SocketAddress socketAddress = new InetSocketAddress(hostIP, port);
        Socket socket = new Socket();

        // Timeout required - it's in milliseconds
        int timeout = 2000;

        try {
            socket.connect(socketAddress, timeout);
            socket.close();
            isAlive = true;
        } catch (SocketTimeoutException exception) {
            System.out.println("SocketTimeoutException " + hostIP + ":" + port + ". " + exception.getMessage());
        } catch (IOException exception) {
            System.out.println(
                    "IOException - Unable to connect to " + hostIP + ":" + port + ". " + exception.getMessage());
        }
        return isAlive;
    }

    public static String findBackup(String owner) {
        String backupNode;

        int replicFactor = Integer.parseInt(ReadProperties.getByName("Replication_Factor"));
        int ordinalNumber = Integer.parseInt(owner.substring(5));
        int newNumber = (ordinalNumber + replicFactor) % 16;

        backupNode = "simba" + newNumber;

        return backupNode;
    }
}
