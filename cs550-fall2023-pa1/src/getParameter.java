import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;


public class getParameter {

    public static String byName(String name)  {
        String retString = "get parameters error";
        String filePath = "config.properties";
        HashMap<String, String> hashMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split("="); 
                
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    
                    hashMap.put(key, value);
                } else {
                    System.out.println("Invalid line format: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // get the property value and print it out
        switch (name) {
        case "cisIp":
            retString = hashMap.get("Central_Indexing_Server_Ip");
            break;
        case "cisPort":
            retString = hashMap.get("Central_Indexing_Server_Port");
            break;
        case "peer1_sharedDirectory":
            retString = hashMap.get("Peer1_Shared_Directory");
            break;
        case "peer1_Ip":
            retString = hashMap.get("Peer1_Ip");
            break;
        case "peer1_serverPort":
            retString = hashMap.get("Peer1_Server_Port");
            break;
        case "peer2_sharedDirectory":
            retString = hashMap.get("Peer2_Shared_Directory");
            break;
        case "peer2_Ip":
            retString = hashMap.get("Peer2_Ip");
            break;
        case "peer2_serverPort":
            retString = hashMap.get("Peer2_Server_Port");
            break;
        case "peerNode":
            retString = hashMap.get("Peer_Node");
            break;
        case "fileNode":
            retString = hashMap.get("File_Node");
            break;
        }

        return retString;

    }
}