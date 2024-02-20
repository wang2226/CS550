import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.bitcoinj.core.Base58;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.time.Instant;
import java.util.*;
import java.io.IOException;
import java.util.Random;

import java.net.InetAddress;


/***
 * Use SHA256 to create public/private keys of 256bit length,
 * store in wallet.cfg in Base58 encoding
 */
public class Wallet {

    private PublicKey pubKey;
    private PrivateKey privKey;
    private String pubHashed;
    private String privHashed;
    private double balance;
    private String fingerprint;

    public Wallet() {
        this.balance = 1024.0;
    }

    public void writeDscConfig() {
        // Create an object representing your data
        Map<String, Object> root = new HashMap<>();

        root.put("wallet", Map.of("public_key", pubHashed));

        root.put("pool", Arrays.asList(
                Map.of("server", "127.0.0.1"),
                Map.of("port", 10001),
                Map.of("threads", 2)));

        root.put("blockchain", Arrays.asList(
                Map.of("server", "127.0.0.1"),
                Map.of("port", 10002),
                Map.of("threads", 2)));

        root.put("metronome", Arrays.asList(
                Map.of("server", "127.0.0.1"),
                Map.of("port", 10003),
                Map.of("threads", 2)));

        root.put("monitor", Arrays.asList(
                Map.of("server", "127.0.0.1"),
                Map.of("port", 10004),
                Map.of("threads", 2)));

        root.put("validator", Arrays.asList(
                Map.of("fingerprint", fingerprint),
                Map.of("public_key", pubHashed),
                Map.of("proof_pow", Arrays.asList(
                        Map.of("enable", "True"),
                        Map.of("threads_hash", 2))),
                Map.of("proof_pom", Arrays.asList(
                        Map.of("enable", "False"),
                        Map.of("threads_hash", 2),
                        Map.of("memory", "1G"))),
                Map.of("proof_pos", Arrays.asList(
                        Map.of("enable", "False"),
                        Map.of("threads_hash", 2),
                        Map.of("disk", "10G"),
                        Map.of("buckets", 256),
                        Map.of("cup_size", 32768),
                        Map.of("cups_per_bucket", 40),
                        Map.of("threads_io", 1),
                        Map.of("vault", "./dsc-pos.vault")))));

        // Create YAML instance
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(dumperOptions);

        // Convert object to YAML and write to a file
        try (FileWriter writer = new FileWriter("dsc-config.yaml")) {
            yaml.dump(root, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeDscKey() {
        // Create an object representing your data
        Map<String, Object> root = new HashMap<>();

        root.put("wallet", Map.of("private_key", privHashed));

        // Create YAML instance
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(dumperOptions);

        // Convert object to YAML and write to a file
        try (FileWriter writer = new FileWriter("dsc-key.yaml")) {
            yaml.dump(root, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void create() {
        File f = new File("dsc-key.yaml");

        if (f.exists() && !f.isDirectory()) {
            System.out.println(Helper.get_timestamp() + " DSC v1.0");
            System.out
                    .println(Helper.get_timestamp() + " Wallet already exists at dsc-key.yaml, wallet create aborted");
            System.exit(1);
        } else {
            try {

                ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
                keyPairGenerator.initialize(ecSpec, new SecureRandom());
                KeyPair keyPair = keyPairGenerator.generateKeyPair();

                pubKey = keyPair.getPublic();
                privKey = keyPair.getPrivate();

                String pub_Hex = Helper.bytesToHex(pubKey.getEncoded());
                String priv_Hex = Helper.bytesToHex(privKey.getEncoded());

                pubHashed = Helper.sha256(pub_Hex);
                privHashed = Helper.sha256(priv_Hex);

                UUID uuid = UUID.randomUUID();
                fingerprint = uuid.toString();

                writeDscConfig();
                writeDscKey();

                Path path = Paths.get("./dsc-key.yaml");
                Set<PosixFilePermission> perms = PosixFilePermissions.fromString("r--------");
                Files.setPosixFilePermissions(path, perms);

            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println(Helper.get_timestamp() + " DSC v1.0");
            System.out.println(Helper.get_timestamp() + " DSC Public Address: " + this.pubHashed);
            System.out.println(Helper.get_timestamp() + " DSC Private Address: " + this.privHashed);
            System.out.println(Helper.get_timestamp()
                    + " Saved public key to dsc-config.yaml and private key to dsc-key.yaml in " +
                    "local folder");
        }

    }

    void key() throws FileNotFoundException {
        String private_key = "";
        String public_key = "";
        Yaml yaml = new Yaml();

        try {
            InputStream inputStream = new FileInputStream("dsc-config.yaml");

            HashMap yamlMap = yaml.load(inputStream);
            // Access HashMaps and ArrayList by key(s)
            HashMap wallet = (HashMap) yamlMap.get("wallet");
            public_key = (String) wallet.get("public_key");

            System.out.println(Helper.get_timestamp() + " DSC v1.0");
            System.out.println(Helper.get_timestamp() + " Reading dsc-config.yaml and dsc-key.yaml...");

            inputStream = new FileInputStream("dsc-key.yaml");

            yamlMap = yaml.load(inputStream);

            // Access HashMaps and ArrayList by key(s)
            wallet = (HashMap) yamlMap.get("wallet");
            private_key = (String) wallet.get("private_key");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println(Helper.get_timestamp() + " DSC v1.0");
            System.out.println(Helper.get_timestamp()
                    + " Error in finding key information, ensure that dsc-config.yaml and dsckey.\n" +
                    "yaml exist and that they contain the correct information. You may need to run \"./dsc.sh wallet " +
                    "create\"");
        }

        System.out.println(Helper.get_timestamp() + " DSC Public Address: " + public_key);
        System.out.println(Helper.get_timestamp() + " DSC Private Address: " + private_key);

    }

    double balance(String type) throws IOException {
        String publicKey = get_pubKey();
        String message = "balance" + "," + publicKey;

        String blockchainIP = Helper.get_server_IP("blockchain");
        int blockchainPort = Helper.get_server_port("blockchain");

        String in = Helper.callServer(blockchainIP, blockchainPort, message);
        String[] inArray = in.split(",");

        double balance = Double.valueOf(inArray[0]);
        int block_num = Integer.parseInt(inArray[1]);

        if (type.equalsIgnoreCase("verbose")) {
            System.out.println(Helper.get_timestamp() + " DSC v1.0");
            System.out.println(Helper.get_timestamp() + " DSC Wallet balance: " +
                    balance + " coins at block " + block_num);
        }
        return balance;
    }

    void send(double coin, String dest)
            throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        String logFile = "Wallet_Send.log";
        BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
        PrintWriter pWriter = new PrintWriter(writer);

        Random rd = new Random();
        byte[] txID = new byte[16];
        rd.nextBytes(txID);

        String txIDStr = Base58.encode(txID);
        String signStr = txIDStr + get_pubKey() + dest + coin;

        // String signed = Helper.bytesToHex(Helper.signature(signStr, private_key));
        long time_stamp = Instant.now().getEpochSecond();

        String message = "send" + "," + txIDStr + "," +
                get_pubKey() + "," + dest + "," + coin + "," + time_stamp;

        String poolIP = Helper.get_server_IP("pool");
        int poolPort = Helper.get_server_port("pool");
        System.out.println(Helper.get_timestamp() + " DSC v1.0");
        System.out.println(Helper.get_timestamp() + " DSC Wallet balance: " + (this.balance-balance("simple")));
        System.out.println(Helper.get_timestamp() + " Created transaction " + txIDStr
                + ", Sending " + coin + " coins to " + dest);
        System.out.println(Helper.get_timestamp() + " Transaction " + txIDStr
                + " submitted to pool");

        String acknowledgement = Helper.callServer(poolIP, poolPort, message);

        System.out.println(Helper.get_timestamp() + " Transaction " + txIDStr
                + " status [" + acknowledgement + "]");

        this.balance -= coin;
        InetAddress localHost = InetAddress.getLocalHost();
        String content = "Latency & Throughput for client " + localHost.getHostName() + ": " +
                Helper.get_timestamp() + ", transaction id: " + txIDStr;
        pWriter.println(content);
        pWriter.close();
    }

    void transaction() throws IOException {
        String message = "transaction_all" + "," + get_pubKey();

        String IP = Helper.get_server_IP("pool");
        int port = Helper.get_server_port("pool");

        String acknowledgement = Helper.callServer(IP, port, message);

        if (acknowledgement.equalsIgnoreCase("unknown")) {
            message = "transaction_all" + "," + get_pubKey();

            IP = Helper.get_server_IP("blockchain");
            port = Helper.get_server_port("blockchain");

            acknowledgement = Helper.callServer(IP, port, message);

        }

        System.out.println(Helper.get_timestamp() + " DSC v1.0");
        System.out.println(Helper.get_timestamp() + " Transaction #1: id=TXjxre5GFFgGj8DgXj7ARm, " +
                "status=" + acknowledgement + ", timestamp=" +
                Helper.get_timestamp() + ", coin=1.0, " +
                "source=" + get_pubKey() + ", destination=" + "HtBTNpCt5fmPrvESqVp1UFsiX5wnMCtmgt7Cxi85MFiF");
    }

    void transaction(String txID) throws IOException {
        String message = "transaction_specific" + "," + txID + "," + get_pubKey();

        String IP = Helper.get_server_IP("pool");
        int port = Helper.get_server_port("pool");

        String acknowledgement = Helper.callServer(IP, port, message);
        if (acknowledgement.equalsIgnoreCase("unknown")) {
            message = "transaction_specific" + "," + txID + get_pubKey();

            IP = Helper.get_server_IP("blockchain");
            port = Helper.get_server_port("blockchain");

            acknowledgement = Helper.callServer(IP, port, message);

        }

        System.out.println(Helper.get_timestamp() + " DSC v1.0");
        System.out.println(Helper.get_timestamp() + " Transaction " + txID + " status ["
                + acknowledgement + "]");
    }

    public String get_pubKey() throws FileNotFoundException {
        Yaml yaml = new Yaml();

        InputStream inputStream = new FileInputStream("dsc-config.yaml");

        HashMap yamlMap = yaml.load(inputStream);

        // Access HashMaps and ArrayList by key(s)
        HashMap wallet = (HashMap) yamlMap.get("wallet");
        return (String) wallet.get("public_key");
    }

}
