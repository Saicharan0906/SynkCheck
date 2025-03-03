package com.rite.products.convertrite.respository;

import com.rite.products.convertrite.model.CrObjectInformationEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrObjectInfRepository extends JpaRepository<CrObjectInformationEntity, Long> {
    List<CrObjectInformationEntity> findAllByInfoTypeIn(List<String> infotype);
}
