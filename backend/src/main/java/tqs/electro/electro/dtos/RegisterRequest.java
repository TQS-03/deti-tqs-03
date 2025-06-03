package tqs.electro.electro.dtos;

public class RegisterRequest {
  private String firstName;
  private String lastName;
  private String email;
  private String password;
  private Boolean isWorker;

  public RegisterRequest(String firstName, String lastName, String email, String password, Boolean isWorker) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.password = password;
    this.isWorker = isWorker;
  }

  public RegisterRequest() {
  }

  // getters/setters
  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Boolean getIsWorker() {
    return isWorker;
  }

  public void setIsWorker(Boolean isWorker) {
    this.isWorker = isWorker;
  }
}
