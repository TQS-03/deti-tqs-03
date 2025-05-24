package tqs.electro.electro.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tqs.electro.electro.entities.Reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    List<Reservation> findByStationIdAndStartTimeBetween(UUID stationId, LocalDateTime start, LocalDateTime end);
    List<Reservation> findByPersonIdAndEndTimeAfter(UUID personId, LocalDateTime now);

}
