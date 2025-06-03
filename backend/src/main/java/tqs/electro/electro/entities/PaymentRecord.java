package tqs.electro.electro.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class PaymentRecord {
    @Id
    private UUID id;

    private LocalDateTime timestamp;

    private double amount;

    private String last4Digits;

    @ManyToOne
    private Person person;

    public PaymentRecord() {}

    public PaymentRecord(UUID id, double amount, String last4Digits, Person person) {
        this.id = id;
        this.timestamp = LocalDateTime.now();
        this.amount = amount;
        this.last4Digits = last4Digits;
        this.person = person;
    }

    // Getters and setters
    public UUID getId() { return id; }

    public void setId(UUID id) { this.id = id; }

    public LocalDateTime getTimestamp() { return timestamp; }

    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public double getAmount() { return amount; }

    public void setAmount(double amount) { this.amount = amount; }

    public String getLast4Digits() { return last4Digits; }

    public void setLast4Digits(String last4Digits) { this.last4Digits = last4Digits; }

    public Person getPerson() { return person; }

    public void setPerson(Person person) { this.person = person; }
}
