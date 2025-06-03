package tqs.electro.electro.unitTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tqs.electro.electro.controllers.DriverController;
import tqs.electro.electro.dtos.ExistingCardDTO;
import tqs.electro.electro.dtos.PaymentCardDTO;
import tqs.electro.electro.dtos.PaymentResponseDTO;
import tqs.electro.electro.entities.PaymentRecord;
import tqs.electro.electro.services.PaymentService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DriverController.class)
public class PaymentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    private final UUID userId = UUID.randomUUID();

    @Test
    public void testPayEndpoint() throws Exception {
        UUID userId = UUID.randomUUID();
        PaymentCardDTO dto = new PaymentCardDTO("4242424242424242", "12/30", "123", 50.0, true);
        PaymentResponseDTO response = new PaymentResponseDTO(UUID.randomUUID(), "SUCCESS", 200);

        when(paymentService.pay(eq(userId), any())).thenReturn(response);

        mockMvc.perform(post("/backend/driver/payments/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"));
    }

    @Test
    public void testGetHistoryEndpoint() throws Exception {
        UUID userId = UUID.randomUUID();
        when(paymentService.getPaymentHistory(userId)).thenReturn(List.of(new PaymentRecord()));

        mockMvc.perform(get("/backend/driver/payments/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testAutoPaySuccess() throws Exception {
        // Arrange: Service returns a successful PaymentResponseDTO
        PaymentResponseDTO successDto = new PaymentResponseDTO(UUID.randomUUID(), "SUCCESS", 200);
        when(paymentService.autoPay(eq(userId), eq(25.0))).thenReturn(successDto);

        // Act & Assert
        mockMvc.perform(post("/backend/driver/payments/" + userId + "/auto")
                        .param("amount", "25.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    public void testAutoPayUserNotFound() throws Exception {
        // Arrange: Service returns a 404 USER NOT FOUND
        PaymentResponseDTO notFoundDto = new PaymentResponseDTO(null, "USER NOT FOUND", 404);
        when(paymentService.autoPay(eq(userId), eq(10.0))).thenReturn(notFoundDto);

        // Act & Assert
        mockMvc.perform(post("/backend/driver/payments/" + userId + "/auto")
                        .param("amount", "10.0"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.paymentStatus").value("USER NOT FOUND"))
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    public void testAutoPayNoSavedCard() throws Exception {
        // Arrange: Service returns a 400 NO SAVED CARD
        PaymentResponseDTO noCardDto = new PaymentResponseDTO(null, "NO SAVED CARD", 400);
        when(paymentService.autoPay(eq(userId), eq(15.0))).thenReturn(noCardDto);

        // Act & Assert
        mockMvc.perform(post("/backend/driver/payments/" + userId + "/auto")
                        .param("amount", "15.0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.paymentStatus").value("NO SAVED CARD"))
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    public void testHasSavedCardFound() throws Exception {
        // Arrange: Service returns a masked number and expiry
        ExistingCardDTO cardDto = new ExistingCardDTO("************4242", "12/25");
        when(paymentService.hasSavedCard(eq(userId))).thenReturn(cardDto);

        // Act & Assert
        mockMvc.perform(get("/backend/driver/" + userId + "/has-card"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").value("************4242"))
                .andExpect(jsonPath("$.expiryDate").value("12/25"));
    }

    @Test
    public void testHasSavedCardNotFoundOrNoCard() throws Exception {
        // Arrange: Service returns placeholders when user not found or no card
        ExistingCardDTO noCardDto = new ExistingCardDTO("-1", "-1");
        when(paymentService.hasSavedCard(eq(userId))).thenReturn(noCardDto);

        // Act & Assert
        mockMvc.perform(get("/backend/driver/" + userId + "/has-card"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").value("-1"))
                .andExpect(jsonPath("$.expiryDate").value("-1"));
    }
}
