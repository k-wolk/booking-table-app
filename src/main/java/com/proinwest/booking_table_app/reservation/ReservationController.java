package com.proinwest.booking_table_app.reservation;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.proinwest.booking_table_app.exceptions.types.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("reservations")
public class ReservationController {
    @JsonManagedReference
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReservationDTO>> getAllReservations() {
        List<ReservationDTO> allReservations = reservationService.getAllReservations();
        return ResponseEntity.ok(allReservations);
    }

    @GetMapping("{id}")
    public ResponseEntity<ReservationDTO> getReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservation(id));
    }

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ReservationDTO> createReservation(@RequestBody Reservation reservation) {
        ReservationDTO savedReservation = reservationService.createReservation(reservation);
        return ResponseEntity.created(reservationService.location(reservation))
                .body(savedReservation);
    }

    @PatchMapping("{id}")
    public ResponseEntity<ReservationDTO> updateReservation(@PathVariable Long id,
                                                            @RequestBody Reservation reservation) {
        ReservationDTO updateReservation = reservationService.updateReservation(id, reservation);
        return ResponseEntity.ok(updateReservation);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReservationDTO>> getUserReservations(@PathVariable Long userId) {
        List<ReservationDTO> userReservations = reservationService.getUserReservations(userId);
        return ResponseEntity.ok(userReservations);
    }

    @GetMapping("/date/{date}/table/{tableId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReservationDTO>> findAllByDateAndTableId(@PathVariable LocalDate date,
                                                                        @PathVariable Integer tableId) {
        List<ReservationDTO> allByDateAndTableId = reservationService.getAllByDateAndTableId(date, tableId);
        if (allByDateAndTableId.isEmpty()) throw new NotFoundException("There is no reservation on " + date +
                " for the table with ID " + tableId + ".");
        return ResponseEntity.ok(allByDateAndTableId);
    }

    @GetMapping("/date/{date}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReservationDTO>> findAllByDate(@PathVariable LocalDate date) {
        List<ReservationDTO> allByDate = reservationService.findAllByDate(date);
        return ResponseEntity.ok(allByDate);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReservationDTO>> searchReservations(@RequestParam String query) {
        List<ReservationDTO> reservations = reservationService.searchReservations(query);
        return ResponseEntity.ok(reservations);
    }
}
