package com.proinwest.booking_table_app.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    List<Reservation> findAllByReservationDate(LocalDate date);
    List<Reservation> findAllByDiningTableId(Long id);
    List<Reservation> findAllByUserId(Long id);
    List<Reservation> findAllByUserLoginContainingIgnoreCase(String login);
    List<Reservation> findAllByUserFirstNameContainingIgnoreCase(String firstName);
    List<Reservation> findAllByUserLastNameContainingIgnoreCase(String lastName);
    List<Reservation> findAllByUserEmailContainingIgnoreCase(String email);
    List<Reservation> findAllByUserPhoneNumberContaining(String phoneNumber);
    List<Reservation> findAllByReservationDateAndDiningTableId(LocalDate date, Integer id);
    List<Reservation> findAllByReservationDateAndReservationTime(LocalDate date, LocalTime time);
}
