package tamtam.mooney.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tamtam.mooney.domain.entity.CategoryBudget;
import tamtam.mooney.domain.entity.CategoryName;
import tamtam.mooney.domain.entity.User;

import java.math.BigDecimal;
import java.util.List;

public interface CategoryBudgetRepository extends JpaRepository<CategoryBudget, Long> {
    @Query("SELECT COALESCE(SUM(cb.amount), 0) FROM CategoryBudget cb " +
            "WHERE cb.user = :user " +
            "AND cb.categoryName IN :categoryNames " +
            "AND cb.period = :period")
    BigDecimal findTotalCategoryBudgetByUserAndPeriod(User user, List<CategoryName> categoryNames, String period);

    List<CategoryBudget> findByUserAndPeriod(User user, String period);
}
