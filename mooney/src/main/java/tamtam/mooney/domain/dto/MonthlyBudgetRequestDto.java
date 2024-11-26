package tamtam.mooney.domain.dto;

import java.math.BigDecimal;

public record MonthlyBudgetRequestDto (
        BigDecimal budgetAmount,
        String message
) {
}
