package tqs.electro.electro.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ReservationDTO {

    private UUID personId;
    private UUID stationId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}
