import org.bitcoinj.core.Base58;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.ArrayList;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;

public class BlockChain implements Runnable {
    private static ServerSocket serverSocket;
    private static serverServices services;
    private static Thread thread;

    // ArrayList to store the blocks
    public static LinkedList<Block> blockChain = new LinkedList<Block>();
    public static HashMap<Integer, Integer> confirmed = new HashMap<>();
    public static HashMap<Integer, Integer> empty = new HashMap<>();
    public static HashMap<Integer, ArrayList<Transaction>> block_txs = new HashMap<>();

    // public static int current_block_id = 1;
    public static HashMap<String, ArrayList<String>> addr_balance = new HashMap<>();
    public static ArrayList<String> unique_wallet_addresses = new ArrayList<>();
    public static double number_total_coins = 0.0;
    public static HashMap<String, Transaction> tx_specific = new HashMap<>();
    public static HashMap<String, ArrayList<Transaction>> tx_all = new HashMap<>();

    /**
     * Default constructor to create the BlockChain Object.
     */
    public BlockChain() {
        BlockChain.blockChain.add(this.create_genesis_Block());
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
            selfServerIP = (String) serverMap.get("server");
            HashMap portMap = (HashMap) blockchain.get(1);
            selfServerPort = (int) portMap.get("port");
            HashMap threadMap = (HashMap) blockchain.get(2);
            thread_num = (int) threadMap.get("threads");

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        System.out.println(Helper.get_timestamp() + " DSC v1.0");
        System.out.println(Helper.get_timestamp() + " BlockChain server started with " + thread_num + " threads");

        try {
            serverSocket = new ServerSocket(selfServerPort);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
                    String respond = "";

                    switch (requestType.toLowerCase()) {
                        case "balance": // balance message
                            String publicAddress = reciveArray[1];
                            double coins = 1.0;
                            int block_id = 0;
                            if (addr_balance.containsKey(publicAddress)) {
                                ArrayList<String> str_list = addr_balance.get(publicAddress);
                                String str = str_list.get(0);
                                String[] strArray = str.split(",");
                                coins = Double.parseDouble(strArray[0]);
                                block_id = Integer.parseInt(strArray[1]);
                            }
                            respond = coins + "," + block_id;

                            outputStream.writeUTF(respond);
                            outputStream.flush();

                            System.out.println(Helper.get_timestamp() +
                                    " Balance request for " + publicAddress + ", " + coins + " coins");
                            break;

                        case "win_proof": // someone win
                            get_confirmed_tx(reciveArray);
                            respond = "success";

                            outputStream.writeUTF(respond);
                            outputStream.flush();

                            break;

                        case "new_blocks": // process new blocks from the metronome

                            process_new_block(reciveArray);
                            respond = "success";

                            outputStream.writeUTF(respond);
                            outputStream.flush();
                            break;

                        case "transaction_all": // request all transaction status

                            respond = process_transaction_all(reciveArray);

                            outputStream.writeUTF(respond);
                            outputStream.flush();
                            break;

                        case "transaction_specific": // request specific transaction status

                            respond = process_transaction_specific(reciveArray);

                            outputStream.writeUTF(respond);
                            outputStream.flush();
                            break;

                        case "block_request": // Block request from validator

                            respond = process_block_request(reciveArray);

                            outputStream.writeUTF(respond);
                            outputStream.flush();
                            break;

                        case "last_block_header":

                            respond = get_last_block_header();

                            outputStream.writeUTF(respond);
                            outputStream.flush();
                            break;

                        case "number_addresses":

                            respond = number_addresses();

                            outputStream.writeUTF(respond);
                            outputStream.flush();
                            break;

                        case "number_total_coins":

                            respond = number_total_coins();

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

        public void get_confirmed_tx(String[] reciveArray) throws IOException {
            String logFile = "Tx_Confirmed.log";
            BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
            PrintWriter pWriter = new PrintWriter(writer);

            int block_id = Integer.parseInt(reciveArray[1]);
            String transactions = reciveArray[2];
            // System.out.println("get_confirmed_tx:transactions = " + transactions);
            if (transactions.equalsIgnoreCase("Not Found")) {
                System.out.println("********************************************************");
                System.out.println("**No submitted found, pls send transaction from wallet**");
                System.out.println("********************************************************");
                pWriter.close();
                return;
            }

            int index = empty.get(block_id);
            confirmed.put(block_id, index);

            ArrayList<Transaction> tx_list = new ArrayList<>();
            String[] txs = transactions.split("#");
            for (int i = 0; i < txs.length; i++) {
                String[] tx = txs[i].split("\\|");

                String sender = tx[0];
                String recipient = tx[1];
                double value = Double.parseDouble(tx[2]);
                long timestamp = Long.parseLong(tx[3]);
                String txId = tx[4];
                String sign = tx[5];

                Transaction t = new Transaction(Helper.hexToBytes(sender),
                        Helper.hexToBytes(recipient), value, timestamp, txId);
                tx_list.add(t);

                if (!unique_wallet_addresses.contains(sender)) {
                    unique_wallet_addresses.add(sender);
                }

                number_total_coins += value;

                tx_specific.put(txId, t);
                if (tx_all.containsKey(sender)) {
                    ArrayList<Transaction> list = tx_all.get(sender);
                    list.add(t);
                    tx_all.put(sender, list);

                } else {
                    ArrayList<Transaction> list = new ArrayList<>();
                    list.add(t);
                    tx_all.put(sender, list);
                }

                if (addr_balance.containsKey(recipient)) {
                    ArrayList<String> str_list = addr_balance.get(recipient);
                    String str = value + "," + block_id;
                    str_list.add(str);
                    addr_balance.put(recipient, str_list);

                } else {
                    ArrayList<String> str_list = new ArrayList<>();
                    String str = value + "," + block_id;
                    str_list.add(str);
                    addr_balance.put(recipient, str_list);
                }
                String content = "Latency & Throughput for Tx Confirmed: " + Helper.get_timestamp()
                        + ", transaction id: " + txId;
                pWriter.println(content);
            }
            block_txs.put(block_id, tx_list);
            pWriter.close();
        }

        public void process_new_block(String[] reciveArray) {
            String[] block_header = reciveArray[1].split("\\|");
            LinkedList<Transaction> txs = new LinkedList<>();
            String prev_hash = get_last_block_hash();
            int block_id = Integer.parseInt(block_header[3]);
            Long timestamp = Long.valueOf(block_header[4]);
            short difficulty = Integer.valueOf(block_header[5]).shortValue();
            long nonce = Long.parseLong(block_header[6]);

            Block new_block = new Block(txs, Helper.hexToBytes(prev_hash), block_id, timestamp, difficulty, nonce);
            int index = blockChain.size();
            blockChain.add(new_block);
            empty.put(block_id, index);
            // current_block_id = block_id;
            String hash = Helper.calculate_hash(new_block.toHash());

            System.out.println(Helper.get_timestamp() + " New block received from metronome, Block " + block_id
                    + " hash " + Base58.encode(Helper.hexToBytes(hash)));
        }

        public String process_block_request(String[] reciveArray) {
            String fingerprint = reciveArray[1];
            int current_block_id = Integer.parseInt(reciveArray[2]);
            int index = empty.get(current_block_id);
            Block block_empty = blockChain.get(index);
            String hash = Helper.calculate_hash(block_empty.toHash());
            String ret = current_block_id + "," + hash;
            System.out.println(Helper.get_timestamp() + " Block request from validator " + fingerprint
                    + ", Block " + current_block_id + " hash " + Base58.encode(Helper.hexToBytes(hash)));
            // current_block_id++;
            return ret;
        }

        public String process_transaction_all(String[] reciveArray) {
            String addr = reciveArray[2];
            String status = "unknown";

            if (tx_all.containsKey(addr))
                status = "confirmed";

            System.out.println(Helper.get_timestamp() + " Transaction request status for " + addr +
                    ", " + status);
            return status;
        }

        public String process_transaction_specific(String[] reciveArray) {
            String txId = reciveArray[1];
            String status = "unknown";
            if (tx_specific.containsKey(txId))
                status = "confirmed";

            System.out.println(Helper.get_timestamp() + " Transaction request status for " + txId +
                    ", " + status);
            return status;
        }

    }

    public Block create_genesis_Block() {
        LinkedList<Transaction> txs = new LinkedList<>();
        byte[] prev_hash = new byte[32];
        Long timestamp = Instant.now().getEpochSecond();
        Block genesis_block = new Block(txs, prev_hash, 0, timestamp, (short) 30, 0);
        return genesis_block;
    }

    public String get_last_block_hash() {
        Block last_block = blockChain.getLast();
        return Helper.calculate_hash(last_block.toHash());
    }

    // monitor, last block header
    public String get_last_block_header() {
        Block last_block = blockChain.getLast();
        return last_block.toString();
    }

    // monitor, number of unique wallet addresses
    public String number_addresses() {
        return String.valueOf(unique_wallet_addresses.size() + 1);
    }

    // monitor, number of total coins in circulation
    public String number_total_coins() {
        return String.valueOf(number_total_coins);
    }

}
