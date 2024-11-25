package tamtam.mooney.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import tamtam.mooney.global.common.BaseTimeEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Expense extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long expenseId;

    @Column(nullable = false)
    private LocalDateTime transactionDateTime;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column
    private String paymentMethod;

    @Column
    private CategoryName categoryName;

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paymentMethodId", nullable = false)
    @NotNull
    private PaymentMethod paymentMethod; // 결제 수단 ID*/

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull
    private Category category; // 카테고리 ID*/

    @Builder
    public Expense(LocalDateTime transactionDateTime, BigDecimal amount, String description,
                   User user, String paymentMethod, CategoryName categoryName) {
        this.transactionDateTime = transactionDateTime;
        this.amount = amount;
        this.description = description;
        this.user = user;
        this.paymentMethod = paymentMethod;
        this.categoryName = categoryName;
    }
}