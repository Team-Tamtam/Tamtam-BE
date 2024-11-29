package tamtam.mooney.domain.dto;

import lombok.Getter;
import lombok.Setter;
import tamtam.mooney.domain.entity.UserSchedule;

import java.util.List;

@Getter
@Setter
public class MergeSchedulesRequest {
    // Getters and setters
    private List<UserSchedule> tomorrowSchedules;
    private String jsonResponse;
}
