package tqs.electro.electro.entities;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class PaymentCard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String cardNumber;
    private String expiryDate;
    private String cvv;

    @OneToOne
    @JoinColumn(name = "person_id", referencedColumnName = "id")
    private Person owner;

    public PaymentCard(UUID id, String cardNumber, String expiryDate, String cvv, Person owner) {
        this.id = id;
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
        this.owner = owner;
    }

    public PaymentCard() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }
}