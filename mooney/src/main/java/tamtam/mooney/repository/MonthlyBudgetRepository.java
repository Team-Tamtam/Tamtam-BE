package tamtam.mooney.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.entity.MonthlyBudget;

import java.util.Optional;

public interface MonthlyBudgetRepository extends JpaRepository<MonthlyBudget, Long> {
    Optional<MonthlyBudget> findByUserIdAndPeriod(Long userId, String period);
}
