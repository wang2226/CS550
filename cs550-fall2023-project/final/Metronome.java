import org.bitcoinj.core.Base58;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.*;

public class Metronome implements Runnable {
    private static ServerSocket serverSocket;
    private static Metronome.serverServices services;
    private static Thread thread;
    private static int current_block_id = 1;
    private static ArrayList<String> validators = new ArrayList<>();
    private static int difficulty;
    private static ArrayList<String> new_block_hash = new ArrayList<>();
    private static HashMap<Integer, String> win = new HashMap<>();

    /**
     * Default constructor to create the Metronome Object.
     */
    public Metronome() {
        this.difficulty = 30;
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
            ArrayList metronome = (ArrayList) yamlMap.get("metronome");

            HashMap serverMap = (HashMap) metronome.get(0);
            selfServerIP = (String) serverMap.get("server");
            HashMap portMap = (HashMap) metronome.get(1);
            selfServerPort = (int) portMap.get("port");
            HashMap threadMap = (HashMap) metronome.get(2);
            thread_num = (int) threadMap.get("threads");

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        System.out.println(Helper.get_timestamp() + " DSC v1.0");
        System.out.println(Helper.get_timestamp() + " Metronome server started with " + thread_num + " threads");

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable task = new create_block();
        ScheduledFuture<?> scheduledFuture = scheduler.scheduleAtFixedRate(task, 0, Helper.block_time,
                TimeUnit.MILLISECONDS);

        try {
            serverSocket = new ServerSocket(selfServerPort);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while (true) {
            try {
                services = new Metronome.serverServices(serverSocket.accept());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            thread = new Thread(services);
            thread.start();
        }
    }

    class create_block implements Runnable {
        public void send_blockchain(Block block) throws IOException {
            String IP = Helper.get_server_IP("blockchain");
            int port = Helper.get_server_port("blockchain");

            Helper.callServer(IP, port, "new_blocks" + "," + block.toString());
        }

        public void run() {
            if (!win.containsKey(current_block_id)) {
                // LinkedList<Transaction> txs = new LinkedList<>();
                byte[] prev_hash = new byte[32];

                Long timestamp = Instant.now().getEpochSecond();
                Block new_block = new Block(null, prev_hash, current_block_id, timestamp, (short) 30, 0);
                String hash = Helper.calculate_hash(new_block.toHash());
                new_block_hash.add(hash);

                try {
                    send_blockchain(new_block);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                System.out.println(Helper.get_timestamp() + " New block created, hash " +
                        Base58.encode(Helper.hexToBytes(hash)) + ", sent " + "to blockchain");
                current_block_id++;
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
                        case "difficulty": // difficulty level
                            int diff = calculate_diff();
                            respond = String.valueOf(diff);

                            outputStream.writeUTF(respond);
                            outputStream.flush();

                            break;
                        case "validators_register":
                            validators.add(reciveArray[1]);
                            respond = "success";

                            outputStream.writeUTF(respond);
                            outputStream.flush();
                            System.out.println(Helper.get_timestamp() + " Validator " +
                             reciveArray[1] + " request approved");

                            break;

                        case "win_proof":
                            int block_id = Integer.parseInt(reciveArray[1]);
                            String fingerprint = reciveArray[2];
                            int win_diff = Integer.parseInt(reciveArray[3]);
                            String hash = reciveArray[4];
                            long nonce = Long.parseLong(reciveArray[5]);
                            win.put(block_id, fingerprint);
                            respond = "success";

                            outputStream.writeUTF(respond);
                            outputStream.flush();
                            System.out.println(Helper.get_timestamp() + " Validator " +
                                    fingerprint + " claims to win block with diff " + win_diff
                                    + " hash " + hash + " NONCE " + nonce);

                            break;

                        case "number_validators":
                            respond = number_validators();

                            outputStream.writeUTF(respond);
                            outputStream.flush();

                            break;

                        case "hash_stored":
                            respond = hash_stored();

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

    // monitor number of validators, hashes/sec, total hashes stored
    public String number_validators() {
        return String.valueOf(validators.size());
    }

    public String hash_stored() {
        return String.valueOf(new_block_hash.size());
    }

    public int calculate_diff() {
        int vailidator_num = validators.size();
        if (vailidator_num < 4)
            return difficulty - 1;
        else if (vailidator_num > 8)
            return difficulty + 1;
        else
            return difficulty;
    }
}
