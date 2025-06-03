package tqs.electro.electro.unitTests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tqs.electro.electro.dtos.ExistingCardDTO;
import tqs.electro.electro.dtos.PaymentCardDTO;
import tqs.electro.electro.dtos.PaymentResponseDTO;
import tqs.electro.electro.entities.PaymentCard;
import tqs.electro.electro.entities.PaymentRecord;
import tqs.electro.electro.entities.Person;
import tqs.electro.electro.repositories.PaymentCardRepository;
import tqs.electro.electro.repositories.PaymentRecordRepository;
import tqs.electro.electro.repositories.PersonRepository;
import tqs.electro.electro.services.PaymentService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private PaymentRecordRepository paymentRecordRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private PaymentCardRepository paymentCardRepository;


    @Test
    public void testPayUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(personRepository.findById(userId)).thenReturn(Optional.empty());

        PaymentCardDTO dto = new PaymentCardDTO("4242424242424242", "12/30", "123", 50.0, false);
        PaymentResponseDTO response = paymentService.pay(userId, dto);

        assertEquals(404, response.getStatusCode());
        assertEquals("USER NOT FOUND", response.getPaymentStatus());
    }

    @Test
    public void testPayCardInvalid() {
        UUID userId = UUID.randomUUID();
        Person person = new Person();
        when(personRepository.findById(userId)).thenReturn(Optional.of(person));

        PaymentCardDTO dto = new PaymentCardDTO("1111222233334444", "12/19", "999", 50.0, false);
        PaymentResponseDTO response = paymentService.pay(userId, dto);

        assertEquals(400, response.getStatusCode());
        assertEquals("PAYMENT FAILDED", response.getPaymentStatus());
    }

    @Test
    public void testPaySuccessAndSaveCard() {
        UUID userId = UUID.randomUUID();
        Person person = new Person();
        when(personRepository.findById(userId)).thenReturn(Optional.of(person));

        PaymentCardDTO dto = new PaymentCardDTO("4242424242424242", "12/30", "123", 50.0, true);
        PaymentResponseDTO response = paymentService.pay(userId, dto);

        assertEquals(200, response.getStatusCode());
        assertEquals("SUCCESS", response.getPaymentStatus());

        verify(paymentCardRepository, times(1)).save(any());
        verify(paymentRecordRepository, times(1)).save(any());
    }

    @Test
    public void testGetPaymentHistory() {
        UUID userId = UUID.randomUUID();
        when(paymentRecordRepository.findByPersonId(userId)).thenReturn(List.of(new PaymentRecord()));

        List<PaymentRecord> history = paymentService.getPaymentHistory(userId);
        assertEquals(1, history.size());
    }

    @Test
    public void testAutoPayUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(personRepository.findById(userId)).thenReturn(Optional.empty());

        PaymentResponseDTO response = paymentService.autoPay(userId, 30.0);

        assertEquals(404, response.getStatusCode());
        assertEquals("USER NOT FOUND", response.getPaymentStatus());
        assertNull(response.getPaymentId());
    }

    @Test
    public void testAutoPayNoSavedCard() {
        UUID userId = UUID.randomUUID();
        Person person = new Person();
        when(personRepository.findById(userId)).thenReturn(Optional.of(person));

        PaymentResponseDTO response = paymentService.autoPay(userId, 20.0);

        assertEquals(400, response.getStatusCode());
        assertEquals("NO SAVED CARD", response.getPaymentStatus());
        assertNull(response.getPaymentId());
    }

    @Test
    public void testAutoPayCardInvalid() {
        UUID userId = UUID.randomUUID();
        Person person = new Person();
        // Saved card with invalid number (fails Luhn)
        PaymentCard invalidCard = new PaymentCard();
        invalidCard.setCardNumber("1234567890123456");
        invalidCard.setExpiryDate("12/30");
        invalidCard.setCvv("123");
        person.setPaymentCard(invalidCard);

        when(personRepository.findById(userId)).thenReturn(Optional.of(person));

        PaymentResponseDTO response = paymentService.autoPay(userId, 15.0);

        assertEquals(400, response.getStatusCode());
        assertEquals("AUTO PAYMENT FAILED", response.getPaymentStatus());
        assertNull(response.getPaymentId());
    }

    @Test
    public void testAutoPaySuccess() {
        UUID userId = UUID.randomUUID();
        Person person = new Person();
        // Valid test Visa number that passes Luhn: 4242424242424242
        PaymentCard validCard = new PaymentCard();
        validCard.setCardNumber("4242424242424242");
        validCard.setExpiryDate("12/30");
        validCard.setCvv("123");
        person.setPaymentCard(validCard);

        when(personRepository.findById(userId)).thenReturn(Optional.of(person));
        when(paymentRecordRepository.save(any(PaymentRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponseDTO response = paymentService.autoPay(userId, 50.0);

        assertEquals(200, response.getStatusCode());
        assertEquals("SUCCESS", response.getPaymentStatus());
        assertNotNull(response.getPaymentId());

        // Verify that a PaymentRecord was saved
        verify(paymentRecordRepository, times(1)).save(any(PaymentRecord.class));
    }

    @Test
    public void testHasSavedCardUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(personRepository.findById(userId)).thenReturn(Optional.empty());

        ExistingCardDTO result = paymentService.hasSavedCard(userId);

        assertEquals("-1", result.getCardNumber());
        assertEquals("-1", result.getExpiryDate());
    }

    @Test
    public void testHasSavedCardNoCard() {
        UUID userId = UUID.randomUUID();
        Person person = new Person();
        when(personRepository.findById(userId)).thenReturn(Optional.of(person));

        ExistingCardDTO result = paymentService.hasSavedCard(userId);

        assertEquals("-1", result.getCardNumber());
        assertEquals("-1", result.getExpiryDate());
    }

    @Test
    public void testHasSavedCardFound() {
        UUID userId = UUID.randomUUID();
        Person person = new Person();
        // Valid test card number
        String fullNumber = "4242424242424242";
        PaymentCard savedCard = new PaymentCard();
        savedCard.setCardNumber(fullNumber);
        savedCard.setExpiryDate("11/29");
        savedCard.setCvv("321");
        person.setPaymentCard(savedCard);

        when(personRepository.findById(userId)).thenReturn(Optional.of(person));

        ExistingCardDTO result = paymentService.hasSavedCard(userId);

        String expectedMasked = "************4242";
        assertEquals(expectedMasked, result.getCardNumber());
        assertEquals("11/29", result.getExpiryDate());
    }
}
