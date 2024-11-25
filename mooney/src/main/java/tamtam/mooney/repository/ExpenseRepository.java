package tamtam.mooney.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tamtam.mooney.entity.Expense;
import tamtam.mooney.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
            "WHERE e.user = :user AND e.transactionDateTime BETWEEN :startOfMonth AND :endOfMonth")
    BigDecimal findTotalExpenseAmountByUserAndMonth(User user, LocalDate startOfMonth, LocalDate endOfMonth);
    List<Expense> findByUserAndTransactionDateTimeBetween(User user, LocalDate startDate, LocalDate endDate);
}