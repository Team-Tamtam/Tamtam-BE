package tamtam.mooney.global.openai.dto;

import java.math.BigDecimal;
import java.util.List;

public record DailyBudgetInputRequestDto (
        List<RecurringExpenseInputDto> recurringExpense,
        List<ScheduledExpenseInputDto> scheduledExpenses,
        BigDecimal totalBudget,
        double weightForCategory
) {}