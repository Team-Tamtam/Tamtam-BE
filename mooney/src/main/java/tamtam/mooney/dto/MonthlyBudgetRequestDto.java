package tamtam.mooney.dto;

import java.math.BigDecimal;

public record MonthlyBudgetRequestDto (
        BigDecimal budgetAmount,
        String message
) {
}
