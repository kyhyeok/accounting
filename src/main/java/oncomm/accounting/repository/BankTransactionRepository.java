package oncomm.accounting.repository;

import oncomm.accounting.dto.AccountingRecordDto;
import oncomm.accounting.entity.BankTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {

    List<BankTransaction> findByCompany_CompanyId(String companyId);

    List<BankTransaction> findByIsClassified(Boolean isClassified);

    @Query("SELECT new oncomm.accounting.dto.AccountingRecordDto(" +
            "bt.id, bt.transactionDate, bt.description, bt.depositAmount, bt.withdrawalAmount, " +
            "bt.balanceAfter, bt.branch, c.companyId, c.companyName, cat.categoryId, cat.categoryName, bt.isClassified) " +
            "FROM BankTransaction bt " +
            "LEFT JOIN bt.company c " +
            "LEFT JOIN bt.category cat " +
            "WHERE c.companyId = :companyId " +
            "ORDER BY bt.transactionDate DESC")
    Page<AccountingRecordDto> findAccountingRecordsByCompanyId(@Param("companyId") String companyId, Pageable pageable);

    @Query("SELECT new oncomm.accounting.dto.AccountingRecordDto(" +
            "bt.id, bt.transactionDate, bt.description, bt.depositAmount, bt.withdrawalAmount, " +
            "bt.balanceAfter, bt.branch, c.companyId, c.companyName, cat.categoryId, cat.categoryName, bt.isClassified) " +
            "FROM BankTransaction bt " +
            "LEFT JOIN bt.company c " +
            "LEFT JOIN bt.category cat " +
            "WHERE bt.isClassified = false " +
            "ORDER BY bt.transactionDate DESC")
    Page<AccountingRecordDto> findUnclassifiedRecords(Pageable pageable);

    @Query("SELECT COUNT(bt) FROM BankTransaction bt WHERE bt.company.companyId = :companyId")
    long countByCompanyId(@Param("companyId") String companyId);

    @Query("SELECT COUNT(bt) FROM BankTransaction bt WHERE bt.company.companyId = :companyId AND bt.isClassified = true")
    long countClassifiedByCompanyId(@Param("companyId") String companyId);

    List<BankTransaction> findByTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT CASE WHEN COUNT(bt) > 0 THEN true ELSE false END " +
            "FROM BankTransaction bt " +
            "WHERE bt.description = :description " +
            "AND bt.transactionDate = :transactionDate " +
            "AND bt.balanceAfter = :balanceAfter")
    boolean existsByDescriptionAndTransactionDateAndBalanceAfter(
            @Param("description") String description,
            @Param("transactionDate") LocalDateTime transactionDate,
            @Param("balanceAfter") Long balanceAfter
    );
}