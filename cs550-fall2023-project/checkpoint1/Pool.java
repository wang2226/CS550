import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Pool implements Runnable {

    private static ServerSocket serverSocket;
    private static Pool.serverServices services;
    private static Thread thread;

    // ArrayList to store the blocks
    public static ArrayList<Transaction> pool = new ArrayList<Transaction>();

    /**
     * Default constructor to create the Pool Object.
     */
    public Pool() {
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
            ArrayList pool = (ArrayList) yamlMap.get("pool");

            HashMap serverMap = (HashMap) pool.get(0);
            selfServerIP = (String) serverMap.get("server");
            HashMap portMap = (HashMap) pool.get(1);
            selfServerPort = (int) portMap.get("port");
            HashMap threadMap = (HashMap) pool.get(2);
            thread_num = (int) threadMap.get("threads");

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }


        System.out.println(get_timestamp() + " DSC v1.0");
        System.out.println(get_timestamp() + " Pool server started with " + thread_num + " threads");
        try {
            serverSocket = new ServerSocket(selfServerPort);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Runnable task = new receive_transaction();
        Thread worker = new Thread(task);
        worker.start();
        while (true) {
            try {
                services = new Pool.serverServices(serverSocket.accept());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            thread = new Thread(services);
            thread.start();
        }
    }

    class receive_transaction implements Runnable {
        public void run() {
            String hash = "8cxiskBh2AJSNefWKPQ7ErfmLoM4hs4esGq8REu63C3U";
            String transaction_id = "0xEksacU61zEbu5kP6WBTW48";
            String status = "unknown";
            while (true) {
                Block new_block = new Block();
                String data = "this a test";

                try {
                    // hash = Crypt.sha256(data);
                    System.out.println(get_timestamp() + " Transaction id " + transaction_id +
                            " received from " + hash + ", ACK");
                    System.out.println(get_timestamp() + " Transaction id " + transaction_id +
                            " status [" + status + "]");
                    System.out.println(get_timestamp() + " Transactions for " + hash +
                            ", none found");
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

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
                    String txID;
                    String sender_public_Address;
                    String recipient_public_address;
                    double value;
                    String respond;

                    switch (requestType.toLowerCase()) {
                        case "send": // send message
                            txID = reciveArray[1];
                            sender_public_Address = reciveArray[2];
                            recipient_public_address = reciveArray[3];
                            value = Double.valueOf(reciveArray[4]);
                            respond = "submitted ";

                            System.out.println(" request: " + reciveString);

                            outputStream.writeUTF(respond);
                            outputStream.flush();

                            break;

                        case "transcation_1": // send message
                            txID = reciveArray[1];
                            sender_public_Address = reciveArray[2];
                            respond = "submitted ";

                            System.out.println(" request: " + reciveString);

                            outputStream.writeUTF(respond);
                            outputStream.flush();

                            break;
                        case "transcation_all": // send message
                            sender_public_Address = reciveArray[1];
                            respond = "submitted ";

                            System.out.println(" request: " + reciveString);

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
