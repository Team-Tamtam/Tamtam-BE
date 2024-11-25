package tamtam.mooney.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
