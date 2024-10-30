package com.proinwest.booking_table_app.reservation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
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
        return reservationService.getReservation(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());

    }

    @PostMapping()
    public ResponseEntity<ReservationDTO> addReservation(@Valid @RequestBody Reservation reservation) {
        ReservationDTO savedReservation = reservationService.addReservation(reservation);
        return ResponseEntity.created(reservationService.location(reservation))
                .body(savedReservation);
    }

    @PutMapping("{id}")
    public ResponseEntity<ReservationDTO> updateReservation(@PathVariable Long id, @RequestBody Reservation reservation) {
        return reservationService.updateReservation(id, reservation)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("{id}")
    public ResponseEntity<ReservationDTO> partiallyUpdateReservation(@PathVariable Long id, @RequestBody Reservation reservation) {
        return reservationService.partiallyUpdateReservation(id, reservation)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        if (!reservationService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

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
    public ResponseEntity<List<ReservationDTO>> findAllByUserName(@PathVariable String firstName) {
        List<ReservationDTO> allByUserName = reservationService.findAllByUserName(firstName);
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
    public ResponseEntity<List<ReservationDTO>> findAllByTableId(@PathVariable Long id) {
        List<ReservationDTO> allByTableId = reservationService.findAllByTableId(id);
        return ResponseEntity.ok(allByTableId);
    }

    @GetMapping("/search/date/{date}/id/{id}")
    public ResponseEntity<List<ReservationDTO>> findAllByReservationDateAndDiningTableId(@PathVariable LocalDate date, @PathVariable Integer id) {
        List<ReservationDTO> allByDateAndId = reservationService.findAllByDateAndTableId(date, id);
        return ResponseEntity.ok(allByDateAndId);
    }

    @GetMapping("/szukaj/date/{date}/time/{time}")
    public ResponseEntity<List<ReservationDTO>> findAllByDateAndTime(@PathVariable LocalDate date, @PathVariable LocalTime time) {
        List<ReservationDTO> allByDate = reservationService.findAllByDateAndTime(date, time);
        return ResponseEntity.ok(allByDate);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        var errors = new HashMap<String, String>();
        exception.getBindingResult().getAllErrors()
                .forEach(error -> {
                    var fieldName = ((FieldError) error).getField();
                    var errorMessage = error.getDefaultMessage();
                    errors.put(fieldName, errorMessage);
                });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

}
