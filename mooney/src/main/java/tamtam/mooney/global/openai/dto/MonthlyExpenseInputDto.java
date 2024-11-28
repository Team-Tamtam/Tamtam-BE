package tamtam.mooney.global.openai.dto;

public record MonthlyExpenseInputDto(
        String date,
        String category,
        String description,
        int amount
) {}