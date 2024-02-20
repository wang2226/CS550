import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;


class peerClient {
    private String peerId;
    private String cisIp;
    private int cisPort;
    private Socket cisSocket;
    private String sharedDirectory;

    peerClient(){
        this.peerId = getParameter.byName("peerNode");
        this.sharedDirectory = getParameter.byName("peer" + peerId + "_sharedDirectory");
        this.cisIp = getParameter.byName("cisIp");
        this.cisPort = Integer.parseInt(getParameter.byName("cisPort"));

		try {
        	this.cisSocket = new Socket(this.cisIp, this.cisPort);
		}
		catch(IOException e){
			e.printStackTrace();
		}
    }

    public void runBatch(String level){
			String retResult, fileName;
			double startTime = 0.0;
			double endTime = 0.0;
			int count = 0;

			String content = "";

            // Define the file path
            String filePath = "benchmark.txt";

			try {
				Scanner readCIS = new Scanner(cisSocket.getInputStream());
				PrintStream writeCIS = new PrintStream(cisSocket.getOutputStream());

				String fileNode = getParameter.byName("fileNode");

				//Automatically registering files as the peer is connected
				File peerDirectory = new File(this.sharedDirectory);
				File[] sharedFiles = peerDirectory.listFiles();

            	// Create a BufferedWriter and a FileWriter to write to the file
            	BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
				PrintWriter pwriter = new PrintWriter(writer);

					
				for(int i = 0; i < sharedFiles.length; i++){
					writeCIS.println("1");
					writeCIS.println(peerId + ' ' + sharedFiles[i].getName());
					retResult = readCIS.nextLine();

					count++;
					if(count%500 == 0){
						System.out.println("Now is register " + Integer.toString(count) + " record");
					}
				}


				if(level.equalsIgnoreCase("weak")){
					count = 0;
					for(int round=0; round<1; round++){
						pwriter.println(Integer.toString(round) + " round search time:");
						startTime = System.currentTimeMillis();
						for(int i=0; i<10000; i++){
							fileName = "1KB_" + fileNode + "_" + Integer.toString(i+1) + ".txt" ;
							writeCIS.println("6");
							writeCIS.println(fileName);
							retResult = readCIS.nextLine();

						count++;
						if(count%500 == 0){
							System.out.println("Now is search " + Integer.toString(count) + " record");
						}
						}
						endTime = System.currentTimeMillis();
						content="10K requests for peer" + fileNode + " :" +(endTime-startTime);
            			pwriter.println(content);
            			pwriter.println("\n");
					}
        			// Close the writer when done
        			pwriter.close();

				}else if(level.equalsIgnoreCase("strong")){

				for(int round=0; round<1; round++){
					pwriter.println(Integer.toString(round) + " round search and transfer time:");
					startTime = System.currentTimeMillis();
					for(int i=0; i<10000; i++){
						fileName = "1KB_" + fileNode + "_" + Integer.toString(i+1) + ".txt" ;
						writeCIS.println("6");
						writeCIS.println(fileName);
						retResult = readCIS.nextLine();
						String ownerId = retResult.split(" ")[0];
						obtain(fileName, ownerId, this.sharedDirectory);
					}
					endTime = System.currentTimeMillis();
					content="10K small files (1KB) for peer" + fileNode +" :" +(endTime-startTime);
            		pwriter.println(content);

					startTime = System.currentTimeMillis();
					for(int i=0; i<1000; i++){
						fileName = "1MB_" + fileNode + "_" + Integer.toString(i+1) + ".txt" ;
						writeCIS.println("6");
						writeCIS.println(fileName);
						retResult = readCIS.nextLine();
						String ownerId = retResult.split(" ")[0];
						obtain(fileName, ownerId, this.sharedDirectory);
					}
					endTime = System.currentTimeMillis();
					content="1K medium files (1MB) for peer" + fileNode +" :" +(endTime-startTime);
            		pwriter.println(content);

					startTime = System.currentTimeMillis();
					for(int i=0; i<8; i++){
						fileName = "1GB_" + fileNode + "_" + Integer.toString(i+1) + ".bin" ;
						writeCIS.println("6");
						writeCIS.println(fileName);
						retResult = readCIS.nextLine();
						String ownerId = retResult.split(" ")[0];
						obtain(fileName, ownerId, this.sharedDirectory);
					}
					endTime = System.currentTimeMillis();
					content="8 large files (1GB) for peer" + fileNode +" :" +(endTime-startTime);
            		pwriter.println(content);
            		pwriter.println("\n");
				}
			}
        // Close the writer when done
        pwriter.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}

		System.exit(0);
    }

    void runInteractive(){

		try{
			String option, retResult, fileName, ownerId;

			Scanner getInput = new Scanner(System.in);
			Scanner readCIS = new Scanner(cisSocket.getInputStream());

			do{
				System.out.println("****MENU****");
				System.out.println("1. Register Files");
				System.out.println("2. Search for a File");
				System.out.println("3. Obtain a File");
				System.out.println("4. Exit");
				
				option = getInput.nextLine();
				
				PrintStream writeCIS = new PrintStream(cisSocket.getOutputStream());
				
				switch(option){
				
				case "1": // case to register a file
					
					System.out.println("Please input Registering file name. ");
					fileName = getInput.nextLine();
					writeCIS.println(option);
					writeCIS.println(peerId + ' ' + fileName);
					retResult = readCIS.nextLine();
					if (retResult.equalsIgnoreCase("success"))
						System.out.println("Files Registered!!!");
					break;
					
				case "2": // case to search a file
					
					writeCIS.println(option);
					System.out.println("Enter filename: ");
					fileName = getInput.nextLine();
					writeCIS.println(fileName);
					retResult = readCIS.nextLine();
					System.out.println(retResult);
					break;
					
				case "3": // case to download a file
					
					// writeCIS.println(option);
					System.out.println("Enter the name of the file to be downloaded:");
					fileName = getInput.nextLine();
					System.out.println("Enter the peer id from where you want to download the file: ");
					ownerId = getInput.nextLine();
					obtain(fileName, ownerId, this.sharedDirectory);
					// retResult = readCIS.nextLine();
					break;
					
				case "4": // exit case
					
					writeCIS.println(option);
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
/*
	 * Obtain function -  invoked by a peer to download a file from another peer
	 */
	public static void obtain(String fileName, String ownerId, String downPath){
		
		//Check if the folder exists
		//Create if it doesn't exist
		File directory = new File(downPath);
		if(!directory.exists()){
			System.out.println("Creating a new folder named: ");
			directory.mkdir();
			System.out.println("The file will be found at: " + downPath);
		}

		//Make a connection with server to get file from
		String peerIp = getParameter.byName("peer" + ownerId + "_Ip");
		int peerPort = Integer.parseInt(getParameter.byName("peer" + ownerId + "_serverPort"));
		
		try {
			Socket peerSocket = new Socket(peerIp, peerPort);
			System.out.println("Downloading File Please wait ...");

			//Input & Output for socket Communication
			Scanner readPeer = new Scanner(peerSocket.getInputStream());
			PrintStream writePeer = new PrintStream(peerSocket.getOutputStream());
	
			writePeer.println(fileName);
			writePeer.println(ownerId);	
	
			long buffSize = readPeer.nextLong();
			int newBuffSize = (int) buffSize;
	
			byte[] byteStream = new byte[newBuffSize];
			String filePath = downPath + "/" + fileName;

			//Write the file requested by the peer
			FileOutputStream writeFileStream = new FileOutputStream(filePath);

			writeFileStream.write(byteStream);
			writeFileStream.close();

			System.out.println("Downloaded Successfully");

			peerSocket.close();

		} 
		catch (FileNotFoundException ex){
			System.out.println("FileNotFoundException : " + ex);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
}

class peerServer implements Runnable{
	String fileDownloadPath;

	int peerPort;
	Socket peerSocket;
	Scanner readClient;
	PrintStream writeClient;

	public peerServer(){
		String peerId = getParameter.byName("peerNode");
		this.peerPort = Integer.parseInt(getParameter.byName("peer" + peerId + "_serverPort"));
	}

	public void run(){
		try{
			ServerSocket downloadSocket = new ServerSocket(peerPort);
			System.out.println("starting client socket now");

			while(true){
				//accept the connection from the socket
				peerSocket = downloadSocket.accept();
				System.out.println("Client connected for File sharing ...");

				writeClient = new PrintStream(peerSocket.getOutputStream());
				readClient = new Scanner(peerSocket.getInputStream());

				//get the fileName from ClientAskingForFile
				String fileName = readClient.nextLine();
				System.out.println("Requested file is: "+fileName);
				String ownerId = readClient.nextLine();

				fileDownloadPath = getParameter.byName("peer" + ownerId + "_sharedDirectory");
				
				File targetFile = new File(fileDownloadPath + "/" + fileName);
				
				FileInputStream fin = new FileInputStream(targetFile);
				BufferedInputStream buffReader = new BufferedInputStream(fin);
				
				//check if the file exists, for it to be downloaded
				if (!targetFile.exists()){
					System.out.println("File doesnot Exists");
					buffReader.close();
					return;
				}

				//get the file size, as the buffer needs to be allocated an initial size
				int size = (int) targetFile.length();	//convert from long to int
				byte[] buffContent = new byte[size];
				
				//send file size
				writeClient.println(size);
				
				//allocate a buffer to store contents of file
				int startRead = 0;	//how much is read in total
				int numOfRead = 0;	//how much is read in each read() call

				//read into buffContent, from StartRead until end of file
				while (startRead < buffContent.length && (numOfRead = buffReader.read(buffContent, startRead, buffContent.length - startRead)) >= 0) 
				{
					startRead = startRead + numOfRead;
				}
				//Validate all the bytes have been read
				if (startRead < buffContent.length){
					System.out.println("File Read Incompletely" + targetFile.getName());
				}
				writeClient.println(buffContent);
				buffReader.close();
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
}

public class Peer {
    private  static String runModel = "";

    public static void main(String[] args) {
       
		if(args.length == 1)
        	runModel = args[0];

        peerClient Client = new peerClient();
        peerServer Server = new peerServer();

			
		// thread for file download amongst peers
		Thread workThread = new Thread(Server);
		workThread.start();

        if (runModel.equalsIgnoreCase("weak") || runModel.equalsIgnoreCase("strong")) {
            Client.runBatch(runModel);
        }
        else {
            Client.runInteractive();
        }
    }
}
