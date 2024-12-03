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
            "AND TIMESTAMP(r.reservation_date, r.reservation_time) + INTERVAL r.duration HOUR > TIMESTAMP(:date, :time)", nativeQuery = true)
    List<DiningTable> BookedTablesByDateTimeAndDuration(LocalDate date, LocalTime time, int duration);

    @Query(value = "SELECT dt.number FROM dining_table dt WHERE dt.id = :id", nativeQuery = true)
    int findNumberById(Integer id);

    @Query(value = "SELECT DISTINCT dt.* FROM dining_table dt WHERE dt.seats >= :seats", nativeQuery = true)
    List<DiningTable> allDiningTablesWithMinSeats(Integer seats);

    boolean existsByNumber(int tableNumber);
}
