package tqs.electro.electro.integrationTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tqs.electro.electro.dtos.PaymentCardDTO;
import tqs.electro.electro.entities.PaymentCard;
import tqs.electro.electro.entities.PaymentRecord;
import tqs.electro.electro.entities.Person;
import tqs.electro.electro.repositories.PaymentCardRepository;
import tqs.electro.electro.repositories.PaymentRecordRepository;
import tqs.electro.electro.repositories.PersonRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PersonRepository personRepository;

    @MockitoBean
    private PaymentCardRepository paymentCardRepository;

    @MockitoBean
    private PaymentRecordRepository paymentRecordRepository;

    private UUID personId;
    private Person mockPerson;

    @BeforeEach
    public void setup() {
        personId = UUID.randomUUID();
        mockPerson = new Person();
        mockPerson.setId(personId);
        mockPerson.setFirstName("Test");
        mockPerson.setLastName("User");
        mockPerson.setEmail("test@example.com");
        mockPerson.setPassword_hash("secret");
        mockPerson.setIsWorker(false);
    }

    @Test
    public void testSuccessfulPaymentIntegration() throws Exception {
        PaymentCardDTO dto = new PaymentCardDTO("4242424242424242", "12/30", "123", 20.00, true);

        when(personRepository.findById(personId)).thenReturn(Optional.of(mockPerson));
        when(paymentRecordRepository.save(any())).thenReturn(new PaymentRecord());

        mockMvc.perform(post("/backend/driver/payments/" + personId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"));
    }

    @Test
    public void testPaymentWithInvalidCard() throws Exception {
        PaymentCardDTO dto = new PaymentCardDTO("123", "12/30", "000", 20.00, false);

        when(personRepository.findById(personId)).thenReturn(Optional.of(mockPerson));

        mockMvc.perform(post("/backend/driver/payments/" + personId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.paymentStatus").value("PAYMENT FAILDED"));
    }

    @Test
    public void testGetPaymentHistory() throws Exception {
        when(paymentRecordRepository.findByPersonId(personId)).thenReturn(List.of(new PaymentRecord()));

        mockMvc.perform(get("/backend/driver/payments/" + personId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testAutoPaySuccess() throws Exception {
        // Arrange: Person has a valid saved card
        PaymentCard savedCard = new PaymentCard();
        savedCard.setCardNumber("4242424242424242");
        savedCard.setExpiryDate("12/30");
        savedCard.setCvv("123");
        mockPerson.setPaymentCard(savedCard);

        when(personRepository.findById(personId)).thenReturn(Optional.of(mockPerson));
        when(paymentRecordRepository.save(any(PaymentRecord.class)))
                .thenReturn(new PaymentRecord());

        // Act & Assert
        mockMvc.perform(post("/backend/driver/payments/" + personId + "/auto")
                        .param("amount", "25.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    public void testAutoPayUserNotFound() throws Exception {
        when(personRepository.findById(personId)).thenReturn(Optional.empty());

        mockMvc.perform(post("/backend/driver/payments/" + personId + "/auto")
                        .param("amount", "10.0"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.paymentStatus").value("USER NOT FOUND"))
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    public void testAutoPayNoSavedCard() throws Exception {
        // Person exists but has no saved card
        when(personRepository.findById(personId)).thenReturn(Optional.of(mockPerson));

        mockMvc.perform(post("/backend/driver/payments/" + personId + "/auto")
                        .param("amount", "15.0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.paymentStatus").value("NO SAVED CARD"))
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    public void testAutoPayCardInvalid() throws Exception {
        // Person exists with an invalid saved card
        PaymentCard invalidCard = new PaymentCard();
        invalidCard.setCardNumber("1234567890123456");
        invalidCard.setExpiryDate("12/30");
        invalidCard.setCvv("123");
        mockPerson.setPaymentCard(invalidCard);

        when(personRepository.findById(personId)).thenReturn(Optional.of(mockPerson));

        mockMvc.perform(post("/backend/driver/payments/" + personId + "/auto")
                        .param("amount", "30.0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.paymentStatus").value("AUTO PAYMENT FAILED"))
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    public void testHasSavedCardFound() throws Exception {
        // Person has a saved card
        PaymentCard savedCard = new PaymentCard();
        savedCard.setCardNumber("4242424242424242");
        savedCard.setExpiryDate("11/29");
        savedCard.setCvv("321");
        mockPerson.setPaymentCard(savedCard);

        when(personRepository.findById(personId)).thenReturn(Optional.of(mockPerson));

        mockMvc.perform(get("/backend/driver/" + personId + "/has-card"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").value("************4242"))
                .andExpect(jsonPath("$.expiryDate").value("11/29"));
    }

    @Test
    public void testHasSavedCardNotFoundOrNoCard() throws Exception {
        when(personRepository.findById(personId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/backend/driver/" + personId + "/has-card"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").value("-1"))
                .andExpect(jsonPath("$.expiryDate").value("-1"));

        when(personRepository.findById(personId)).thenReturn(Optional.of(mockPerson));

        mockMvc.perform(get("/backend/driver/" + personId + "/has-card"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").value("-1"))
                .andExpect(jsonPath("$.expiryDate").value("-1"));
    }
}
