package tamtam.mooney.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.entity.DailyBudget;

public interface DailyBudgetRepository extends JpaRepository<DailyBudget, Long> {
}
