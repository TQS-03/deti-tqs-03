package tqs.electro.electro.integrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tqs.electro.electro.entities.Station;
import tqs.electro.electro.repositories.StationRepository;
import tqs.electro.electro.utils.ChargerType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StationRepository stationRepository;

    @BeforeEach
    void cleanup() {
        stationRepository.deleteAll();
    }

    @Test
    void whenGetAllStations_thenReturnList() throws Exception {
        // given two stations in the DB
        Station s1 = createStation("A", ChargerType.TYPE2);
        Station s2 = createStation("B", ChargerType.CCS);
        stationRepository.saveAll(List.of(s1, s2));

        List<Station> stations = stationRepository.findAll();
        s1 = stations.get(0);
        s2 = stations.get(1);

        String objectJson = String.format("""
            [
                {
                    "id":"%s",
                    "name":"A",
                    "address":"addr",
                    "maxOccupation":5,
                    "currentOccupation":1,
                    "latitude":"0.0",
                    "longitude":"0.0",
                    "chargerTypes":["Type 2"]
                },
                {
                    "id":"%s",
                    "name":"B",
                    "address":"addr",
                    "maxOccupation":5,
                    "currentOccupation":1,
                    "latitude":"0.0",
                    "longitude":"0.0",
                    "chargerTypes":["CCS"]
                }
            ]
        """,
        s1.getId().toString(),
        s2.getId().toString());

        // when
        MvcResult result = mockMvc.perform(get("/backend/station"))
                .andExpect(status().isOk())
                .andReturn();

        String actualJson = result.getResponse().getContentAsString();
        JSONAssert.assertEquals(objectJson, actualJson, true);
    }

    @Test
    void whenGetStationById_thenReturnOne() throws Exception {
        // given
        Station saved = stationRepository.save(createStation("X", ChargerType.CHADEMO));

        // when / then
        mockMvc.perform(get("/backend/station/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId().toString()))
                .andExpect(jsonPath("$.chargerTypes[0]").value("CHAdeMO"));
    }

    @Test
    void whenAddStation_thenPersisted() throws Exception {
        // raw JSON payload
        String payload = """
            {
                "name":"NewOne",
                "address":"Addr",
                "maxOccupation":12,
                "currentOccupation":3,
                "latitude":"12.3",
                "longitude":"45.6",
                "chargerTypes":["TYPE 1","TESLA"]
            }
        """;

        // when
        mockMvc.perform(post("/backend/station")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("NewOne"));

        // then repo has one entry
        assertThat(stationRepository.count()).isEqualTo(1);
    }

    @Test
    void whenUpdateStation_thenFieldsChange() throws Exception {
        // given existing station
        Station existing = stationRepository.save(createStation("OldName", ChargerType.SCHUKO));

        // new JSON for update
        String updateJson = """
            {
                "name":"UpdatedName",
                "address":"NewAddr",
                "maxOccupation":99,
                "currentOccupation":9,
                "latitude":"99.9",
                "longitude":"88.8",
                "chargerTypes":["CCS"]
            }
        """;

        // when
        mockMvc.perform(put("/backend/station/{id}", existing.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("UpdatedName"))
                .andExpect(jsonPath("$.maxOccupation").value(99))
                .andExpect(jsonPath("$.chargerTypes[0]").value("CCS"));

        // and the repository reflects the update
        Station reloaded = stationRepository.findById(existing.getId()).orElseThrow();
        assertThat(reloaded.getName()).isEqualTo("UpdatedName");
        assertThat(reloaded.getChargerTypes()).containsExactly(ChargerType.CCS);
    }

    private Station createStation(String name, ChargerType type) {
        Station s = new Station();
        s.setName(name);
        s.setAddress("addr");
        s.setMaxOccupation(5);
        s.setCurrentOccupation(1);
        s.setLatitude("0.0");
        s.setLongitude("0.0");
        s.setChargerTypes(List.of(type));
        return s;
    }

    @Test
    void whenFilterStationByType_thenReturnMatchingStations() throws Exception {
        // given
        Station s1 = createStation("Station1", ChargerType.TYPE2);
        Station s2 = createStation("Station2", ChargerType.CCS);
        Station s3 = createStation("Station3", ChargerType.TYPE2);
        stationRepository.saveAll(List.of(s1, s2, s3));

        // when
        MvcResult result = mockMvc.perform(get("/backend/station/filter/{type}", "TYPE2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].chargerTypes[0]").value("Type 2"))
                .andExpect(jsonPath("$[1].chargerTypes[0]").value("Type 2"))
                .andReturn();

        String json = result.getResponse().getContentAsString();
        System.out.println("Filtered result: " + json);
    }

    @Test
    void whenFilterStationByType_thenReturnNotFound() throws Exception {
        // no station saved with this charger type
        stationRepository.save(createStation("OnlyStation", ChargerType.TESLA));

        mockMvc.perform(get("/backend/station/filter/{type}", "CHADEMO"))
                .andExpect(status().isNotFound());
    }

}
