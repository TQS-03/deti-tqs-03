package tqs.electro.electro.services;

import org.springframework.stereotype.Service;
import tqs.electro.electro.entities.Reservation;
import tqs.electro.electro.entities.Station;
import tqs.electro.electro.repositories.ReservationRepository;
import tqs.electro.electro.repositories.StationRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final Logger logger;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
        this.logger = Logger.getLogger(ReservationService.class.getName());
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public Optional<Reservation> getReservationById(UUID id) {
        return reservationRepository.findById(id);
    }

    public Reservation addReservation(Reservation reservation) {
        try {
            Station station = reservation.getStation();
            station.setCurrentOccupation(station.getCurrentOccupation() + 1);
            if(station.getCurrentOccupation() > station.getMaxOccupation()) {
                return null;
            }
            return reservationRepository.save(reservation);
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }
        return null;
    }

    public Optional<Reservation> updateReservation(UUID id, Reservation updatedReservation) {
        return reservationRepository.findById(id).map(existing -> {
            existing.setStartTime(updatedReservation.getStartTime());
            existing.setEndTime(updatedReservation.getEndTime());
            existing.setPerson(updatedReservation.getPerson());
            existing.setStation(updatedReservation.getStation());
            return reservationRepository.save(existing);
        });
    }

    public Optional<Reservation> updateReservationPaidStatus(UUID id, boolean paid) {
        return reservationRepository.findById(id).map(existing -> {
            existing.setPaid(paid);
            Station savedStation = existing.getStation();
            savedStation.setCurrentOccupation(savedStation.getCurrentOccupation() - 1);
            return reservationRepository.save(existing);
        });
    }

    public List<Reservation> getReservationsByStationIdAndDate(UUID stationId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return reservationRepository.findByStationIdAndStartTimeBetween(stationId, startOfDay, endOfDay);
    }

    public List<Reservation> getReservationsByPersonId(UUID personId) {
        return reservationRepository.findByPersonIdAndEndTimeAfter(personId, LocalDateTime.now());
    }

    public void deleteReservation(UUID id) {
        reservationRepository.findById(id).ifPresent(reservation -> {
            Station station = reservation.getStation();
            station.setCurrentOccupation(station.getCurrentOccupation() - 1);
            reservationRepository.save(reservation);
        });
        reservationRepository.deleteById(id);
    }

}
