package tamtam.mooney.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import tamtam.mooney.global.common.BaseTimeEntity;

import java.math.BigDecimal;
import java.time.YearMonth;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class MonthlyBudget extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long monthlyBudgetId;

    @Column(nullable = false, length = 7)
    private String period;

    @Column(nullable = false)
    @NotNull
    private BigDecimal initialAmount;

    @Column(nullable = false)
    @NotNull
    private BigDecimal finalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Builder
    public MonthlyBudget(String period, BigDecimal initialAmount, BigDecimal finalAmount, User user) {
        this.period = period;
        this.initialAmount = initialAmount;
        this.finalAmount = finalAmount;
        this.user = user;
    }
}