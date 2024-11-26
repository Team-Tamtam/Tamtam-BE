package tamtam.mooney.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import tamtam.mooney.global.common.BaseTimeEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class DailyBudget extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long budgetId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal budgetAmount;

    @Column
    private BigDecimal spentAmount;

    @Column
    private BigDecimal remainingAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public DailyBudget(LocalDate date, BigDecimal budgetAmount, BigDecimal spentAmount, BigDecimal remainingAmount, User user) {
        this.date = date;
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
