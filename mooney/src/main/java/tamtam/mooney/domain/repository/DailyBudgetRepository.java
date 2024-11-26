package tamtam.mooney.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.domain.entity.DailyBudget;

public interface DailyBudgetRepository extends JpaRepository<DailyBudget, Long> {
}
