package oncomm.accounting.repository;

import oncomm.accounting.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByCategoryId(String categoryId);

    List<Category> findByCompany_CompanyId(String companyId);

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.keywords WHERE c.company.companyId = :companyId")
    List<Category> findByCompanyIdWithKeywords(@Param("companyId") String companyId);

    boolean existsByCategoryId(String categoryId);
}