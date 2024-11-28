package tamtam.mooney.global.openai.dto;

import java.util.List;

public record DailyBudgetInputRequestDto (
        RecurringExpenseInputDto recurringExpense,
        List<ScheduledExpenseInputDto> scheduledExpenses,
        double totalBudget,
        double weightForCategory
) {}