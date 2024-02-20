
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
    private long timestamp;
    private String tx_id;
    private byte[] signature = new byte[32];

    public Transaction(byte[] sender, byte[] recipient, double value, long timestamp, String tx_id) {
        this.sender_public_Address = sender;
        this.recipient_public_address = recipient;
        this.value = value;
        this.timestamp = timestamp;
        this.tx_id = tx_id;
    }

    public String toString() {
        return Helper.bytesToHex(sender_public_Address) + "|" +
                Helper.bytesToHex(recipient_public_address) + "|" +
                Double.toString(value) + "|" +
                Long.toString(timestamp) + "|" +
                tx_id + "|" +
                Helper.bytesToHex(signature);
    }

    public String get_sneder() {
        return Helper.bytesToHex(sender_public_Address);
    }

    public String get_recipient() {
        return Helper.bytesToHex(recipient_public_address);
    }

    public double get_value() {
        return value;
    }

    public long get_timestamp() {
        return timestamp;
    }

    public String get_txId() {
        return tx_id;
    }
}
