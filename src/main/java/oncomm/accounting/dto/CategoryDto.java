package oncomm.accounting.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CategoryDto {
    private Long id;
    private String categoryId;
    private String categoryName;
    private List<String> keywords;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}