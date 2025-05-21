package tqs.electro.electro.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tqs.electro.electro.entities.Station;
import tqs.electro.electro.repositories.StationRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class StationService {

    @Autowired
    private StationRepository stationRepository;

    private final Logger logger = Logger.getLogger(StationService.class.getName());

    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    public Optional<Station> getStationById(UUID id) {
        return Optional.ofNullable(stationRepository.findById(id));
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
        return Optional.ofNullable(stationRepository.findById(id)).map(existing -> {
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
