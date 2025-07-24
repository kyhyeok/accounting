package oncomm.accounting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BankTransactionDto {
    @JsonProperty("거래일시")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transactionDate;

    @JsonProperty("적요")
    private String description;

    @JsonProperty("입금액")
    private Long depositAmount;

    @JsonProperty("출금액")
    private Long withdrawalAmount;

    @JsonProperty("거래후잔액")
    private Long balanceAfter;

    @JsonProperty("거래점")
    private String branch;
}
