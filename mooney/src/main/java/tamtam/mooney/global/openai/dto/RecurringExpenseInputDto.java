package tamtam.mooney.global.openai.dto;

public record RecurringExpenseInputDto (
    String category,
    int remainingBudget,
    int remainingDays,
    double perDayAmount,
    double perMealAmount
) {}
