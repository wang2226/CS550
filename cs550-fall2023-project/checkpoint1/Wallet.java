import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.bitcoinj.core.Base58;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;


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
    }


    public void writeCfg() throws IOException {
        String cfgFile = "wallet.cfg";
        BufferedWriter writer = new BufferedWriter(new FileWriter(cfgFile));
        PrintWriter pWriter = new PrintWriter(writer);

        try {
            pWriter.println("public_key:" + pubHashed);
            pWriter.println("private_key:" + privHashed);
        } catch (Exception e) {
            e.printStackTrace();
        }
        pWriter.close();
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
                        Map.of("bucket_size", 32768),
                        Map.of("threads_io", 1),
                        Map.of("vault", "~/dsc-pos.vault")))));


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
            System.out.println(get_timestamp() + " DSC v1.0");
            System.out.println(get_timestamp() + " Wallet already exists at dsc-key.yaml, wallet create aborted");
            System.exit(1);
        } else {
            try {

                ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
                keyPairGenerator.initialize(ecSpec, new SecureRandom());
                KeyPair keyPair = keyPairGenerator.generateKeyPair();

                pubKey = keyPair.getPublic();
                privKey = keyPair.getPrivate();

                String pub_Hex = Crypt.bytesToHex(pubKey.getEncoded());
                String priv_Hex = Crypt.bytesToHex(privKey.getEncoded());


                pubHashed = Crypt.sha256(pub_Hex);
                privHashed = Crypt.sha256(priv_Hex);

                UUID uuid = UUID.randomUUID();
                fingerprint = uuid.toString();

                writeCfg();
                writeDscConfig();
                writeDscKey();

                Path path = Paths.get("./dsc-key.yaml");
                Set<PosixFilePermission> perms = PosixFilePermissions.fromString("r--------");
                Files.setPosixFilePermissions(path, perms);

            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println(get_timestamp() + " DSC v1.0");
            System.out.println(get_timestamp() + " DSC Public Address: " + this.pubHashed);
            System.out.println(get_timestamp() + " DSC Private Address: " + this.privHashed);
            System.out.println(get_timestamp() + " Saved public key to dsc-config.yaml and private key to dsc-key.yaml in " +
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

            System.out.println(get_timestamp() + " DSC v1.0");
            System.out.println(get_timestamp() + " Reading dsc-config.yaml and dsc-key.yaml...");

            inputStream = new FileInputStream("dsc-key.yaml");

            yamlMap = yaml.load(inputStream);

            // Access HashMaps and ArrayList by key(s)
            wallet = (HashMap) yamlMap.get("wallet");
            private_key = (String) wallet.get("private_key");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println(get_timestamp() + " DSC v1.0");
            System.out.println(get_timestamp() + " Error in finding key information, ensure that dsc-config.yaml and dsckey.\n" +
                    "yaml exist and that they contain the correct information. You may need to run \"./dsc.sh wallet " +
                    "create\"");
        }

        System.out.println(get_timestamp() + " DSC Public Address: " + public_key);
        System.out.println(get_timestamp() + " DSC Private Address: " + private_key);

    }

    double balance(String type) throws IOException {
        String publicKey = get_pubKey();
        String message = "balance" + "," + publicKey;

        String IP = "127.0.0.1";
        int port = 10002;

        String in = callServer(IP, port, message);
        String[] inArray = in.split(",");

        double balance = Double.valueOf(inArray[0]);
        int block_num = Integer.parseInt(inArray[1]);

        if (type.equalsIgnoreCase("verbose")) {
            System.out.println(get_timestamp() + " DSC v1.0");
            System.out.println(get_timestamp() + " DSC Wallet balance: " +
                    balance + " coins at block " + block_num);
        }
        return balance;
    }

    void send(double coin, String dest) throws IOException {
        Random rd = new Random();
        byte[] txID = new byte[16];
        rd.nextBytes(txID);

        String txIDStr = Base58.encode(txID);

        String message = "send" + "," + txIDStr + "," +
                get_pubKey() + "," + dest + "," + coin;

        String IP = "127.0.0.1";
        int port = 10001;

        System.out.println(get_timestamp() + " DSC v1.0");
        System.out.println(get_timestamp() + " DSC Wallet balance: " + balance("simple"));
        System.out.println(get_timestamp() + " Created transaction " + txIDStr
                + ", Sending " + coin + " coins to " + dest);
        System.out.println(get_timestamp() + " Transaction " + txIDStr
                + " submitted to pool");

        String acknowledgement = callServer(IP, port, message);

        System.out.println(get_timestamp() + " Transaction " + txIDStr
                + " status [" + acknowledgement + "]");

    }

    void transaction() throws IOException{
        String message = "transcation_all" + "," +  get_pubKey();

        String IP = "127.0.0.1";
        int port = 10001;

        String acknowledgement = callServer(IP, port, message);

        System.out.println(get_timestamp() + " DSC v1.0");
        System.out.println(get_timestamp() + " Transaction #1: id=TXjxre5GFFgGj8DgXj7ARm, " +
                "status=" + acknowledgement +", timestamp=" +
                get_timestamp() +  ", coin=1.0, " +
                "source=" + get_pubKey() + ", destination=" + "HtBTNpCt5fmPrvESqVp1UFsiX5wnMCtmgt7Cxi85MFiF");
    }

    void transaction(String txID) throws IOException{
        String message = "transcation_1" + "," + txID + "," +  get_pubKey();

        String IP = "127.0.0.1";
        int port = 10001;

        String acknowledgement = callServer(IP, port, message);

        System.out.println(get_timestamp() + " DSC v1.0");
        System.out.println(get_timestamp() + " Transaction " + txID + " status ["
                + acknowledgement +"]");
    }


    public String get_pubKey() throws FileNotFoundException {
        Yaml yaml = new Yaml();

        InputStream inputStream = new FileInputStream("dsc-config.yaml");

        HashMap yamlMap = yaml.load(inputStream);

        // Access HashMaps and ArrayList by key(s)
        HashMap wallet = (HashMap) yamlMap.get("wallet");
        return (String) wallet.get("public_key");
    }

    public String get_timestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");
        String timestamp = dateFormat.format(new Date());
        return timestamp;
    }

    public String callServer(String IP, int port, String message) throws IOException {
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
            socket.close();
            System.out.println("Server not startup!");
            // throw new RuntimeException(e);

        }

        return receive;
    }
}
