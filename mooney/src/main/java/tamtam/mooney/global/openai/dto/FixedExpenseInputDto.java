package tamtam.mooney.global.openai.dto;

public record FixedExpenseInputDto (
        String category,
        int amount
) {}