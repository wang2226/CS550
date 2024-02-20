import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.net.InetAddress;
import java.util.Scanner;
import java.nio.file.*;


class CISClient {
    protected static String WHO_AM_I = "";
    protected static String directory = "";

    protected static String cisIp;
    protected static int cisPort;
    protected static Socket cisSocket;

    CISClient() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            this.WHO_AM_I = localHost.getHostName();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.directory = ReadProperties.getByName("Shared_Directory");

    }

    public static String callServer(String message) throws IOException {

        cisIp = ReadProperties.getByName("CIS_IP");
        cisPort = Integer.parseInt(ReadProperties.getByName("CIS_Port"));

        // Create stream writer/reader
        try {
            cisSocket = new Socket(cisIp, cisPort);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        PrintWriter write = new PrintWriter(cisSocket.getOutputStream(), true);
        BufferedReader read = new BufferedReader(new InputStreamReader(cisSocket.getInputStream()));

        // Send message to server
        write.println(message);

        String response = read.readLine();

        read.close();
        write.close();
        cisSocket.close();

        // Return server response
        return response;
    }


    public static String query(String fileName) throws IOException {
        String message = "";
        String retResult = "";

        message = "search" + "," + fileName;

        retResult = callServer(message);

        return  retResult;
    }

    public static void obtain(String owner, String fileName) throws UnknownHostException {
        boolean isAlive ;

        //Data resilience mechanism
        //Make a connection with server to get file from
        String peerIP = ReadProperties.getByName(owner + "_IP");
        if (peerIP.equals("error")) {
            System.out.println("Obtain: wrong Peer ID, please note case sensitive");
            return;
        }

        String property = ReadProperties.getByName(owner + "_Server_Port");
        if (property.equals("error")) {
            System.out.println("Obtain: wrong Peer ID, please note case sensitive");
            return;
        }
        int peerPort = Integer.parseInt(property);

        isAlive = FileDownload.isSocketAlive(InetAddress.getByName(peerIP), peerPort);

        if (isAlive) {
            FileDownload.download(owner, fileName);
        } else {
            String backupNode = FileDownload.findBackup(owner);
            FileDownload.download(backupNode, fileName);
        }
    }

    public static String register(ArrayList<String> item) throws IOException {
        StringBuilder fileList = new StringBuilder();
        String message = "";
        String retResult = "";

        for (String s : item) {
            fileList.append("|").append(s);
        }

        message = "register" + "," + WHO_AM_I + "," + fileList;

        retResult = callServer(message);

        return  retResult;
    }

    protected static void batchRegister() throws IOException {
        Path dir = Paths.get(ReadProperties.getByName("Shared_Directory"));
        String pattern = "*_" + WHO_AM_I + "_*"; // Wildcard pattern to match local  files
        ArrayList<String> sharedFiles = new ArrayList<String>();
        String retResult = "";

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, pattern)) {
            for (Path entry : stream) {
                if (!Files.isDirectory(entry)) {
                    sharedFiles.add(String.valueOf(entry.getFileName()));
                }
            }
        } catch (IOException | DirectoryIteratorException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Now is batch register ....");
        retResult = register(sharedFiles);
        System.out.println("Total register" + sharedFiles.size() + " file and status is " + retResult +".");

    }

    public static void main(String[] args) throws IOException {
        String option, retResult, fileName, ownerId;
        ArrayList<String> files = new ArrayList<String>();

        CISClient client = new CISClient();
        CISServer Server = new CISServer();


        // thread for file download amongst peers
        Thread workThread = new Thread(Server);
        workThread.start();

        batchRegister();

        try{

            Scanner getInput = new Scanner(System.in);

            do{
                System.out.println("****MENU****");
                System.out.println("1. Register Files");
                System.out.println("2. Search for a File");
                System.out.println("3. Obtain a File");
                System.out.println("4. Exit");

                option = getInput.nextLine();

                switch(option){

                    case "1": // case to register a file

                        System.out.println("Please input Registering file name. ");
                        fileName = getInput.nextLine();
                        files.add(fileName);
                        retResult = register(files);
                        if (retResult.equalsIgnoreCase("success"))
                            System.out.println("Files Registered!!!");
                        break;

                    case "2": // case to search a file

                        System.out.println("Enter filename: ");
                        fileName = getInput.nextLine();
                        retResult = query(fileName);
                        System.out.println(retResult);
                        break;

                    case "3": // case to download a file

                        System.out.println("Enter the name of the file to be downloaded:");
                        fileName = getInput.nextLine();
                        System.out.println("Enter the peer id from where you want to download the file: ");
                        ownerId = getInput.nextLine();
                        obtain(ownerId, fileName);
                        break;

                    case "4": // exit case

                        System.out.println("Client Closed!!");
                        System.exit(0);
                        break;
                }
            }while(!(option.equals("4")));
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
