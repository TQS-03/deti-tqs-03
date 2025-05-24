package tqs.electro.electro.integrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tqs.electro.electro.entities.Person;
import tqs.electro.electro.entities.Reservation;
import tqs.electro.electro.entities.Station;
import tqs.electro.electro.repositories.PersonRepository;
import tqs.electro.electro.repositories.ReservationRepository;
import tqs.electro.electro.repositories.StationRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReservationControllerIT {

    private static final String BASE_URL = "/backend/reservation";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private PersonRepository personRepository;

    private Station station;
    private Person person;

    @BeforeEach
    void setup() {
        reservationRepository.deleteAll();
        stationRepository.deleteAll();
        personRepository.deleteAll();

        station = new Station();
        station.setName("Test Station");
        station.setAddress("123 Street");
        station.setMaxOccupation(10);
        station.setCurrentOccupation(3);
        station.setLatitude("40.0");
        station.setLongitude("-8.0");
        station = stationRepository.save(station);

        person = new Person();
        person.setFirstName("Test");
        person.setLastName("Person");
        person.setEmail("testperson@mail.com");
        person = personRepository.save(person);
    }

    private Reservation createReservation(LocalDate date) {
        LocalDateTime startTime = date.atTime(10, 0);
        LocalDateTime endTime = date.atTime(12, 0);

        Reservation r = new Reservation();
        r.setPerson(person);
        r.setStation(station);
        r.setStartTime(startTime);
        r.setEndTime(endTime);
        return r;
    }

    @Test
    void whenAddReservation_thenCreatedAndPersisted() throws Exception {
        String payload = String.format("""
            {
                "personId": "%s",
                "stationId": "%s",
                "startTime": "2025-05-24T10:00:00",
                "endTime": "2025-05-24T12:00:00"
            }
            """, person.getId(), station.getId());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.person.id").value(person.getId().toString()))
                .andExpect(jsonPath("$.station.id").value(station.getId().toString()))
                .andExpect(jsonPath("$.paid").value(false))
                .andReturn();

        assertThat(reservationRepository.count()).isEqualTo(1);
    }

    @Test
    void whenGetReservationById_thenReturnReservation() throws Exception {
        Reservation saved = reservationRepository.save(createReservation(LocalDate.now()));

        mockMvc.perform(get(BASE_URL + "/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId().toString()))
                .andExpect(jsonPath("$.station.id").value(station.getId().toString()))
                .andExpect(jsonPath("$.person.id").value(person.getId().toString()));
    }

    @Test
    void whenGetReservationById_notFound_then404() throws Exception {
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(get(BASE_URL + "/{id}", randomId))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenUpdateReservation_thenUpdated() throws Exception {
        Reservation saved = reservationRepository.save(createReservation(LocalDate.now()));

        String updateJson = String.format("""
            {
                "personId": "%s",
                "stationId": "%s",
                "startTime": "2025-06-01T16:00:00",
                "endTime": "2025-06-01T18:00:00"
            }
            """, person.getId(), station.getId());

        mockMvc.perform(put(BASE_URL + "/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startTime").value("2025-06-01T16:00:00"))
                .andExpect(jsonPath("$.paid").value(false));

        Reservation updated = reservationRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getStartTime()).isEqualTo(LocalDateTime.of(2025, 6, 1, 16, 0));
        assertThat(updated.isPaid()).isFalse();
    }

    @Test
    void whenUpdateReservation_notFound_then404() throws Exception {
        UUID randomId = UUID.randomUUID();

        String updateJson = String.format("""
            {
                "personId": "%s",
                "stationId": "%s",
                "startTime": "2025-06-01T16:00:00",
                "endTime": "2025-06-01T18:00:00"
            }
            """, person.getId(), station.getId());

        mockMvc.perform(put(BASE_URL + "/{id}", randomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenUpdateReservationPaidStatus_thenUpdated() throws Exception {
        Reservation saved = reservationRepository.save(createReservation(LocalDate.now()));

        mockMvc.perform(put(BASE_URL + "/{id}/paid?value=true", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paid").value(true));

        Reservation updated = reservationRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.isPaid()).isTrue();
    }

    @Test
    void whenUpdateReservationPaidStatus_notFound_then404() throws Exception {
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(put(BASE_URL + "/{id}/paid?value=true", randomId))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenDeleteReservation_thenNoContentAndRemoved() throws Exception {
        Reservation saved = reservationRepository.save(createReservation(LocalDate.now()));

        mockMvc.perform(delete(BASE_URL + "/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        assertThat(reservationRepository.existsById(saved.getId())).isFalse();
    }

    @Test
    void whenGetReservationsByStationIdAndDate_thenReturnList() throws Exception {
        LocalDate date = LocalDate.of(2025, 5, 24);
        Reservation r1 = createReservation(date);
        Reservation r2 = createReservation(date);
        reservationRepository.save(r1);
        reservationRepository.save(r2);

        mockMvc.perform(get(BASE_URL)
                        .param("stationId", station.getId().toString())
                        .param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void whenGetReservationsByPersonId_thenReturnList() throws Exception {
        Reservation r1 = createReservation(LocalDate.of(2025, 6, 25));
        Reservation r2 = createReservation(LocalDate.of(2025, 6, 25));
        reservationRepository.save(r1);
        reservationRepository.save(r2);

        mockMvc.perform(get(BASE_URL)
                        .param("personId", person.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void whenGetAllReservations_thenReturnAll() throws Exception {
        reservationRepository.save(createReservation(LocalDate.now()));
        reservationRepository.save(createReservation(LocalDate.now()));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

}
