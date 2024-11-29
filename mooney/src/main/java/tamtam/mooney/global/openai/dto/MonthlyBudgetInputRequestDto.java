package tamtam.mooney.global.openai.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record MonthlyBudgetInputRequestDto(
        Map<String, Integer> currentBudget,
        String feedbackMessage,
        List<FixedExpenseInputDto> fixedExpenses,
        String userOpinions,
        List<NextMonthScheduleInputDto> nextMonthSchedules,
        BigDecimal totalBudget
) {}