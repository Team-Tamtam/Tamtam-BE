package tamtam.mooney.dto;

import java.time.LocalDateTime;


public record UserScheduleDto(
        Long scheduleId,
        String title,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        String location
) {}