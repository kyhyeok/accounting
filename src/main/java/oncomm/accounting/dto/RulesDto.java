package oncomm.accounting.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RulesDto {
    private List<CompanyRule> companies;

    @Data
    public static class CompanyRule {
        @JsonProperty("company_id")
        private String companyId;

        @JsonProperty("company_name")
        private String companyName;

        private List<CategoryRule> categories;
    }

    @Data
    public static class CategoryRule {
        @JsonProperty("category_id")
        private String categoryId;

        @JsonProperty("category_name")
        private String categoryName;

        private List<String> keywords;
    }
}