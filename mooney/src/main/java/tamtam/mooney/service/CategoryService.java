package tamtam.mooney.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tamtam.mooney.entity.Category;
import tamtam.mooney.entity.CategoryName;
import tamtam.mooney.repository.CategoryRepository;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @PostConstruct
    public void init() {
        if (categoryRepository.count() == 0) {
            for (CategoryName categoryName : CategoryName.values()) {
                categoryRepository.save(
                        Category.builder()
                                .categoryName(categoryName)
                                .icon(categoryName.getIcon())
                                .build()
                );
            }
        }
    }
}
