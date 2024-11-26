package tamtam.mooney.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class CategoryExpenseResponseDto {
    private List<CategoryDto> categories;
    private BigDecimal budgetAmount;

    @Getter
    @Builder
    public static class CategoryDto {
        private String categoryName;
        private BigDecimal percentage;
        private BigDecimal categoryExpenseAmount;
    }
}