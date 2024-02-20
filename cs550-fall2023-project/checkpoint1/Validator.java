import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.codec.digest.Blake3;
import org.yaml.snakeyaml.Yaml;


class Blake {
    public static String blake3(String hash_input) {

        Blake3 hasher = Blake3.initHash();
        hasher.update(hash_input.getBytes(StandardCharsets.UTF_8));
        byte[] hash = new byte[24];
        hasher.doFinalize(hash);
        return bytesToHex(hash);
    }

    public static String bytesToHex(byte[] bytes) {
        final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.UTF_8);
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }

    // method to convert Decimal to Binary
    public static String decimalToBinary(int decimal) {
        // variable to store the convert  binary string
        String binaryString = "";
        // loop to generate the binary
        while (decimal != 0) {
            // concatenating the remainder on dividing by 2 to the binary string
            binaryString = (decimal % 2) + binaryString;
            // updating the decimal integer by dividing by 2 in each iteration
            decimal /= 2;
        }
        // loop to ensure that each Hexadecimal character is represented by 4 bits
        while (binaryString.length() % 4 != 0) {
            // adding leading 0's if the character is represented by less than 4 bits
            binaryString = "0" + binaryString;
        }
        // returning the converted binary string
        return binaryString;
    }

    public static String hexToBin(String hex) {
        // declaring the variables
        int i;
        char ch;
        String binary = "";
        int returnedBinary;

        // converting the accepted Hexadecimal String to upper case
        hex = hex.toUpperCase();

        // loop to iterate through the length of the Hexadecimal String
        for (i = 0; i < hex.length(); i++) {
            // extracting the characters
            ch = hex.charAt(i);
            // condition to check if the character is not a valid Hexadecimal character
            if (Character.isDigit(ch) == false && ((int) ch >= 65 && (int) ch <= 70) == false) {
                // returning Invalid Hexadecimal String for the invalid Hexadecimal character
                binary = "Invalid Hexadecimal String";
                return binary;
            }
            // checking if the character is a valid Hexadecimal alphabet
            else if ((int) ch >= 65 && (int) ch <= 70)
                // converting alphabet to  corresponding value such as 10  for A and so on using ASCII code
                returnedBinary = (int) ch - 55;
            else
                returnedBinary = Integer.parseInt(String.valueOf(ch));
            // converting the decimal to binary by calling the decimalToBinary() method
            binary += decimalToBinary(returnedBinary);
        }
        // returning the converted binary sequence
        return binary;
    }

    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();//need flip
        return buffer.getLong();
    }

    public static String ByteArraysToBinary(byte[] input) {

        StringBuilder result = new StringBuilder();
        for (byte b : input) {
            int val = b;
            for (int i = 0; i < 8; i++) {
                result.append((val & 128) == 0 ? 0 : 1);      // 128 = 1000 0000
                val <<= 1;
            }
        }
        return result.toString();

    }
}

/**
 * Proof  algorithm
 */
class ProofOfWork implements Runnable {
    public String hash_lookup = "";
    public String fingerprint = "";
    public String public_key = "";
    public int difficulty = 30;
    public int blocktime = 6000;
    public int NONCE = 0;
    public int block_num = 4;
    public long counter = 0;

    private final Object lock = new Object();

    ProofOfWork(String fingerprint, String public_key) {
        this.public_key = public_key;
        this.fingerprint = fingerprint;
    }

    public long pow_lookup(String prefix_hash_lookup) {
        double start_time = System.currentTimeMillis();

        counter = 0;
        while (System.currentTimeMillis() < (start_time + this.blocktime)) {
            String hash_input = this.fingerprint + this.public_key + Long.toString(this.NONCE);
            String hash_output = Blake.blake3(hash_input);
            synchronized (lock) {
                counter++;
            }
            String prefix_hash_output = Blake.ByteArraysToBinary(hash_output.getBytes()).substring(0, this.difficulty);

            if (prefix_hash_lookup.equals(prefix_hash_output))
                return this.NONCE;
            else
                synchronized (lock) {
                    this.NONCE++;
                }
        }
        return -1;
    }

    //Set the difficulty by typing the amount of zeros that you want to the hash to begin
    public void run() {

        while (true) {
            try {
                this.difficulty = Validator.getDifficulty();
                this.hash_lookup = Validator.lastBlockHash();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            String prefix_hash_lookup = Blake.ByteArraysToBinary(hash_lookup.getBytes()).substring(0, this.difficulty);
            System.out.println(get_timestamp() + " block " +
                    block_num + ", diff " + this.difficulty + ", hash " + prefix_hash_lookup);

            long mined = pow_lookup(prefix_hash_lookup);

            System.out.println(get_timestamp() + " block " +
                    this.block_num + ", NONCE " + mined + " (" + (this.counter / 1000000) + " MH/s)");


            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public String get_timestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");
        String timestamp = dateFormat.format(new Date());
        return timestamp;
    }
}

class memory_store{
    private static byte[][] memory_store;

    public static void sortMemoryStore() {

        Arrays.sort(memory_store, (byte[] a, byte[] b) -> 0);
        //for (int i = 1; i < 10; i++) {
         //System.out.println("i = " + i);
          // System.out.println("memory_store[" + i + "]= " + Blake.bytesToHex(memory_store[i]));
        //}
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
    //set difficulty parameter
    public static long lookupMemoryStore(String prefix_hash_lookup, int difficulty, int size) {

        int left = 0;
        int right = size;

        while (left <= right) {
            int mid = left + (right - left) / 2;

            byte[] hash_output = Arrays.copyOfRange(memory_store[mid], 0, 24);
            byte[] nonce = Arrays.copyOfRange(memory_store[mid], 24, 32);
            String prefix_hash_output = Blake.ByteArraysToBinary(hash_output).substring(0, difficulty);

            // Check if prefix_hash_lookup is present at mid
            if (prefix_hash_output.compareTo(prefix_hash_lookup) == 0)
                return Blake.bytesToLong(nonce);

            // If prefix_hash_lookup greater, ignore left half
            if (prefix_hash_output.compareTo(prefix_hash_output) < 0)
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

            String hash_input = this.fingerprint + this.public_key + this.NONCE;
            String hash_output = Blake.blake3(hash_input);

            try {
                byte[] first = hash_output.getBytes();
                byte[] second = Blake.longToBytes(this.NONCE);
                byte[] combined = new byte[first.length + second.length];

                System.arraycopy(first, 0, combined, 0, first.length);
                System.arraycopy(second, 0, combined, first.length, second.length);

                memory_store.setMemoryStore(i, combined);
                outputStream.flush();
               // synchronized (this) {
                //    System.out.println("memory_store[" + i + "]= " + Blake.bytesToHex(memory_store.getMemoryStore(i)));
               // }

                synchronized (this) {
                    this.NONCE++;
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

class ProofOfStorage implements Runnable {
    ProofOfStorage() {

    }

    public void run() {
    }
}

public class Validator {

    String fingerprint = "";
    String public_key = "";
    String on_duty = "";
    int threads_hash = 0;
    String memory = "";
    String disk = "";
    int buckets = 0;
    int bucket_size = 0;
    int threads_io = 0;
    String vault = "";


    public void dispatch() throws IOException, InterruptedException {
        readConf();


        List<Thread> threads = new ArrayList<Thread>();

        if (on_duty.equalsIgnoreCase("pow")) {
            System.out.println(get_timestamp() + " DSC v1.0");
            System.out.println(get_timestamp() + " Proof of Work (" + this.threads_hash + "-threads)");
            System.out.println(get_timestamp() + " Fingerprint: " + this.fingerprint);

            for (int i = 0; i < threads_hash; i++) {
                Runnable task = new ProofOfWork(fingerprint, public_key);
                Thread worker = new Thread(task);
                // set the name of the thread
                worker.setName(String.valueOf(i));
                // Start the thread, never call method run() direct
                worker.start();
                // Remember the thread for later usage
                threads.add(worker);
            }
        } else if (on_duty.equalsIgnoreCase("pom")) {
            System.out.println(get_timestamp() + " DSC v1.0");
            System.out.println(get_timestamp() + " Proof of Memory (" + this.threads_hash + "-threads, " +
                    memory + "B RAM)");
            System.out.println(get_timestamp() + " Fingerprint: " + this.fingerprint);
            System.out.println(get_timestamp() + " gen/org " + this.memory + "B hashes using 2 passes");

            while (true) {

                int difficulty = getDifficulty();
                String lastBlockHash = lastBlockHash();
                int block_num = 4;

                double startTime = System.currentTimeMillis();
                // Create an in-memory file
                int size = (1024 * 1024 * 1024) / (24 + 8);
                //int size = 10;
                int num_hahs = size / threads_hash;
                memory_store.createMemoryStore(size);
                // create threads_hash threads


                for (int i = 0; i < threads_hash; i++) {
                    Runnable task = null;
                    task = new ProofOfMemory(fingerprint, public_key, num_hahs);
                    Thread worker = new Thread(task);
                    // set the name of the thread
                    worker.setName(String.valueOf(i));
                    // Start the thread, never call method run() direct
                    System.out.println(get_timestamp() + " generating hashes  [Thread #" + worker.getName() + "]");
                    worker.start();
                    // Remember the thread for later usage
                    threads.add(worker);
                    worker.join();
                    System.out.println(get_timestamp() + " finished generating hashes [Thread #" + worker.getName() + "]");
                }


                System.out.println(get_timestamp() + " sorting hashes  ");
                memory_store.sortMemoryStore();
                System.out.println(get_timestamp() + " finished sorting hashes  ");

                String prefix_hash_lookup = Blake.ByteArraysToBinary(lastBlockHash.getBytes()).substring(0, difficulty);

                System.out.println(get_timestamp() + " block " +
                        block_num + ", diff " + difficulty + ", hash " + prefix_hash_lookup);
                long mined = memory_store.lookupMemoryStore(prefix_hash_lookup, difficulty, size);

                System.out.println(get_timestamp() + " block " +
                         block_num + ", NONCE " + mined);
                // block_num + ", NONCE " + mined + " (lookup " + (this.counter / 1000000 / 6000) + " sec");

                double endTime = System.currentTimeMillis();
                System.out.println(get_timestamp() + " gen/org " + this.memory + "B hashes (" +
                        (endTime - startTime) / 1000 + " sec ~ " + 1024 / ((endTime - startTime) / 1000) +
                        " MB/s");

                Thread.sleep(6000);
            }
        } else if (on_duty.equalsIgnoreCase("pos")) {
            System.out.println(get_timestamp() + " DSC v1.0");
            System.out.println(get_timestamp() + " Proof of Storage (" + this.threads_hash + "-threads)");
            System.out.println(get_timestamp() + " Fingerprint: " + this.fingerprint);
            // create threads_hash threads
            for (int i = 0; i < threads_hash; i++) {
                Runnable task = null;
                task = new ProofOfStorage();
                Thread worker = new Thread(task);
                // set the name of the thread
                worker.setName(String.valueOf(i));
                // Start the thread, never call method run() direct
                worker.start();
                // Remember the thread for later usage
                threads.add(worker);
            }
        }
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
                bucket_size = (int) ((HashMap) posList.get(4)).get("bucket_size");
                threads_io = (int) ((HashMap) posList.get(5)).get("threads_io");
                vault = (String) ((HashMap) posList.get(6)).get("vault");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println(get_timestamp() + " DSC v1.0");
            System.out.println(get_timestamp() + " Error in finding config information, ensure that dsc-config.yaml.\n" +
                    "yaml exist and that they contain the correct information. Validator abort");
        }

    }

    public static int getDifficulty() throws IOException {
        int difficulty = 0;
        String metronomeIP = "127.0.0.1";
        int metronomePort = 10003;
        String message = "difficulty";

        difficulty = Integer.parseInt(callServer(metronomeIP, metronomePort, message));

        return difficulty;
    }

    public static String lastBlockHash() throws IOException {
        String blockchainIP = "127.0.0.1";
        int blockchainPort = 10002;
        String message = "hash";

        String hash = callServer(blockchainIP, blockchainPort, message);

        return hash;
    }


    public void sendProof() {

    }

    /**
     * print the PoS file header, the first 5 hashes and NONCEs,
     * and the last 5 hashes and  NONCEs
     */

    public void pos_check() {

    }

    public String get_timestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");
        String timestamp = dateFormat.format(new Date());
        return timestamp;
    }

    public static String callServer(String IP, int port, String message) throws IOException {
        Socket socket = null;
        DataOutputStream outputStream = null;
        DataInputStream inputStream = null;
        String receive = "";

        try {
            try {
                socket = new Socket(IP, port);

                // Create stream writer and reader

                outputStream = new DataOutputStream(socket.getOutputStream());
                inputStream = new DataInputStream(socket.getInputStream());

                // Send message to server
                outputStream.writeUTF(message);
                outputStream.flush();
                receive = inputStream.readUTF();

                inputStream.close();
                outputStream.close();
            } catch (NullPointerException e) {
                System.out.println("Null Pointer Exception");
            }

        } catch (IOException e) {
            //outputStream.close();
            System.out.println("Server not startup!");
            socket.close();
            // throw new RuntimeException(e);

        }

        return receive;
    }

}
