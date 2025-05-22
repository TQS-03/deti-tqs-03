package tqs.electro.electro.unitTests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tqs.electro.electro.entities.Person;
import tqs.electro.electro.entities.Reservation;
import tqs.electro.electro.entities.Station;
import tqs.electro.electro.repositories.ReservationRepository;
import tqs.electro.electro.services.ReservationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private ReservationRepository reservationRepository;

    @Test
    void testGetAllReservations() {
        Reservation r1 = new Reservation();
        Reservation r2 = new Reservation();
        when(reservationRepository.findAll()).thenReturn(List.of(r1, r2));

        List<Reservation> reservations = reservationService.getAllReservations();
        assertEquals(2, reservations.size());
        verify(reservationRepository, times(1)).findAll();
    }

    @Test
    void testGetReservationById_found() {
        UUID id = UUID.randomUUID();
        Reservation reservation = new Reservation();
        reservation.setId(id);
        when(reservationRepository.findById(id)).thenReturn(Optional.of(reservation));

        Optional<Reservation> result = reservationService.getReservationById(id);
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    void testGetReservationById_notFound() {
        UUID id = UUID.randomUUID();
        when(reservationRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Reservation> result = reservationService.getReservationById(id);
        assertFalse(result.isPresent());
    }

    @Test
    void testAddReservation() {
        Reservation reservation = new Reservation();
        when(reservationRepository.save(reservation)).thenReturn(reservation);

        Reservation saved = reservationService.addReservation(reservation);
        assertEquals(reservation, saved);
        verify(reservationRepository).save(reservation);
    }

    @Test
    void testUpdateReservation_found() {
        UUID id = UUID.randomUUID();
        Reservation existing = new Reservation();
        existing.setId(id);

        Reservation update = new Reservation();
        LocalDateTime now = LocalDateTime.now();
        update.setStartTime(now);
        update.setEndTime(now);
        update.setPerson(existing.getPerson());
        update.setStation(existing.getStation());

        when(reservationRepository.findById(id)).thenReturn(Optional.of(existing));
        when(reservationRepository.save(any())).thenReturn(existing);

        Optional<Reservation> updated = reservationService.updateReservation(id, update);

        assertTrue(updated.isPresent());
        assertEquals(now, updated.get().getStartTime());
        assertEquals(now, updated.get().getEndTime());
        assertEquals(existing.getPerson(), updated.get().getPerson());
        assertEquals(existing.getStation(), updated.get().getStation());
        verify(reservationRepository).save(existing);
    }

    @Test
    void testUpdateReservation_notFound() {
        UUID id = UUID.randomUUID();
        Reservation update = new Reservation();
        when(reservationRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Reservation> updated = reservationService.updateReservation(id, update);
        assertFalse(updated.isPresent());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void testUpdateReservationPaidStatus() {
        UUID id = UUID.randomUUID();
        Reservation existing = new Reservation();
        existing.setId(id);
        existing.setPaid(false);

        when(reservationRepository.findById(id)).thenReturn(Optional.of(existing));
        when(reservationRepository.save(any())).thenReturn(existing);

        Optional<Reservation> updated = reservationService.updateReservationPaidStatus(id, true);
        assertTrue(updated.isPresent());
        assertTrue(updated.get().isPaid());
        verify(reservationRepository).save(existing);
    }

    @Test
    void testDeleteReservationById() {
        UUID id = UUID.randomUUID();
        Reservation reservation = new Reservation();
        reservation.setId(id);
        when(reservationRepository.save(any())).thenReturn(reservation);

        reservationService.addReservation(reservation);
        reservationService.deleteReservation(id);
        assertTrue(reservationService.getAllReservations().isEmpty());
        verify(reservationRepository).deleteById(id);
    }

    @Test
    void testGetReservationsByStationIdAndDate() {
        UUID stationId = UUID.randomUUID();
        LocalDate date = LocalDate.now();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        Station station = new Station();
        station.setId(stationId);

        Reservation r1 = new Reservation();
        r1.setStation(station);
        r1.setStartTime(startOfDay);

        Reservation r2 = new Reservation();
        r2.setStation(station);
        r2.setStartTime(date.atTime(10, 0));

        when(reservationRepository.findByStationIdAndStartTimeBetween(stationId, startOfDay, endOfDay)).thenReturn(List.of(r1, r2));

        List<Reservation> reservations = reservationService.getReservationsByStationIdAndDate(stationId, date);
        assertEquals(2, reservations.size());
    }

    @Test
    void testGetReservationsByPersonId() {
        UUID personId = UUID.randomUUID();
        Person person = new Person();
        person.setId(personId);

        Reservation r1 = new Reservation();
        r1.setPerson(person);

        Reservation r2 = new Reservation();
        r2.setPerson(person);

        when(reservationRepository.findByPersonIdAndEndTimeAfter(eq(personId), any(LocalDateTime.class))).thenReturn(List.of(r1, r2));

        List<Reservation> reservations = reservationService.getReservationsByPersonId(personId);
        assertEquals(2, reservations.size());
    }

}