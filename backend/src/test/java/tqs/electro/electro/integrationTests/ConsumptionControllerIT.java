package tqs.electro.electro.integrationTests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tqs.electro.electro.entities.Consumption;
import tqs.electro.electro.repositories.ConsumptionRepository;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ConsumptionControllerIT {

    private static final String BASE_URL = "/backend/consumption";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConsumptionRepository consumptionRepository;

    @Test
    void whenGetConsumptionById_thenReturnDurationAndCost() throws Exception {
        // Set up your entity with Duration.ofHours(1) and cost 10.5
        LocalDateTime now = LocalDateTime.now();
        Consumption consumption = new Consumption();
        consumption.setStartTime(now.minusHours(1));
        consumption.setEndTime(now);
        consumption.setEnergyUsed(21.0);
        consumption.setPricePerKWh(0.5);
        consumption = consumptionRepository.save(consumption);

        mockMvc.perform(get(BASE_URL + "/{id}", consumption.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duration").value("PT1H")) // Note the string "PT1H"
                .andExpect(jsonPath("$.cost").value(10.5));
    }

}
