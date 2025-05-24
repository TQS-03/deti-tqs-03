package tqs.electro.electro.unitTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tqs.electro.electro.controllers.ReservationController;
import tqs.electro.electro.dtos.ReservationDTO;
import tqs.electro.electro.entities.Person;
import tqs.electro.electro.entities.Reservation;
import tqs.electro.electro.entities.Station;
import tqs.electro.electro.repositories.PersonRepository;
import tqs.electro.electro.repositories.StationRepository;
import tqs.electro.electro.services.ReservationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private PersonRepository personRepository;

    @MockitoBean
    private StationRepository stationRepository;

    private static final String BASE_URL = "/backend/reservation";

    @Autowired
    private ObjectMapper objectMapper;

    private Reservation createSampleReservation(UUID id, Person person, Station station) {
        Reservation r = new Reservation();
        r.setId(id);
        r.setPerson(person);
        r.setStation(station);
        r.setStartTime(LocalDateTime.of(2025, 5, 24, 15, 0));
        r.setEndTime(LocalDateTime.of(2025, 5, 24, 16, 0));
        r.setPaid(false);
        r.setCreatedAt(LocalDateTime.now());
        return r;
    }

    @Test
    void testGetAllReservations() throws Exception {
        Reservation r1 = createSampleReservation(UUID.randomUUID(), new Person(), new Station());
        Reservation r2 = createSampleReservation(UUID.randomUUID(), new Person(), new Station());
        when(reservationService.getAllReservations()).thenReturn(List.of(r1, r2));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetReservationById_found() throws Exception {
        UUID id = UUID.randomUUID();
        Reservation r = createSampleReservation(id, new Person(), new Station());
        when(reservationService.getReservationById(id)).thenReturn(Optional.of(r));

        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.paid").value(false));
    }

    @Test
    void testGetReservationById_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(reservationService.getReservationById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetReservationsByStationIdAndDate() throws Exception {
        UUID stationId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2025, 5, 24);

        Reservation r1 = createSampleReservation(UUID.randomUUID(), new Person(), new Station());
        Reservation r2 = createSampleReservation(UUID.randomUUID(), new Person(), new Station());

        when(reservationService.getReservationsByStationIdAndDate(stationId, date))
                .thenReturn(List.of(r1, r2));

        mockMvc.perform(get(BASE_URL)
                        .param("stationId", stationId.toString())
                        .param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetReservationsByPersonId() throws Exception {
        UUID personId = UUID.randomUUID();

        Reservation r1 = createSampleReservation(UUID.randomUUID(), new Person(), new Station());
        Reservation r2 = createSampleReservation(UUID.randomUUID(), new Person(), new Station());

        when(reservationService.getReservationsByPersonId(personId))
                .thenReturn(List.of(r1, r2));

        mockMvc.perform(get(BASE_URL)
                        .param("personId", personId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testAddReservation() throws Exception {
        UUID personId = UUID.randomUUID();
        UUID stationId = UUID.randomUUID();

        Person person = new Person();
        person.setId(personId);

        Station station = new Station();
        station.setId(stationId);

        ReservationDTO dto = new ReservationDTO();
        dto.setPersonId(personId);
        dto.setStationId(stationId);
        dto.setStartTime(LocalDateTime.of(2025, 5, 24, 10, 0));
        dto.setEndTime(LocalDateTime.of(2025, 5, 24, 12, 0));

        Reservation saved = createSampleReservation(UUID.randomUUID(), person, station);

        when(personRepository.findById(personId)).thenReturn(Optional.of(person));
        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(reservationService.addReservation(any(Reservation.class))).thenReturn(saved);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.person.id").value(personId.toString()))
                .andExpect(jsonPath("$.station.id").value(stationId.toString()))
                .andExpect(jsonPath("$.paid").value(false));
    }

    @Test
    void testUpdateReservation_found() throws Exception {
        UUID id = UUID.randomUUID();
        Reservation updated = createSampleReservation(id, new Person(), new Station());

        when(reservationService.updateReservation(eq(id), any(Reservation.class)))
                .thenReturn(Optional.of(updated));

        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void testUpdateReservation_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        Reservation updated = createSampleReservation(id, new Person(), new Station());

        when(reservationService.updateReservation(eq(id), any(Reservation.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateReservationPaid() throws Exception {
        UUID id = UUID.randomUUID();
        Reservation updated = createSampleReservation(id, new Person(), new Station());
        updated.setPaid(true);

        when(reservationService.updateReservationPaidStatus(eq(id), eq(true)))
                .thenReturn(Optional.of(updated));

        mockMvc.perform(put(BASE_URL + "/{id}/paid", id)
                                .param("value", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paid").value(true));
    }

}
