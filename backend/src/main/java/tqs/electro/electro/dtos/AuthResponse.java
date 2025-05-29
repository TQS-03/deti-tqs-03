package tqs.electro.electro.dtos;

import java.util.UUID;

public class AuthResponse {
    private String token;
    private UUID userId;
    private String email;

    public AuthResponse(String token, UUID userId, String email) {
        this.token = token;
        this.userId = userId;
        this.email = email;
    }

    // getters
    public String getToken() { return token; }
    public UUID getUserId() { return userId; }
    public String getEmail() { return email; }
}
