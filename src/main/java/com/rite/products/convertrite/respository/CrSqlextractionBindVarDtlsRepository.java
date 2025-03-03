package com.rite.products.convertrite.respository;

import com.rite.products.convertrite.model.CrSqlextractionBindVarDtls;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CrSqlextractionBindVarDtlsRepository extends JpaRepository<CrSqlextractionBindVarDtls, Long> {

    @Query("SELECT c FROM CrSqlextractionBindVarDtls c WHERE c.batchName = :batchName AND (c.srcTemplateId IS NULL OR c.srcTemplateId = :srcTemplateId)")
    List<CrSqlextractionBindVarDtls> findByBatchNameAndSrcTemplateId(@Param("batchName") String batchName, @Param("srcTemplateId") Long srcTemplateId);

}
