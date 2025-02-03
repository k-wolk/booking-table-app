package com.proinwest.booking_table_app.reservation;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.proinwest.booking_table_app.exceptions.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
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
    public ResponseEntity<List<ReservationDTO>> getAllReservations() {
        List<ReservationDTO> allReservations = reservationService.getAllReservations();
        return ResponseEntity.ok(allReservations);
    }

    @GetMapping("{id}")
    public ResponseEntity<ReservationDTO> getReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservation(id));
    }

    @PostMapping()
    public ResponseEntity<ReservationDTO> addReservation(@RequestBody Reservation reservation) {
        ReservationDTO savedReservation = reservationService.addReservation(reservation);
        return ResponseEntity.created(reservationService.location(reservation))
                .body(savedReservation);
    }

    @PutMapping("{id}")
    public ResponseEntity<ReservationDTO> updateReservation(@PathVariable Long id, @RequestBody Reservation reservation) {
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
    public ResponseEntity<List<ReservationDTO>> findAllByDate(@PathVariable LocalDate date) {
        List<ReservationDTO> allByDate = reservationService.findAllByDate(date);
        return ResponseEntity.ok(allByDate);
    }

    @GetMapping("/search/user/{userId}")
    public ResponseEntity<List<ReservationDTO>> findAllByUserId(@PathVariable Long userId) {
        List<ReservationDTO> allByUserId = reservationService.findAllByUserId(userId);
        if (allByUserId.isEmpty()) throw new NotFoundException("There is no reservation booked by user with id: " + userId + ".");
        return ResponseEntity.ok(allByUserId);
    }

    @GetMapping("/search/login/{login}")
    public ResponseEntity<List<ReservationDTO>> findAllByUserLogin(@PathVariable String login) {
        List<ReservationDTO> allByUserLogin = reservationService.findAllByUserLogin(login);
        return ResponseEntity.ok(allByUserLogin);
    }

    @GetMapping("/search/firstname/{firstName}")
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

    @GetMapping("/search/table/{tableId}")
    public ResponseEntity<List<ReservationDTO>> findAllByTableId(@PathVariable Integer tableId) {
        List<ReservationDTO> allByTableId = reservationService.findAllByTableId(tableId);
        if (allByTableId.isEmpty()) throw new NotFoundException("There is no reservation with table id: " + tableId);
        return ResponseEntity.ok(allByTableId);
    }

    @GetMapping("/search/date/{date}/table/{tableId}")
    public ResponseEntity<List<ReservationDTO>> findAllByDateAndTableId(@PathVariable LocalDate date, @PathVariable Integer tableId) {
        List<ReservationDTO> allByDateAndId = reservationService.findAllByDateAndTableId(date, tableId);
        return ResponseEntity.ok(allByDateAndId);
    }

    @GetMapping("/search/date/{date}/time/{time}")
    public ResponseEntity<List<ReservationDTO>> findAllByDateAndTime(@PathVariable LocalDate date, @PathVariable LocalTime time) {
        List<ReservationDTO> allByDateAndTime = reservationService.findAllByDateAndTime(date, time);
        return ResponseEntity.ok(allByDateAndTime);
    }
}
