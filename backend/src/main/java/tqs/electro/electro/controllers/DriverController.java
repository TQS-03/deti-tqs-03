package tqs.electro.electro.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tqs.electro.electro.dtos.ExistingCardDTO;
import tqs.electro.electro.dtos.PaymentCardDTO;
import tqs.electro.electro.dtos.PaymentResponseDTO;
import tqs.electro.electro.entities.PaymentRecord;
import tqs.electro.electro.services.PaymentService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("backend/driver")
public class DriverController {

    private PaymentService paymentService;

    public DriverController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments/{personId}")
    public ResponseEntity<PaymentResponseDTO> pay(
            @PathVariable UUID personId,
            @RequestBody PaymentCardDTO cardDTO
    ) {
        PaymentResponseDTO response = paymentService.pay(personId, cardDTO);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/payments/{personId}")
    public ResponseEntity<List<PaymentRecord>> getHistory(@PathVariable UUID personId) {
        List<PaymentRecord> records = paymentService.getPaymentHistory(personId);
        return ResponseEntity.ok(records);
    }

    @PostMapping("payments/{id}/auto")
    public ResponseEntity<PaymentResponseDTO> autoPay(
            @PathVariable("id") UUID id,
            @RequestParam double amount) {

        PaymentResponseDTO response = paymentService.autoPay(id, amount);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/{id}/has-card")
    public ResponseEntity<ExistingCardDTO> hasSavedCard(@PathVariable("id") UUID id) {
        ExistingCardDTO cardInfo = paymentService.hasSavedCard(id);
        return ResponseEntity.ok(cardInfo);
    }
}
