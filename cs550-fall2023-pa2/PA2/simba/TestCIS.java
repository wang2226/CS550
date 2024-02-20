import java.io.*;

public class TestCIS extends Test {
    private final CISClient client;
    private final CISServer server;

    TestCIS() {
        client = new CISClient();
        server = new CISServer();

    }

    protected void runTest(String caseType) throws Throwable {
        double startTime = 0.0;
        double endTime = 0.0;

        String content = "";
        String[] target = new String[0];

        // Create a BufferedWriter and a FileWriter to write to the log file
        // Define the log file path
        String logFile = "CIS_benchmark.txt";
        BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
        PrintWriter pWriter = new PrintWriter(writer);

        try {
            switch (caseType) {
                case "case1": // case to test Query latency
                    target = getRandomFile("small", 10000);

                    startTime = System.currentTimeMillis();
                    for (String file : target) {
                        String owner = client.query(file);
                        if (owner.equalsIgnoreCase("NotExist")) {
                            System.out.println("File " + file + " not exist in " + owner + " node.");
                            break;
                        }
                    }
                    endTime = System.currentTimeMillis();

                    content = "Query latency experiments for peer " + nodeId + " :" + (endTime - startTime);
                    pWriter.println(content);
                    pWriter.println("\n");

                    break;
                case "case2": // case to test Query throughput
                    target = getRandomFile("small", 10000);

                    startTime = System.currentTimeMillis();
                    for (String file : target) {
                        String owner = client.query(file);
                        if (owner.equalsIgnoreCase("NotExist")) {
                            System.out.println("File " + file + " not exist in " + owner + " node.");
                            break;
                        }

                    }
                    endTime = System.currentTimeMillis();

                    content = "Query throughput experiments for peer " + nodeId + " :" + (endTime - startTime);
                    pWriter.println(content);
                    pWriter.println("\n");

                    break;
                case "case3": // case to register a file
                    target = getRandomFile("small", 10000);

                    startTime = System.currentTimeMillis();
                    for (String file : smallFiles) {
                        String owner = client.query(file);
                        if (owner.equalsIgnoreCase("NotExist")) {
                            System.out.println("File " + file + " not exist in " + owner + " node.");
                            break;
                        }
                        client.obtain(owner, file);
                    }
                    endTime = System.currentTimeMillis();

                    content = "Transfer throughput Small experiments for peer " + nodeId + " :" + (endTime - startTime);
                    pWriter.println(content);
                    pWriter.println("\n");

                    break;
                case "case4": // case to register a file
                    target = getRandomFile("large", 10);

                    startTime = System.currentTimeMillis();
                    for (String file : largeFiles) {
                        String owner = client.query(file);
                        if (owner.equalsIgnoreCase("error")) {
                            System.out.println("File " + file + " not exist in " + owner + " node.");
                            break;
                        }
                        client.obtain(owner, file);
                    }
                    endTime = System.currentTimeMillis();

                    content = "Transfer throughput Large experiments for peer " + nodeId + " :" + (endTime - startTime);
                    pWriter.println(content);
                    pWriter.println("\n");

                    break;
            }

            // Close the writer when done
            pWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.exit(0);

    }

    public static void main(String[] args) throws Throwable {

        if (args.length != 1) {
            System.out.println("Usage: java TestCIS caseN.");
            System.exit(1);
        }

        TestCIS simba = new TestCIS();

        Thread workThread = new Thread(simba.server);
        workThread.start();

        simba.runTest(args[0]);

    }
}
