package tamtam.mooney.domain.dto.common;

import java.time.LocalDateTime;


public record UserScheduleDto(
        Long scheduleId,
        String title,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        String location
) {}