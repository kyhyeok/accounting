package oncomm.accounting.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "bank_transaction")
@Getter
@Setter
@NoArgsConstructor
public class BankTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "deposit_amount")
    private Long depositAmount = 0L;

    @Column(name = "withdrawal_amount")
    private Long withdrawalAmount = 0L;

    @Column(name = "balance_after", nullable = false)
    private Long balanceAfter;

    @Column(name = "branch")
    private String branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "is_classified")
    private Boolean isClassified = false;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public BankTransaction(LocalDateTime transactionDate, String description,
                           Long depositAmount, Long withdrawalAmount,
                           Long balanceAfter, String branch) {
        this.transactionDate = transactionDate;
        this.description = description;
        this.depositAmount = depositAmount != null ? depositAmount : 0L;
        this.withdrawalAmount = withdrawalAmount != null ? withdrawalAmount : 0L;
        this.balanceAfter = balanceAfter;
        this.branch = branch;
    }
}