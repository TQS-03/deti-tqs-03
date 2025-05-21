package tqs.electro.electro.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tqs.electro.electro.controllers.StationController;
import tqs.electro.electro.entities.Station;
import tqs.electro.electro.services.StationService;
import tqs.electro.electro.utils.ChargerType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StationController.class)
class StationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private StationService stationService;

  @Autowired
  private ObjectMapper objectMapper;

  private Station createSampleStation(UUID id) {
    Station s = new Station();
    s.setId(id);
    s.setName("Test Station");
    s.setAddress("123 Street");
    s.setMaxOccupation(10);
    s.setCurrentOccupation(3);
    s.setLatitude("40.0");
    s.setLongitude("-8.0");
    s.setChargerTypes(List.of(ChargerType.TYPE2));
    return s;
  }

  @Test
  void testGetAllStations() throws Exception {
    Station s1 = createSampleStation(UUID.randomUUID());
    Station s2 = createSampleStation(UUID.randomUUID());
    Mockito.when(stationService.getAllStations()).thenReturn(List.of(s1, s2));

    mockMvc.perform(get("/backend/station"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
  }

  @Test
  void testGetStationById_found() throws Exception {
    UUID id = UUID.randomUUID();
    Station station = createSampleStation(id);
    Mockito.when(stationService.getStationById(id)).thenReturn(Optional.of(station));

    mockMvc.perform(get("/backend/station/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.name").value("Test Station"));
  }

  @Test
  void testGetStationById_notFound() throws Exception {
    UUID id = UUID.randomUUID();
    Mockito.when(stationService.getStationById(id)).thenReturn(Optional.empty());

    mockMvc.perform(get("/backend/station/{id}", id))
        .andExpect(status().isNotFound());
  }

  @Test
  void testAddStation() throws Exception {
    Station station = createSampleStation(null);
    Station savedStation = createSampleStation(UUID.randomUUID());

    Mockito.when(stationService.addStation(any(Station.class))).thenReturn(savedStation);

    mockMvc.perform(post("/backend/station")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(station)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(savedStation.getId().toString()));
  }

  @Test
  void testUpdateStation_found() throws Exception {
    UUID id = UUID.randomUUID();
    Station updated = createSampleStation(id);

    Mockito.when(stationService.updateStation(eq(id), any(Station.class)))
        .thenReturn(Optional.of(updated));

    mockMvc.perform(put("/backend/station/{id}", id)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updated)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()));
  }

  @Test
  void testUpdateStation_notFound() throws Exception {
    UUID id = UUID.randomUUID();
    Station updated = createSampleStation(id);

    Mockito.when(stationService.updateStation(eq(id), any(Station.class)))
        .thenReturn(Optional.empty());

    mockMvc.perform(put("/backend/station/{id}", id)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updated)))
        .andExpect(status().isNotFound());
  }
}
