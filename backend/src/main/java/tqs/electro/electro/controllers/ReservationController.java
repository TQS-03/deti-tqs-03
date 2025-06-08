package tqs.electro.electro.controllers;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tqs.electro.electro.dtos.ReservationDTO;
import tqs.electro.electro.entities.Reservation;
import tqs.electro.electro.repositories.PersonRepository;
import tqs.electro.electro.repositories.StationRepository;
import tqs.electro.electro.services.ReservationService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/backend/reservation")
public class ReservationController {

    private final ReservationService reservationService;
    private final PersonRepository personRepository;
    private final StationRepository stationRepository;

    public ReservationController(ReservationService reservationService, PersonRepository personRepository, StationRepository stationRepository) {
        this.reservationService = reservationService;
        this.personRepository = personRepository;
        this.stationRepository = stationRepository;
    }

    // GET /reservation/{id} - Get reservation by id
    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservation(
            @PathVariable UUID id
    ) {
        return reservationService.getReservationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /reservation?stationId={stationId}&personId={personId}&date={date} - Get reservations by person id or station id and date
    @GetMapping
    public ResponseEntity<List<Reservation>> getReservations(
            @RequestParam(required = false) UUID stationId,
            @RequestParam(required = false) UUID personId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<Reservation> reservations;

        if (stationId != null && date != null) {
            reservations = reservationService.getReservationsByStationIdAndDate(stationId, date);
        } else if (personId != null) {
            reservations = reservationService.getReservationsByPersonId(personId);
        } else {
            reservations = reservationService.getAllReservations();
        }

        return ResponseEntity.ok(reservations);
    }

    // POST /reservation - Add a reservation
    @PostMapping
    public ResponseEntity<Reservation> addReservation(
            @RequestBody ReservationDTO dto
    ) {
        Reservation reservation = new Reservation();
        reservation.setPerson(personRepository.findById(dto.getPersonId()).orElseThrow());
        reservation.setStation(stationRepository.findById(dto.getStationId()).orElseThrow());
        reservation.setStartTime(dto.getStartTime());
        reservation.setEndTime(dto.getEndTime());

        Reservation saved = reservationService.addReservation(reservation);
        System.out.println(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // PUT /reservation/{id} - Update a reservation
    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(
            @PathVariable UUID id,
            @RequestBody Reservation updatedReservation
    ) {
        return reservationService.updateReservation(id, updatedReservation)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT /reservation/{id}/paid?value={value} - Update the paid status of a reservation
    @PutMapping("/{id}/paid")
    public ResponseEntity<Reservation> updateReservationPaid(
            @PathVariable UUID id,
            @RequestParam boolean value
    ) {
        return reservationService.updateReservationPaidStatus(id, value)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /reservation/{id} - Cancel a reservation
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable UUID id
    ) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

}
