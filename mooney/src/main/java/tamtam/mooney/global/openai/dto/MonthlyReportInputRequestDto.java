package tamtam.mooney.global.openai.dto;

import java.math.BigDecimal;
import java.util.Map;

public record MonthlyReportInputRequestDto(
        BigDecimal totalBudgetAmount,
        Map<String, BigDecimal> categoryBudgets,
        BigDecimal totalExpenseAmount,
        Map<String, BigDecimal> categoryExpenses
) {}