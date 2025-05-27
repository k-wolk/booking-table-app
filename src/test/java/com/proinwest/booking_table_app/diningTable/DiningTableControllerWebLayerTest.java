package com.proinwest.booking_table_app.diningTable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proinwest.booking_table_app.security.config.AuthEntryPointJwt;
import com.proinwest.booking_table_app.security.config.SecurityConfig;
import com.proinwest.booking_table_app.security.userDetails.CustomUserDetailsService;
import com.proinwest.booking_table_app.security.jwt.JwtUtils;
import com.proinwest.booking_table_app.reservation.Reservation;
import com.proinwest.booking_table_app.user.UserRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DiningTableController.class)
//@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class DiningTableControllerWebLayerTest {
    @MockBean
    private DiningTableService tableService;
    @MockBean
    private JwtUtils jwtUtils;
    @MockBean
    private UserRepository userRepository;
//    @MockBean
//    private UserDetailsService userDetailsService;
    @MockBean
    private CustomUserDetailsService customUserDetailsService;
    @MockBean
    private AuthEntryPointJwt authEntryPointJwt;
//    @MockBean
//    private SecurityFilterChain securityFilterChain;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllTables_whenUserIsAdmin_shouldReturnOk() throws Exception {
        // given
        final DiningTable table1 = Instancio.create(DiningTable.class);
        final DiningTable table2 = Instancio.create(DiningTable.class);
        table2.setActive(false);

        when(tableService.getAllTables()).thenReturn(List.of(table1, table2));

        // when & then
        mockMvc.perform(get("/tables")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")     .value(2))
                .andExpect(jsonPath("$[0].id")      .value(table1.getId()))
                .andExpect(jsonPath("$[0].number")  .value(table1.getNumber()))
                .andExpect(jsonPath("$[0].seats")   .value(table1.getSeats()))
                .andExpect(jsonPath("$[1].id")      .value(table2.getId()))
                .andExpect(jsonPath("$[1].number")  .value(table2.getNumber()))
                .andExpect(jsonPath("$[1].seats")   .value(table2.getSeats()));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllTables_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
        // when & then
        mockMvc.perform(get("/tables"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void getAllActiveTables_whenUserIsAuthenticated_shouldReturnOk() throws Exception {
        // given
        DiningTable table = Instancio.create(DiningTable.class);
        when(tableService.getAllActiveTables()).thenReturn(List.of(table));

        // when & then
        mockMvc.perform(get("/tables/getactive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(table.getId()))
                .andExpect(jsonPath("$[0].number").value(table.getNumber()))
                .andExpect(jsonPath("$[0].seats").value(table.getSeats()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllActiveTables_whenUserIsAdmin_shouldReturnOk() throws Exception {
        // when & then
        mockMvc.perform(get("/tables/getactive"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getTable_whenUserIsAuthenticated_shouldReturnOk() throws Exception {
        // given
        final DiningTable table = Instancio.create(DiningTable.class);
        Integer tableId = table.getId();

        when(tableService.getTable(tableId)).thenReturn(table);

        // when & then
        mockMvc.perform(get("/tables/{tableId}", tableId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")     .value(table.getId()))
                .andExpect(jsonPath("$.number") .value(table.getNumber()))
                .andExpect(jsonPath("$.seats")  .value(table.getSeats()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTable_whenUserIsAdmin_shouldReturnCreated() throws Exception {
        // given
        final Integer tableId =  1;

        final DiningTable table = Instancio.create(DiningTable.class);
        table.setId(null);

        final DiningTable savedTable = new DiningTable();
        savedTable.setId(tableId);
        savedTable.setNumber(table.getNumber());
        savedTable.setSeats(table.getSeats());

        when(tableService.createTable(any(DiningTable.class))).thenReturn(savedTable);
        when(tableService.location(savedTable)).thenReturn(URI.create("/tables/" + tableId));

        // when & then
        mockMvc.perform(post("/tables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(table)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/tables/" + tableId))
                .andExpect(jsonPath("$.id")     .value(savedTable.getId()))
                .andExpect(jsonPath("$.number") .value(savedTable.getNumber()))
                .andExpect(jsonPath("$.seats")  .value(savedTable.getSeats()));

        verify(tableService, times(1)).createTable(any(DiningTable.class));
        verify(tableService, times(1)).location(savedTable);
    }

    @Test
    @WithMockUser(roles = "USER")
    void createTable_whenIsNotAdmin_shouldReturnForbidden() throws Exception {
        // given
        DiningTable table = Instancio.create(DiningTable.class);

        // when & then
        mockMvc.perform(post("/tables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(table)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivateTable_whenUserIsAdmin_shouldReturnNoContent() throws Exception {
        // when & then
        mockMvc.perform(post("/tables/deactivate/{tableId}", 1))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deactivateTable_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
        // when & then
        mockMvc.perform(post("/tables/deactivate/{tableId}", 1))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateTable_whenUserIsAdmin_shouldReturnNoContent() throws Exception {
        // when & then
        mockMvc.perform(post("/tables/activate/{tableId}", 1))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void activateTable_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
        // when & then
        mockMvc.perform(post("/tables/activate/{tableId}", 1))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateTable_whenUserIsAdmin_shouldReturnOk() throws Exception {
        // given
        final Integer tableId = 1;

        final DiningTable tableToUpdate = Instancio.create(DiningTable.class);
        tableToUpdate.setId(tableId);

        final DiningTable newTable = new DiningTable();
        newTable.setId(tableId);
        newTable.setNumber(1);

        final DiningTable updatedTable = new DiningTable();
        updatedTable.setId(tableId);
        updatedTable.setNumber(newTable.getNumber());
        updatedTable.setSeats(tableToUpdate.getSeats());

        when(tableService.updateTable(eq(tableId), any(DiningTable.class))).thenReturn(updatedTable);

        // when & then
        mockMvc.perform(patch("/tables/{tableId}", tableId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(tableToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")     .value(updatedTable.getId()))
                .andExpect(jsonPath("$.number") .value(updatedTable.getNumber()))
                .andExpect(jsonPath("$.seats")  .value(updatedTable.getSeats()));

        verify(tableService, times(1)).updateTable(eq(tableId), any(DiningTable.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateTable_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
        // given
        DiningTable table = Instancio.create(DiningTable.class);
        table.setId(1);

        // when & then
        mockMvc.perform(patch("/tables/{tableId}", table.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(table)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteTable_whenUserIsAdmin_shouldReturnNoContent() throws Exception {
        // when & then
        mockMvc.perform(delete("/tables/{tableId}", 1))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteTable_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
        // when & then
        mockMvc.perform(delete("/tables/{tableId}", 1))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void availableTimes_whenUserIsAuthenticated_shouldReturnOk() throws Exception {
        // given
        Integer tableId = 1;
        LocalDate date = LocalDate.now().plusDays(1);
        List<String> availableTimes = List.of("11:00 - 15:00", "17:30 - 23:00");

        when(tableService.whenTableIsAvailable(tableId, date)).thenReturn(availableTimes);

        // when & then
        mockMvc.perform(get("/tables/{tableId}/date/{date}", tableId, date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(availableTimes.size()))
                .andExpect(jsonPath("$[0]").value("11:00 - 15:00"))
                .andExpect(jsonPath("$[1]").value("17:30 - 23:00"));

        verify(tableService, times(1)).whenTableIsAvailable(tableId, date);
    }

    @Test
    @WithMockUser
    void availableTables_whenUserIsAuthenticated_shouldReturnListOfFreeTables() throws Exception {
        // given
        final Reservation reservation = Instancio.create(Reservation.class);

        final DiningTable table1 = Instancio.create(DiningTable.class);
        final DiningTable table2 = Instancio.create(DiningTable.class);

        final List<DiningTable> freeTables = List.of(table1, table2);

        when(tableService.getAvailableTables(any(Reservation.class))).thenReturn(freeTables);

        // when & then
        mockMvc.perform(get("/tables/available")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reservation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")      .value(table1.getId()))
                .andExpect(jsonPath("$[0].number")  .value(table1.getNumber()))
                .andExpect(jsonPath("$[0].seats")   .value(table1.getSeats()))
                .andExpect(jsonPath("$[1].id")      .value(table2.getId()))
                .andExpect(jsonPath("$[1].number")  .value(table2.getNumber()))
                .andExpect(jsonPath("$[1].seats")   .value(table2.getSeats()));

        verify(tableService, times(1)).getAvailableTables(any(Reservation.class));
    }
}