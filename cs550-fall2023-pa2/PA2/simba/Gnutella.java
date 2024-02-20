import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.*;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


abstract class Gnutella {
    protected static String WHO_AM_I = null;
    protected static String directory;
    protected static CopyOnWriteArrayList<String> fileOwned = new CopyOnWriteArrayList<String>();
    protected static ConcurrentHashMap<String, ArrayList<String>> neighbors = new ConcurrentHashMap<String, ArrayList<String>>();
    // Stores [message ID, upstream peer ID] pairs, upstream peer ID is peer's hostname
    protected static ConcurrentHashMap<String, String> pathRouting = new ConcurrentHashMap<>();
    protected static String selfIP;
    protected static int selfServerPort;
    protected static String p2pType;
    protected static final int nThreads = 1000;
    protected static ConcurrentHashMap<String, String> targetNode = new ConcurrentHashMap<String, String>();

    Gnutella() {

        try {
            InetAddress localHost = InetAddress.getLocalHost();
            this.WHO_AM_I = localHost.getHostName();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.directory = ReadProperties.getByName("Shared_Directory");
        this.selfIP = ReadProperties.getByName(WHO_AM_I + "_IP");
        this.selfServerPort = Integer.parseInt(ReadProperties.getByName(WHO_AM_I + "_Server_Port"));

        localFiles();
    }

    static void setP2PType(String type) {
        p2pType = type;
    }

    static void setTargetNode(String fileName, String target) {
        targetNode.put(fileName, target);
    }

    static String getTargetNode(String fileName) {
        return targetNode.get(fileName);
    }

    static void clearTargetNode() {
        targetNode.clear();
    }

    static void setPathRouting(String messageID, String upstreamOwner) {
        pathRouting.put(messageID, upstreamOwner);
    }

    static String getPathRouting(String messageID) {
        String upstreamOwner = "";

        upstreamOwner = pathRouting.get(messageID);
        return upstreamOwner;
    }

    static boolean isIDExist(String messageID) {
        return pathRouting.containsKey(messageID);
    }

    public void initialNeighbors(String p2pType) {
        String nodes;
        if (p2pType.equalsIgnoreCase("Star")) {
            nodes = ReadProperties.getByName("Star_" + WHO_AM_I + "_Neighbors");
            ArrayList<String> host = new ArrayList<>(Arrays.asList(nodes.split(",")));
            neighbors.put(WHO_AM_I, host);
        } else if (p2pType.equalsIgnoreCase("Mesh")) {
            nodes = ReadProperties.getByName("Mesh_" + WHO_AM_I + "_Neighbors");
            ArrayList<String> host = new ArrayList<>(Arrays.asList(nodes.split(",")));
            neighbors.put(WHO_AM_I, host);
        }

    }

    public void localFiles() {
        Path dir = Paths.get(ReadProperties.getByName("Shared_Directory"));
        String pattern = "*_" + WHO_AM_I + "_*"; // Wildcard pattern to match local  files

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, pattern)) {
            for (Path entry : stream) {
                if (!Files.isDirectory(entry)) {
                    fileOwned.add(String.valueOf(entry.getFileName()));
                }
            }
        } catch (IOException | DirectoryIteratorException e) {
            throw new RuntimeException(e);
        }

    }

    /*
     * Lookup file - Search file in local node
     */
    public static boolean searchLocal(String fileName) {
        boolean exist = false;
        Iterator<String> itr = fileOwned.iterator();

        while (itr.hasNext()) {
            String file = itr.next();
            if (file.equalsIgnoreCase(fileName)) {
                exist = true;
                break;
            }
        }
        return exist;
    }

    public void callServer(String message, String nodeName) throws IOException {
        String IP = ReadProperties.getByName(nodeName + "_IP");
        int port = Integer.parseInt(ReadProperties.getByName(nodeName + "_Server_Port"));
        Socket socket = null;
        DataOutputStream outputStream = null;

        //System.out.println("callServer: nodeName=" + nodeName + " message=" + message);
        try {
            try {
                socket = new Socket(IP, port);

                // Create stream writer and reader

                outputStream = new DataOutputStream(socket.getOutputStream());

                // Send message to server
                outputStream.writeUTF(message);
                outputStream.flush();
            } catch (NullPointerException e) {
                System.out.println("Null Pointer Exception");
            }

        } catch (IOException e) {
            //outputStream.close();
            socket.close();
            throw new RuntimeException(e);
        }
    }
}
