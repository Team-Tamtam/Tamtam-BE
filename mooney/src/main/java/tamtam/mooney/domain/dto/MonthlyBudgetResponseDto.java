package tamtam.mooney.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class MonthlyBudgetResponseDto {
    private BigDecimal budgetAmount;
    private List<CategoryBudgetDto> categories;
    private List<ScheduleDto> keySchedules;

    @Getter
    @Builder
    public static class CategoryBudgetDto {
        private String categoryName;
        private BigDecimal categoryBudgetAmount;
    }

    @Getter
    @Builder
    public static class ScheduleDto {
        private Long scheduleId;
        private String title;
        private String categoryName;
        private String startDateTime;
        private String endDateTime;
    }
}
