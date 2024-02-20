import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.locks.*;


public class Central_Indexing_Server {
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
            int cisPort = Integer.parseInt(getParameter.byName("cisPort"));
			ServerSocket socket = new ServerSocket(cisPort);
			System.out.println("Server is up and running!!!");
			while(true){
				Socket cisSocket = socket.accept();
				//thread creation
				Thread workThread = new Thread(new cisServices(cisSocket));
				workThread.start();
			}		
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
}

/*
 * Class cisServices has all the functionality
 * It runs on the thread 
 */
class cisServices implements Runnable{
	
	public static HashMap<String,ArrayList<String>> fileMap = new HashMap<String, ArrayList<String>>();
	
	public Socket cisSocket;
	int count=0;
	// constructor to initialize socket
	public cisServices(Socket socket)throws IOException{
		this.cisSocket = socket;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Boolean condition = true;
		
			while(condition){
				try{
					String fileName, searchResult, error;
					String requestType;
					
					String location = "";
					ArrayList <String> fileLocation = new ArrayList<String>();
					
					Scanner cisRecived = new Scanner(cisSocket.getInputStream());
					PrintStream cisSend = new PrintStream(cisSocket.getOutputStream());
					
					//getting the option selected at client side
					requestType = cisRecived.nextLine();
					switch(requestType){
				
					case "1": // case to register a file.
						String reciveString = cisRecived.nextLine();
						String [] reciveArray = reciveString.split(" ");

						registry(reciveArray[0], reciveArray[1]);
						cisSend.println("success");
						break;
						
					case "2": // case to search a file.
						
						fileName = cisRecived.nextLine();
						fileLocation = search(fileName);
						
						try{
							for(int i = 0; i<fileLocation.size();i++){
								location += fileLocation.get(i)+" ";
							}
							searchResult = "The file is present with these peers: "+location;
							// System.out.println(location);
							cisSend.println(searchResult);
							
						}
						catch(Exception e){
							error = "File not registered";
							cisSend.println(error);
						}
						
						break;
					
						
					case "3": // case to download a file
						
						cisSend.println(requestType);
						break;
					
					case "4": // exit case

						System.out.println("Client Connection Closing");
						condition = false;
						cisSend.println(requestType);
						break;

					case "5": // get fileMap size
						// Get the size of the HashMap
        				int size = fileMap.size();

						cisSend.println(Integer.toString(size));
						break;

					case "6": // case to search a file in batch mode.
						
						fileName = cisRecived.nextLine();
						fileLocation = search(fileName);
						
						try{
							for(int i = 0; i<fileLocation.size();i++){
								location += fileLocation.get(i)+" ";
							}
							//System.out.println(location);
							cisSend.println(location);
							
						}
						catch(Exception e){
							error = "File not registered";
							cisSend.println(error);
						}
						
						break;
					}
				}
				catch(IOException e){
					e.printStackTrace();
				}
		}		
	}
	
	/*
	 * Register function - To register file present with each peer  
	 */
	public void registry(String peerId, String fileName) throws IOException{
		
		ArrayList<String> newList = new ArrayList<String>();
		ArrayList<String> oldList = new ArrayList<String>();

		ReadWriteLock lock = new ReentrantReadWriteLock();
		Lock writeLock = lock.writeLock();

		newList.add(peerId);
		oldList = fileMap.get(fileName);
		
		try {
           	writeLock.lock();
			if(oldList == null || oldList.isEmpty()){
				fileMap.put(fileName, newList);
			}
			else{
				for(int i = 0; i <oldList.size();i++){
					if(oldList.get(i).equals(peerId)){
						oldList.remove(i);
					}
			}
			oldList.add(peerId);
			fileMap.put(fileName, oldList);
			}
		} finally {
   			writeLock.unlock();
     	}
	}
	
	/*
	 * Lookup file - Search file return the peer with which it is present 
	 */
	public ArrayList<String> search(String fileName)throws IOException{
		
		ArrayList<String> peerList = new ArrayList<String>();
		peerList = fileMap.get(fileName);
		return peerList;
		
	}
	
}