package tamtam.mooney.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.dto.MonthlyReportResponseDto;
import tamtam.mooney.entity.MonthlyBudget;
import tamtam.mooney.entity.MonthlyReport;
import tamtam.mooney.entity.User;
import tamtam.mooney.repository.ExpenseRepository;
import tamtam.mooney.repository.MonthlyBudgetRepository;
import tamtam.mooney.repository.MonthlyReportRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class MonthlyReportService {
    private final UserService userService;
    private final MonthlyBudgetRepository monthlyBudgetRepository;
    private final ExpenseRepository expenseRepository;
    private final MonthlyReportRepository monthlyReportRepository;

    @Transactional
    public MonthlyReportResponseDto getMonthlyReport(int year, int month) {
        User user = userService.getCurrentUser();

        // 월 시작 및 종료 날짜 계산
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
        String period = String.format("%04d-%02d", year, month); // YYYY-MM 형식으로 변환

        // BudgetAmount 가져오기
        BigDecimal budgetAmount = getMonthlyBudgetAmount(user, period);

        // 소비 및 수입 금액 계산
        BigDecimal totalExpenseAmount = expenseRepository.findTotalExpenseAmountByUserAndMonth(user, startOfMonth, endOfMonth);
        BigDecimal totalIncomeAmount = BigDecimal.valueOf(350000); // TODO: 스정

        // 피드백 메시지 생성
        String agentComment = generateFeedbackMessage(budgetAmount, totalExpenseAmount);

        // MonthlyReport 저장 또는 갱신
        var existingReport = monthlyReportRepository.findByUserAndPeriod(user.getUserId(),  period);
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

    private BigDecimal getMonthlyBudgetAmount(User user, String period) {
        MonthlyBudget monthlyBudget = monthlyBudgetRepository.findByUserAndPeriod(user, period)
                .orElseThrow(() -> new IllegalArgumentException("해당 월에 대한 예산 정보가 없습니다."));
        return monthlyBudget.getFinalAmount() != null ? monthlyBudget.getFinalAmount() : monthlyBudget.getInitialAmount();
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
