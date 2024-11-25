package tamtam.mooney.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.entity.MonthlyBudget;
import tamtam.mooney.entity.User;

import java.util.Optional;

public interface MonthlyBudgetRepository extends JpaRepository<MonthlyBudget, Long> {
    Optional<MonthlyBudget> findByUserAndPeriod(User user, String period);
}
