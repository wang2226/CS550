import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GnutellaServer extends Gnutella implements Runnable {
    private static ServerSocket serverSocket;
    private static serverServices services;
    private static Thread thread;

    GnutellaServer(String p2pType) {
        super.initialNeighbors(p2pType);
        super.localFiles();
        super.setP2PType(p2pType);
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(selfServerPort);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Server is up and running!!!");

        while (true) {
            try {
                services = new serverServices(serverSocket.accept());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            thread = new Thread(services);
            thread.start();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        if (args.length != 1) {
            System.out.println("Usage: java GnutellaServer Star(or Mesh)");
            System.exit(1);
        }

        String p2pType = args[0];
        GnutellaServer server = new GnutellaServer(p2pType);
        // Thread workThread = new Thread(server);
        // workThread.start();
        server.run();

    }

}

class serverServices extends Gnutella implements Runnable {

    private Socket clientSocket;

    serverServices(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {

        try {
            DataOutputStream outputStream = null;
            DataInputStream inputStream = null;

            while (true) {
                String messageID, fileName, fileOwner, upPeer, fileDownloadPath;
                String requestType = null, reciveString = null;
                int TTL, hops;

                //Read incoming message
                try {
                    inputStream = new DataInputStream(clientSocket.getInputStream());
                    outputStream = new DataOutputStream(clientSocket.getOutputStream());

                    try {
                        reciveString = inputStream.readUTF();
                    } catch (EOFException eofe) {
                        //System.out.println("End of data stream reached");
                        break;
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }

                } catch (IOException e) {
                    System.out.println("WHOAMI=" + WHO_AM_I + ", reciveString=" + reciveString);
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    throw new RuntimeException(e);
                }

                String[] reciveArray = reciveString.split(",");
                requestType = reciveArray[0];

                switch (requestType) {
                    case "query": // query message
                        messageID = reciveArray[1];
                        TTL = Integer.parseInt(reciveArray[2]);
                        hops = Integer.parseInt(reciveArray[3]);
                        fileName = reciveArray[4];
                        upPeer = reciveArray[5];

                        TTL = TTL - 1;
                        hops = hops + 1;

                        // Discard, have seen before
                        if (isIDExist(messageID)) {
                            return;
                        } else {
                            setPathRouting(messageID, upPeer); // set upstream peers
                        }

                        // The file is on this node, send hitquery to uptream peer
                        if (searchLocal(fileName)) {
                            hitQuery(messageID, hops, fileName, WHO_AM_I, upPeer);
                            //setTargetNode(fileName, WHO_AM_I); // should be removed

                        } else if (TTL > 0) {
                            broadcastQuery(messageID, TTL, hops, fileName, upPeer);
                        }

                        break;

                    case "hitquery": //hit message
                        messageID = reciveArray[1];
                        hops = Integer.parseInt(reciveArray[2]);
                        fileName = reciveArray[3];
                        fileOwner = reciveArray[4];

                        hops = hops - 1;

                        // never before seen
                        if (!isIDExist(messageID)) {
                            return;
                        }

                        if (hops == 0) {// Come to the original spot
                            setTargetNode(fileName, fileOwner);
                        } else { // Continue propagate back hitquery
                            upPeer = getPathRouting(messageID);
                            hitQuery(messageID, hops, fileName, fileOwner, upPeer);
                        }

                        break;
                    case "download": //download required file
                        //get the fileName from ClientAskingForFile
                        fileName = reciveArray[1];
                        fileDownloadPath = ReadProperties.getByName("Shared_Directory");
                        File targetFile = new File(fileDownloadPath + "/" + fileName);

                        try {
                            //check if the file exists, for it to be downloaded
                            if (!targetFile.exists()) {
                                outputStream.writeUTF("NotFound");

                                // inputStream.close();
                                // outputStream.close();
                                return;
                            }

                            outputStream.writeUTF("Ready");
                            outputStream.flush();

                            sendFile(outputStream, targetFile);

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + requestType);
                }
            }
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void hitQuery(String messageID, int hops, String fileName, String fileOwner, String upPeer) throws IOException {
        String hitMessage = new String("hitquery," + messageID + "," + hops + "," + fileName + "," + fileOwner);
        callServer(hitMessage, upPeer);
    }

    public void broadcastQuery(String messageID, int TTL, int hops, String fileName, String upPeer) throws InterruptedException {
        ArrayList<String> nodes = new ArrayList<String>();
        String queryMessage = "query" + "," + messageID + "," + TTL + "," + hops + "," + fileName + "," + WHO_AM_I;

        nodes = neighbors.get(WHO_AM_I);

        // Remove upstream peer
        nodes.remove(upPeer);

        if (nodes.isEmpty()) {
            System.out.println("BroadcastQuery: no neighbor.");
            return;
        }
        if (targetNode.containsKey(fileName)) {
            System.out.println("BroadcastQuery: file have found.");
            return;
        }

        ExecutorService executor = Executors.newFixedThreadPool(nThreads);

        // Look up neighbors
        for (String node : nodes) {
            String IP = ReadProperties.getByName(node + "_IP");
            if (IP.equals("error")) {
                System.out.println("BroadcastQuery: get wrong IP, please note peer's name.");
                return;
            }

            String property = ReadProperties.getByName(node + "_Server_Port");
            if (property.equals("error")) {
                System.out.println("BroadcastQuery: get wrong port number, please note peer's name.");
                return;
            }
            int port = Integer.parseInt(property);

            Runnable worker = new SendNeighbor(IP, port, queryMessage);
            executor.execute(worker);
        }
        // This will make the executor accept no new threads and finish all existing threads in the queue
        // executor.shutdown();
        // Wait until all threads are finish
        // executor.awaitTermination(1000, TimeUnit.MICROSECONDS);
    }

    private static void sendFile(DataOutputStream out, File file) throws Exception {
        int bytes = 0;
        FileInputStream fileInputStream = new FileInputStream(file);

        // send file size
        out.writeLong(file.length());
        // break file into chunks
        byte[] buffer = new byte[4 * 1024];
        while ((bytes = fileInputStream.read(buffer)) != -1) {
            out.write(buffer, 0, bytes);
            out.flush();
        }
        fileInputStream.close();
    }
}

