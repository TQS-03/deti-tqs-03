package tqs.electro.electro.dtos;

import java.util.UUID;

public class AuthResponse {
    private UUID userId;
    private String email;

    public AuthResponse(UUID userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    // getters
    public UUID getUserId() { return userId; }
    public String getEmail() { return email; }
}
