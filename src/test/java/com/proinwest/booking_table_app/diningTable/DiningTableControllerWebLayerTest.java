package com.proinwest.booking_table_app.diningTable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proinwest.booking_table_app.reservation.Reservation;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DiningTableController.class)
@ExtendWith(MockitoExtension.class)
class DiningTableControllerWebLayerTest {
    @MockBean
    private DiningTableService tableService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;

    @Test
    void shouldGetAllTables() throws Exception {
        // given
        final DiningTable table1 = Instancio.create(DiningTable.class);
        final DiningTable table2 = Instancio.create(DiningTable.class);

        final List<DiningTable> tablesList = new ArrayList<>();
        tablesList.add(table1);
        tablesList.add(table2);

        when(tableService.getAllTables()).thenReturn(tablesList);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/tables")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")     .value(tablesList.size()))
                .andExpect(jsonPath("$[0].id")      .value(table1.getId()))
                .andExpect(jsonPath("$[0].number")  .value(table1.getNumber()))
                .andExpect(jsonPath("$[0].seats")   .value(table1.getSeats()))
                .andExpect(jsonPath("$[1].id")      .value(table2.getId()))
                .andExpect(jsonPath("$[1].number")  .value(table2.getNumber()))
                .andExpect(jsonPath("$[1].seats")   .value(table2.getSeats()));
    }

    @Test
    void shouldGetTableById() throws Exception {
        // given
        final Integer tableId = 1;

        final DiningTable table = Instancio.create(DiningTable.class);
        table.setId(tableId);

        when(tableService.getTable(tableId)).thenReturn(table);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/tables/{tableId}", tableId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")     .value(table.getId()))
                .andExpect(jsonPath("$.number") .value(table.getNumber()))
                .andExpect(jsonPath("$.seats")  .value(table.getSeats()));
    }

    @Test
    void shouldAddTable() throws Exception {
        // given
        final Integer tableId =  1;

        final DiningTable table = Instancio.create(DiningTable.class);
        table.setId(null);

        final DiningTable savedTable = new DiningTable();
        savedTable.setId(tableId);
        savedTable.setNumber(table.getNumber());
        savedTable.setSeats(table.getSeats());

        when(tableService.addTable(any(DiningTable.class))).thenReturn(savedTable);
        when(tableService.location(savedTable)).thenReturn(URI.create("/tables/" + tableId));

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/tables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(table)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/tables/" + tableId))
                .andExpect(jsonPath("$.id")     .value(savedTable.getId()))
                .andExpect(jsonPath("$.number") .value(savedTable.getNumber()))
                .andExpect(jsonPath("$.seats")  .value(savedTable.getSeats()));

        verify(tableService, times(1)).addTable(any(DiningTable.class));
        verify(tableService, times(1)).location(savedTable);
    }

    @Test
    void shouldUpdateTable() throws Exception {
        // given
        final Integer tableId = 1;

        final DiningTable table = Instancio.create(DiningTable.class);
        table.setId(tableId);

        final DiningTable updatedTable = Instancio.create(DiningTable.class);
        updatedTable.setId(tableId);

        when(tableService.updateTable(eq(tableId), any(DiningTable.class))).thenReturn(updatedTable);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/tables/{tableId}", tableId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(table)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")     .value(updatedTable.getId()))
                .andExpect(jsonPath("$.number") .value(updatedTable.getNumber()))
                .andExpect(jsonPath("$.seats")  .value(updatedTable.getSeats()));

        verify(tableService, times(1)).updateTable(eq(tableId), any(DiningTable.class));
    }

    @Test
    void shouldPartiallyUpdateTable() throws Exception {
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

        when(tableService.partiallyUpdateTable(eq(tableId), any(DiningTable.class))).thenReturn(updatedTable);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                .patch("/tables/{tableId}", tableId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(tableToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")     .value(updatedTable.getId()))
                .andExpect(jsonPath("$.number") .value(updatedTable.getNumber()))
                .andExpect(jsonPath("$.seats")  .value(updatedTable.getSeats()));

        verify(tableService, times(1)).partiallyUpdateTable(eq(tableId), any(DiningTable.class));
    }

    @Test
    void shouldDeleteTable() throws Exception {
        // given
        final Integer tableId = 1;

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                .delete("/tables/{tableId}", tableId))
                .andExpect(status().isNoContent());

        verify(tableService, times(1)).deleteTable(tableId);
    }

    @Test
    void shouldReturnListOfFreeTables() throws Exception {
        // given
        final Reservation reservation = Instancio.create(Reservation.class);

        final DiningTable table1 = Instancio.create(DiningTable.class);
        final DiningTable table2 = Instancio.create(DiningTable.class);

        final List<DiningTable> freeTables = new ArrayList<>();
        freeTables.add(table1);
        freeTables.add(table2);

        when(tableService.getFreeTables(any(Reservation.class))).thenReturn(freeTables);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                .get("/tables/freetables")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(reservation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")      .value(table1.getId()))
                .andExpect(jsonPath("$[0].number")  .value(table1.getNumber()))
                .andExpect(jsonPath("$[0].seats")   .value(table1.getSeats()))
                .andExpect(jsonPath("$[1].id")      .value(table2.getId()))
                .andExpect(jsonPath("$[1].number")  .value(table2.getNumber()))
                .andExpect(jsonPath("$[1].seats")   .value(table2.getSeats()));

        verify(tableService, times(1)).getFreeTables(any(Reservation.class));
    }
}