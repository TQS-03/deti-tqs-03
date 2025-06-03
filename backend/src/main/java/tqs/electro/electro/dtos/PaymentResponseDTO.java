package tqs.electro.electro.dtos;

import java.util.UUID;

public class PaymentResponseDTO {
    private UUID paymentId;
    private String paymentStatus;
    private int statusCode;

    public PaymentResponseDTO(UUID paymentId, String paymentStatus, int statusCode) {
        this.paymentId = paymentId;
        this.paymentStatus = paymentStatus;
        this.statusCode = statusCode;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
