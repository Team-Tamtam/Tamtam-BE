package tamtam.mooney.entity;

import jakarta.persistence.*;
import lombok.*;
import tamtam.mooney.global.common.BaseTimeEntity;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class DailyBudget extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long budgetId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal budgetAmount;

    @Column
    private BigDecimal spentAmount;

    @Column
    private BigDecimal remainingAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 사용자 ID

    @Builder
    public DailyBudget(BigDecimal budgetAmount, BigDecimal spentAmount, BigDecimal remainingAmount, User user) {
        this.budgetAmount = budgetAmount;
        this.spentAmount = spentAmount;
        this.remainingAmount = remainingAmount;
        this.user = user;
    }

    public void updateSpentAmount(BigDecimal amount) {
        this.spentAmount = amount;
        this.remainingAmount = this.budgetAmount.subtract(amount);
    }
}