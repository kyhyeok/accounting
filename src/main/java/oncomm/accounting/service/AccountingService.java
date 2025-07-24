package oncomm.accounting.service;

import oncomm.accounting.dto.*;
import oncomm.accounting.entity.*;
import oncomm.accounting.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountingService {

    private final CompanyRepository companyRepository;
    private final CategoryRepository categoryRepository;
    private final KeywordRepository keywordRepository;
    private final BankTransactionRepository bankTransactionRepository;
    private final ObjectMapper objectMapper;

    /**
     * 자동 회계 처리 메인 로직
     */
    public ProcessingResponseDto processAccounting(MultipartFile transactionsFile, MultipartFile rulesFile) {
        try {
            // 1. 규칙 파일 파싱 및 저장
            RulesDto rules = parseRulesFile(rulesFile);
            saveRulesToDatabase(rules);

            // 2. 거래 내역 파싱
            List<BankTransactionDto> transactions = parseTransactionsFile(transactionsFile);

            // 3. 거래 내역 분류 및 저장
            int classifiedCount = classifyAndSaveTransactions(transactions, rules);

            return new ProcessingResponseDto(transactions.size(), classifiedCount);

        } catch (Exception e) {
            log.error("Error processing accounting data", e);
            throw new RuntimeException("Failed to process accounting data: " + e.getMessage());
        }
    }

    /**
     * 회사별 분류 결과 조회
     */
    @Transactional(readOnly = true)
    public Page<AccountingRecordDto> getAccountingRecords(String companyId, Pageable pageable) {
        if (companyId == null || companyId.trim().isEmpty()) {
            return bankTransactionRepository.findUnclassifiedRecords(pageable);
        }

        // 회사가 존재하는지 확인
        if (!companyRepository.existsByCompanyId(companyId)) {
            throw new IllegalArgumentException("Company not found: " + companyId);
        }

        return bankTransactionRepository.findAccountingRecordsByCompanyId(companyId, pageable);
    }

    /**
     * 규칙 파일 파싱
     */
    private RulesDto parseRulesFile(MultipartFile rulesFile) throws Exception {
        try (Reader reader = new InputStreamReader(rulesFile.getInputStream(), StandardCharsets.UTF_8)) {
            return objectMapper.readValue(reader, RulesDto.class);
        }
    }

    /**
     * 거래 내역 CSV 파싱
     */
    private List<BankTransactionDto> parseTransactionsFile(MultipartFile transactionsFile) throws Exception {
        List<BankTransactionDto> transactions = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (Reader reader = new InputStreamReader(transactionsFile.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {

            for (CSVRecord record : parser) {
                BankTransactionDto dto = new BankTransactionDto();
                dto.setTransactionDate(LocalDateTime.parse(record.get("거래일시"), formatter));
                dto.setDescription(record.get("적요"));
                dto.setDepositAmount(parseAmount(record.get("입금액")));
                dto.setWithdrawalAmount(parseAmount(record.get("출금액")));
                dto.setBalanceAfter(parseAmount(record.get("거래후잔액")));
                dto.setBranch(record.get("거래점"));

                transactions.add(dto);
            }
        }

        return transactions;
    }

    /**
     * 금액 파싱 (빈 값 처리)
     */
    private Long parseAmount(String amount) {
        if (amount == null || amount.trim().isEmpty()) {
            return 0L;
        }
        return Long.parseLong(amount.trim());
    }

    /**
     * 규칙을 데이터베이스에 저장
     */
    private void saveRulesToDatabase(RulesDto rules) {
        if (rules == null || rules.getCompanies() == null) {
            throw new IllegalArgumentException("Rules data is invalid or empty");
        }

        for (RulesDto.CompanyRule companyRule : rules.getCompanies()) {
            // 필수 필드 검증
            if (companyRule.getCompanyId() == null || companyRule.getCompanyId().trim().isEmpty()) {
                log.error("Company ID is null or empty for company: {}", companyRule.getCompanyName());
                throw new IllegalArgumentException("Company ID cannot be null or empty");
            }

            if (companyRule.getCompanyName() == null || companyRule.getCompanyName().trim().isEmpty()) {
                log.error("Company name is null or empty for company ID: {}", companyRule.getCompanyId());
                throw new IllegalArgumentException("Company name cannot be null or empty");
            }

            log.info("Processing company: {} ({})", companyRule.getCompanyName(), companyRule.getCompanyId());

            Company company = companyRepository.findByCompanyId(companyRule.getCompanyId())
                    .orElseGet(() -> new Company(companyRule.getCompanyId(), companyRule.getCompanyName()));

            company.setCompanyName(companyRule.getCompanyName());
            company = companyRepository.save(company);

            if (companyRule.getCategories() != null) {
                for (RulesDto.CategoryRule categoryRule : companyRule.getCategories()) {
                    // 카테고리 필수 필드 검증
                    if (categoryRule.getCategoryId() == null || categoryRule.getCategoryId().trim().isEmpty()) {
                        log.error("Category ID is null or empty for category: {}", categoryRule.getCategoryName());
                        continue; // 해당 카테고리 스킵
                    }

                    if (categoryRule.getCategoryName() == null || categoryRule.getCategoryName().trim().isEmpty()) {
                        log.error("Category name is null or empty for category ID: {}", categoryRule.getCategoryId());
                        continue; // 해당 카테고리 스킵
                    }

                    log.info("Processing category: {} ({})", categoryRule.getCategoryName(), categoryRule.getCategoryId());

                    Company finalCompany = company;
                    Category category = categoryRepository.findByCategoryId(categoryRule.getCategoryId())
                            .orElseGet(() -> new Category(categoryRule.getCategoryId(), categoryRule.getCategoryName(), finalCompany));

                    category.setCategoryName(categoryRule.getCategoryName());
                    category.setCompany(company);
                    category = categoryRepository.save(category);

                    // 기존 키워드 삭제 후 새로 추가
                    List<Keyword> existingKeywords = keywordRepository.findByCategory_CategoryId(category.getCategoryId());
                    if (!existingKeywords.isEmpty()) {
                        keywordRepository.deleteAll(existingKeywords);
                    }

                    // 키워드 추가
                    if (categoryRule.getKeywords() != null) {
                        for (String keywordText : categoryRule.getKeywords()) {
                            if (keywordText != null && !keywordText.trim().isEmpty()) {
                                Keyword keyword = new Keyword(keywordText.trim(), category);
                                keywordRepository.save(keyword);
                                log.debug("Added keyword: {} for category: {}", keywordText, category.getCategoryName());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 거래 내역 분류 및 저장
     */
    private int classifyAndSaveTransactions(List<BankTransactionDto> transactions, RulesDto rules) {
        // 키워드 매핑 캐시 생성
        Map<String, CategoryInfo> keywordToCategory = buildKeywordCache(rules);
        int classifiedCount = 0;

        for (BankTransactionDto dto : transactions) {
            // 중복 거래 확인
            if (bankTransactionRepository.existsByDescriptionAndTransactionDateAndBalanceAfter(
                    dto.getDescription(), dto.getTransactionDate(), dto.getBalanceAfter())) {
                continue;
            }

            BankTransaction transaction = new BankTransaction(
                    dto.getTransactionDate(),
                    dto.getDescription(),
                    dto.getDepositAmount(),
                    dto.getWithdrawalAmount(),
                    dto.getBalanceAfter(),
                    dto.getBranch()
            );

            // 키워드 매칭을 통한 분류
            CategoryInfo categoryInfo = classifyTransaction(dto.getDescription(), keywordToCategory);
            if (categoryInfo != null) {
                Company company = companyRepository.findByCompanyId(categoryInfo.companyId).orElse(null);
                Category category = categoryRepository.findByCategoryId(categoryInfo.categoryId).orElse(null);

                if (company != null && category != null) {
                    transaction.setCompany(company);
                    transaction.setCategory(category);
                    transaction.setIsClassified(true);
                    classifiedCount++;
                }
            }

            bankTransactionRepository.save(transaction);
        }

        return classifiedCount;
    }

    /**
     * 키워드 캐시 구성
     */
    private Map<String, CategoryInfo> buildKeywordCache(RulesDto rules) {
        Map<String, CategoryInfo> cache = new HashMap<>();

        for (RulesDto.CompanyRule company : rules.getCompanies()) {
            for (RulesDto.CategoryRule category : company.getCategories()) {
                for (String keyword : category.getKeywords()) {
                    cache.put(keyword.toLowerCase(),
                            new CategoryInfo(company.getCompanyId(), category.getCategoryId()));
                }
            }
        }

        return cache;
    }

    /**
     * 거래 내역 분류 로직
     */
    private CategoryInfo classifyTransaction(String description, Map<String, CategoryInfo> keywordToCategory) {
        if (description == null || description.trim().isEmpty()) {
            return null;
        }

        String lowerDescription = description.toLowerCase();

        // 키워드 매칭 (가장 긴 키워드 우선)
        return keywordToCategory.entrySet().stream()
                .filter(entry -> lowerDescription.contains(entry.getKey()))
                .max(Comparator.comparing(entry -> entry.getKey().length()))
                .map(Map.Entry::getValue)
                .orElse(null);
    }

    /**
     * 카테고리 정보 내부 클래스
     */
    private static class CategoryInfo {
        final String companyId;
        final String categoryId;

        CategoryInfo(String companyId, String categoryId) {
            this.companyId = companyId;
            this.categoryId = categoryId;
        }
    }

    /**
     * 회사 통계 조회
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCompanyStatistics(String companyId) {
        long totalCount = bankTransactionRepository.countByCompanyId(companyId);
        long classifiedCount = bankTransactionRepository.countClassifiedByCompanyId(companyId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("companyId", companyId);
        stats.put("totalTransactions", totalCount);
        stats.put("classifiedTransactions", classifiedCount);
        stats.put("unclassifiedTransactions", totalCount - classifiedCount);
        stats.put("classificationRate", totalCount > 0 ? (double) classifiedCount / totalCount * 100 : 0.0);

        return stats;
    }

    /**
     * 전체 회사 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CompanyDto> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(this::convertToCompanyDto)
                .collect(Collectors.toList());
    }

    /**
     * Company Entity를 DTO로 변환
     */
    private CompanyDto convertToCompanyDto(Company company) {
        CompanyDto dto = new CompanyDto();
        dto.setId(company.getId());
        dto.setCompanyId(company.getCompanyId());
        dto.setCompanyName(company.getCompanyName());
        dto.setCreatedAt(company.getCreatedAt());
        dto.setUpdatedAt(company.getUpdatedAt());

        List<CategoryDto> categoryDtos = company.getCategories().stream()
                .map(this::convertToCategoryDto)
                .collect(Collectors.toList());
        dto.setCategories(categoryDtos);

        return dto;
    }

    /**
     * Category Entity를 DTO로 변환
     */
    private CategoryDto convertToCategoryDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setCategoryId(category.getCategoryId());
        dto.setCategoryName(category.getCategoryName());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());

        List<String> keywords = category.getKeywords().stream()
                .map(Keyword::getKeyword)
                .collect(Collectors.toList());
        dto.setKeywords(keywords);

        return dto;
    }
}