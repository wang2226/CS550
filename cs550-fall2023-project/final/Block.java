// Java implementation for creating
// a block in a Blockchain

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.time.Instant;
import java.util.LinkedList;

/***
 * Block (128B header + 128B*#trans)
 * – Block Size (unsigned integer 4B)
 * – Block Header (56B):
 *      Version (unsigned short integer 2B),
 *      Previous Block Hash (32B),
 *      BlockID (unsigned integer 4B),
 *      Timestamp (signed integer 8B),
 *      Difficulty Target ((unsigned short integer 2B)),
 *      Nonce (8B)
 * – Transaction Counter (unsigned integer 4B)
 * – Reserved (64B)
 * – Array of Transactions (variable)
 */

public class Block {

    // Every block contains a hash, previous hash and data of the transaction made
    private int blok_size;
    private short version;
    private byte[] prev_hash = new byte[32];
    private int block_id;
    private long timestamp;
    private short difficulty;
    private long nonce;
    private int tx_counter;
    private byte[] reserved = new byte[64];
    private LinkedList<Transaction> txs;

    // caches Transaction SHA256 hashes

    // Constructor for the block
    public Block(LinkedList<Transaction> txs, byte[] prev_hash, int block_id, Long timestamp, short difficulty,long nonce) {
        this.txs = txs != null ? txs : new LinkedList<>();
        if(txs == null) {
            this.blok_size = 128;
        }else {
            this.blok_size = 128 + 128 * txs.size();
        }
        this.version = 2;
        this.prev_hash = prev_hash;
        this.block_id = block_id;
        this.timestamp = timestamp != null ? timestamp : Instant.now().getEpochSecond();
        this.difficulty = difficulty;
        this.nonce = nonce;
        if(txs == null) {
            this.tx_counter = 0;
        }else{
            this.tx_counter = txs.size();
        }
        for (int i = 0; i < 64; i++) {this.reserved[i] = (byte) i;}
    }

    public String toHash(){
        return String.valueOf(blok_size) +
                String.valueOf(version) +
                new String(prev_hash, StandardCharsets.UTF_8) +
                String.valueOf(block_id) +
                Long.toString(timestamp) +
                String.valueOf(difficulty) +
                Long.toString(nonce) +
                String.valueOf(tx_counter) +
                new String(reserved, StandardCharsets.UTF_8);
    }
    public String toString(){
        return String.valueOf(blok_size) + "|" +
                String.valueOf(version) + "|" +
                new String(prev_hash, StandardCharsets.UTF_8) + "|" +
                String.valueOf(block_id) + "|" +
                Long.toString(timestamp) + "|" +
                String.valueOf(difficulty) + "|" +
                Long.toString(nonce) + "|"  +
                String.valueOf(tx_counter) + "|" +
                new String(reserved, StandardCharsets.UTF_8);
                //`txs.toString();
    }
}
