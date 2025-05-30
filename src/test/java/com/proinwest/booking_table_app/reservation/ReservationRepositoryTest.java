package com.proinwest.booking_table_app.reservation;

import com.proinwest.booking_table_app.diningTable.DiningTable;
import com.proinwest.booking_table_app.diningTable.DiningTableRepository;
import com.proinwest.booking_table_app.user.User;
import com.proinwest.booking_table_app.user.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.proinwest.booking_table_app.reservation.ReservationService.MIN_DURATION;
import static com.proinwest.booking_table_app.reservation.ReservationService.OPENING_TIME;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ReservationRepositoryTest {
    @Container
    @ServiceConnection
    private static final MySQLContainer mySQLContainer = new MySQLContainer("mysql:8.4.0");
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DiningTableRepository tableRepository;
    private User user;
    private DiningTable table;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setLogin("testUser");
        user.setEmail("john@mail.com");
        user.setFirstName("Johnny");
        user.setLastName("Doe");
        user.setPhoneNumber("999 999 999");
        user.setPassword("secretpassword");
        userRepository.save(user);

        table = new DiningTable();
        table.setNumber(4444);
        table.setSeats(5);
        table.setActive(true);
        tableRepository.save(table);

        reservation = new Reservation();
        reservation.setReservationDate(LocalDate.now().plusDays(1));
        reservation.setReservationTime(OPENING_TIME);
        reservation.setDuration(MIN_DURATION);
        reservation.setUser(user);
        reservation.setDiningTable(table);
        reservationRepository.save(reservation);
    }

    @AfterEach
    void tearDown() {
        reservationRepository.deleteAll();
        userRepository.deleteAll();
        tableRepository.deleteAll();
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
    void searchReservations_whenExists_shouldFindReservationById() {
        // given
        String searchTerm = reservation.getId().toString();

        // when
        List<Reservation> result = reservationRepository.searchReservations(searchTerm);

        // then
        assertNotNull(result);
        assertEquals(List.of(reservation), result);
    }

    @Test
    void searchReservations_whenExists_shouldFindReservationByDate() {
        // given
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd");
        String searchTerm = reservation.getReservationDate().format(formatter);

        // when
        List<Reservation> result = reservationRepository.searchReservations(searchTerm);

        // then
        assertNotNull(result);
        assertEquals(List.of(reservation), result);
    }

    @Test
    void searchReservations_whenExists_shouldFindReservationByTime() {
        // given
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH");
        String searchTerm = reservation.getReservationTime().format(formatter);

        // when
        List<Reservation> result = reservationRepository.searchReservations(searchTerm);

        // then
        assertNotNull(result);
        assertEquals(List.of(reservation), result);
    }

    @Test
    void searchReservations_whenExists_shouldFindReservationByUserLogin() {
        // given
        String searchTerm = "testU";

        // when
        List<Reservation> result = reservationRepository.searchReservations(searchTerm);

        // then
        assertNotNull(result);
        assertEquals(List.of(reservation), result);
    }

    @Test
    void searchReservations_whenExists_shouldFindReservationByUserEmail() {
        // given
        String searchTerm = "@m";

        // when
        List<Reservation> result = reservationRepository.searchReservations(searchTerm);

        // then
        assertNotNull(result);
        assertEquals(List.of(reservation), result);
    }

    @Test
    void searchReservations_whenExists_shouldFindReservationByUserFirstName() {
        // given
        String searchTerm = "nny";

        // when
        List<Reservation> result = reservationRepository.searchReservations(searchTerm);

        // then
        assertNotNull(result);
        assertEquals(List.of(reservation), result);
    }

    @Test
    void searchReservations_whenExists_shouldFindReservationByUserLastName() {
        // given
        String searchTerm = "oe";

        // when
        List<Reservation> result = reservationRepository.searchReservations(searchTerm);

        // then
        assertNotNull(result);
        assertEquals(List.of(reservation), result);
    }

    @Test
    void searchReservations_whenExists_shouldFindReservationByUserPhoneNumber() {
        // given
        String searchTerm = "999";

        // when
        List<Reservation> result = reservationRepository.searchReservations(searchTerm);

        // then
        assertNotNull(result);
        assertEquals(List.of(reservation), result);
    }

    @Test
    void searchReservations_whenExists_shouldFindReservationByTableNumber() {
        // given
        String searchTerm = "444";

        // when
        List<Reservation> result = reservationRepository.searchReservations(searchTerm);

        // then
        assertNotNull(result);
        assertEquals(List.of(reservation), result);
    }

    @Test
    void searchReservations_whenNotExists_shouldReturnEmptyList() {
        // given
        String searchTerm = "nonexistent";

        // when
        List<Reservation> result = reservationRepository.searchReservations(searchTerm);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findAllByDateAndTableId_whenExists_shouldReturnReservations() {
        // given
        LocalDate date = reservation.getReservationDate();
        Integer tableId = table.getId();

        // when
        List<Reservation> result = reservationRepository.findAllByDateAndTableId(date, tableId);

        // then
        assertNotNull(result);
        assertEquals(List.of(reservation), result);
    }

    @Test
    void findAllByDateAndTableId_whenNotExists_shouldReturnEmptyList() {
        // given
        LocalDate date = LocalDate.now().plusDays(2);
        Integer tableId = table.getId();

        // when
        List<Reservation> result = reservationRepository.findAllByDateAndTableId(date, tableId);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}