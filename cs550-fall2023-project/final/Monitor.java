import org.bitcoinj.core.Base58;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class Monitor implements Runnable {
    private static ServerSocket serverSocket;
    private static Monitor.serverServices services;
    private static Thread thread;

    public Monitor() {

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
            ArrayList monitor = (ArrayList) yamlMap.get("monitor");

            HashMap serverMap = (HashMap) monitor.get(0);
            selfServerIP = (String) serverMap.get("server");
            HashMap portMap = (HashMap) monitor.get(1);
            selfServerPort = (int) portMap.get("port");
            HashMap threadMap = (HashMap) monitor.get(2);
            thread_num = (int) threadMap.get("threads");

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        System.out.println(Helper.get_timestamp() + " DSC v1.0");
        System.out.println(Helper.get_timestamp() + " Monitor server started with " + thread_num + " threads");

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable task = new collect_statistics();
        ScheduledFuture<?> scheduledFuture = scheduler.scheduleAtFixedRate(task, 0, Helper.block_time,
                TimeUnit.MILLISECONDS);

        try {
            serverSocket = new ServerSocket(selfServerPort);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while (true) {
            try {
                services = new Monitor.serverServices(serverSocket.accept());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            thread = new Thread(services);
            thread.start();
        }
    }

    class collect_statistics implements Runnable {
        public void stat_blockchain() throws IOException {
            String blockchainIP = Helper.get_server_IP("blockchain");
            int blockchainPort = Helper.get_server_port("blockchain");
            String message;
            String respond;

            message = "last_block_header";
            respond = Helper.callServer(blockchainIP, blockchainPort, message);
            String[] block_header = respond.split("\\|");
            int block_size = Integer.parseInt(block_header[0]);
            short version = (short) Integer.parseInt(block_header[1]);
            byte[] prev_hash = block_header[1].getBytes();
            int block_id = Integer.parseInt(block_header[3]);
            Long timestamp = Long.valueOf(block_header[4]);
            short difficulty = Integer.valueOf(block_header[5]).shortValue();
            long nonce = Long.parseLong(block_header[6]);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");
            Instant instant = Instant.ofEpochSecond(timestamp);
            Date date = Date.from(instant);
            String ts_format = dateFormat.format(date);

            System.out.println(Helper.get_timestamp() + " block_size: " + block_size);
            System.out.println(Helper.get_timestamp() + " version: " + version);
            System.out.println(Helper.get_timestamp() + " prev_hash: " + Base58.encode(prev_hash));
            System.out.println(Helper.get_timestamp() + " block_id: " + block_id);
            System.out.println(Helper.get_timestamp() + " timestamp: " + ts_format);
            System.out.println(Helper.get_timestamp() + " difficulty: " + difficulty);
            System.out.println(Helper.get_timestamp() + " nonce: " + nonce);

            message = "number_addresses";
            respond = Helper.callServer(blockchainIP, blockchainPort, message);
            System.out.println(Helper.get_timestamp() + " number of unique wallet addresses: " +
                    Integer.parseInt(respond));

            message = "number_total_coins";
            respond = Helper.callServer(blockchainIP, blockchainPort, message);
            System.out.println(Helper.get_timestamp() + " number of total coins in circulation: " +
                    Double.parseDouble(respond));
        }

        public void stat_pool() throws IOException {
            String poolIP = Helper.get_server_IP("pool");
            int poolPort = Helper.get_server_port("pool");
            String message;
            String respond;

            message = "number_transactions";
            respond = Helper.callServer(poolIP, poolPort, message);
            String[] strArray = respond.split(",");
            int submitted = Integer.valueOf(strArray[0]);
            int unconfirmed = Integer.valueOf(strArray[1]);

            System.out.println(Helper.get_timestamp() + " number of transactions in submitted: " + submitted);
            System.out.println(Helper.get_timestamp() + " number of transactions in unconfirmed: " + unconfirmed);

        }

        public void stat_metronome() throws IOException {
            String metronomeIP = Helper.get_server_IP("metronome");
            int metronomePort = Helper.get_server_port("metronome");
            String message;
            String respond;

            message = "number_validators";
            respond = Helper.callServer(metronomeIP, metronomePort, message);
            int number_validators = Integer.parseInt(respond);
            System.out.println(Helper.get_timestamp() + " number of validators: " + number_validators);

            message = "hash_stored";
            respond = Helper.callServer(metronomeIP, metronomePort, message);
            int hash_stored = Integer.parseInt(respond);
            System.out.println(Helper.get_timestamp() + " total hashes stored: " + hash_stored);

        }

        public void run() {
            try {
                stat_blockchain();
                stat_pool();
                stat_metronome();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class serverServices implements Runnable {

        private Socket clientSocket;

        serverServices(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {

        }
    }
}
