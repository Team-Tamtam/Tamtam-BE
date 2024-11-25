package tamtam.mooney.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.entity.Category;
import tamtam.mooney.entity.CategoryName;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByCategoryName(CategoryName categoryName);
}
