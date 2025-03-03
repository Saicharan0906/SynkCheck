package com.rite.products.convertrite.respository;

import com.rite.products.convertrite.model.CrQueryFilterCols;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueryBindVarColRepository extends JpaRepository<CrQueryFilterCols, Long> {

    List<CrQueryFilterCols> findAllByObjectIdIn(List<Long> ids);
}
