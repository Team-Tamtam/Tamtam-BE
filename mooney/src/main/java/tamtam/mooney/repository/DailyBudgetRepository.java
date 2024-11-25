package tamtam.mooney.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tamtam.mooney.entity.DailyBudget;
import tamtam.mooney.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DailyBudgetRepository extends JpaRepository<DailyBudget, Long> {

    @Query("SELECT db.budgetAmount FROM DailyBudget db WHERE db.createdAt BETWEEN :startOfMonth AND :endOfMonth AND db.user.userId = :userId")
    BigDecimal findBudgetAmountByUserAndMonth(Long userId, LocalDate startOfMonth, LocalDate endOfMonthd);
}