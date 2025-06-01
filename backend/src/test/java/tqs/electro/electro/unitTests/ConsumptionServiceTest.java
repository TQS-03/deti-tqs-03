package tqs.electro.electro.unitTests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tqs.electro.electro.entities.Consumption;
import tqs.electro.electro.entities.Station;
import tqs.electro.electro.repositories.ConsumptionRepository;
import tqs.electro.electro.services.ConsumptionService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsumptionServiceTest {

    @InjectMocks
    private ConsumptionService consumptionService;

    @Mock
    private ConsumptionRepository consumptionRepository;

    @Test
    void testAddConsumption() {
        Consumption consumption = new Consumption();
        when(consumptionRepository.save(consumption)).thenReturn(consumption);

        Consumption saved = consumptionService.save(consumption);
        assertEquals(consumption, saved);
        verify(consumptionRepository).save(consumption);
    }

    @Test
    void testGetAllConsumptionsByStationId() {
        UUID stationId = UUID.randomUUID();
        Consumption c1 = new Consumption();
        Consumption c2 = new Consumption();
        when(consumptionRepository.findByStationId(stationId)).thenReturn(List.of(c1, c2));

        List<Consumption> result = consumptionService.getAllConsumptionsByStationId(stationId);
        assertEquals(2, result.size());
        verify(consumptionRepository, times(1)).findByStationId(stationId);
    }

    @Test
    void testGetConsumptionById_found() {
        UUID id = UUID.randomUUID();
        Consumption consumption = new Consumption();
        consumption.setId(id);
        when(consumptionRepository.findById(id)).thenReturn(Optional.of(consumption));

        Optional<Consumption> result = consumptionService.getConsumptionById(id);
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    void testGetConsumptionById_notFound() {
        UUID id = UUID.randomUUID();
        when(consumptionRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Consumption> result = consumptionService.getConsumptionById(id);
        assertFalse(result.isPresent());
    }

    @Test
    void testUpdateConsumption_found() {
        UUID id = UUID.randomUUID();
        Station station = new Station();
        LocalDateTime start = LocalDateTime.now().minusHours(2);
        LocalDateTime end = LocalDateTime.now();

        Consumption existing = new Consumption();
        existing.setId(id);

        Consumption updated = new Consumption();
        updated.setStation(station);
        updated.setStartTime(start);
        updated.setEndTime(end);
        updated.setEnergyUsed(1000.0);
        updated.setPricePerKWh(0.20);

        when(consumptionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(consumptionRepository.save(existing)).thenReturn(existing);

        Optional<Consumption> result = consumptionService.updateConsumption(id, updated);

        assertTrue(result.isPresent());
        assertEquals(start, result.get().getStartTime());
        assertEquals(end, result.get().getEndTime());
        assertEquals(1000.0, result.get().getEnergyUsed());
        assertEquals(0.20, result.get().getPricePerKWh());
        assertEquals(station, result.get().getStation());
        verify(consumptionRepository).save(existing);
    }

    @Test
    void testUpdateConsumption_notFound() {
        UUID id = UUID.randomUUID();
        Consumption updated = new Consumption();
        when(consumptionRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Consumption> result = consumptionService.updateConsumption(id, updated);
        assertFalse(result.isPresent());
    }

    @Test
    void testGetConsumptionDuration_found() {
        UUID id = UUID.randomUUID();
        Consumption c = new Consumption();
        c.setStartTime(LocalDateTime.now().minusHours(1));
        c.setEndTime(LocalDateTime.now());

        when(consumptionRepository.findById(id)).thenReturn(Optional.of(c));

        Duration duration = consumptionService.getConsumptionDuration(id);
        assertEquals(1, duration.toHours());
    }

    @Test
    void testGetConsumptionCost_found() {
        UUID id = UUID.randomUUID();
        Consumption c = new Consumption();
        c.setStartTime(LocalDateTime.now().minusHours(1));
        c.setEndTime(LocalDateTime.now());
        c.setEnergyUsed(20);
        c.setPricePerKWh(0.25);

        when(consumptionRepository.findById(id)).thenReturn(Optional.of(c));

        double cost = consumptionService.getConsumptionCost(id);
        double expected = 20 * 0.25;

        assertEquals(expected, cost, 0.001);
    }

    @Test
    void testDeleteConsumption() {
        UUID id = UUID.randomUUID();

        consumptionService.deleteConsumption(id);
        verify(consumptionRepository).deleteById(id);
    }

}
