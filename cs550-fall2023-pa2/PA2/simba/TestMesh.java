import java.io.*;

public class TestMesh extends Test {
    private final GnutellaClient client;
    private final GnutellaServer server;

    private final String p2pType;

    TestMesh(String type) {
        p2pType = type;
        client = new GnutellaClient(p2pType);
        server = new GnutellaServer(p2pType);
    }

    protected void runTest(String caseType) throws Throwable {
        double startTime = 0.0;
        double endTime = 0.0;

        String content = "";
        String[] target = new String[0];


        // Create a BufferedWriter and a FileWriter to write to the log file
        // Define the log file path
        String logFile = "Mesh_benchmark.txt";
        BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
        PrintWriter pWriter = new PrintWriter(writer);

        try {
            switch (caseType) {
                case "case1": // case to test Query latency
                    target = getRandomFile("small", 10000);
                    client.clearTargetNode();

                    startTime = System.currentTimeMillis();
                    for (String file : target) {
                        client.query(client.makeID(), client.getTTL(), 0, file);
                    }
                    endTime = System.currentTimeMillis();

                    content = "Query latency experiments for peer " + nodeId + " :" + (endTime - startTime);
                    pWriter.println(content);
                    pWriter.println("\n");

                    break;
                case "case2": // case to test Query throughput
                    target = getRandomFile("small", 10000);
                    client.clearTargetNode();

                    startTime = System.currentTimeMillis();
                    for (String file : target) {
                        client.query(client.makeID(), client.getTTL(), 0, file);
                    }
                    endTime = System.currentTimeMillis();

                    content = "Query throughput experiments for peer " + nodeId + " :" + (endTime - startTime);
                    pWriter.println(content);
                    pWriter.println("\n");

                    break;
                case "case3": // case to register a file
                    target = getRandomFile("small", 10000);
                    client.clearTargetNode();

                    startTime = System.currentTimeMillis();
                    for (String file : target) {
                        client.query(client.makeID(), client.getTTL(), 0, file);
                    }

                    for (String file : target) {
                        client.obtain(client.getTargetNode(file), file);
                    }
                    endTime = System.currentTimeMillis();

                    content = "Transfer throughput Small experiments for peer " + nodeId + " :" + (endTime - startTime);
                    pWriter.println(content);
                    pWriter.println("\n");

                    break;
                case "case4": // case to register a file
                    target = getRandomFile("large", 10);
                    client.clearTargetNode();

                    startTime = System.currentTimeMillis();
                    for (String file : target) {
                        client.query(client.makeID(), client.getTTL(), 0, file);
                    }
                    for (String file : target) {
                        client.obtain(client.getTargetNode(file), file);
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
            System.out.println("Usage: java TestMesh caseN.");
            System.exit(1);
        }

        TestMesh mesh = new TestMesh("Mesh");

        Thread workThread = new Thread(mesh.server);
        workThread.start();
        mesh.runTest(args[0]);

    }
}

