import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Metronome implements Runnable {
    private static ServerSocket serverSocket;
    private static Metronome.serverServices services;
    private static Thread thread;
    private static int nonce;

    /**
     * Default constructor to create the Metronome Object.
     */
    public Metronome() {
    }

    public String get_timestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");
        String timestamp = dateFormat.format(new Date());
        return timestamp;
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

        System.out.println(get_timestamp() + " DSC v1.0");
        System.out.println(get_timestamp() + " Metronome server started with " + thread_num + " threads");

        try {
            serverSocket = new ServerSocket(selfServerPort);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Runnable task = new create_block();
        Thread worker = new Thread(task);
        worker.start();

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
        public void run() {
            String hash = "HtBTNpCt5fmPrvESqVp1UFsiX5wnMCtmgt7Cxi85MFiF";
            while (true) {
                Block new_block = new Block();
                nonce++;
                String data = "this a test" + nonce;

                try {
                   // hash = Crypt.sha256(data);
                    System.out.println(get_timestamp() + " New block created, hash " + hash + ", sent " +
                            "to blockchain");
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

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

                    //Read incoming message
                    try {
                        inputStream = new DataInputStream(clientSocket.getInputStream());
                        outputStream = new DataOutputStream(clientSocket.getOutputStream());

                        try {
                            reciveString = inputStream.readUTF();
                        } catch (EOFException eofe) {
                            //System.out.println("End of data stream reached");
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
                            respond = String.valueOf(30);

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
