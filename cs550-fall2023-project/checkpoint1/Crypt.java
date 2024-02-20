// Java program for Generating Hashes

import org.bitcoinj.core.Base58;
import java.security.*;

public class Crypt {

    // Function that takes the string input and returns the hashed string.
    public static String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte b : bytes) result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    public static String sha256(String string) throws NoSuchAlgorithmException {
        string = "80" + string;
        byte[] data = hexStringToByteArray(string);
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(data);

        return Base58.encode(digest);
    }

}
