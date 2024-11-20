package com.proinwest.booking_table_app.reservation;

import com.proinwest.booking_table_app.exceptions.NotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("reservations")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping()
    public ResponseEntity<List<ReservationDTO>> getAllReservation() {
        List<ReservationDTO> allReservations = reservationService.getAllReservation();
        return ResponseEntity.ok(allReservations);
    }

    @GetMapping("{id}")
    public ResponseEntity<ReservationDTO> getReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservation(id));
    }

    @PostMapping()
    public ResponseEntity<ReservationDTO> addReservation(@Valid @RequestBody Reservation reservation) {
        ReservationDTO savedReservation = reservationService.addReservation(reservation);
        return ResponseEntity.created(reservationService.location(reservation))
                .body(savedReservation);
    }

    @PutMapping("{id}")
    public ResponseEntity<ReservationDTO> updateReservation(@PathVariable Long id, @Valid @RequestBody Reservation reservation) {
        ReservationDTO updatedReservation = reservationService.updateReservation(id, reservation);
        return ResponseEntity.ok(updatedReservation);
    }

    @PatchMapping("{id}")
    public ResponseEntity<ReservationDTO> partiallyUpdateReservation(@PathVariable Long id, @RequestBody Reservation reservation) {
        ReservationDTO partiallyUpdateReservation = reservationService.partiallyUpdateReservation(id, reservation);
        return ResponseEntity.ok(partiallyUpdateReservation);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search/date/{date}")
    public ResponseEntity<List<ReservationDTO>> findAllByReservationDate(@PathVariable LocalDate date) {
        List<ReservationDTO> allByDate = reservationService.findAllByReservationDate(date);
        return ResponseEntity.ok(allByDate);
    }

    @GetMapping("/search/user/{id}")
    public ResponseEntity<List<ReservationDTO>> findAllByUserId(@PathVariable Long id) {
        List<ReservationDTO> allByUserId = reservationService.findAllByUserId(id);
        return ResponseEntity.ok(allByUserId);
    }

    @GetMapping("/search/login/{login}")
    public ResponseEntity<List<ReservationDTO>> findAllByUserLogin(@PathVariable String login) {
        List<ReservationDTO> allByUserLogin = reservationService.findAllByUserLogin(login);
        return ResponseEntity.ok(allByUserLogin);
    }

    @GetMapping("/search/firstName/{firstName}")
    public ResponseEntity<List<ReservationDTO>> findAllByUserFirstName(@PathVariable String firstName) {
        List<ReservationDTO> allByUserName = reservationService.findAllByUserFirstName(firstName);
        return ResponseEntity.ok(allByUserName);
    }

    @GetMapping("/search/lastname/{lastName}")
    public ResponseEntity<List<ReservationDTO>> findAllByUserLastName(@PathVariable String lastName) {
        List<ReservationDTO> allByUserLastName = reservationService.findAllByUserLastName(lastName);
        return ResponseEntity.ok(allByUserLastName);
    }

    @GetMapping("/search/email/{email}")
    public ResponseEntity<List<ReservationDTO>> findAllByUserEmail(@PathVariable String email) {
        List<ReservationDTO> allByUserEmail = reservationService.findAllByUserEmail(email);
        return ResponseEntity.ok(allByUserEmail);
    }

    @GetMapping("/search/phonenumber/{phoneNumber}")
    public ResponseEntity<List<ReservationDTO>> findAllByUserPhoneNumber(@PathVariable String phoneNumber) {
        List<ReservationDTO> allByUserPhoneNumber = reservationService.findAllByUserPhoneNumber(phoneNumber);
        return ResponseEntity.ok(allByUserPhoneNumber);
    }

    @GetMapping("/search/table/{id}")
    public ResponseEntity<List<ReservationDTO>> findAllByTableId(@PathVariable Integer id) {
        List<ReservationDTO> allByTableId = reservationService.findAllByTableId(id);
        return ResponseEntity.ok(allByTableId);
    }

    @GetMapping("/search/date/{date}/id/{id}")
    public ResponseEntity<List<ReservationDTO>> findAllByReservationDateAndDiningTableId(@PathVariable LocalDate date, @PathVariable Integer id) {
        List<ReservationDTO> allByDateAndId = reservationService.findAllByDateAndTableId(date, id);
        return ResponseEntity.ok(allByDateAndId);
    }

    @GetMapping("/search/date/{date}/time/{time}")
    public ResponseEntity<List<ReservationDTO>> findAllByDateAndTime(@PathVariable LocalDate date, @PathVariable LocalTime time) {
        List<ReservationDTO> allByDateAndTime = reservationService.findAllByDateAndTime(date, time);
        return ResponseEntity.ok(allByDateAndTime);
    }
}
