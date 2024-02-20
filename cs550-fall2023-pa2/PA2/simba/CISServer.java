import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

class CISServer implements Runnable {
    private final String WHO_AM_I;

    private static ServerSocket serverSocket;
    private static CISServices services;
    private static Thread thread;


    int peerPort;

    public CISServer() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            this.WHO_AM_I = localHost.getHostName();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.peerPort = Integer.parseInt(ReadProperties.getByName(WHO_AM_I + "_Server_Port"));
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(peerPort);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Server is up and running!!!");

        while (true) {
            try {
                services = new CISServices(serverSocket.accept());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            thread = new Thread(services);
            thread.start();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        CISClient client = new CISClient();
        CISClient.batchRegister();

        CISServer server = new CISServer();
//        Thread workThread = new Thread(server);
        //       workThread.start();
        server.run();

    }
}

class CISServices implements Runnable {
    private Socket peerSocket;

    CISServices(Socket peerSocket) {
        this.peerSocket = peerSocket;
    }

    public void run() {

        DataOutputStream outputStream = null;
        DataInputStream inputStream = null;

        while (true) {
            String reciveString = "";

            //Read incoming message
            try {
                inputStream = new DataInputStream(peerSocket.getInputStream());
                outputStream = new DataOutputStream(peerSocket.getOutputStream());

                try {
                    reciveString = inputStream.readUTF();
                } catch (EOFException eofe) {
                    //System.out.println("End of data stream reached");
                    break;
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }

                String[] reciveArray = reciveString.split(",");

                //get the fileName from ClientAskingForFile
                String fileName = reciveArray[1];
                String fileDownloadPath = ReadProperties.getByName("Shared_Directory");
                File targetFile = new File(fileDownloadPath + "/" + fileName);

                //check if the file exists, for it to be downloaded
                if (!targetFile.exists()) {
                    outputStream.writeUTF("NotFound");

                    inputStream.close();
                    outputStream.close();
                    return;
                }

                outputStream.writeUTF("Ready");

                sendFile(outputStream, targetFile);

                inputStream.close();
                outputStream.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void sendFile(DataOutputStream outputStream, File file) throws Exception {
        int bytes = 0;
        FileInputStream fileInputStream = new FileInputStream(file);

        // send file size
        outputStream.writeLong(file.length());
        // break file into chunks
        byte[] buffer = new byte[4 * 1024];
        while ((bytes = fileInputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytes);
            outputStream.flush();
        }
        fileInputStream.close();
    }
}
