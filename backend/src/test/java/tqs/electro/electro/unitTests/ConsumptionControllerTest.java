package tqs.electro.electro.unitTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tqs.electro.electro.controllers.ConsumptionController;
import tqs.electro.electro.entities.Consumption;
import tqs.electro.electro.entities.Station;
import tqs.electro.electro.repositories.StationRepository;
import tqs.electro.electro.services.ConsumptionService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConsumptionController.class)
class ConsumptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsumptionService consumptionService;

    @MockitoBean
    private StationRepository stationRepository;

    private static final String BASE_URL = "/backend/consumption";

    @Autowired
    private ObjectMapper objectMapper;

    private Consumption createSampleConsumption(UUID id, Station station) {
        Consumption c = new Consumption();
        c.setId(id);
        c.setStation(station);
        c.setStartTime(LocalDateTime.of(2025, 5, 24, 15, 0));
        c.setEndTime(LocalDateTime.of(2025, 5, 24, 16, 0));
        c.setEnergyUsed(1000.0);
        c.setPricePerKWh(0.25);
        return c;
    }

    @Test
    void testGetConsumptionById_found() throws Exception {
        UUID id = UUID.randomUUID();
        Consumption consumption = createSampleConsumption(id, new Station());

        when(consumptionService.getConsumptionById(id)).thenReturn(Optional.of(consumption));
        when(consumptionService.getConsumptionDuration(id)).thenReturn(Duration.ofHours(1));
        when(consumptionService.getConsumptionCost(id)).thenReturn(10.5);

        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consumption.id").value(id.toString()))
                .andExpect(jsonPath("$.duration").value("PT1H"))
                .andExpect(jsonPath("$.cost").value("10.5"));
    }

    @Test
    void testGetConsumptionById_notFound() throws Exception {
        UUID id = UUID.randomUUID();

        when(consumptionService.getConsumptionById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetConsumptionsByStationId() throws Exception {
        UUID stationId = UUID.randomUUID();
        Consumption c1 = createSampleConsumption(UUID.randomUUID(), new Station());
        Consumption c2 = createSampleConsumption(UUID.randomUUID(), new Station());

        when(consumptionService.getAllConsumptionsByStationId(stationId)).thenReturn(List.of(c1, c2));

        mockMvc.perform(get(BASE_URL + "/station/{id}", stationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testAddConsumption_success() throws Exception {
        Consumption toAdd = createSampleConsumption(null, new Station());
        Consumption saved = createSampleConsumption(UUID.randomUUID(), new Station());

        when(consumptionService.save(any(Consumption.class))).thenReturn(saved);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(toAdd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId().toString()));
    }

    @Test
    void testAddConsumption_failure() throws Exception {
        Consumption toAdd = createSampleConsumption(null, new Station());

        when(consumptionService.save(any(Consumption.class))).thenReturn(null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(toAdd)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateConsumption_found() throws Exception {
        UUID id = UUID.randomUUID();
        Consumption updated = createSampleConsumption(id, new Station());

        when(consumptionService.updateConsumption(eq(id), any(Consumption.class))).thenReturn(Optional.of(updated));

        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void testUpdateConsumption_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        Consumption updated = createSampleConsumption(id, new Station());

        when(consumptionService.updateConsumption(eq(id), any(Consumption.class))).thenReturn(Optional.empty());

        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteConsumption() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isNoContent());
    }

}
