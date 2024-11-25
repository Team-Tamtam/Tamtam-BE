package tamtam.mooney.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.dto.MonthlyReportResponseDto;
import tamtam.mooney.entity.Expense;
import tamtam.mooney.entity.MonthlyReport;
import tamtam.mooney.entity.User;
import tamtam.mooney.repository.ExpenseRepository;
import tamtam.mooney.repository.MonthlyReportRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MonthlyReportService {
    private final ExpenseService expenseService;
    private final MonthlyReportRepository monthlyReportRepository;
    private final UserService userService;
    private final MonthlyBudgetService monthlyBudgetService;

    @Transactional
    public MonthlyReportResponseDto getMonthlyReport(int year, int month) {
        User user = userService.getCurrentUser();

        // 월 시작 및 종료 날짜 계산
        String period = String.format("%04d-%02d", year, month);

        // BudgetAmount 가져오기
        BigDecimal budgetAmount = monthlyBudgetService.getMonthlyBudgetAmount(user, period);

        // 소비 및 수입 금액 계산
        List<Expense> expenses = expenseService.findExpensesForUser(year, month, user);
        BigDecimal totalExpenseAmount = expenseService.calculateTotalExpenseAmount(expenses);
        BigDecimal totalIncomeAmount = BigDecimal.valueOf(350000); // TODO: 스정

        // 피드백 메시지 생성
        String agentComment = generateFeedbackMessage(budgetAmount, totalExpenseAmount);

        // MonthlyReport 저장 또는 갱신
        var existingReport = monthlyReportRepository.findByUserAndPeriod(user,  period);
        if (existingReport.isPresent()) {
            var monthlyReport = existingReport.get();
            monthlyReport.update(budgetAmount, totalExpenseAmount, totalIncomeAmount, agentComment);
            monthlyReportRepository.save(monthlyReport);
        } else {
            var monthlyReport = MonthlyReport.builder()
                    .user(user)
                    .period(period)
                    .budgetAmount(budgetAmount)
                    .totalExpenseAmount(totalExpenseAmount)
                    .totalIncomeAmount(totalIncomeAmount)
                    .agentComment(agentComment)
                    .build();
            monthlyReportRepository.save(monthlyReport);
        }

        return MonthlyReportResponseDto.builder()
                .budgetAmount(budgetAmount)
                .totalExpenseAmount(totalExpenseAmount)
                .totalIncomeAmount(totalIncomeAmount)
                .feedbackMessage(agentComment)
                .build();
    }

    private String generateFeedbackMessage(BigDecimal budgetAmount, BigDecimal totalExpenseAmount) {
        if (totalExpenseAmount.compareTo(budgetAmount) < 0) {
            return "Good job maintaining your budget!";
        } else if (totalExpenseAmount.compareTo(budgetAmount) == 0) {
            return "You hit your budget exactly. Great job staying on track!";
        } else {
            return "You exceeded your budget this month. Try to adjust your spending next month.";
        }
    }
}
