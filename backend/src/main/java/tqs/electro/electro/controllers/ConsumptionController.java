package tqs.electro.electro.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tqs.electro.electro.dtos.ConsumptionResponse;
import tqs.electro.electro.entities.Consumption;
import tqs.electro.electro.repositories.StationRepository;
import tqs.electro.electro.services.ConsumptionService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/backend/consumption")
public class ConsumptionController {

    private final ConsumptionService consumptionService;

    public ConsumptionController(ConsumptionService consumptionService, StationRepository stationRepository) {
        this.consumptionService = consumptionService;
    }

    // GET /consumption/{id} - Get consumption by id
    @GetMapping("/{id}")
    public ResponseEntity<ConsumptionResponse> getConsumption(
            @PathVariable UUID id
    ) {
        Consumption consumption = consumptionService.getConsumptionById(id).orElse(null);
        if (consumption == null) {
            return ResponseEntity.notFound().build();
        }

        ConsumptionResponse response = new ConsumptionResponse();
        response.setConsumption(consumption);
        response.setDuration(consumptionService.getConsumptionDuration(id));
        response.setCost(consumptionService.getConsumptionCost(id));

        return ResponseEntity.ok(response);
    }

    // GET /consumption/station/{id} - Get all consumptions by station id
    @GetMapping("/station/{id}")
    public ResponseEntity<List<Consumption>> getConsumptionsByStationId(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(consumptionService.getAllConsumptionsByStationId(id));
    }

    // POST /consumption - Add a consumption entry
    @PostMapping
    public ResponseEntity<Consumption> addConsumption(@RequestBody Consumption consumption) {
        Consumption newConsumption = consumptionService.save(consumption);
        if (newConsumption == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(newConsumption);
    }

    // PUT /consumption/{id} - Update an existing consumption entry
    @PutMapping("/{id}")
    public ResponseEntity<Consumption> updateConsumption(
            @PathVariable UUID id,
            @RequestBody Consumption updatedConsumption
    ) {
        return consumptionService.updateConsumption(id, updatedConsumption)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /consumption/{id} - Delete a consumption entry
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConsumption(@PathVariable UUID id) {
        consumptionService.deleteConsumption(id);
        return ResponseEntity.noContent().build();
    }

}
