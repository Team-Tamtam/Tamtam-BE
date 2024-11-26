package tamtam.mooney.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.domain.entity.MonthlyBudget;
import tamtam.mooney.domain.entity.User;

import java.util.Optional;

public interface MonthlyBudgetRepository extends JpaRepository<MonthlyBudget, Long> {
    Optional<MonthlyBudget> findByUserAndPeriod(User user, String period);
}
