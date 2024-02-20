import java.io.*;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.yaml.snakeyaml.Yaml;

/**
 * Proof algorithm
 */
class ProofOfWork implements Runnable {
    public String hash_lookup = "";
    public String fingerprint = "";
    public String public_key = "";
    public int difficulty = 30;
    public int NONCE = 0;
    public int block_id;
    public long counter = 0;

    private final Object lock = new Object();

    ProofOfWork(String fingerprint, String public_key, int difficulty, String hash_lookup, int block_id) {
        this.public_key = public_key;
        this.fingerprint = fingerprint;
        this.difficulty = difficulty;
        this.hash_lookup = hash_lookup;
        this.block_id = block_id;
    }

    public long pow_lookup(String prefix_hash_lookup) {
        double start_time = System.currentTimeMillis();

        counter = 0;
        while (System.currentTimeMillis() < (start_time + Helper.block_time)) {
            String hash_input = this.fingerprint + this.public_key + Long.toString(this.NONCE);
            byte[] hash_output_byte = Helper.blake3(hash_input);
            synchronized (lock) {
                counter++;
            }
            String prefix_hash_output = Helper.ByteArraysToBinary(hash_output_byte).substring(0, this.difficulty);

            if (prefix_hash_lookup.equals(prefix_hash_output))
                return this.NONCE;
            else
                synchronized (lock) {
                    this.NONCE++;
                }
        }
        return -1;
    }

    // Set the difficulty by typing the amount of zeros that you want to the hash to
    // begin
    public void run() {

        String prefix_hash_lookup = Helper.ByteArraysToBinary(hash_lookup.getBytes()).substring(0, this.difficulty);
        System.out.println(Helper.get_timestamp() + " block " +
                block_id + ", diff " + this.difficulty + ", hash " + prefix_hash_lookup);

        long mined = pow_lookup(prefix_hash_lookup);

        System.out.println(Helper.get_timestamp() + " block " +
                this.block_id + ", NONCE " + mined + " (" + Helper.df2.format(this.counter / 1000000) + " MH/s)");
        if (mined != -1) {
            try {
                Validator.send_proof(block_id, fingerprint, difficulty, hash_lookup, mined);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

}

class memory_store {
    private static byte[][] memory_store;

    public static void sortMemoryStore() {

        Arrays.sort(memory_store, (byte[] a, byte[] b) -> 0);
        // for (int i = 1; i < 10; i++) {
        // System.out.println("i = " + i);
        // System.out.println("memory_store[" + i + "]= " +
        // Blake.bytesToHex(memory_store[i]));
        // }
    }

    public static void createMemoryStore(int size) {
        memory_store = new byte[size][];
    }

    public static void setMemoryStore(int index, byte[] data) {
        memory_store[index] = Arrays.copyOf(data, data.length);
    }

    public static byte[] getMemoryStore(int index) {
        return memory_store[index];
    }

    // set difficulty parameter
    public static long lookupMemoryStore(String prefix_hash_lookup, int difficulty, int size) {

        int left = 0;
        int right = size;

        while (left <= right) {
            int mid = left + (right - left) / 2;

            byte[] hash_output = Arrays.copyOfRange(memory_store[mid], 0, 24);
            byte[] nonce = Arrays.copyOfRange(memory_store[mid], 24, 32);
            String prefix_hash_output = Helper.ByteArraysToBinary(hash_output).substring(0, difficulty);

            // Check if prefix_hash_lookup is present at mid
            if (prefix_hash_output.compareTo(prefix_hash_lookup) == 0)
                return Helper.bytesToLong(nonce);

            // If prefix_hash_lookup greater, ignore left half
            if (prefix_hash_output.compareTo(prefix_hash_lookup) < 0)
                left = mid + 1;

            // If prefix_hash_lookup is smaller, ignore right half
            else
                right = mid - 1;
        }

        // If reach here, then element was not present
        return -1;
    }
}

class ProofOfMemory implements Runnable {

    public String fingerprint = "";
    public String public_key = "";
    public int num_hashes = 0;
    public static long NONCE = 0;

    ProofOfMemory(String fingerprint, String public_key, int num_hashes) {

        this.fingerprint = fingerprint;
        this.public_key = public_key;
        this.num_hashes = num_hashes;
    }

    public void run() {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int START = Integer.valueOf(Thread.currentThread().getName()) * this.num_hashes;
        int END = START + this.num_hashes;

        if (Integer.valueOf(Thread.currentThread().getName()) == 1) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {

            }
        }

        for (int i = START; i < END; i++) {

            String hash_input = this.fingerprint + this.public_key + ProofOfMemory.NONCE;
            byte[] hash_output_byte = Helper.blake3(hash_input);

            try {
                byte[] first = hash_output_byte;
                byte[] second = Helper.longToBytes(ProofOfMemory.NONCE);
                byte[] combined = new byte[first.length + second.length];

                System.arraycopy(first, 0, combined, 0, first.length);
                System.arraycopy(second, 0, combined, first.length, second.length);

                memory_store.setMemoryStore(i, combined);
                outputStream.flush();
                // synchronized (this) {
                // System.out.println("memory_store[" + i + "]= " +
                // Blake.bytesToHex(memory_store.getMemoryStore(i)));
                // }

                synchronized (this) {
                    ProofOfMemory.NONCE++;
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

class ProofOfStorage implements Runnable {
    private String fingerprint = "";
    private String public_key = "";
    private String disk = "";
    private int buckets = 0;
    private int cup_size = 0;
    private int cups_per_bucket = 0;
    public int num_hashes = 0;
    public static byte[][][] cup;
    public static long NONCE = 0;
    public static File vaultFile;
    public static int[] cup_counter = new int[256]; // order within cup
    public static int[] cup_number = new int[256]; // cups within bucket

    ProofOfStorage(File vaultFile, String fingerprint, String public_key,
            String disk, int buckets, int cup_size, int cups_per_bucket, int num_hashes, byte[][][] cup) {
        this.vaultFile = vaultFile;
        this.fingerprint = fingerprint;
        this.public_key = public_key;
        this.disk = disk;
        this.buckets = buckets;
        this.cup_size = cup_size;
        this.cups_per_bucket = cups_per_bucket;
        this.num_hashes = num_hashes;
        this.cup = cup;
    }

    public static final boolean bytes_all_zeros(final byte[] array, final int start, final int length) {
        for (int i = start; i < start + length; i++) {
            if (array[i] != 0) {
                return false;
            }
        }
        return true;
    }

    public void pos_write_residues() throws IOException {

        for (int bucket = 0; bucket < 256; bucket++) {
            File bucket_name = new File(this.vaultFile + "/" + "bucket" + String.format("%03d", bucket));
            RandomAccessFile raf = new RandomAccessFile(bucket_name, "rw");
            long position = cup_number[bucket] * (this.cup_size * 32);
            raf.seek(position);

            for (int i = 0; i < this.cup_size; i++) {
                if (!bytes_all_zeros(cup[bucket][i], 0, 32)) {
                    // System.out.println("bucket = " + bucket + " i = " + i + " byte[]=" +
                    // Helper.bytesToHex(cup[bucket][i]));
                    raf.write(cup[bucket][i]);
                }
            }
            raf.close();
        }
    }

    public void pos_write(byte[][] buffer, int bucket_num, int cup_no) throws IOException {
        long position = cup_no * (this.cup_size * 32);

        File bucket_name = new File(this.vaultFile + "/" + "bucket" + String.format("%03d", bucket_num));
        RandomAccessFile raf = new RandomAccessFile(bucket_name, "rw");
        raf.seek(position);
        for (int i = 0; i < this.cup_size; i++) {
            raf.write(buffer[i]);
        }
        raf.close();
    }

    public void run() {
        for (int i = 0; i < this.num_hashes; i++) {

            String hash_input = this.fingerprint + this.public_key + this.NONCE;
            byte[] hash_output_byte = Helper.blake3(hash_input); // hex string

            // System.out.println("hash_output_byte = " + hash_output_byte.length);
            byte prefix_hash_output = hash_output_byte[0];
            byte[] nonce_byte = Helper.longToBytes(this.NONCE);

            int bucket_num = prefix_hash_output & 0xFF;

            int current_cup_index;
            int current_cup_no;

            synchronized (this) {
                current_cup_index = cup_counter[bucket_num];
                cup_counter[bucket_num]++;
                if (cup_counter[bucket_num] == cup_size) {
                    cup_counter[bucket_num] = 0;
                }
            }
            System.arraycopy(hash_output_byte, 0, cup[bucket_num][current_cup_index], 0,
                    hash_output_byte.length);
            System.arraycopy(nonce_byte, 0, cup[bucket_num][current_cup_index], hash_output_byte.length,
                    nonce_byte.length);

            if (current_cup_index == cup_size - 1) {
                current_cup_no = cup_number[bucket_num];
                cup_number[bucket_num]++;
                try {
                    pos_write(cup[bucket_num], bucket_num, current_cup_no);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                cup[bucket_num] = new byte[cup_size][24 + 8];
                // cup_counter[bucket_num] = 0;
            }
            synchronized (this) {
                this.NONCE++;
            }
        }
        try {
            pos_write_residues();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

public class Validator {
    public String fingerprint = "";
    public String public_key = "";
    public String on_duty = "";
    public int threads_hash = 0;
    public String memory = "";
    public String disk = "";
    public int buckets = 0;
    public int cup_size = 0;
    public int cups_per_bucket = 0;
    public int threads_io = 0;
    String vault = "";
    public int current_block_id = 1;

    public int counter = 0;
    public static HashMap<Integer, String> block_transaction = new HashMap<>(); // transactions add to block

    Validator() throws IOException {
        readConf();
        validator_register();
    }

    public void dispatch() throws IOException, InterruptedException {

        List<Thread> threads = new ArrayList<Thread>();

        if (on_duty.equalsIgnoreCase("pow")) {
            while (true) {
                int difficulty = Validator.getDifficulty();
                String ret = Validator.lastBlockHash(this.fingerprint, current_block_id);
                String[] retArray = ret.split(",");
                int block_id = Integer.parseInt(retArray[0]);
                String hash_lookup = retArray[1];

                System.out.println(Helper.get_timestamp() + " DSC v1.0");
                System.out.println(Helper.get_timestamp() + " Proof of Work (" + this.threads_hash + "-threads)");
                System.out.println(Helper.get_timestamp() + " Fingerprint: " + this.fingerprint);

                for (int i = 0; i < threads_hash; i++) {
                    Runnable task = new ProofOfWork(fingerprint, public_key, difficulty, hash_lookup, current_block_id);
                    Thread worker = new Thread(task);
                    // set the name of the thread
                    worker.setName(String.valueOf(i));
                    // Start the thread, never call method run() direct
                    worker.start();
                    // Remember the thread for later usage
                    threads.add(worker);
                    worker.join();
                }
                try {
                    Thread.sleep(Helper.block_time);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                current_block_id++;
            }
        } else if (on_duty.equalsIgnoreCase("pom")) {
            System.out.println(Helper.get_timestamp() + " DSC v1.0");
            System.out.println(Helper.get_timestamp() + " Proof of Memory (" + this.threads_hash + "-threads, " +
                    memory + "B RAM)");
            System.out.println(Helper.get_timestamp() + " Fingerprint: " + this.fingerprint);
            System.out.println(Helper.get_timestamp() + " gen/org " + this.memory + "B hashes using 2 passes");

            double startTime = System.currentTimeMillis();
            // Create an in-memory file

            int size = (1024 * 1024 * 1024) / (24 + 8);
            int num_hashs = size / threads_hash;
            memory_store.createMemoryStore(size);

            // create threads_hash threads

            for (int i = 0; i < threads_hash; i++) {
                Runnable task = null;
                task = new ProofOfMemory(fingerprint, public_key, num_hashs);
                Thread worker = new Thread(task);
                // set the name of the thread
                worker.setName(String.valueOf(i));
                // Start the thread, never call method run() direct
                System.out
                        .println(Helper.get_timestamp() + " generating hashes  [Thread #" + worker.getName() + "]");
                worker.start();
                // Remember the thread for later usage
                threads.add(worker);
                worker.join();
                System.out.println(Helper.get_timestamp() + " finished generating hashes [Thread #"
                        + worker.getName() + "]");
            }

            System.out.println(Helper.get_timestamp() + " sorting hashes  ");
            memory_store.sortMemoryStore();
            System.out.println(Helper.get_timestamp() + " finished sorting hashes  ");

            double endTime = System.currentTimeMillis();
            System.out.println(Helper.get_timestamp() + " gen/org " + this.memory + "B hashes (" +
                    Helper.df1.format((endTime - startTime) / 1000) + " sec ~ "
                    + Helper.df1.format(1024 / ((endTime - startTime) / 1000)) + " MB/s)");

            while (true) {

                int difficulty = getDifficulty();
                String ret = lastBlockHash(this.fingerprint, current_block_id);
                String[] retArray = ret.split(",");
                int block_num = Integer.parseInt(retArray[0]);
                String lastBlockHash = retArray[1];

                String prefix_hash_lookup = Helper.ByteArraysToBinary(lastBlockHash.getBytes()).substring(0,
                        difficulty);
                System.out.println(Helper.get_timestamp() + " block " +
                        block_num + ", diff " + difficulty + ", hash " + prefix_hash_lookup);

                startTime = System.currentTimeMillis();
                long mined = memory_store.lookupMemoryStore(prefix_hash_lookup, difficulty, size);
                if (mined != -1)
                    Validator.send_proof(block_num, fingerprint, difficulty, lastBlockHash, mined);
                endTime = System.currentTimeMillis();

                System.out.println(Helper.get_timestamp() + " block " +
                        block_num + ", NONCE " + mined + " (lookup " +
                        Helper.df3.format((endTime - startTime) / 1000) + " sec)");

                Thread.sleep(Helper.block_time);
                current_block_id++;
            }
        } else if (on_duty.equalsIgnoreCase("pos")) {
            System.out.println(Helper.get_timestamp() + " DSC v1.0");
            System.out.println(Helper.get_timestamp() + " Proof of Storage (" + this.threads_hash + "-threads), "
                    + this.disk + "B Storage, " + this.buckets + " buckets, "
                    + cup_size + " per bucket, 1MB write, 256MB RAM)");
            System.out.println(Helper.get_timestamp() + " Fingerprint: " + this.fingerprint);

            byte[][][] cup = new byte[this.buckets][this.cup_size][24 + 8];
            int size = this.buckets * this.cup_size * cups_per_bucket;
            int num_hashs = size / threads_hash;
            double startTime = 0;
            double endTime = 0;

            startTime = System.currentTimeMillis();

            File vaultDir = new File(this.vault);
            if (!vaultDir.exists()) {
                vaultDir.mkdir();
            }

            // create threads_hash threads
            for (int i = 0; i < threads_hash; i++) {
                Runnable task = null;
                task = new ProofOfStorage(vaultDir, this.fingerprint, this.public_key,
                        this.disk, this.buckets, this.cup_size, this.cups_per_bucket, num_hashs, cup);
                Thread worker = new Thread(task);
                // set the name of the thread
                worker.setName(String.valueOf(i));
                // Start the thread, never call method run() direct
                worker.start();
                // Remember the thread for later usage
                threads.add(worker);
                worker.join();
            }
            pos_sort();
            endTime = System.currentTimeMillis();
            System.out.println(Helper.get_timestamp() + " gen/org " + this.disk + "B hashes (" +
                    Helper.df1.format((endTime - startTime) / 1000)
                    + " sec ~ " + Helper.df1.format(10240 / ((endTime - startTime) / 1000)) + " MB/s)");

            while (true) {
                int difficulty = getDifficulty();
                String ret = lastBlockHash(this.fingerprint, current_block_id);
                current_block_id++;
                String[] retArray = ret.split(",");
                int block_num = Integer.parseInt(retArray[0]);
                String lastBlockHash = retArray[1];

                String prefix_hash_lookup = Helper.ByteArraysToBinary(lastBlockHash.getBytes()).substring(0,
                        difficulty);

                startTime = System.currentTimeMillis();
                byte first = lastBlockHash.getBytes()[0];
                long mined = pos_lookup(vaultDir, prefix_hash_lookup, difficulty, first);
                if (mined != -1)
                    Validator.send_proof(block_num, fingerprint, difficulty, lastBlockHash, mined);
                endTime = System.currentTimeMillis();

                System.out.println(Helper.get_timestamp() + " block " +
                        block_num + ", diff " + difficulty + ", hash " + prefix_hash_lookup);

                System.out.println(Helper.get_timestamp() + " block " +
                        block_num + ", NONCE " + mined + " (lookup " +
                        Helper.df3.format((endTime - startTime) ) + " msec)");

                Thread.sleep(Helper.block_time);
            }
        }
    }

    public void pos_sort() throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(new File(new String(this.vault)));
        for (int i = 0; i < 256; i++) {
            File bucket_name = new File(vault + "/" + "bucket" + String.format("%03d", i));

            builder.command("sh", "-c", "sort " + bucket_name);
            Process process = builder.start();

            boolean isFinished = process.waitFor(600, TimeUnit.SECONDS);
            if (!isFinished) {
                process.destroyForcibly();
            }
        }
    }

    public long pos_lookup(File vault, String prefix_hash_lookup, int difficulty, byte first) throws IOException {
        // byte prefix_hash = Helper.hexToBytes(prefix_hash_lookup)[0];
        int bucket_num = first & 0xFF;
            //System.out.println("bucket_num = " + bucket_num);

        File bucket_name = new File(vault + "/" + "bucket" + String.format("%03d", bucket_num));
        RandomAccessFile raf = new RandomAccessFile(bucket_name, "rw");
        byte[] buffer = new byte[(24 + 8)];
        int position = 0;
        byte[] hash = new byte[24];
        byte[] nonce = new byte[8];

        int left = 0;
        int right = (int) raf.length() / 32;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            position = mid * 32;
            raf.seek(position);
            raf.read(buffer, 0, 32);
            System.arraycopy(buffer, 0, hash, 0, 24);
            System.arraycopy(buffer, 24, nonce, 0, 8);

            String prefix_hash_output = Helper.ByteArraysToBinary(hash).substring(0, difficulty);

            //System.out.println("left = " + left + " right = " + right);
            //System.out.println("lookup = " + prefix_hash_lookup + " output = " + prefix_hash_output);


            // Check if prefix_hash_lookup is present at mid
            if (prefix_hash_output.compareTo(prefix_hash_lookup) == 0) {
                raf.close();
                return Helper.bytesToLong(nonce);
            }

            // If prefix_hash_lookup greater, ignore left half
            if (prefix_hash_output.compareTo(prefix_hash_lookup) < 0)
                left = mid + 1;

            // If prefix_hash_lookup is smaller, ignore right half
            else
                right = mid - 1;
        }

        // If reach here, then element was not present
        raf.close();
        return -1;
    }

    /**
     * print the PoS file header, the first 5 hashes and NONCEs,
     * and the last 5 hashes and NONCEs
     */

    public void pos_check() throws IOException {
        System.out.println(Helper.get_timestamp() + " Path: " + this.vault);
        System.out.println(Helper.get_timestamp() + " Fingerprint: " + this.fingerprint);
        System.out.println(Helper.get_timestamp() + " Public_key: " + this.public_key);
        System.out.println(Helper.get_timestamp() + " Disk: " + this.disk);
        System.out.println(Helper.get_timestamp() + " Buckets: " + this.buckets);
        System.out.println(Helper.get_timestamp() + " Cup_Size: " + this.cup_size);

        int line_num = 5;
        File head = new File(this.vault + "/" + "bucket000");
        RandomAccessFile raf = new RandomAccessFile(head, "r");
        byte[] buffer = new byte[line_num * (24 + 8)];
        int position = 0;
        raf.seek(position);
        raf.read(buffer);

        byte[] hash = new byte[24];
        byte[] nonce = new byte[8];
        for (int i = 0; i < line_num; i++) {
            System.arraycopy(buffer, i * 32, hash, 0, 24);
            System.arraycopy(buffer, (i * 32) + 24, nonce, 0, 8);
            System.out.println("head - " + i + ": " + Helper.bytesToHex(hash) + "/" + Helper.bytesToHex(nonce));
            // hash = new byte[24];
            // nonce = new byte[8];
        }
        raf.close();
        System.out.println("... ");
        File tail = new File(this.vault + "/" + "bucket255");
        raf = new RandomAccessFile(tail, "r");

        int file_rows = (int) raf.length() / 32;
        int start = file_rows - line_num;
        buffer = new byte[line_num * (24 + 8)];
        position = start * (24 + 8);
        raf.seek(position);
        raf.read(buffer);

        for (int i = 0; i < line_num; i++) {
            System.arraycopy(buffer, i * 32, hash, 0, 24);
            System.arraycopy(buffer, (i * 32) + 24, nonce, 0, 8);
            System.out.println("tail - " + i + ": " + Helper.bytesToHex(hash) + "/" + Helper.bytesToHex(nonce));
            // hash = new byte[24];
            // nonce = new byte[8];
        }
        raf.close();
    }

    public void readConf() {
        Yaml yaml = new Yaml();

        try {
            InputStream inputStream = new FileInputStream("dsc-config.yaml");

            HashMap yamlMap = yaml.load(inputStream);

            // System.out.println(get_timestamp() + " DSC v1.0");
            // System.out.println(get_timestamp() + " Reading dsc-config.yaml...");

            // Access HashMaps and ArrayList by key(s)
            ArrayList validator = (ArrayList) yamlMap.get("validator");

            HashMap fingerprintMap = (HashMap) validator.get(0);
            fingerprint = (String) fingerprintMap.get("fingerprint");
            HashMap public_keyMap = (HashMap) validator.get(1);
            public_key = (String) public_keyMap.get("public_key");

            HashMap powMap = (HashMap) validator.get(2);
            ArrayList powList = (ArrayList) powMap.get("proof_pow");
            HashMap pomMap = (HashMap) validator.get(3);
            ArrayList pomList = (ArrayList) pomMap.get("proof_pom");
            HashMap posMap = (HashMap) validator.get(4);
            ArrayList posList = (ArrayList) posMap.get("proof_pos");

            HashMap powStatusMap = (HashMap) powList.get(0);
            HashMap pomStatusMap = (HashMap) pomList.get(0);
            HashMap posStatusMap = (HashMap) posList.get(0);

            String powStatus = (String) powStatusMap.get("enable");
            String pomStatus = (String) pomStatusMap.get("enable");
            String posStatus = (String) posStatusMap.get("enable");

            if (powStatus.equalsIgnoreCase("True")) {
                on_duty = "pow";
                threads_hash = (int) ((HashMap) powList.get(1)).get("threads_hash");

            } else if (pomStatus.equalsIgnoreCase("True")) {
                on_duty = "pom";
                threads_hash = (int) ((HashMap) pomList.get(1)).get("threads_hash");
                memory = (String) ((HashMap) pomList.get(2)).get("memory");

            } else if (posStatus.equalsIgnoreCase("True")) {
                on_duty = "pos";
                threads_hash = (int) ((HashMap) posList.get(1)).get("threads_hash");
                disk = (String) ((HashMap) posList.get(2)).get("disk");
                buckets = (int) ((HashMap) posList.get(3)).get("buckets");
                cup_size = (int) ((HashMap) posList.get(4)).get("cup_size");
                cups_per_bucket = (int) ((HashMap) posList.get(5)).get("cups_per_bucket");
                threads_io = (int) ((HashMap) posList.get(6)).get("threads_io");
                vault = (String) ((HashMap) posList.get(7)).get("vault");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println(Helper.get_timestamp() + " DSC v1.0");
            System.out.println(
                    Helper.get_timestamp() + " Error in finding config information, ensure that dsc-config.yaml.\n" +
                            "yaml exist and that they contain the correct information. Validator abort");
        }

    }

    public static int getDifficulty() throws IOException {
        int difficulty = 0;
        String metronomeIP = Helper.get_server_IP("metronome");
        int metronomePort = Helper.get_server_port("metronome");
        String message = "difficulty";

        difficulty = Integer.parseInt(Helper.callServer(metronomeIP, metronomePort, message));

        return difficulty;
    }

    public static String lastBlockHash(String fingerprint, int current_block_id) throws IOException {
        String blockchainIP = Helper.get_server_IP("blockchain");
        int blockchainPort = Helper.get_server_port("blockchain");

        String message = "block_request" + "," + fingerprint + "," + current_block_id;
        String block_empty = Helper.callServer(blockchainIP, blockchainPort, message);
        String[] block_empty_array = block_empty.split(",");
        int block_id = Integer.parseInt(block_empty_array[0]);

        String poolIP = Helper.get_server_IP("pool");
        int poolPort = Helper.get_server_port("pool");

        message = "get_transactions" + "," + fingerprint;
        String transactions = Helper.callServer(poolIP, poolPort, message);

        block_transaction.put(block_id, transactions);

        return block_empty;
    }

    public static void send_proof(int block_id, String fingerprint, int diff, String hash, long nonce)
            throws IOException {
        String message = "";
        String respond = "";

        String metronomeIP = Helper.get_server_IP("metronome");
        int metronomePort = Helper.get_server_port("metronome");
        message = "win_proof" + "," + block_id + "," +fingerprint + "," + diff + "," + hash + "," + nonce;
        Helper.callServer(metronomeIP, metronomePort, message);
            
        String blockchainIP = Helper.get_server_IP("blockchain");
        int blockchainPort = Helper.get_server_port("blockchain");
        message = "win_proof" + "," + block_id + "," + block_transaction.get(block_id);
        Helper.callServer(blockchainIP, blockchainPort, message);

        String poolIP = Helper.get_server_IP("pool");
        int poolPort = Helper.get_server_port("pool");
        message = "win_proof" + "," + block_id + "," + block_transaction.get(block_id);
        Helper.callServer(poolIP, poolPort, message);

    }

    public void validator_register() throws IOException {
        InetAddress localHost = InetAddress.getLocalHost();
        String WHO_AM_I = localHost.getHostName();
        String metronomeIP = Helper.get_server_IP("metronome");
        int metronomePort = Helper.get_server_port("metronome");

        String message = "validators_register" + "," + WHO_AM_I;

        Helper.callServer(metronomeIP, metronomePort, message);
    }

}
