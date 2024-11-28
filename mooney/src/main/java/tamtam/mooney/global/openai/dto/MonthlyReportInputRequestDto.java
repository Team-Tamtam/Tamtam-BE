package tamtam.mooney.global.openai.dto;

import java.util.List;
import java.util.Map;

public record MonthlyReportInputRequestDto(
        double totalBudget,
        Map<String, Integer> categoryBudgets,
        List<MonthlyExpenseInputDto> monthlyExpenses
) {}