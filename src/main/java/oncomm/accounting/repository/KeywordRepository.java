package oncomm.accounting.repository;

import oncomm.accounting.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword, Long> {

    List<Keyword> findByCategory_CategoryId(String categoryId);

    @Query("SELECT k FROM Keyword k JOIN FETCH k.category cat JOIN FETCH cat.company WHERE k.keyword = :keyword")
    List<Keyword> findByKeywordWithCategoryAndCompany(@Param("keyword") String keyword);

    @Query("SELECT k FROM Keyword k JOIN FETCH k.category cat WHERE cat.company.companyId = :companyId")
    List<Keyword> findByCompanyId(@Param("companyId") String companyId);

    Optional<Keyword> findByKeywordAndCategory_CategoryId(String keyword, String categoryId);
}
