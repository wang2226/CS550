import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GnutellaClient extends Gnutella {
    private static final int TTL = 2;
    // Defined a [peer ID, sequence number]
    protected static String messageID;

    GnutellaClient(String p2pType) {
        if (this.p2pType == null) {
            setP2PType(p2pType);
        } else if (!this.p2pType.equalsIgnoreCase(p2pType)) {
            setP2PType(p2pType);
        }

        initialNeighbors(p2pType);
    }

    protected int getTTL() {
        return TTL;
    }

    protected String makeID() {
        int min = 1;
        int max = Integer.MAX_VALUE - 1;
        int sequenceNumber = new Random().nextInt(max - min + 1) + min;
        return new String(WHO_AM_I + "_" + sequenceNumber);
    }


    // The query is sent to all neighbors.
    public void query(String messageID, int TTL, int hops, String fileName) throws IOException, InterruptedException {
        String queryMessage = "query" + "," + messageID + "," + TTL + "," + hops + "," + fileName + "," + WHO_AM_I;

        //System.out.println("WHOAMI=" + WHO_AM_I + ",message=" + queryMessage);

        ArrayList<String> nodes = neighbors.get(WHO_AM_I);

        // File locate itself node
        for (String file : fileOwned) {
            if (file.equalsIgnoreCase(fileName)) {
                setTargetNode(fileName, WHO_AM_I);
                return;
            }
        }
        setPathRouting(messageID, WHO_AM_I); // I send the query

        ExecutorService executor = Executors.newFixedThreadPool(nThreads);

        // Look up neighbors
        for (String node : nodes) {
            String IP = ReadProperties.getByName(node + "_IP");
            if (IP.equals("error")) {
                System.out.println("Query: get wrong IP, please note peer's name.");
                return;
            }

            String property = ReadProperties.getByName(node + "_Server_Port");
            if (property.equals("error")) {
                System.out.println("Query: get wrong port number, please note peer's name.");
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

    public static void obtain(String owner, String fileName) throws UnknownHostException {
        boolean isAlive;

        //Data resilience mechanism
        //Make a connection with server to get file from

        if (owner == null) { //means the file not found in any peer
            return;
        }

        if (searchLocal(fileName) || owner.equalsIgnoreCase(WHO_AM_I)) {
            System.out.println(fileName + " already on your node, no download required.");
            return;
        }

        String peerIP = ReadProperties.getByName(owner + "_IP");
        if (peerIP.equals("error")) {
            System.out.println("Obtain: wrong Peer ID, please note case sensitive");
            return;
        }

        String property = ReadProperties.getByName(owner + "_Server_Port");
        if (property.equals("error")) {
            System.out.println("Obtain: wrong Peer ID, please note case sensitive");
            return;
        }
        int peerPort = Integer.parseInt(property);

        isAlive = FileDownload.isSocketAlive(InetAddress.getByName(peerIP), peerPort);

        if (isAlive) {
            FileDownload.download(owner, fileName);
        } else {
            String backupNode = FileDownload.findBackup(owner);
            FileDownload.download(backupNode, fileName);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String option, retResult, fileName, ownerId;

        if (args.length != 1) {
            System.out.println("Usage: java GnutellaClient Star(or Mesh)");
            System.exit(1);
        }

        String p2pType = args[0];
        GnutellaServer server = new GnutellaServer(p2pType);
        Thread workThread = new Thread(server);
        workThread.start();


        GnutellaClient client = new GnutellaClient(p2pType);

        Scanner getInput = new Scanner(System.in);

        do {
            System.out.println("****MENU****");
            System.out.println("1. Search for a File");
            System.out.println("2. Obtain a File");
            System.out.println("3. Exit");

            option = getInput.nextLine();

            switch (option) {

                case "1": // case to search a file
                    System.out.println("Enter filename: ");
                    fileName = getInput.nextLine();
                    client.query(client.makeID(), client.getTTL(), 0, fileName);

                    Thread.sleep(2000);
                    String queryResult = getTargetNode(fileName);

                    if (queryResult == null) {
                        System.out.println("The file above does not exist or because of delay , try again!");
                        break;
                    }
                    System.out.println(queryResult);
                    break;

                case "2": // case to download a file

                    // writeCIS.println(option);
                    System.out.println("Enter the name of the file to be downloaded:");
                    fileName = getInput.nextLine();
                    System.out.println("Enter the peer id from where you want to download the file: ");
                    ownerId = getInput.nextLine();
                    obtain(ownerId, fileName);
                    break;

                case "3": // exit case

                    System.out.println("Client Closed!!");
                    System.exit(0);
                    break;
            }
        } while (!(option.equals("3")));
    }
}
