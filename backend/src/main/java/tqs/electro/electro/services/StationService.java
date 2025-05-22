package tqs.electro.electro.services;

import org.springframework.stereotype.Service;
import tqs.electro.electro.entities.Station;
import tqs.electro.electro.repositories.StationRepository;
import tqs.electro.electro.utils.ChargerType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class StationService {

    private final StationRepository stationRepository;
    private final Logger logger;

    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
        this.logger = Logger.getLogger(StationService.class.getName());
    }

    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    public Optional<Station> getStationById(UUID id) {
        return stationRepository.findById(id);
    }

    public Station addStation(Station station) {
        try {
            return stationRepository.save(station);
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }
        return null;
    }

    public Optional<Station> updateStation(UUID id, Station updatedStation) {
        return stationRepository.findById(id).map(existing -> {
            existing.setName(updatedStation.getName());
            existing.setAddress(updatedStation.getAddress());
            existing.setMaxOccupation(updatedStation.getMaxOccupation());
            existing.setCurrentOccupation(updatedStation.getCurrentOccupation());
            existing.setLatitude(updatedStation.getLatitude());
            existing.setLongitude(updatedStation.getLongitude());
            existing.setChargerTypes(updatedStation.getChargerTypes());
            return stationRepository.save(existing);
        });
    }

    public List<Station> getStationsByChargerType(ChargerType chargerType) {
        return stationRepository.findByChargerTypesContaining(chargerType);
    }

}
