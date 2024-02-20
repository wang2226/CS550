import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;

public class BatchRegister {
    CISClient client ;
    String thisNode;
    //Automatically registering files as the peer is connected
    protected BatchRegister() {
        client = new CISClient();
    }

    protected void sendRegisterInfo() throws IOException {
        Path dir = Paths.get(ReadProperties.getByName("Shared_Directory"));
        String pattern = "*_" + thisNode + "_*"; // Wildcard pattern to match local  files
        ArrayList<String> sharedFiles = new ArrayList<String>();
        String retResult ="";

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
        retResult=client.register(sharedFiles);
        System.out.println("Total register" + sharedFiles.size() + " file and status is " + retResult +".");

    }

    public static void main(String[] args) throws Throwable {

        BatchRegister batchRegister = new BatchRegister();

        batchRegister.sendRegisterInfo();

    }

}
