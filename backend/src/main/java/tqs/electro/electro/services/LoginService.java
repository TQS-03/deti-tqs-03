package tqs.electro.electro.services;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import tqs.electro.electro.dtos.AuthResponse;
import tqs.electro.electro.dtos.LoginRequest;
import tqs.electro.electro.dtos.RegisterRequest;
import tqs.electro.electro.entities.Person;
import tqs.electro.electro.repositories.PersonRepository;

import java.util.UUID;

@Service
public class LoginService {
    private final PersonRepository userRepo;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public LoginService(PersonRepository userRepo) {
        this.userRepo = userRepo;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }
        Person user = new Person();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword_hash(passwordEncoder.encode(request.getPassword()));
        user.setIsWorker(request.getIsWorker());
        Person saved = userRepo.save(user);
        user.setId(saved.getId());

        return new AuthResponse(user.getId(), user.getEmail(), user.isWorker());
    }

    public AuthResponse login(LoginRequest request) {
        Person user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword_hash())) {
            throw new RuntimeException("Invalid credentials");
        }
        return new AuthResponse(user.getId(), user.getEmail(), user.isWorker());
    }
}
