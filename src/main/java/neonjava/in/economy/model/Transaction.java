package neonjava.in.economy.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Transaction {

    public enum Type {
        DEPOSIT("➕ Deposit"),
        WITHDRAW("➖ Withdraw"),
        TRANSFER_SENT("📤 Transfer Sent"),
        TRANSFER_RECEIVED("📥 Transfer Received"),
        ADMIN_SET("⚙️ Admin Set"),
        ADMIN_ADD("➕ Admin Add"),
        ADMIN_TAKE("➖ Admin Take");

        private final String displayName;

        Type(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final long timestamp;
    private final Type type;
    private final double amount;
    private final String description;
    private final String relatedPlayer;

    public Transaction(Type type, double amount, String description, String relatedPlayer) {
        this.timestamp = System.currentTimeMillis();
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.relatedPlayer = relatedPlayer;
    }

    public Transaction(long timestamp, Type type, double amount, String description, String relatedPlayer) {
        this.timestamp = timestamp;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.relatedPlayer = relatedPlayer;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Type getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public String getRelatedPlayer() {
        return relatedPlayer;
    }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(timestamp));
    }
}
