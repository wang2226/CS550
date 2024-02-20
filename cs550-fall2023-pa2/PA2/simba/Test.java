import java.io.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public abstract class Test {
    protected String nodeId;
    protected static ArrayList<String> smallFiles = new ArrayList<String>();
    protected static ArrayList<String> largeFiles = new ArrayList<String>();

    Test() {

        setNodeInfo();
        try {
            setTestFile("Small_Files");
            setTestFile("Large_Files");
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    protected void setNodeInfo() {
        try {
            // get host name
            nodeId = InetAddress.getLocalHost().getHostName();
        } catch (Exception E) {
            System.err.println(E.getMessage());
        }
    }

    protected void setTestFile(String dataSet) throws Throwable {
        ArrayList<String> listOfFiles = new ArrayList<String>();
        String fileName = ReadProperties.getByName(dataSet);

        // load name of file
        BufferedReader bf = new BufferedReader(new FileReader(fileName));

        // read entire line as string
        String line = bf.readLine();

        // checking for end of file
        while (line != null) {
            listOfFiles.add(line);
            line = bf.readLine();
        }

        // closing bufferreader object
        bf.close();

        if (dataSet.equalsIgnoreCase("Small_Files")) {
            smallFiles.addAll(listOfFiles);
        }
        else {
            largeFiles.addAll(listOfFiles);
        }
    }

    protected String[] getRandomFile(String type, int size) {

        ArrayList<String> selectedFiles = new ArrayList<>();
        int randomIndex;


        for (int i = 0; i < size; i++) {
            String randomFile = "";

            if (type.equalsIgnoreCase("small")) {
                int min = 0;
                int max = smallFiles.size() - 1;
                randomIndex = new Random().nextInt(max - min + 1) + min;
                randomFile = smallFiles.get(randomIndex);
            } else {
                int min = 0;
                int max = largeFiles.size() - 1;
                randomIndex = new Random().nextInt(max - min + 1) + min;
                randomFile = largeFiles.get(randomIndex);
            }
            selectedFiles.add(randomFile);
        }

        return selectedFiles.toArray(new String[0]);
    }


    protected abstract void runTest(String type) throws Throwable;
}
