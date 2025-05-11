package tqs.electro.electro;

import org.springframework.boot.SpringApplication;

public class TestElectroApplication {

  public static void main(String[] args) {
    SpringApplication.from(ElectroApplication::main).with(TestcontainersConfiguration.class).run(args);
  }

}
