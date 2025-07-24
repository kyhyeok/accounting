package oncomm.accounting.controller;

import oncomm.accounting.dto.*;
import oncomm.accounting.service.AccountingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/accounting")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AccountingController {

    private final AccountingService accountingService;

    /**
     * 자동 회계 처리 API
     * POST /api/v1/accounting/process
     */
    @PostMapping("/process")
    public ResponseEntity<ProcessingResponseDto> processAccounting(
            @RequestParam("transactionsFile") MultipartFile transactionsFile,
            @RequestParam("rulesFile") MultipartFile rulesFile) {

        try {
            // 파일 유효성 검사
            validateFiles(transactionsFile, rulesFile);

            ProcessingResponseDto response = accountingService.processAccounting(transactionsFile, rulesFile);

            log.info("Accounting processing completed: {}", response.getMessage());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error processing accounting data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal server error occurred"));
        }
    }

    /**
     * 사업체별 분류 결과 조회 API
     * GET /api/v1/accounting/records?companyId=...&page=0&size=20&sort=transactionDate,desc
     */
    @GetMapping("/records")
    public ResponseEntity<Page<AccountingRecordDto>> getAccountingRecords(
            @RequestParam(required = false) String companyId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            // 정렬 방향 설정
            Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ?
                    Sort.Direction.ASC : Sort.Direction.DESC;

            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<AccountingRecordDto> records = accountingService.getAccountingRecords(companyId, pageable);

            return ResponseEntity.ok(records);

        } catch (IllegalArgumentException e) {
            log.error("Invalid company ID: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error retrieving accounting records", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 회사별 통계 조회 API
     * GET /api/v1/accounting/statistics/{companyId}
     */
    @GetMapping("/statistics/{companyId}")
    public ResponseEntity<Map<String, Object>> getCompanyStatistics(@PathVariable String companyId) {
        try {
            Map<String, Object> statistics = accountingService.getCompanyStatistics(companyId);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error retrieving company statistics for: {}", companyId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 전체 회사 목록 조회 API
     * GET /api/v1/accounting/companies
     */
    @GetMapping("/companies")
    public ResponseEntity<List<CompanyDto>> getAllCompanies() {
        try {
            List<CompanyDto> companies = accountingService.getAllCompanies();
            return ResponseEntity.ok(companies);
        } catch (Exception e) {
            log.error("Error retrieving companies", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 미분류 거래 내역 조회 API
     * GET /api/v1/accounting/unclassified?page=0&size=20
     */
    @GetMapping("/unclassified")
    public ResponseEntity<Page<AccountingRecordDto>> getUnclassifiedRecords(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {

        try {
            Pageable pageable = PageRequest.of(page, size,
                    Sort.by(Sort.Direction.DESC, "transactionDate"));

            Page<AccountingRecordDto> records = accountingService.getAccountingRecords(null, pageable);
            return ResponseEntity.ok(records);

        } catch (Exception e) {
            log.error("Error retrieving unclassified records", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 파일 유효성 검사
     */
    private void validateFiles(MultipartFile transactionsFile, MultipartFile rulesFile) {
        if (transactionsFile == null || transactionsFile.isEmpty()) {
            throw new IllegalArgumentException("Bank transactions CSV file is required");
        }

        if (rulesFile == null || rulesFile.isEmpty()) {
            throw new IllegalArgumentException("Rules JSON file is required");
        }

        // 파일 확장자 검사
        String transactionsFileName = transactionsFile.getOriginalFilename();
        if (transactionsFileName == null || !transactionsFileName.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("Transactions file must be a CSV file");
        }

        String rulesFileName = rulesFile.getOriginalFilename();
        if (rulesFileName == null || !rulesFileName.toLowerCase().endsWith(".json")) {
            throw new IllegalArgumentException("Rules file must be a JSON file");
        }

        // 파일 크기 검사 (10MB 제한)
        long maxFileSize = 10 * 1024 * 1024; // 10MB
        if (transactionsFile.getSize() > maxFileSize || rulesFile.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size must not exceed 10MB");
        }
    }

    /**
     * 에러 응답 생성
     */
    private ProcessingResponseDto createErrorResponse(String message) {
        ProcessingResponseDto response = new ProcessingResponseDto(0, 0);
        response.setMessage(message);
        return response;
    }
}
