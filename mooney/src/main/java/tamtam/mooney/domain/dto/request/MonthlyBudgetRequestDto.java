package tamtam.mooney.domain.dto.request;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public record MonthlyBudgetRequestDto (
        @NotNull BigDecimal budgetAmount,
        @NotNull String message
) {
}
