import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ReadProperties {

    public static String getByName(String name) {

        // Create a Properties object
        Properties props = new Properties();

        try {
            // Load properties file from file system
            props.load(new FileInputStream("config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String property = props.getProperty(name);

	if(property == null){
		property = "error";		
    	}

	return property;
    }
}
