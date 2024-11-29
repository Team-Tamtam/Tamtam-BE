package tamtam.mooney.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tamtam.mooney.domain.entity.Expense;
import tamtam.mooney.domain.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
            "WHERE e.user = :user AND e.transactionDateTime BETWEEN :startDateTime AND :endDateTime")
    BigDecimal findTotalExpenseAmountByUserAndMonth(@Param("user") User user,
                                                    @Param("startDateTime") LocalDateTime startDateTime,
                                                    @Param("endDateTime") LocalDateTime endDateTime);
    List<Expense> findByUserAndTransactionDateTimeBetween(User user, LocalDateTime startDateTime, LocalDateTime endDateTime);
}
