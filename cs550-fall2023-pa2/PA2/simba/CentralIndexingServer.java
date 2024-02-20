import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.*;

public class CentralIndexingServer {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        int cisPort = Integer.parseInt(ReadProperties.getByName("CIS_Port"));
        try {
            ServerSocket socket = new ServerSocket(cisPort);
            System.out.println("Server is up and running!!!");
            while (true) {
                Socket cisSocket = socket.accept();
                //thread creation
                Thread workThread = new Thread(new cisServices(cisSocket));
                workThread.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

/*
 * Class cisServices has all the functionality
 * It runs on the thread
 */
class cisServices implements Runnable {

    public static ConcurrentHashMap<String, CopyOnWriteArrayList<String>> fileMap = new ConcurrentHashMap<>();
    public Socket cisSocket;

    // constructor to initialize socket
    public cisServices(Socket socket) throws IOException {
        this.cisSocket = socket;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub

        while (true) {
            String peerId, fileName, fileList;
            String requestType;

            CopyOnWriteArrayList<String> fileLocation = new CopyOnWriteArrayList<String>();

            Scanner cisRecived;
            String reciveString = null;
            try {
                cisRecived = new Scanner(cisSocket.getInputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            PrintStream cisSend;
            try {
                cisSend = new PrintStream(cisSocket.getOutputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            //getting the option selected at client side
            if (cisRecived.hasNext()) {
                reciveString = cisRecived.nextLine();
            }else {
                break;
            }

            String[] reciveArray = reciveString.split(",");
            requestType = reciveArray[0];
            switch (requestType) {

                case "register": // case to register a file.
                    peerId = reciveArray[1];
                    // fileList = reciveArray[2];

                    // System.out.println("reciveArray[2]=" + reciveArray[2]);
                    String[] fileArray = reciveArray[2].split("\\|");

                    for (String s : fileArray) {
                       // System.out.println("s=" + s);

                        try {
                            registry(peerId, s);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    cisSend.println("success");

                    break;

                case "search": // case to search a file.
                    fileName = reciveArray[1];
                    try {
                        fileLocation = search(fileName);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    if (fileLocation != null && !fileLocation.isEmpty()) {
                        //System.out.println(location);
                        cisSend.println(fileLocation.get(0));
                    } else {
                        cisSend.println("NotFound");
                    }

                    break;

                default:
                    throw new IllegalStateException("Unexpected value: " + requestType);
            }
        }
    }

    /*
     * Register function - To register file present with each peer
     */
    public void registry(String peerId, String fileName) throws IOException {

        CopyOnWriteArrayList<String> newList = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<String> oldList = new CopyOnWriteArrayList<>();

        ReadWriteLock lock = new ReentrantReadWriteLock();
        Lock writeLock = lock.writeLock();

        newList.add(peerId);
        oldList = fileMap.get(fileName);

        try {
            // writeLock.lock();
            if (oldList == null || oldList.isEmpty()) {
                fileMap.put(fileName, newList);
            } else {
                oldList.add(peerId);
                fileMap.put(fileName, oldList);
            }
        } finally {
            // writeLock.unlock();
        }
    }

    /*
     * Lookup file - Search file return the peer with which it is present
     */
    public CopyOnWriteArrayList <String> search(String fileName) throws IOException {
        CopyOnWriteArrayList<String> peerList = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<String> ret = new CopyOnWriteArrayList<>();
        peerList = fileMap.get(fileName);
        if(peerList != null) {
            return peerList;
        }
        else {
            ret.add("NotFound");
            return   ret;
        }
    }
}
