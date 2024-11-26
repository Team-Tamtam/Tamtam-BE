package tamtam.mooney.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.dto.CategoryExpenseResponseDto;
import tamtam.mooney.domain.entity.CategoryName;
import tamtam.mooney.domain.entity.Expense;
import tamtam.mooney.domain.entity.MonthlyBudget;
import tamtam.mooney.domain.entity.User;
import tamtam.mooney.global.exception.CustomException;
import tamtam.mooney.global.exception.ErrorCode;
import tamtam.mooney.domain.repository.MonthlyBudgetRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryExpenseService {
    private final UserService userService;
    private final ExpenseService expenseService;
    private final MonthlyBudgetRepository monthlyBudgetRepository;

    public CategoryExpenseResponseDto getCategoryExpenseStats(int year, int month) {
        User user = userService.getCurrentUser();
        String period = String.format("%04d-%02d", year, month);

        // 이번 달 예산 가져오기
        MonthlyBudget monthlyBudget = monthlyBudgetRepository.findByUserAndPeriod(user, period)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
        BigDecimal budgetAmountLastMonth = monthlyBudget.getFinalAmount();

        // 이번 달 카테고리별 총 지출 금액 가져오기
        List<Expense> expenses = expenseService.findExpensesForUser(year, month, user);

        // 카테고리별 지출 금액 집계
        Map<CategoryName, BigDecimal> categoryStatsMap = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategoryName,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));

        // 이번 달 총 지출 금액 계산
        BigDecimal totalExpenseThisMonth = categoryStatsMap.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 카테고리별 예산 비율 계산 및 DTO로 변환
        List<CategoryExpenseResponseDto.CategoryDto> categoryStats = categoryStatsMap.entrySet().stream()
                .map(entry -> {
                    CategoryName categoryName = entry.getKey();
                    BigDecimal totalAmountThisMonth = entry.getValue();

                    // 이번 달 총 지출 금액에 대한 카테고리 지출 비율 계산
                    BigDecimal percentageOfTotal = totalExpenseThisMonth.compareTo(BigDecimal.ZERO) > 0
                            ? totalAmountThisMonth.divide(totalExpenseThisMonth, 3, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100))
                            : BigDecimal.ZERO;

                    // Build and return CategoryDto
                    return CategoryExpenseResponseDto.CategoryDto.builder()
                            .categoryName(categoryName.name())  // 카테고리 이름을 문자열로 변환
                            .categoryExpenseAmount(totalAmountThisMonth)
                            .percentage(percentageOfTotal)
                            .build();
                })
                .collect(Collectors.toList());

        return CategoryExpenseResponseDto.builder()
                .categories(categoryStats)
                .budgetAmount(budgetAmountLastMonth)
                .build();
    }
}
