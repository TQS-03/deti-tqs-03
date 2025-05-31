package tqs.electro.electro.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Consumption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private Station station;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double energyUsed; // in kWh
    private double pricePerKWh;

    public Duration getDuration() {
        return Duration.between(startTime, endTime);
    }

    public double getCost() {
        return energyUsed * pricePerKWh;
    }

}
