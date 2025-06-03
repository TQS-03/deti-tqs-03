package tqs.electro.electro.unitTests;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tqs.electro.electro.controllers.LoginController;
import tqs.electro.electro.dtos.AuthResponse;
import tqs.electro.electro.dtos.LoginRequest;
import tqs.electro.electro.dtos.RegisterRequest;
import tqs.electro.electro.services.LoginService;

import java.util.UUID;

@WebMvcTest(LoginController.class)
public class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoginService loginService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testRegisterSuccess() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setEmail("john.doe@example.com");
        req.setPassword("password");
        req.setIsWorker(false);

        AuthResponse authRes = new AuthResponse(UUID.randomUUID(), "john.doe@example.com", false);
        when(loginService.register(any(RegisterRequest.class))).thenReturn(authRes);

        mockMvc.perform(post("/backend/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(authRes.getUserId().toString()))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    public void testRegisterFailure() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("Jane");
        req.setLastName("Doe");
        req.setEmail("jane.doe@example.com");
        req.setPassword("password");
        req.setIsWorker(true);

        when(loginService.register(any(RegisterRequest.class))).thenThrow(new RuntimeException("Email already in use"));

        mockMvc.perform(post("/backend/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testLoginSuccess() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("john.doe@example.com");
        req.setPassword("password");

        AuthResponse authRes = new AuthResponse(UUID.randomUUID(), "john.doe@example.com", true);
        when(loginService.login(any(LoginRequest.class))).thenReturn(authRes);

        mockMvc.perform(post("/backend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(authRes.getUserId().toString()))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    public void testLoginFailure() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("john.doe@example.com");
        req.setPassword("wrongpassword");

        when(loginService.login(any(LoginRequest.class))).thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/backend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
