import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.Blake3;
import org.bitcoinj.core.Base58;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Helper {
    public static final int block_time = 6000;
    public static final DecimalFormat df1 = new DecimalFormat("0.0");
    public static final DecimalFormat df2 = new DecimalFormat("0.00");
    public static final DecimalFormat df3 = new DecimalFormat("0.000");

    public static String sha256(String string) throws NoSuchAlgorithmException {
        string = "80" + string;
        byte[] data = hexToBytes(string);
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(data);

        return Base58.encode(digest);
    }

    public static byte[] signature(String string, PrivateKey privateKey)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        string = "80" + string;
        byte[] data = hexToBytes(string);

        // MessageDigest digest = MessageDigest.getInstance("SHA-256");
        // byte[] hashedData = digest.digest(data);

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data);
        byte[] signedHash = signature.sign();
        return signedHash;
    }

    public static byte[] blake3(String hash_input) {

        Blake3 hasher = Blake3.initHash();
        hasher.update(hash_input.getBytes(StandardCharsets.UTF_8));
        byte[] hash = new byte[24];
        hasher.doFinalize(hash);
        // return bytesToHex(hash);
        return hash;
    }

    public static String bytesToHex(byte[] b) {
        return String.valueOf(Hex.encodeHex(b, true));
    }

    public static byte[] hexToBytes(String s) {
        byte[] ret = new byte[s.length() / 2];
        try {
            ret = Hex.decodeHex(s.toCharArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] b) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(b);
        buffer.flip();// need flip
        return buffer.getLong();
    }

    public static String ByteArraysToBinary(byte[] input) {

        StringBuilder result = new StringBuilder();
        for (byte b : input) {
            int val = b & 0xFF;
            for (int i = 0; i < 8; i++) {
                result.append((val & 128) == 0 ? 0 : 1); // 128 = 1000 0000
                val <<= 1;
            }
        }
        return result.toString();

    }

    public static String get_timestamp() {
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
            // outputStream.close();
            socket.close();
            System.out.println("Server not startup!");
            // throw new RuntimeException(e);

        }

        return receive;
    }

    public static String get_server_IP(String server) {
        Yaml yaml = new Yaml();
        InputStream inputStream = null;

        try {
            inputStream = new FileInputStream("dsc-config.yaml");
            HashMap yamlMap = yaml.load(inputStream);
            // Access HashMaps and ArrayList by key(s)
            ArrayList list = (ArrayList) yamlMap.get(server);

            HashMap serverMap = (HashMap) list.get(0);
            return (String) serverMap.get("server");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    public static int get_server_port(String server) {
        Yaml yaml = new Yaml();
        InputStream inputStream = null;

        try {
            inputStream = new FileInputStream("dsc-config.yaml");
            HashMap yamlMap = yaml.load(inputStream);
            // Access HashMaps and ArrayList by key(s)
            ArrayList list = (ArrayList) yamlMap.get(server);

            HashMap portMap = (HashMap) list.get(1);
            return (int) portMap.get("port");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static String calculate_hash(String hash_input) {
        byte[] hash_output_byte = Helper.blake3(hash_input); // hex string

        return Helper.bytesToHex(hash_output_byte);
    }

}
