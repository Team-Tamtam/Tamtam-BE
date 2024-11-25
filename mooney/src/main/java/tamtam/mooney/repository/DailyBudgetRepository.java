package tamtam.mooney.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tamtam.mooney.entity.DailyBudget;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DailyBudgetRepository extends JpaRepository<DailyBudget, Long> {

    @Query("SELECT db.budgetAmount FROM DailyBudget db WHERE db.date BETWEEN :startOfMonth AND :endOfMonth AND db.user.userId = :userId")
    BigDecimal findBudgetAmountByUserAndMonth(@Param("userId") Long userId,
                                              @Param("startOfMonth") LocalDate startOfMonth,
                                              @Param("endOfMonth") LocalDate endOfMonth);
}