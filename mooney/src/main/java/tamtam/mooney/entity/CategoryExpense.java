package tamtam.mooney.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import tamtam.mooney.global.common.BaseTimeEntity;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class CategoryExpense extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryExpenseId; // 월별 카테고리 지출 ID

    @Column
    private BigDecimal spentAmount; // 지출 금액

    @Column(precision = 5, scale = 2)
    private BigDecimal percentage; // 진척 예산 대비 비율

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monthly_report_id", nullable = false)
    @NotNull
    private MonthlyReport monthlyReport; // 월별 리포트 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Category category;

    @Builder
    public CategoryExpense(BigDecimal spentAmount, BigDecimal percentage,
                           MonthlyReport monthlyReport, Category category) {
        this.spentAmount = spentAmount;
        this.percentage = percentage;
        this.monthlyReport = monthlyReport;
        this.category = category;
    }
}
