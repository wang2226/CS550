import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public class dsc {

    protected static Wallet wallet;
    protected static BlockChain blockChain;
    protected static Pool pool;
    protected static Metronome metronome;
    protected static Validator validator;
    protected static Monitor monitor;

    public dsc() {

    }

    public static void dscHelp() {
        System.out.println("DSC: DataSys Coin Blockchain v1.0");
        System.out.println("Help menu, supported commands:");
        System.out.println("./dsc.sh help");
        System.out.println("./dsc.sh wallet");
        System.out.println("./dsc.sh blockchain");
        System.out.println("./dsc.sh pool key");
        System.out.println("./dsc.sh metronome");
        System.out.println("./dsc.sh validator");
        System.out.println("./dsc.sh monitor");
    }

    public static void walletHelp() {
        System.out.println("DSC: DataSys Coin Blockchain v1.0");
        System.out.println("Help menu for Wallet, supported commands:");
        System.out.println("./dsc.sh wallet help");
        System.out.println("./dsc.sh wallet create");
        System.out.println("./dsc.sh wallet key");
        System.out.println("./dsc.sh wallet balance");
        System.out.println("./dsc.sh wallet send <amount> <address>");
        System.out.println("./dsc.sh wallet transaction <ID>");
    }

    public static void validatorHelp() {
        System.out.println("DSC: DataSys Coin Blockchain v1.0");
        System.out.println("Help menu for validator, supported commands:");
        System.out.println("./dsc.sh validator help");
        System.out.println("./dsc.sh validator pos_check");
        System.out.println("./dsc.sh validator");
    }

    public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException,
            SignatureException, InvalidKeyException {
        String components;

        if (args.length == 0) {
            System.out.println("Usage: ./dsc.sh help.");
            System.exit(1);
        }

        components = args[0];

        switch (components.toLowerCase()) {
            case "help":
                dscHelp();

                break;
            case "wallet":
                if (args.length < 2) {
                    System.out.println("Usage: ./dsc.sh wallet help.");
                    System.exit(1);
                }
                wallet = new Wallet();
                String walletFunc = args[1];
                switch (walletFunc.toLowerCase()) {
                    case "help":
                        walletHelp();

                        break;
                    case "create":
                        wallet.create();

                        break;
                    case "key":
                        wallet.key();

                        break;
                    case "balance":
                        wallet.balance("verbose");

                        break;
                    case "send":
                        if (args.length != 4) {
                            System.out.println("Usage: ./dsc.sh wallet send <amount> <address>.");
                            System.exit(1);
                        }
                        double coin = Double.valueOf(args[2]);
                        String address = args[3];
                        wallet.send(coin, address);

                        break;
                    case "transaction":
                        if (args.length == 3) {
                            String txID = args[2];
                            wallet.transaction(txID);
                            break;
                        }
                        wallet.transaction();

                        break;

                    default:
                        System.out.println("Usage: ./dsc.sh help.");
                        System.exit(1);
                }

                break;

            case "blockchain":
                BlockChain bockChainServer = new BlockChain();
                bockChainServer.run();

                break;

            case "pool":
                Pool poolServer = new Pool();
                poolServer.run();
                break;

            case "metronome":
                Metronome metronomeServer = new Metronome();
                metronomeServer.run();
                break;

            case "validator":
                Validator validatorServer = new Validator();

                if (args.length == 2) {
                    String validatorFunc = args[1];
                    switch (validatorFunc.toLowerCase()) {
                        case "help":
                            validatorHelp();
                            break;
                        case "pos_check":
                            validatorServer.pos_check();
                            break;
                    }
                } else
                    validatorServer.dispatch();

                break;

            case "monitor":
                Monitor monitorServer = new Monitor();
                monitorServer.run();
                break;

            default:
                System.out.println("Usage: ./dsc.sh help.");
                System.exit(1);
        }

    }
}
