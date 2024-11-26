package tamtam.mooney.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.domain.entity.Category;
import tamtam.mooney.domain.entity.CategoryName;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByCategoryName(CategoryName categoryName);
}
