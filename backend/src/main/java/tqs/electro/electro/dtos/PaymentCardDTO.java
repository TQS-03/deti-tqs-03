package tqs.electro.electro.dtos;

public class PaymentCardDTO {
    private String cardNumber;
    private String expiryDate;
    private String cvv;
    private boolean saveCard;
    private double amount;

    // Constructors
    public PaymentCardDTO() {}

    public PaymentCardDTO(String cardNumber, String expiryDate, String cvv, double amount, boolean saveCard) {
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
        this.saveCard = saveCard;
        this.amount = amount;
    }

    // Getters and Setters
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }

    public boolean isSaveCard() { return saveCard; }
    public void setSaveCard(boolean saveCard) { this.saveCard = saveCard; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}
