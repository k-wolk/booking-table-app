package com.proinwest.booking_table_app.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {
    List<Reservation> findAllByReservationDate(LocalDate date);
    List<Reservation> findAllByDiningTableId(Integer id);
    List<Reservation> findAllByUserId(Long id);

    default List<Reservation> findAllByDate(LocalDate date) {
        return findAllByReservationDate(date);
    }
    default List<Reservation> findAllByTableId(Integer tableId) {
        return findAllByDiningTableId(tableId);
    }

    @Query(value = "SELECT r.* FROM reservation r " +
            "JOIN user u ON r.user_id = u.id " +
            "JOIN dining_table t ON r.table_id = t.id " +
            "WHERE CAST(r.id AS CHAR) LIKE CONCAT('%', :searchTerm, '%') OR " +
            "      CAST(u.id AS CHAR) LIKE CONCAT('%', :searchTerm, '%') OR " +
            "      CAST(t.id AS CHAR) LIKE CONCAT('%', :searchTerm, '%') OR " +
            "      CAST(r.reservation_date AS CHAR) LIKE CONCAT('%', :searchTerm, '%') OR " +
            "      CAST(r.reservation_time AS CHAR) LIKE CONCAT('%', :searchTerm, '%') OR " +
            "LOWER(u.login) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.first_name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.last_name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "u.phone_number LIKE CONCAT('%', :searchTerm, '%') OR " +
            "CAST(t.number AS CHAR) LIKE CONCAT('%', :searchTerm, '%')", nativeQuery = true)
    List<Reservation> searchReservations(String searchTerm);

    @Query(value = "SELECT * FROM reservation r " +
            "WHERE r.reservation_date = :date " +
            "AND r.table_id = :tableId " +
            "ORDER BY r.reservation_time", nativeQuery = true)
    List<Reservation> findAllByDateAndTableId(LocalDate date, Integer tableId);
}
