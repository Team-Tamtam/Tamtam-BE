package tamtam.mooney.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.entity.CategoryName;
import tamtam.mooney.entity.Expense;
import tamtam.mooney.entity.User;
import tamtam.mooney.repository.ExpenseRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final UserService userService;

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

    @PostConstruct
    public void initExpense() {
        User user = userService.getCurrentUser();
        // 값이 있으면 초기화 작업을 하지 않음
        if (expenseRepository.count() > 0) {
            return;
        }

        // `Expense` 데이터 삽입
        Expense expense1 = Expense.builder()
                .transactionDateTime(LocalDateTime.now().minusDays(1))
                .amount(BigDecimal.valueOf(5000))
                .description("식사")
                .user(user)
                .paymentMethod("신용카드")
                .categoryName(CategoryName.FOOD)
                .build();

        Expense expense2 = Expense.builder()
                .transactionDateTime(LocalDateTime.now().minusDays(2))
                .amount(BigDecimal.valueOf(10000))
                .description("교통비")
                .user(user)
                .paymentMethod("체크카드")
                .categoryName(CategoryName.TRANSPORT)
                .build();

        Expense expense3 = Expense.builder()
                .transactionDateTime(LocalDateTime.now().minusDays(3))
                .amount(BigDecimal.valueOf(5000))
                .description("학습 자료 구입")
                .user(user)
                .paymentMethod("카카오페이")
                .categoryName(CategoryName.EDUCATION)
                .build();

        Expense expense4 = Expense.builder()
                .transactionDateTime(LocalDateTime.now().minusDays(5))
                .amount(BigDecimal.valueOf(2000))
                .description("영화 관람")
                .user(user)
                .paymentMethod("현금")
                .categoryName(CategoryName.ENTERTAINMENT)
                .build();

        expenseRepository.save(expense1);
        expenseRepository.save(expense2);
        expenseRepository.save(expense3);
        expenseRepository.save(expense4);
    }
}