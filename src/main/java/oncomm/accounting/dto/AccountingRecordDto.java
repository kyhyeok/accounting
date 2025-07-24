package oncomm.accounting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AccountingRecordDto {
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transactionDate;

    private String description;
    private Long depositAmount;
    private Long withdrawalAmount;
    private Long balanceAfter;
    private String branch;
    private String companyId;
    private String companyName;
    private String categoryId;
    private String categoryName;
    private Boolean isClassified;

    public AccountingRecordDto(Long id, LocalDateTime transactionDate, String description,
                               Long depositAmount, Long withdrawalAmount, Long balanceAfter,
                               String branch, String companyId, String companyName,
                               String categoryId, String categoryName, Boolean isClassified) {
        this.id = id;
        this.transactionDate = transactionDate;
        this.description = description;
        this.depositAmount = depositAmount;
        this.withdrawalAmount = withdrawalAmount;
        this.balanceAfter = balanceAfter;
        this.branch = branch;
        this.companyId = companyId;
        this.companyName = companyName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.isClassified = isClassified;
    }
}
