package oncomm.accounting.repository;

import oncomm.accounting.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByCompanyId(String companyId);

    boolean existsByCompanyId(String companyId);

    @Query("SELECT c FROM Company c LEFT JOIN FETCH c.categories cat LEFT JOIN FETCH cat.keywords WHERE c.companyId = :companyId")
    Optional<Company> findByCompanyIdWithCategoriesAndKeywords(@Param("companyId") String companyId);
}