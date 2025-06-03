package tqs.electro.electro.dtos;

import lombok.Getter;
import lombok.Setter;
import tqs.electro.electro.entities.Consumption;

import java.time.Duration;


public class ConsumptionResponse {

    private Consumption consumption;
    private Duration duration;
    private double cost;

    public Consumption getConsumption() {
        return consumption;
    }

    public void setConsumption(Consumption consumption) {
        this.consumption = consumption;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }
}
