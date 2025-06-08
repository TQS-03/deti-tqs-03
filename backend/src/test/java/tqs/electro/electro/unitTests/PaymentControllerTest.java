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
    public void testPayEndpointSuccess() throws Exception {
        PaymentCardDTO dto = new PaymentCardDTO("4242424242424242", "12/30", "123", 50.0, true);
        PaymentResponseDTO response = new PaymentResponseDTO(UUID.randomUUID(), "SUCCESS", 200);
        UUID reservationId = UUID.randomUUID();

        when(paymentService.pay(eq(userId), eq(reservationId), any())).thenReturn(response);

        mockMvc.perform(post("/backend/driver/payments/" + userId + "/reservations/" + reservationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    public void testPayEndpointUserNotFound() throws Exception {
        PaymentCardDTO dto = new PaymentCardDTO("4242424242424242", "12/30", "123", 50.0, true);
        PaymentResponseDTO response = new PaymentResponseDTO(null, "USER NOT FOUND", 404);
        UUID reservationId = UUID.randomUUID();

        when(paymentService.pay(eq(userId), eq(reservationId), any())).thenReturn(response);

        mockMvc.perform(post("/backend/driver/payments/" + userId + "/reservations/" + reservationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.paymentStatus").value("USER NOT FOUND"))
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    public void testPayEndpointCardInvalid() throws Exception {
        PaymentCardDTO dto = new PaymentCardDTO("1234567890123456", "12/30", "123", 50.0, false);
        PaymentResponseDTO response = new PaymentResponseDTO(null, "PAYMENT FAILDED", 400);
        UUID reservationId = UUID.randomUUID();

        when(paymentService.pay(eq(userId), eq(reservationId), any())).thenReturn(response);

        mockMvc.perform(post("/backend/driver/payments/" + userId + "/reservations/" + reservationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.paymentStatus").value("PAYMENT FAILDED"))
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    public void testGetHistoryEndpoint() throws Exception {
        when(paymentService.getPaymentHistory(userId)).thenReturn(List.of(new PaymentRecord()));

        mockMvc.perform(get("/backend/driver/payments/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testAutoPaySuccess() throws Exception {
        PaymentResponseDTO successDto = new PaymentResponseDTO(UUID.randomUUID(), "SUCCESS", 200);
        UUID reservationId = UUID.randomUUID();

        when(paymentService.autoPay(eq(userId), eq(reservationId), eq(25.0))).thenReturn(successDto);

        mockMvc.perform(post("/backend/driver/payments/" + userId + "/reservations/" + reservationId + "/auto")
                        .param("amount", "25.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    public void testAutoPayUserNotFound() throws Exception {
        PaymentResponseDTO notFoundDto = new PaymentResponseDTO(null, "USER NOT FOUND", 404);
        UUID reservationId = UUID.randomUUID();

        when(paymentService.autoPay(eq(userId), eq(reservationId), eq(10.0))).thenReturn(notFoundDto);

        mockMvc.perform(post("/backend/driver/payments/" + userId + "/reservations/" + reservationId + "/auto")
                        .param("amount", "10.0"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.paymentStatus").value("USER NOT FOUND"))
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    public void testAutoPayNoSavedCard() throws Exception {
        PaymentResponseDTO noCardDto = new PaymentResponseDTO(null, "NO SAVED CARD", 400);
        UUID reservationId = UUID.randomUUID();

        when(paymentService.autoPay(eq(userId), eq(reservationId), eq(15.0))).thenReturn(noCardDto);

        mockMvc.perform(post("/backend/driver/payments/" + userId + "/reservations/" + reservationId + "/auto")
                        .param("amount", "15.0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.paymentStatus").value("NO SAVED CARD"))
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    public void testAutoPayCardInvalid() throws Exception {
        PaymentResponseDTO invalidDto = new PaymentResponseDTO(null, "AUTO PAYMENT FAILED", 400);
        UUID reservationId = UUID.randomUUID();

        when(paymentService.autoPay(eq(userId), eq(reservationId), eq(30.0))).thenReturn(invalidDto);

        mockMvc.perform(post("/backend/driver/payments/" + userId + "/reservations/" + reservationId + "/auto")
                        .param("amount", "30.0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.paymentStatus").value("AUTO PAYMENT FAILED"))
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    public void testHasSavedCardFound() throws Exception {
        ExistingCardDTO cardDto = new ExistingCardDTO("************4242", "12/25");
        when(paymentService.hasSavedCard(eq(userId))).thenReturn(cardDto);

        mockMvc.perform(get("/backend/driver/" + userId + "/has-card"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").value("************4242"))
                .andExpect(jsonPath("$.expiryDate").value("12/25"));
    }

    @Test
    public void testHasSavedCardNotFoundOrNoCard() throws Exception {
        ExistingCardDTO noCardDto = new ExistingCardDTO("-1", "-1");
        when(paymentService.hasSavedCard(eq(userId))).thenReturn(noCardDto);

        mockMvc.perform(get("/backend/driver/" + userId + "/has-card"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").value("-1"))
                .andExpect(jsonPath("$.expiryDate").value("-1"));
    }
}
