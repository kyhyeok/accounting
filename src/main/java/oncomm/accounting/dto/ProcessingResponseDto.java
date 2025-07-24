package oncomm.accounting.dto;

import lombok.Data;

@Data
public class ProcessingResponseDto {
    private int totalTransactions;
    private int classifiedTransactions;
    private int unclassifiedTransactions;
    private String message;

    public ProcessingResponseDto(int totalTransactions, int classifiedTransactions) {
        this.totalTransactions = totalTransactions;
        this.classifiedTransactions = classifiedTransactions;
        this.unclassifiedTransactions = totalTransactions - classifiedTransactions;
        this.message = String.format("Processing completed. %d/%d transactions classified.",
                classifiedTransactions, totalTransactions);
    }
}