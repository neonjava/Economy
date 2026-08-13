package neonjava.in.economy.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class UserProfile {

    private final UUID uuid;
    private String name;
    private double balance;
    private double bankBalance;
    private double totalEarned;
    private double totalSpent;
    private long firstJoin;
    private long lastSeen;
    private boolean locked;
    private final List<Transaction> transactions;

    public UserProfile(UUID uuid, String name, double initialBalance) {
        this.uuid = uuid;
        this.name = name;
        this.balance = initialBalance;
        this.bankBalance = 0.0;
        this.totalEarned = initialBalance;
        this.totalSpent = 0.0;
        this.firstJoin = System.currentTimeMillis();
        this.lastSeen = System.currentTimeMillis();
        this.locked = false;
        this.transactions = new ArrayList<>();
        if (initialBalance > 0) {
            addTransaction(new Transaction(Transaction.Type.DEPOSIT, initialBalance, "Initial Balance", "System"));
        }
    }

    public UserProfile(UUID uuid, String name, double balance, double bankBalance, double totalEarned, double totalSpent,
                       long firstJoin, long lastSeen, boolean locked, List<Transaction> transactions) {
        this.uuid = uuid;
        this.name = name;
        this.balance = balance;
        this.bankBalance = bankBalance;
        this.totalEarned = totalEarned;
        this.totalSpent = totalSpent;
        this.firstJoin = firstJoin;
        this.lastSeen = lastSeen;
        this.locked = locked;
        this.transactions = transactions != null ? new ArrayList<>(transactions) : new ArrayList<>();
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getBankBalance() {
        return bankBalance;
    }

    public void setBankBalance(double bankBalance) {
        this.bankBalance = bankBalance;
    }

    public boolean depositToBank(double amount) {
        if (amount <= 0 || balance < amount) return false;
        balance -= amount;
        bankBalance += amount;
        addTransaction(new Transaction(Transaction.Type.WITHDRAW, amount, "Bank Deposit", "Bank"));
        return true;
    }

    public boolean withdrawFromBank(double amount) {
        if (amount <= 0 || bankBalance < amount) return false;
        bankBalance -= amount;
        balance += amount;
        addTransaction(new Transaction(Transaction.Type.DEPOSIT, amount, "Bank Withdrawal", "Bank"));
        return true;
    }

    public double addInterest(double ratePercent) {
        if (bankBalance <= 0 || ratePercent <= 0) return 0.0;
        double interest = (bankBalance * ratePercent) / 100.0;
        bankBalance += interest;
        addTransaction(new Transaction(Transaction.Type.DEPOSIT, interest, "Daily Savings Interest (" + ratePercent + "%)", "Bank Interest"));
        return interest;
    }

    public double getTotalEarned() {
        return totalEarned;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public long getFirstJoin() {
        return firstJoin;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void updateLastSeen() {
        this.lastSeen = System.currentTimeMillis();
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(0, transaction);
        if (transactions.size() > 50) {
            transactions.remove(transactions.size() - 1);
        }
    }

    public boolean deposit(double amount, Transaction.Type type, String description, String relatedPlayer) {
        if (amount <= 0) return false;
        this.balance += amount;
        this.totalEarned += amount;
        addTransaction(new Transaction(type, amount, description, relatedPlayer));
        return true;
    }

    public boolean withdraw(double amount, Transaction.Type type, String description, String relatedPlayer) {
        if (amount <= 0 || this.balance < amount) return false;
        this.balance -= amount;
        this.totalSpent += amount;
        addTransaction(new Transaction(type, amount, description, relatedPlayer));
        return true;
    }

    public String getFormattedFirstJoin() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm");
        return sdf.format(new Date(firstJoin));
    }

    public String getFormattedLastSeen() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm");
        return sdf.format(new Date(lastSeen));
    }
}
