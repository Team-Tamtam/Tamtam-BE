package tamtam.mooney.global.openai.dto;

import java.util.List;
import java.util.Map;

public record MonthlyBudgetInputRequestDto(
        Map<String, Integer> currentBudget,
        String feedbackMessage,
        List<FixedExpenseInputDto> fixedExpenses,
        String userOpinions,
        List<NextMonthScheduleInputDto> nextMonthSchedules,
        double totalBudget
) {}