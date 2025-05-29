package tqs.electro.electro.unitTests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tqs.electro.electro.dtos.AuthResponse;
import tqs.electro.electro.dtos.LoginRequest;
import tqs.electro.electro.dtos.RegisterRequest;
import tqs.electro.electro.entities.Person;
import tqs.electro.electro.repositories.PersonRepository;
import tqs.electro.electro.services.LoginService;
import tqs.electro.electro.utils.JwtUtil;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoginServiceTest {
    @Mock
    private PersonRepository userRepo;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private LoginService loginService;


    @Test
    void register_ShouldCreateUserAndReturnToken() {
        RegisterRequest request = new RegisterRequest("John", "Doe", "john@example.com", "password", false);

        when(userRepo.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(jwtUtil.generateToken("john@example.com")).thenReturn("mocked-token");

        AuthResponse response = loginService.register(request);

        assertNotNull(response);
        assertEquals("john@example.com", response.getEmail());
        assertEquals("mocked-token", response.getToken());
        verify(userRepo).save(any(Person.class));
    }

    @Test
    void register_ShouldThrowIfEmailExists() {
        RegisterRequest request = new RegisterRequest("John", "Doe", "john@example.com", "password", false);
        when(userRepo.findByEmail("john@example.com")).thenReturn(Optional.of(new Person()));

        assertThrows(RuntimeException.class, () -> loginService.register(request));
        verify(userRepo, never()).save(any());
    }

    @Test
    void login_ShouldReturnTokenWhenCredentialsAreValid() {
        String email = "jane@example.com";
        String rawPassword = "securepass";
        String encodedPassword = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(rawPassword);

        Person user = new Person();
        user.setId(UUID.randomUUID());
        user.setFirstName("Jane");
        user.setLastName("Smith");
        user.setEmail(email);
        user.setPassword_hash(encodedPassword);

        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("Jane Smith")).thenReturn("mocked-token");

        LoginRequest request = new LoginRequest(email, rawPassword);
        AuthResponse response = loginService.login(request);

        assertNotNull(response);
        assertEquals("mocked-token", response.getToken());
        assertEquals(email, response.getEmail());
    }

    @Test
    void login_ShouldThrowIfUserNotFound() {
        when(userRepo.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("nonexistent@example.com", "pass");

        assertThrows(RuntimeException.class, () -> loginService.login(request));
    }

    @Test
    void login_ShouldThrowIfPasswordInvalid() {
        Person user = new Person();
        user.setId(UUID.randomUUID());
        user.setFirstName("Bob");
        user.setLastName("Brown");
        user.setEmail("bob@example.com");
        user.setPassword_hash(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("correctPassword"));

        when(userRepo.findByEmail("bob@example.com")).thenReturn(Optional.of(user));

        LoginRequest request = new LoginRequest("bob@example.com", "wrongPassword");

        assertThrows(RuntimeException.class, () -> loginService.login(request));
    }
}
