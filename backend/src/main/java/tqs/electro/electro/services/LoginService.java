package tqs.electro.electro.services;

import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import tqs.electro.electro.dtos.AuthResponse;
import tqs.electro.electro.dtos.LoginRequest;
import tqs.electro.electro.dtos.RegisterRequest;
import tqs.electro.electro.entities.Person;
import tqs.electro.electro.repositories.PersonRepository;
import tqs.electro.electro.utils.JwtUtil;

@Service
public class LoginService {
  private final PersonRepository userRepo;
  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
  private final JwtUtil jwtUtil;

  public LoginService(PersonRepository userRepo, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
    }

  public AuthResponse register(RegisterRequest request) {
    if (userRepo.findByEmail(request.getEmail()).isPresent()) {
      throw new RuntimeException("Email already in use");
    }
    Person user = new Person();
    user.setId(UUID.randomUUID());
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setEmail(request.getEmail());
    user.setPassword_hash(passwordEncoder.encode(request.getPassword()));
    user.setIsWorker(request.getIsWorker());
    userRepo.save(user);

    String token = jwtUtil.generateToken(user.getEmail());
    return new AuthResponse(token, user.getId(), user.getEmail());
  }

  public AuthResponse login(LoginRequest request) {
    Person user = userRepo.findByEmail(request.getEmail())
        .orElseThrow(() -> new RuntimeException("Invalid credentials"));
    if (!passwordEncoder.matches(request.getPassword(), user.getPassword_hash())) {
      throw new RuntimeException("Invalid credentials");
    }
    String token = jwtUtil.generateToken(user.getFirstName() + " " + user.getLastName());
    return new AuthResponse(token, user.getId(), user.getEmail());
  }
}
