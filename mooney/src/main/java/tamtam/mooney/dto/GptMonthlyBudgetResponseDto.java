package tamtam.mooney.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class GptMonthlyBudgetResponseDto {
    private BigDecimal totalBudget;
    private List<GptCategoryDto> categoryBudgets;
    private List<GptScheduleDto> schedules;

    @Getter
    public static class GptCategoryDto {
        private Long id;
        private String categoryName;
        private BigDecimal budgetAmount;
    }

    @Getter
    public static class GptScheduleDto {
        private Long id;
        private String title;
        private String categoryName;
        private String startTime;
        private String endTime;
    }
}