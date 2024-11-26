package tamtam.mooney.domain.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record MonthlyReportResponseDto(
        BigDecimal budgetAmount,
        BigDecimal totalExpenseAmount,
        BigDecimal totalIncomeAmount,
        String feedbackMessage
) {}