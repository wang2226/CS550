import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Pool implements Runnable {

    private static ServerSocket serverSocket;
    private static Pool.serverServices services;
    private static Thread thread;

    // ArrayList to store the blocks
    public static CopyOnWriteArrayList<Transaction> submitted_list = new CopyOnWriteArrayList<>();
    public static ConcurrentHashMap<String, Integer> submitted_map = new ConcurrentHashMap<>();
    public static CopyOnWriteArrayList<Transaction> unconfirmed_list = new CopyOnWriteArrayList<>();
    public static ConcurrentHashMap<String, Integer> unconfirmed_map = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, String> status_map = new ConcurrentHashMap<>();
    // include sender & recipient
    public static ConcurrentHashMap<String, ArrayList<String>> all_status_map = new ConcurrentHashMap<>();

    /**
     * Default constructor to create the Pool Object.
     */
    public Pool() {
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

        System.out.println(Helper.get_timestamp() + " DSC v1.0");
        System.out.println(Helper.get_timestamp() + " Pool server started with " + thread_num + " threads");
        try {
            serverSocket = new ServerSocket(selfServerPort);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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

                    // Read incoming message
                    try {
                        inputStream = new DataInputStream(clientSocket.getInputStream());
                        outputStream = new DataOutputStream(clientSocket.getOutputStream());

                        try {
                            reciveString = inputStream.readUTF();
                        } catch (EOFException eofe) {
                            // System.out.println("End of data stream reached");
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
                    // String signedStr;
                    long time_stamp;
                    String respond;

                    switch (requestType.toLowerCase()) {
                        case "send": // send message
                            txID = reciveArray[1];
                            sender_public_Address = reciveArray[2];
                            recipient_public_address = reciveArray[3];
                            value = Double.valueOf(reciveArray[4]);
                            // signedStr = reciveArray[5];
                            time_stamp = Long.valueOf(reciveArray[5]);

                            recive_send(sender_public_Address, recipient_public_address, value, time_stamp, txID);
                            respond = "submitted";

                            // System.out.println(" request: " + reciveString);

                            outputStream.writeUTF(respond);
                            outputStream.flush();

                            break;

                        case "transaction_specific": // send message
                            txID = reciveArray[1];
                            sender_public_Address = reciveArray[2];
                            respond = receive_transaction_specific(txID);

                            // System.out.println(" request: " + reciveString);

                            outputStream.writeUTF(respond);
                            outputStream.flush();

                            break;

                        case "transaction_all": //
                            sender_public_Address = reciveArray[1];
                            respond = receive_transaction_all(sender_public_Address);

                            // System.out.println(" request: " + reciveString);

                            outputStream.writeUTF(respond);
                            outputStream.flush();

                            break;

                        case "get_transactions": //
                            String fingerprint = reciveArray[1];
                            respond = send_transactions();

                            // System.out.println(" request: " + reciveString);

                            outputStream.writeUTF(respond);
                            outputStream.flush();

                            break;

                        case "win_proof": //
                            confirmed_tx(reciveArray);
                            respond = "success";

                            outputStream.writeUTF(respond);
                            outputStream.flush();

                            break;

                        case "number_transactions": //
                            respond = number_transactions();

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

        public void recive_send(String sender, String recipient, double value, Long time_stamp, String txId) {
            Transaction tx = new Transaction(sender.getBytes(StandardCharsets.UTF_8),
                    recipient.getBytes(StandardCharsets.UTF_8), value, time_stamp, txId);

            submitted_list.add(tx);
            int index = submitted_list.indexOf(tx);
            submitted_map.put(txId, index);
            status_map.put(txId, "submitted");

            ArrayList<String> list;
            if (all_status_map.containsKey(sender)) {
                list = all_status_map.get(sender);
                list.add("submitted");
            } else {
                list = new ArrayList<>();
                list.add("submitted");
            }
            all_status_map.put(sender, list);

            if (all_status_map.containsKey(recipient)) {
                list = all_status_map.get(recipient);
                list.add("submitted");
            } else {
                list = new ArrayList<>();
                list.add("submitted");
            }
            all_status_map.put(recipient, list);
        }

        public void add_unconfirmed(Transaction tx) {
            unconfirmed_list.add(tx);
            int index = unconfirmed_list.indexOf(tx);
            unconfirmed_map.put(tx.get_txId(), index);
            status_map.replace(tx.get_txId(), "unconfirmed");
        }

        public void remove_unconfirmed(String txId) {
            for (int index = 0; index < unconfirmed_list.size(); index++) {
                if (unconfirmed_list.get(index).get_txId().equalsIgnoreCase(txId))
                    unconfirmed_list.remove(index);
                unconfirmed_map.remove(txId);
                status_map.replace(txId, "confirmed");
            }
        }

        public void confirmed_tx(String[] reciveArray) {
            int block_id = Integer.parseInt(reciveArray[1]);
            String transactions = reciveArray[2];

            if (transactions.equalsIgnoreCase("Not Found")) {
                System.out.println("\n");
                System.out.println("***********************************************************");
                System.out.println("**No tx to be confirmed, pls send transaction from wallet**");
                System.out.println("***********************************************************");
                System.out.println("\n");
                return;
            }
            // System.out.println("confirmed_tx:transactions = " + transactions);

            String[] txs = transactions.split("#");
            for (int i = 0; i < txs.length; i++) {
                String[] tx = txs[i].split("\\|");

                String sender = tx[0];
                String recipient = tx[1];
                double value = Double.parseDouble(tx[2]);
                long timestamp = Long.parseLong(tx[3]);
                String txId = tx[4];
                String sign = tx[5];

                remove_unconfirmed(txId);
            }
        }

        public String send_transactions() {
            String transactions = "";
            final int max_transactions = 166;
            int loop_times;

            ArrayList<Transaction> txList = new ArrayList<>();
            synchronized (this) {
                if (submitted_list.size() <= max_transactions)
                    loop_times = submitted_list.size();
                else
                    loop_times = max_transactions;

                for (int index = 0; index < loop_times; index++) {
                    if (index < submitted_list.size()) {
                        Transaction tx = submitted_list.get(index);
                        txList.add(tx);
                        transactions += tx.toString() + "#";
                    }
                }

                if (!txList.isEmpty()) {
                    for (Transaction tx : txList) {
                        submitted_list.remove(tx);
                        submitted_map.remove(tx.get_txId());
                        add_unconfirmed(tx);
                    }
                }
            }

            if (transactions == "") {
                System.out.println("********************************************");
                System.out.println("**No tx send to validator add to new block**");
                System.out.println("**Please send transaction from wallet     **");
                System.out.println("********************************************");
                return "Not Found";
            }

            return transactions;
        }

        public String receive_transaction_specific(String transaction_id) {

            String status = "unknown";
            if (status_map.containsKey(transaction_id))
                status = status_map.get(transaction_id);

            System.out.println(Helper.get_timestamp() + " Transaction request status for " + transaction_id +
                    ", " + status);
            return status;
        }

        public String receive_transaction_all(String pubKey) {

            String status = "unknown";
            if (all_status_map.containsKey(pubKey))
                status = all_status_map.get(pubKey).get(0);

            System.out.println(Helper.get_timestamp() + " Transaction request status for " + pubKey +
                    ", " + status);
            return status;
        }
    }

    // monitor, number of transactions in submitted and unconfirmed
    public String number_transactions() {
        int submitted = submitted_list.size();
        int unconfirmed = unconfirmed_list.size();
        return submitted + "," + unconfirmed;
    }
}
