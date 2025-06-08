package tqs.electro.electro.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tqs.electro.electro.dtos.ExistingCardDTO;
import tqs.electro.electro.dtos.PaymentCardDTO;
import tqs.electro.electro.dtos.PaymentResponseDTO;
import tqs.electro.electro.entities.PaymentCard;
import tqs.electro.electro.entities.PaymentRecord;
import tqs.electro.electro.entities.Person;
import tqs.electro.electro.repositories.PaymentCardRepository;
import tqs.electro.electro.repositories.PaymentRecordRepository;
import tqs.electro.electro.repositories.PersonRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class PaymentService {

    @Autowired
    private PaymentRecordRepository paymentRecordRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @Autowired
    private ReservationService reservationService;

    public PaymentResponseDTO pay(UUID personId, UUID reservationId, PaymentCardDTO cardDTO) {
        Optional<Person> optionalPerson = personRepository.findById(personId);

        if (optionalPerson.isEmpty()) {
            return new PaymentResponseDTO(null, "USER NOT FOUND", 404);
        }

        Person person = optionalPerson.get();

        boolean paymentSuccessful = mockChargeCard(cardDTO);

        if (!paymentSuccessful) {
            return new PaymentResponseDTO(null, "PAYMENT FAILDED", 400);
        }


        if (cardDTO.isSaveCard()) {
            PaymentCard newCard = new PaymentCard();
            newCard.setCardNumber(cardDTO.getCardNumber());
            newCard.setExpiryDate(cardDTO.getExpiryDate());
            newCard.setCvv(cardDTO.getCvv());
            newCard.setOwner(person);

            person.setPaymentCard(newCard);

            paymentCardRepository.save(newCard);
            personRepository.save(person);
        }

        UUID paymentId = UUID.randomUUID();
        String last4 = cardDTO.getCardNumber().substring(cardDTO.getCardNumber().length() - 4);
        PaymentRecord record = new PaymentRecord(paymentId, cardDTO.getAmount(), last4, person);
        paymentRecordRepository.save(record);

        reservationService.updateReservationPaidStatus(reservationId, true);

        return new PaymentResponseDTO(paymentId, "SUCCESS", 200);
    }

    public List<PaymentRecord> getPaymentHistory(UUID personId) {
        return paymentRecordRepository.findByPersonId(personId);
    }

    public PaymentResponseDTO autoPay(UUID personId, UUID reservationId, double amount) {
        Optional<Person> optionalPerson = personRepository.findById(personId);

        if (optionalPerson.isEmpty()) {
            return new PaymentResponseDTO(null, "USER NOT FOUND", 404);
        }

        Person person = optionalPerson.get();
        PaymentCard savedCard = person.getPaymentCard();

        if (savedCard == null) {
            return new PaymentResponseDTO(null, "NO SAVED CARD", 400);
        }

        PaymentCardDTO cardDTO = new PaymentCardDTO(
                savedCard.getCardNumber(),
                savedCard.getExpiryDate(),
                savedCard.getCvv(),
                amount,
                false
        );

        boolean paymentSuccessful = mockChargeCard(cardDTO);

        if (!paymentSuccessful) {
            return new PaymentResponseDTO(null, "AUTO PAYMENT FAILED", 400);
        }

        UUID paymentId = UUID.randomUUID();
        String last4 = savedCard.getCardNumber().substring(savedCard.getCardNumber().length() - 4);
        PaymentRecord record = new PaymentRecord(paymentId, amount, last4, person);
        reservationService.updateReservationPaidStatus(reservationId, true);
        paymentRecordRepository.save(record);

        return new PaymentResponseDTO(paymentId, "SUCCESS", 200);
    }

    public ExistingCardDTO hasSavedCard(UUID personId) {
        Optional<Person> optionalPerson = personRepository.findById(personId);
        if (optionalPerson.isEmpty()) {
            return new ExistingCardDTO("-1", "-1");
        }

        PaymentCard savedCard = optionalPerson.get().getPaymentCard();
        if (savedCard == null) {
            return new ExistingCardDTO("-1", "-1");
        }

        int length = savedCard.getCardNumber().length();
        String masked = "*".repeat(length - 4) + savedCard.getCardNumber().substring(length - 4);
        return new ExistingCardDTO(masked, savedCard.getExpiryDate());
    }

    private boolean mockChargeCard(PaymentCardDTO card) {
        return isValidCardNumber(card.getCardNumber())
                && isValidCVV(card.getCvv())
                && isValidExpiryDate(card.getExpiryDate());
    }

    private boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || !cardNumber.matches("\\d+")) {
            return false;
        }

        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }

    private boolean isValidCVV(String cvv) {
        return cvv != null && cvv.matches("\\d{3,4}");
    }

    private boolean isValidExpiryDate(String expiryDate) {
        if (expiryDate == null || !expiryDate.matches("(0[1-9]|1[0-2])/\\d{2}")) {
            return false;
        }

        String[] parts = expiryDate.split("/");
        int month = Integer.parseInt(parts[0]);
        int year = Integer.parseInt(parts[1]) + 2000;

        java.time.YearMonth current = java.time.YearMonth.now();
        java.time.YearMonth expiry = java.time.YearMonth.of(year, month);

        return !expiry.isBefore(current);
    }
}

