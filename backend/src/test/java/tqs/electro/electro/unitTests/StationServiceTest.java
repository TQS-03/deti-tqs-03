package tqs.electro.electro.unitTests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tqs.electro.electro.dtos.StationRequestDto;
import tqs.electro.electro.entities.Person;
import tqs.electro.electro.entities.Station;
import tqs.electro.electro.repositories.PersonRepository;
import tqs.electro.electro.repositories.StationRepository;
import tqs.electro.electro.services.StationService;
import tqs.electro.electro.utils.ChargerType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StationServiceTest {

    @InjectMocks
    private StationService stationService;

    @Mock
    private StationRepository stationRepository;

    @Mock
    private PersonRepository personRepository;

    @Test
    void testGetAllStations() {
        Station s1 = new Station();
        Station s2 = new Station();
        when(stationRepository.findAll()).thenReturn(Arrays.asList(s1, s2));

        List<Station> stations = stationService.getAllStations();
        assertEquals(2, stations.size());
        verify(stationRepository, times(1)).findAll();
    }

    @Test
    void testGetStationById_found() {
        UUID id = UUID.randomUUID();
        Station station = new Station();
        station.setId(id);
        when(stationRepository.findById(id)).thenReturn(Optional.of(station));

        Optional<Station> result = stationService.getStationById(id);
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    void testGetStationById_notFound() {
        UUID id = UUID.randomUUID();
        when(stationRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Station> result = stationService.getStationById(id);
        assertFalse(result.isPresent());
    }

    @Test
    void testAddStation() {
        StationRequestDto stationReq = new StationRequestDto();
        Station station = new Station();

        Person person = new Person();
        person.setIsWorker(true);

        when(stationRepository.save(any())).thenReturn(station);
        when(personRepository.findById(any())).thenReturn(Optional.of(person));

        Station saved = stationService.addStation(stationReq);
        assertEquals(station, saved);
        verify(stationRepository).save(any(Station.class));
    }

    @Test
    void testUpdateStation_found() {
        UUID id = UUID.randomUUID();
        Station existing = new Station();
        existing.setId(id);

        StationRequestDto update = new StationRequestDto();
        update.setName("New Name");
        update.setAddress("New Address");
        update.setMaxOccupation(10);
        update.setCurrentOccupation(5);
        update.setLatitude("40.1");
        update.setLongitude("-8.6");
        update.setChargerTypes(List.of(ChargerType.TYPE2, ChargerType.CCS));

        Person person = new Person();
        person.setIsWorker(true);

        when(stationRepository.findById(id)).thenReturn(Optional.of(existing));
        when(stationRepository.save(any())).thenReturn(existing);
        when(personRepository.findById(any())).thenReturn(Optional.of(person));


        Optional<Station> updated = stationService.updateStation(id, update);

        assertTrue(updated.isPresent());
        assertEquals("New Name", updated.get().getName());
        assertEquals("New Address", updated.get().getAddress());
        assertEquals(10, updated.get().getMaxOccupation());
        verify(stationRepository).save(existing);
    }

    @Test
    void testUpdateStation_notFound() {
        UUID id = UUID.randomUUID();
        StationRequestDto update = new StationRequestDto();
        Person person = new Person();
        person.setIsWorker(true);

        when(stationRepository.findById(id)).thenReturn(Optional.empty());
        when(personRepository.findById(any())).thenReturn(Optional.of(person));

        Optional<Station> updated = stationService.updateStation(id, update);
        assertFalse(updated.isPresent());
        verify(stationRepository, never()).save(any());
    }

    @Test
    void testGetStationsByChargerType() {
        ChargerType desiredChargerType = ChargerType.CCS;
        ChargerType otherChargerType = ChargerType.TESLA;

        Station s1 = new Station();
        s1.setChargerTypes(Arrays.asList(desiredChargerType, otherChargerType));
        Station s2 = new Station();
        s2.setChargerTypes(List.of(otherChargerType));

        List<Station> expectedStations = List.of(s1);

        when(stationRepository.findByChargerTypesContaining(desiredChargerType)).thenReturn(expectedStations);

        List<Station> stations = stationService.getStationsByChargerType(desiredChargerType);
        assertEquals(1, stations.size());
        assertTrue(stations.contains(s1));
        assertFalse(stations.contains(s2));
        verify(stationRepository, times(1)).findByChargerTypesContaining(desiredChargerType);
    }

}


