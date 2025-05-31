package tqs.electro.electro.services;

import org.springframework.stereotype.Service;
import tqs.electro.electro.entities.Consumption;
import tqs.electro.electro.repositories.ConsumptionRepository;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class ConsumptionService {

    private final ConsumptionRepository consumptionRepository;
    private final Logger logger;

    public ConsumptionService(ConsumptionRepository consumptionRepository) {
        this.consumptionRepository = consumptionRepository;
        this.logger = Logger.getLogger(ConsumptionService.class.getName());
    }

    public Consumption save(Consumption consumption) {
        try {
            return consumptionRepository.save(consumption);
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }
        return null;
    }

    public List<Consumption> getAllConsumptionsByStationId(UUID stationId) {
        return consumptionRepository.findByStationId(stationId);
    }

    public Optional<Consumption> getConsumptionById(UUID id) {
        return consumptionRepository.findById(id);
    }

    public Optional<Consumption> updateConsumption(UUID id, Consumption updatedConsumption) {
        return consumptionRepository.findById(id).map(existing -> {
            existing.setStation(updatedConsumption.getStation());
            existing.setStartTime(updatedConsumption.getStartTime());
            existing.setEndTime(updatedConsumption.getEndTime());
            existing.setEnergyUsed(updatedConsumption.getEnergyUsed());
            existing.setPricePerKWh(updatedConsumption.getPricePerKWh());
            return consumptionRepository.save(existing);
        });
    }

    public Duration getConsumptionDuration(UUID id) {
        return consumptionRepository.findById(id).map(Consumption::getDuration).orElse(Duration.ZERO);
    }

    public double getConsumptionCost(UUID id) {
        return consumptionRepository.findById(id).map(Consumption::getCost).orElse(0.0);
    }

    public void deleteConsumption(UUID id) {
        consumptionRepository.deleteById(id);
    }

}
