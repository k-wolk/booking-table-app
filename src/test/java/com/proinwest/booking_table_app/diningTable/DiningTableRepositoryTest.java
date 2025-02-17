package com.proinwest.booking_table_app.diningTable;

import com.proinwest.booking_table_app.reservation.Reservation;
import com.proinwest.booking_table_app.reservation.ReservationRepository;
import com.proinwest.booking_table_app.user.User;
import com.proinwest.booking_table_app.user.UserRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class DiningTableRepositoryTest {
    @Container
    @ServiceConnection
    private static final MySQLContainer mySQLContainer = new MySQLContainer("mysql:8.4.0");
    @Autowired
    private DiningTableRepository tableRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    @AfterEach
    void tearDown() {
        tableRepository.deleteAll();
        userRepository.deleteAll();
        reservationRepository.deleteAll();
    }

    @AfterAll
    static void stopContainer() {
        mySQLContainer.stop();
    }

    @Test
    void connectionEstablished() {
        assertTrue(mySQLContainer.isCreated());
        assertTrue(mySQLContainer.isRunning());
    }

    @Test
    void shouldReturnListOfBookedTablesByDateTimeAndDuration_whenExists() {
        // given
        final User user = Instancio.create(User.class);
        user.setId(1L);
        user.setReservations(null);

        final DiningTable table1 = new DiningTable();
        table1.setNumber(1);
        table1.setSeats(4);

        final DiningTable table2 = new DiningTable();
        table2.setNumber(2);
        table2.setSeats(6);

        final LocalDate tomorrow = LocalDate.now().plusDays(1);

        final Reservation reservation1 = new Reservation();
        reservation1.setDiningTable(table1);
        reservation1.setReservationDate(tomorrow);
        reservation1.setReservationTime(LocalTime.of(17, 0));
        reservation1.setDuration(2);
        reservation1.setUser(user);

        final Reservation reservation2 = new Reservation();
        reservation2.setDiningTable(table2);
        reservation2.setReservationDate(tomorrow);
        reservation2.setReservationTime(LocalTime.of(18, 0));
        reservation2.setDuration(3);
        reservation2.setUser(user);

        tableRepository.save(table1);
        tableRepository.save(table2);
        userRepository.save(user);
        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);

        // when
        final List<DiningTable> bookedTables = tableRepository.bookedTablesByDateTimeAndDuration(
                tomorrow,
                LocalTime.of(17,0),
                1
        );

        // then
        assertNotNull(bookedTables);
        assertEquals(List.of(table1), bookedTables);
    }

    @Test
    void shouldFindNumberById_whenExists() {
        // given
        final DiningTable table = new DiningTable();
        final int number = 2;
        table.setNumber(number);
        table.setSeats(4);

        tableRepository.save(table);

        // when
        final Integer result = tableRepository.findNumberByTableId(table.getId());

        // then
        assertNotNull(result);
        assertEquals(number, result);
    }

    @Test
    void shouldNotFindNumberById_whenNotExists() {
        // given
        final int id = 1;

        // when
        final Integer result = tableRepository.findNumberByTableId(id);

        // then
        assertNull(result);
    }

    @Test
    void shouldReturnListOfTableWithMinSeats_whenExists() {
        // given
        final DiningTable table1 = new DiningTable();
        table1.setNumber(1);
        table1.setSeats(4);

        final DiningTable table2 = new DiningTable();
        table2.setNumber(2);
        table2.setSeats(6);

        tableRepository.save(table1);
        tableRepository.save(table2);

        // when
        final List<DiningTable> result = tableRepository.allTablesWithMinSeats(6);

        // then
        assertNotNull(result);
        assertEquals(List.of(table2), result);
    }

    @Test
    void shouldReturnEmptyList_whenThereIsNoTableWithMinSeats() {
        // given
        final DiningTable table = new DiningTable();
        table.setNumber(1);
        table.setSeats(4);

        tableRepository.save(table);

        // when
        final List<DiningTable> result = tableRepository.allTablesWithMinSeats(5);

        // then
        assertEquals(Collections.emptyList(), result);
    }
}
