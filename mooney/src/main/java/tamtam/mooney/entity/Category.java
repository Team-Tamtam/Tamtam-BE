package tamtam.mooney.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import tamtam.mooney.global.common.BaseTimeEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Category extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long categoryId;

    @Enumerated(EnumType.STRING)
    @NotNull
    private CategoryName categoryName;

    @Column(nullable = false)
    private String icon; // 아이콘

    @Builder
    public Category(CategoryName categoryName, String icon) {
        this.categoryName = categoryName;
        this.icon = icon;
    }

    public enum CategoryName {
        FOOD,
        TRANSPORT
    }
}