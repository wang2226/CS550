
import java.nio.charset.StandardCharsets;

/**
 * Transaction (128B)
 * – Sender Public Address (32B)
 * – Recipient Public Address (32B)
 * – Value (unsigned double, 8B)
 * – Timestamp (signed integer 8B)
 * – Transaction ID (16B)
 * – Signature (32B)
 */
public class Transaction {
    private byte[] sender_public_Address = new byte[32];
    private byte[] recipient_public_address = new byte[32];
    private double value;
    private long  timestamp;
    private byte[] tx_id = new byte[16];
    private byte[] signature = new byte[32];

    public Transaction(byte[] sender, byte[] recipient, double value, long timestamp, byte[] tx_id, byte[] sign) {
        this.sender_public_Address = sender;
        this.recipient_public_address = recipient;
        this.value = value;
        this.timestamp = timestamp;
        this.tx_id = tx_id;
        this.signature = sign;
    }

    public String toString(){
        return new String(sender_public_Address, StandardCharsets.UTF_8) +
                new String(recipient_public_address, StandardCharsets.UTF_8) +
                Double.toString(value) +
                Long.toString(timestamp) +
                new String(tx_id, StandardCharsets.UTF_8) +
                new String(signature,StandardCharsets.UTF_8) ;
    }
}
