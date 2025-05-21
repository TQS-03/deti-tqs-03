package tqs.electro.electro.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tqs.electro.electro.entities.Station;
import tqs.electro.electro.services.StationService;
import tqs.electro.electro.utils.ChargerType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/backend/station")
public class StationController {

  private final StationService stationService;

  public StationController(StationService stationService) {
    this.stationService = stationService;
  }

  // GET /station - Get all stations
  @GetMapping
  public List<Station> getAllStations() {
    return stationService.getAllStations();
  }

  // GET /station/{id} - Get station by ID
  @GetMapping("/{id}")
  public ResponseEntity<Station> getStationById(@PathVariable UUID id) {
    return stationService.getStationById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  // POST /station - Add a new station
  @PostMapping
  public ResponseEntity<Station> addStation(@RequestBody Station station) {
    Station newsStation = stationService.addStation(station);
    if (newsStation == null) {
      return ResponseEntity.badRequest().build();
    }
    return ResponseEntity.ok(newsStation);
  }

  // PUT /station/{id} - Update an existing station
  @PutMapping("/{id}")
  public ResponseEntity<Station> updateStation(@PathVariable UUID id, @RequestBody Station updatedStation) {
    return stationService.updateStation(id, updatedStation)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/filter/{type}")
  public ResponseEntity<List<Station>> filterStationByType(@PathVariable ChargerType type) {
    Optional<List<Station>> stations = stationService.getStationsByChargerType(type);
    if (stations.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    List<Station> filteredStations = stations.get();
    if (filteredStations.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(filteredStations);
  }

}
