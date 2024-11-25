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
public class MonthlyReport extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long monthlyReportId;

    @Column(nullable = false)
    private YearMonth period; // 보고 기간 (연월)

    @Column(nullable = false)
    @NotNull
    private BigDecimal budgetAmount;

    @Column(nullable = false)
    @NotNull
    private BigDecimal totalExpenseAmount;

    @Column(nullable = false)
    @NotNull
    private BigDecimal totalIncomeAmount;

    @Enumerated(EnumType.STRING)
    @NotNull
    private BudgetStatus budgetStatus;

    @Column
    private String agentComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Builder
    public MonthlyReport(String period, BigDecimal budgetAmount, BigDecimal totalExpenseAmount, BigDecimal totalIncomeAmount,
                         BudgetStatus budgetStatus, String agentComment, User user) {
        this.period = YearMonth.parse(period);
        this.budgetAmount = budgetAmount;
        this.totalExpenseAmount = totalExpenseAmount;
        this.totalIncomeAmount = totalIncomeAmount;
        this.budgetStatus = budgetStatus;
        this.agentComment = agentComment;
        this.user = user;
    }

    public void update(BigDecimal budgetAmount, BigDecimal totalExpenseAmount,
                       BigDecimal totalIncomeAmount, String agentComment) {
        if (budgetAmount != null) {
            this.budgetAmount = budgetAmount;
        }
        if (totalExpenseAmount != null) {
            this.totalExpenseAmount = totalExpenseAmount;
        }
        if (totalIncomeAmount != null) {
            this.totalIncomeAmount = totalIncomeAmount;
        }
        if (agentComment != null && !agentComment.isEmpty()) {
            this.agentComment = agentComment;
        }
    }
}