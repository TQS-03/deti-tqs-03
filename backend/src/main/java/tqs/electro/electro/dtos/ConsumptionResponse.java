package tqs.electro.electro.dtos;

import lombok.Getter;
import lombok.Setter;
import tqs.electro.electro.entities.Consumption;

import java.time.Duration;

@Getter
@Setter
public class ConsumptionResponse {

    private Consumption consumption;
    private Duration duration;
    private double cost;

}
