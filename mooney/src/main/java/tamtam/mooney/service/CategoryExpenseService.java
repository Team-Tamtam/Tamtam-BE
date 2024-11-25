package tamtam.mooney.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.dto.CategoryExpenseResponseDto;
import tamtam.mooney.entity.Expense;
import tamtam.mooney.entity.User;
import tamtam.mooney.repository.DailyBudgetRepository;
import tamtam.mooney.repository.ExpenseRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryExpenseService {
    private final UserService userService;
    private final ExpenseRepository expenseRepository;
    private final DailyBudgetRepository dailyBudgetRepository;

    @Transactional
    public CategoryExpenseResponseDto getCategoryExpenseStats(int year, int month) {
        User user = userService.getCurrentUser();
        // 이번 달의 시작 날짜와 마지막 날짜 계산
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        // 지난 달 예산 정보 가져오기
        LocalDate startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDate endOfLastMonth = startOfMonth.minusDays(1);

        // 지난 달 예산 가져오기
        BigDecimal budgetAmountLastMonth = dailyBudgetRepository.findBudgetAmountByUserAndMonth(user.getUserId(), startOfLastMonth, endOfLastMonth);

        // 이번 달 카테고리별 총 지출 금액 가져오기
        List<Expense> expenses = expenseRepository.findExpensesByMonth(startOfMonth, endOfMonth);

        // 카테고리별 지출 금액 집계
        var categoryStatsMap = expenses.stream()
                .collect(Collectors.groupingBy(
                        expense -> expense.getCategory().getCategoryId(),
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));

        // 카테고리별 예산 비율 계산 및 DTO로 변환
        List<CategoryExpenseResponseDto.CategoryDto> categoryStats = categoryStatsMap.entrySet().stream()
                .map(entry -> {
                    Long categoryId = entry.getKey();
                    BigDecimal totalAmountThisMonth = entry.getValue();

                    // Get category name for the given categoryId
                    String categoryName = expenses.stream()
                            .filter(expense -> expense.getCategory().getCategoryId().equals(categoryId))
                            .findFirst()
                            .map(expense -> expense.getCategory().getCategoryName().name())
                            .orElse("UNKNOWN");

                    // Calculate the percentage of the total amount for this category based on the last month's budget
                    BigDecimal percentageOfTotal = totalAmountThisMonth
                            .divide(budgetAmountLastMonth, 4, BigDecimal.ROUND_HALF_UP)
                            .multiply(BigDecimal.valueOf(100));

                    // Build and return CategoryDto
                    return CategoryExpenseResponseDto.CategoryDto.builder()
                            .categoryId(categoryId)
                            .categoryName(categoryName)
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