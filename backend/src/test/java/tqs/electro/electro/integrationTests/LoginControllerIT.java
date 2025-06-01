package tqs.electro.electro.integrationTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tqs.electro.electro.dtos.LoginRequest;
import tqs.electro.electro.dtos.RegisterRequest;
import tqs.electro.electro.entities.Person;
import tqs.electro.electro.repositories.PersonRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class LoginControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PersonRepository personRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    public void setup() {
        personRepository.deleteAll();
    }

    @Test
    public void testRegisterIntegration() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setPassword("password123");
        request.setIsWorker(false);

        mockMvc.perform(post("/backend/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    public void testRegisterExistingEmailFails() throws Exception {
        Person existing = new Person();
        existing.setFirstName("Jane");
        existing.setLastName("Doe");
        existing.setEmail("jane@example.com");
        existing.setPassword_hash(passwordEncoder.encode("secure"));
        existing.setIsWorker(false);
        personRepository.save(existing);

        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setEmail("jane@example.com");
        request.setPassword("secure");
        request.setIsWorker(false);

        mockMvc.perform(post("/backend/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testLoginIntegration() throws Exception {
        Person user = new Person();
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("testuser@example.com");
        user.setPassword_hash(passwordEncoder.encode("testpass"));
        user.setIsWorker(false);
        personRepository.save(user);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("testuser@example.com");
        loginRequest.setPassword("testpass");

        mockMvc.perform(post("/backend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.email").value("testuser@example.com"));
    }

    @Test
    public void testLoginWithInvalidPasswordFails() throws Exception {
        Person user = new Person();
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("testuser2@example.com");
        user.setPassword_hash(passwordEncoder.encode("rightpass"));
        user.setIsWorker(false);
        personRepository.save(user);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("testuser2@example.com");
        loginRequest.setPassword("wrongpass");

        mockMvc.perform(post("/backend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }
}
