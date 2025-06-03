package tqs.electro.electro.dtos;

import java.util.UUID;

public class AuthResponse {
  private UUID userId;
  private String email;
  private boolean isWorker;

  public AuthResponse(UUID userId, String email, boolean isWorker) {
    this.userId = userId;
    this.email = email;
    this.isWorker = isWorker;
  }

  // getters
  public UUID getUserId() {
    return userId;
  }

  public String getEmail() {
    return email;
  }

  public boolean getIsWorker() {
    return isWorker;
  }
}
