package com.proinwest.booking_table_app.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {
    List<Reservation> findAllByReservationDate(LocalDate date);
    List<Reservation> findAllByDiningTableId(Integer id);
    List<Reservation> findAllByUserId(Long id);
    List<Reservation> findAllByUserLoginContainingIgnoreCase(String login);
    List<Reservation> findAllByUserFirstNameContainingIgnoreCase(String firstName);
    List<Reservation> findAllByUserLastNameContainingIgnoreCase(String lastName);
    List<Reservation> findAllByUserEmailContainingIgnoreCase(String email);
    List<Reservation> findAllByUserPhoneNumberContaining(String phoneNumber);
    List<Reservation> findAllByUserLoginContainingIgnoreCaseOrUserFirstNameContainingIgnoreCaseOrUserLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneNumberContaining(String login, String firstName, String lastName, String email, String phoneNumber);
    List<Reservation> findAllByReservationDateAndDiningTableId(LocalDate date, Integer id);
    List<Reservation> findAllByReservationDateAndReservationTime(LocalDate date, LocalTime time);

    default List<Reservation> findAllByDate(LocalDate date) {
        return findAllByReservationDate(date);
    }
    default List<Reservation> findAllByTableId(Integer tableId) {
        return findAllByDiningTableId(tableId);
    }
    default List<Reservation> findAllByUserLogin(String login) {
        return findAllByUserLoginContainingIgnoreCase(login);
    }
    default List<Reservation> findAllByUserFirstName(String firstName) {
        return findAllByUserFirstNameContainingIgnoreCase(firstName);
    }
    default List<Reservation> findAllByUserLastName(String lastName) {
        return findAllByUserLastNameContainingIgnoreCase(lastName);
    }
    default List<Reservation> findAllByUserEmail(String email) {
        return findAllByUserEmailContainingIgnoreCase(email);
    }
    default List<Reservation> findAllByUserPhoneNumber(String phoneNumber) {
        return findAllByUserPhoneNumberContaining(phoneNumber);
    }
    default List<Reservation> findAllByAnyUserStringField(String login, String firstName, String lastName, String email, String phoneNumber) {
        return findAllByUserLoginContainingIgnoreCaseOrUserFirstNameContainingIgnoreCaseOrUserLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneNumberContaining(login, firstName, lastName, email, phoneNumber);
    }
    default List<Reservation> findAllByDateAndTableId(LocalDate date, Integer tableId) {
        return findAllByReservationDateAndDiningTableId(date, tableId);
    }
    default List<Reservation> findAllByDateAndTime(LocalDate date, LocalTime time) {
        return findAllByReservationDateAndReservationTime(date, time);
    }
}
