package oncomm.accounting.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CompanyDto {
    private Long id;
    private String companyId;
    private String companyName;
    private List<CategoryDto> categories;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}