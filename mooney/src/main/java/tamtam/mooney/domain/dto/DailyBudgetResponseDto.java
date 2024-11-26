package tamtam.mooney.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyBudgetResponseDto {
    private List<RepeatedScheduleDto> repeatedSchedules;
    private List<TomorrowScheduleDto> tomorrowSchedules;
    private BigDecimal budgetAmount;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RepeatedScheduleDto {
        private Long scheduleId;
        private String title;
        private String categoryName;
        private BigDecimal predictedAmount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TomorrowScheduleDto {
        private Long scheduleId;
        private String title;
        private String categoryName;
        private BigDecimal predictedAmount;
    }
}
