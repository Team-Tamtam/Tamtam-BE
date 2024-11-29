package tamtam.mooney.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.entity.Expense;
import tamtam.mooney.domain.entity.User;
import tamtam.mooney.domain.repository.ExpenseRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepository;

    @Transactional(readOnly = true)
    public List<Expense> findExpensesForUser(int year, int month, User user) {
        return expenseRepository.findByUserAndTransactionDateTimeBetween(
                user,
                LocalDate.of(year, month, 1).atStartOfDay(), // 시작 날짜의 00:00:00
                LocalDate.of(year, month, 1).withDayOfMonth(LocalDate.of(year, month, 1).lengthOfMonth()).atTime(LocalTime.MAX) // 종료 날짜의 23:59:59
        );
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateTotalExpenseAmount(List<Expense> expenses) {
        return expenses.stream()
                .map(Expense::getAmount)  // 각 Expense 객체의 amount를 가져옴
                .filter(amount -> amount != null)  // null 값이 있을 경우 필터링
                .reduce(BigDecimal.ZERO, BigDecimal::add);  // 합산, 초기값은 0
    }

    public Map<String, BigDecimal> categorizeExpensesByCategory(List<Expense> expenses) {
        Map<String, BigDecimal> categorizedExpenses = new HashMap<>();

        for (Expense expense : expenses) {
            String categoryName = expense.getCategoryName().getDescription();
            BigDecimal amount = expense.getAmount();

            // Accumulate the expenses for each category
            categorizedExpenses.put(categoryName, categorizedExpenses.getOrDefault(categoryName,  BigDecimal.ZERO).add(amount));
        }
        return categorizedExpenses;
    }
}