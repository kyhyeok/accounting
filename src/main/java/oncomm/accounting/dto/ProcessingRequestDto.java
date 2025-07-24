package oncomm.accounting.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;

@Data
public class ProcessingRequestDto {
    @NotNull(message = "Bank transactions CSV file is required")
    private MultipartFile transactionsFile;

    @NotNull(message = "Rules JSON file is required")
    private MultipartFile rulesFile;
}