import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.*;
import java.util.Date;
import java.util.HashMap;


public class BlockChain implements Runnable {
    private static ServerSocket serverSocket;
    private static serverServices services;
    private static Thread thread;

    // ArrayList to store the blocks
    public static ArrayList<Block> blockChain = new ArrayList<Block>();
    private Block lastBlock;

    /**
     * Default constructor to create the BlockChain Object.
     */
    public BlockChain() {
    }

    public String get_timestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");
        String timestamp = dateFormat.format(new Date());
        return timestamp;
    }

    public void run() {
        String selfServerIP;
        int selfServerPort;
        int thread_num;
        Yaml yaml = new Yaml();
        InputStream inputStream = null;


        try {
            inputStream = new FileInputStream("dsc-config.yaml");
            HashMap yamlMap = yaml.load(inputStream);
            // Access HashMaps and ArrayList by key(s)
            ArrayList blockchain = (ArrayList) yamlMap.get("blockchain");

            HashMap serverMap = (HashMap) blockchain.get(0);
            selfServerIP = (String)serverMap.get("server");
            HashMap portMap = (HashMap) blockchain.get(1);
            selfServerPort = (int)portMap.get("port");
            HashMap threadMap = (HashMap) blockchain.get(2);
            thread_num = (int)threadMap.get("threads");

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }


        System.out.println(get_timestamp() + " DSC v1.0");
        System.out.println(get_timestamp() + " BlockChain server started with " + thread_num +" threads");


        try {
            serverSocket = new ServerSocket(selfServerPort);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Runnable task = new BlockChain.receive_block();
        Thread worker = new Thread(task);
        worker.start();
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
    class receive_block implements Runnable {
        public void run() {
            String hash = "HtBTNpCt5fmPrvESqVp1UFsiX5wnMCtmgt7Cxi85MFiF";
            int block_num = 1;
            String fingerprint = "95df69f2-4423-44da-8e8d-2d0357e69f8f";
            while (true) {

                try {
                    // hash = Crypt.sha256(data);
                    System.out.println(get_timestamp() + " New block received from metronome, Block " + block_num
                            + " hash " + hash);
                    System.out.println(get_timestamp() + " Block request from validator " +  fingerprint
                            + ", Block " + block_num + " hash " + hash);
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                block_num++;
            }
        }
    }
    class serverServices implements Runnable {

        private Socket clientSocket;

        serverServices(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {

            try {
                DataOutputStream outputStream = null;
                DataInputStream inputStream = null;

                while (true) {
                    String requestType = null, reciveString = null;

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
                        System.out.println("reciveString=" + reciveString);
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
                    String respond = "";

                    switch (requestType.toLowerCase()) {
                        case "balance": // balance message
                            String publicAddress = reciveArray[1];
                            double coins = 1024.0;
                            int block_num = 4;
                            respond = coins + "," + block_num;

                            outputStream.writeUTF(respond);
                            outputStream.flush();

                            System.out.println("request: " + reciveString + " respond: " + respond);
                            break;

                        case "hash": // last block message
                            respond = "11010101011101011000110101000000";

                            outputStream.writeUTF(respond);
                            outputStream.flush();

                            break;

                        default:
                            throw new IllegalStateException("Unexpected value: " + requestType);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
