package tamtam.mooney.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.dto.response.MonthlyReportResponseDto;
import tamtam.mooney.domain.entity.*;
import tamtam.mooney.domain.repository.CategoryBudgetRepository;
import tamtam.mooney.domain.repository.MonthlyReportRepository;
import tamtam.mooney.global.openai.AIPromptService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MonthlyReportService {
    private final ExpenseService expenseService;
    private final MonthlyReportRepository monthlyReportRepository;
    private final UserService userService;
    private final MonthlyBudgetService monthlyBudgetService;
    private final CategoryBudgetRepository categoryBudgetRepository;
    private final AIPromptService aiPromptService;

    public MonthlyReportResponseDto getMonthlyReport(int year, int month) {
        User user = userService.getCurrentUser();

        // 월 시작 및 종료 날짜 계산
        String period = String.format("%04d-%02d", year, month);

        // BudgetAmount 가져오기
        BigDecimal totalBudgetAmount = monthlyBudgetService.getMonthlyBudgetAmount(user, period);

        // 소비 금액 계산
        List<Expense> expenses = expenseService.findExpensesForUser(year, month, user);
        Map<String, BigDecimal> categoryBudgets = categoryBudgetRepository.findByUserAndPeriod(user, period)
                .stream()
                .collect(Collectors.toMap(
                        cb -> cb.getCategoryName().toString(), // Convert the category name to a string
                        CategoryBudget::getAmount // Use the amount from each CategoryBudget entity
                ));
        Map<String, BigDecimal> categoryExpenses = expenseService.categorizeExpensesByCategory(expenses);
        BigDecimal totalExpenseAmount = expenseService.calculateTotalExpenseAmount(expenses);
        BigDecimal totalIncomeAmount = BigDecimal.ZERO;

        // 피드백 메시지 생성
        String agentComment = aiPromptService.buildMonthlyReportMessage(
                totalBudgetAmount,
                categoryBudgets,
                totalExpenseAmount,
                categoryExpenses
        );

        // MonthlyReport 저장 또는 갱신
        var existingReport = monthlyReportRepository.findByUserAndPeriod(user, period);
        if (existingReport.isPresent()) {
            MonthlyReport monthlyReport = existingReport.get();
            monthlyReport.update(totalBudgetAmount, totalExpenseAmount, totalIncomeAmount, agentComment);
            monthlyReportRepository.save(monthlyReport);
        } else {
            MonthlyReport monthlyReport = MonthlyReport.builder()
                    .user(user)
                    .period(period)
                    .budgetAmount(totalBudgetAmount)
                    .totalExpenseAmount(totalExpenseAmount)
                    .totalIncomeAmount(totalIncomeAmount)
                    .agentComment(agentComment)
                    .build();
            monthlyReportRepository.save(monthlyReport);
        }

        return MonthlyReportResponseDto.builder()
                .budgetAmount(totalBudgetAmount)
                .totalExpenseAmount(totalExpenseAmount)
                .totalIncomeAmount(totalIncomeAmount)
                .feedbackMessage(agentComment)
                .build();
    }

//    // BudgetStatus 결정하는 메서드
//    private BudgetStatus determineBudgetStatus(BigDecimal budgetAmount, BigDecimal totalExpenseAmount) {
//        if (totalExpenseAmount.compareTo(budgetAmount) > 0) {
//            return BudgetStatus.OVER_BUDGET;  // 예산 초과
//        } else if (totalExpenseAmount.compareTo(budgetAmount) == 0) {
//            return BudgetStatus.ACHIEVED;    // 예산 달성
//        } else {
//            return BudgetStatus.NOT_ACHIEVED; // 예산 미달성
//        }
//    }

}
