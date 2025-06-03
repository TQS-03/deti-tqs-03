package tqs.electro.electro.services;

import org.springframework.stereotype.Service;
import tqs.electro.electro.dtos.StationRequestDto;
import tqs.electro.electro.entities.Person;
import tqs.electro.electro.entities.Station;
import tqs.electro.electro.repositories.PersonRepository;
import tqs.electro.electro.repositories.StationRepository;
import tqs.electro.electro.utils.ChargerType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class StationService {

    private final StationRepository stationRepository;
    private final PersonRepository personRepository;
    private final Logger logger;

    public StationService(StationRepository stationRepository, PersonRepository personRepository) {
        this.stationRepository = stationRepository;
        this.personRepository = personRepository;
        this.logger = Logger.getLogger(StationService.class.getName());
    }

    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    public Optional<Station> getStationById(UUID id) {
        return stationRepository.findById(id);
    }

    public Station addStation(StationRequestDto stationReq) {
        try {
            if (this.checkUsersAuthorization(stationReq.getPersonId())){
                Station station = new Station();
                station.setName(stationReq.getName());
                station.setAddress(stationReq.getAddress());
                station.setLatitude(stationReq.getLatitude());
                station.setLongitude(stationReq.getLongitude());
                station.setChargerTypes(stationReq.getChargerTypes());
                station.setCurrentOccupation(stationReq.getCurrentOccupation());
                station.setMaxOccupation(stationReq.getMaxOccupation());
                station.setPricePerKWh(stationReq.getPricePerKWh());

                return stationRepository.save(station);
            }
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }
        return null;
    }

    public Optional<Station> updateStation(UUID id, StationRequestDto updatedStation) {
        if (this.checkUsersAuthorization(updatedStation.getPersonId())){
            return stationRepository.findById(id).map(existing -> {
                existing.setName(updatedStation.getName());
                existing.setAddress(updatedStation.getAddress());
                existing.setMaxOccupation(updatedStation.getMaxOccupation());
                existing.setCurrentOccupation(updatedStation.getCurrentOccupation());
                existing.setLatitude(updatedStation.getLatitude());
                existing.setLongitude(updatedStation.getLongitude());
                existing.setChargerTypes(updatedStation.getChargerTypes());
                existing.setPricePerKWh(updatedStation.getPricePerKWh());
                return stationRepository.save(existing);
            });
        }

        return Optional.empty();
    }

    public List<Station> getStationsByChargerType(ChargerType chargerType) {
        return stationRepository.findByChargerTypesContaining(chargerType);
    }

    private boolean checkUsersAuthorization(UUID id){
        Optional<Person> user = personRepository.findById(id);
        if (user.isEmpty()) return false;

        return user.get().isWorker();
    }

}
