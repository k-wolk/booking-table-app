package com.proinwest.booking_table_app.diningTable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface DiningTableRepository extends JpaRepository<DiningTable, Integer> {
    @Query(value = "SELECT DISTINCT dt.* " +
            "FROM dining_table dt " +
            "JOIN reservation r ON dt.id = r.table_id " +
            "WHERE TIMESTAMP(r.reservation_date, r.reservation_time) < TIMESTAMP(:date, :time) + INTERVAL :duration HOUR " +
            "AND TIMESTAMP(r.reservation_date, r.reservation_time) + " +
            "INTERVAL r.duration HOUR > TIMESTAMP(:date, :time)", nativeQuery = true)
    List<DiningTable> bookedTablesByDateTimeAndDuration(LocalDate date, LocalTime time, int duration);

    @Query(value = "SELECT dt.number FROM dining_table dt WHERE dt.id = :id", nativeQuery = true)
    Integer findNumberByTableId(Integer id);

    @Query(value = "SELECT DISTINCT dt.* FROM dining_table dt WHERE dt.seats >= :seats AND dt.active = true",
            nativeQuery = true)
    List<DiningTable> allActiveTablesWithMinSeats(Integer seats);

    @Query(value = "SELECT dt.* FROM dining_table dt ORDER BY dt.active DESC", nativeQuery = true)
    List<DiningTable> allTablesOrderByActive();

    @Query(value = "SELECT dt.* FROM dining_table dt WHERE dt.active = true", nativeQuery = true)
    List<DiningTable> allActiveTables();

    boolean existsByNumber(int tableNumber);

    @Query(value = "SELECT dt.active FROM dining_table dt WHERE dt.id = :tableId", nativeQuery = true)
    boolean isActive(int tableId);
}