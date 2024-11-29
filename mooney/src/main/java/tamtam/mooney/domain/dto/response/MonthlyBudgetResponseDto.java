package tamtam.mooney.domain.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyBudgetResponseDto {
    private BigDecimal budgetAmount;
    private List<CategoryBudgetDto> categories;
    private List<MonthScheduleDto> keySchedules;
    private String reason;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryBudgetDto {
        private String categoryName;
        private BigDecimal categoryBudgetAmount;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthScheduleDto {
        private Long scheduleId;
        private String title;
        private String categoryName;
        private String startDateTime;
        private String endDateTime;
    }
}
