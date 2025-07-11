import java.sql.Timestamp;
import java.util.Date;

public class Transaction {
    private int id;
    private double amount;
    private String description;
    private String category;
    private boolean isIncome;
    private Timestamp dateCreated;

    // Constructor for new transactions (without ID)
    public Transaction(double amount, String description, String category, boolean isIncome, Date date) {
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.isIncome = isIncome;
        this.dateCreated = new Timestamp(date.getTime());
    }

    // Constructor for database transactions (with ID)
    public Transaction(int id, double amount, String description, String category, boolean isIncome, Timestamp dateCreated) {
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.isIncome = isIncome;
        this.dateCreated = dateCreated;
    }

    // Getters
    public int getId() { return id; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public boolean isIncome() { return isIncome; }
    public Timestamp getDateCreated() { return dateCreated; }
    public Date getDate() { return new Date(dateCreated.getTime()); }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }
    public void setIsIncome(boolean isIncome) { this.isIncome = isIncome; }
    public void setDateCreated(Timestamp dateCreated) { this.dateCreated = dateCreated; }

    @Override
    public String toString() {
        return String.format("Transaction{id=%d, amount=%.2f, description='%s', category='%s', isIncome=%b, date=%s}",
                id, amount, description, category, isIncome, dateCreated);
    }
}