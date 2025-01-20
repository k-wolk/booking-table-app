package com.proinwest.booking_table_app.diningTable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proinwest.booking_table_app.reservation.Reservation;
import org.hamcrest.Matchers;
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
    private DiningTableService diningTableService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldGetAllTables() throws Exception {
        // given
        final DiningTable table1 = Instancio.create(DiningTable.class);
        final DiningTable table2 = Instancio.create(DiningTable.class);

        final List<DiningTable> tablesList = new ArrayList<>();
        tablesList.add(table1);
        tablesList.add(table2);

        when(diningTableService.getAllDiningTables()).thenReturn(tablesList);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/diningtables")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()",     Matchers.is(tablesList.size())))
                .andExpect(jsonPath("$[0].id",      Matchers.is(table1.getId())))
                .andExpect(jsonPath("$[0].number",  Matchers.is(table1.getNumber())))
                .andExpect(jsonPath("$[0].seats",   Matchers.is(table1.getSeats())))
                .andExpect(jsonPath("$[1].id",      Matchers.is(table2.getId())))
                .andExpect(jsonPath("$[1].number",  Matchers.is(table2.getNumber())))
                .andExpect(jsonPath("$[1].seats",   Matchers.is(table2.getSeats())));
    }

    @Test
    void shouldGetTableById() throws Exception {
        // given
        final Integer tableId = 1;

        final DiningTable table = Instancio.create(DiningTable.class);
        table.setId(tableId);

        when(diningTableService.getDiningTable(tableId)).thenReturn(table);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/diningtables/{id}", tableId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id",     Matchers.is(table.getId())))
                .andExpect(jsonPath("$.number", Matchers.is(table.getNumber())))
                .andExpect(jsonPath("$.seats",  Matchers.is(table.getSeats())));
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

        when(diningTableService.addDiningTable(any(DiningTable.class))).thenReturn(savedTable);
        when(diningTableService.location(savedTable)).thenReturn(URI.create("/diningtables/" + tableId));

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/diningtables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(table)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/diningtables/" + tableId))
                .andExpect(jsonPath("$.id",     Matchers.is(savedTable.getId())))
                .andExpect(jsonPath("$.number", Matchers.is(savedTable.getNumber())))
                .andExpect(jsonPath("$.seats",  Matchers.is(savedTable.getSeats())));

        verify(diningTableService, times(1)).addDiningTable(any(DiningTable.class));
    }

    @Test
    void shouldUpdateTable() throws Exception {
        // given
        final Integer tableId = 1;

        final DiningTable table = Instancio.create(DiningTable.class);
        table.setId(tableId);

        final DiningTable updatedTable = Instancio.create(DiningTable.class);
        updatedTable.setId(tableId);

        when(diningTableService.updateDiningTable(eq(tableId), any(DiningTable.class))).thenReturn(updatedTable);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/diningtables/{id}", tableId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(table)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id",       Matchers.is(updatedTable.getId())))
                .andExpect(jsonPath("$.number",   Matchers.is(updatedTable.getNumber())))
                .andExpect(jsonPath("$.seats",    Matchers.is(updatedTable.getSeats())));

        verify(diningTableService, times(1)).updateDiningTable(eq(tableId), any(DiningTable.class));
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

        when(diningTableService.partiallyUpdateDiningTable(eq(tableId), any(DiningTable.class))).thenReturn(updatedTable);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                .patch("/diningtables/{id}", tableId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(tableToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id",     Matchers.is(updatedTable.getId())))
                .andExpect(jsonPath("$.number", Matchers.is(updatedTable.getNumber())))
                .andExpect(jsonPath("$.seats",  Matchers.is(updatedTable.getSeats())));

        verify(diningTableService, times(1)).partiallyUpdateDiningTable(eq(tableId), any(DiningTable.class));
    }

    @Test
    void shouldDeleteTable() throws Exception {
        // given
        final Integer tableId = 1;

        doNothing().when(diningTableService).deleteDiningTable(tableId);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                .delete("/diningtables/{id}", tableId))
                .andExpect(status().isNoContent());

        verify(diningTableService, times(1)).deleteDiningTable(tableId);
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

        when(diningTableService.getFreeTables(any(Reservation.class))).thenReturn(freeTables);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                .get("/diningtables/freetables")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(reservation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id",      Matchers.is(table1.getId())))
                .andExpect(jsonPath("$[0].number",  Matchers.is(table1.getNumber())))
                .andExpect(jsonPath("$[0].seats",   Matchers.is(table1.getSeats())))
                .andExpect(jsonPath("$[1].id",      Matchers.is(table2.getId())))
                .andExpect(jsonPath("$[1].number",  Matchers.is(table2.getNumber())))
                .andExpect(jsonPath("$[1].seats",   Matchers.is(table2.getSeats())));

        verify(diningTableService, times(1)).getFreeTables(any(Reservation.class));
    }
}