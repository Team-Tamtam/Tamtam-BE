package tamtam.mooney.global.openai.dto;

public record ScheduledExpenseInputDto (
    String time,
    String description,
    String category,
    int categoryBudget,
    int weightedBudget,
    int perEventBudget,
    int averagePriceSuggestion,
    int remainingEventsInCategory
) {}

