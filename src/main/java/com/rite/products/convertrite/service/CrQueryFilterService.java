package com.rite.products.convertrite.service;

import com.rite.products.convertrite.exception.ResourceNotFoundException;
import com.rite.products.convertrite.model.CrObjectInformationEntity;
import com.rite.products.convertrite.model.CrQueryFilterCols;
import com.rite.products.convertrite.model.CrSqlextractionBindVarDtls;
import com.rite.products.convertrite.respository.CrObjectInfRepository;
import com.rite.products.convertrite.respository.CrSqlextractionBindVarDtlsRepository;
import com.rite.products.convertrite.respository.QueryBindVarColRepository;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CrQueryFilterService {
    @Autowired
    CrObjectInfRepository crObjectInfRepository;
    @Autowired
    QueryBindVarColRepository queryBindVarColRepository;
    @Autowired
    CrSqlextractionBindVarDtlsRepository bindVarDtlsRepository;

    public void findingBindVariables() {
        log.info("findingBindVariables method");
        Set<CrQueryFilterCols> crQueryFilterCols = new HashSet<>();
        List<String> infotype = new ArrayList<>();
        infotype.add("SQL_EXTRACTION_QUERY");
        infotype.add("AZURE_EXTRACTION_QUERY");
        List<CrObjectInformationEntity> crObjectInformationEntityList = crObjectInfRepository.findAllByInfoTypeIn(infotype);
        // Regular expression to extract bind variables (without the colon)
        String regex = "=:(\\w+)";
        // Prepare to collect bind variables
        Pattern pattern = Pattern.compile(regex);
        for (CrObjectInformationEntity crObjectInformation : crObjectInformationEntityList) {
            Long objectId = crObjectInformation.getObjectId();
            String queryText = crObjectInformation.getInfoValue();
            // CrQueryFilterCols crQueryFilterCol = new CrQueryFilterCols();

            try {
                // Find all bind variables in the query

                Matcher matcher = pattern.matcher(queryText);
                while (matcher.find()) {
                    CrQueryFilterCols crQueryFilterCol = new CrQueryFilterCols();
                    crQueryFilterCol.setBindVarColName(matcher.group(1));
                    crQueryFilterCol.setObjectId(objectId);
                    crQueryFilterCols.add(crQueryFilterCol);
                }
            } catch (Exception e) {
                log.info(String.valueOf(objectId));
            }

        }
        queryBindVarColRepository.saveAll(crQueryFilterCols);
    }

    public Object getBindVarCols(List<Long> ids) {
        return queryBindVarColRepository.findAllByObjectIdIn(ids);
    }

    @Transactional
    public List<CrSqlextractionBindVarDtls> saveOrUpdate(List<CrSqlextractionBindVarDtls> entities) {
        log.info("saveOrUpdate method for list of entities");

        List<CrSqlextractionBindVarDtls> entitiesToSave = new ArrayList<>();

        for (CrSqlextractionBindVarDtls entity : entities) {
            if (entity.getBindVariableId() != null) {
                CrSqlextractionBindVarDtls existingEntity = bindVarDtlsRepository.findById(entity.getBindVariableId())
                        .orElseThrow(() -> new ResourceNotFoundException("Entity not found with id " + entity.getBindVariableId()));
                // Update existing entity
                entity.setLastUpdateDate(new Date());
                updateEntity(existingEntity, entity);
                entitiesToSave.add(existingEntity);
            } else {
                entity.setLastUpdateDate(new Date());
                entity.setCreationDate(new Date());
                // Prepare new entity for saving
                entitiesToSave.add(entity);
            }
        }

        return bindVarDtlsRepository.saveAll(entitiesToSave);
    }

    private void updateEntity(CrSqlextractionBindVarDtls existingEntity, CrSqlextractionBindVarDtls newEntity) {
        log.info("updateEntity method for entity id: {}", existingEntity.getBindVariableId());
        existingEntity.setModule(newEntity.getModule());
        existingEntity.setParentObjectId(newEntity.getParentObjectId());
        existingEntity.setObjectId(newEntity.getObjectId());
        existingEntity.setTemplateName(newEntity.getTemplateName());
        existingEntity.setCldTemplateId(newEntity.getCldTemplateId());
        existingEntity.setSrcTemplateId(newEntity.getSrcTemplateId());
        existingEntity.setBatchName(newEntity.getBatchName());
        existingEntity.setBindVariable(newEntity.getBindVariable());
        existingEntity.setBindVariableValue(newEntity.getBindVariableValue());
        existingEntity.setBindWhereClob(newEntity.getBindWhereClob());
        existingEntity.setAttribute1(newEntity.getAttribute1());
        existingEntity.setAttribute2(newEntity.getAttribute2());
        existingEntity.setAttribute3(newEntity.getAttribute3());
        existingEntity.setAttribute4(newEntity.getAttribute4());
        existingEntity.setAttribute5(newEntity.getAttribute5());
        existingEntity.setLastUpdatedBy(newEntity.getLastUpdatedBy());
        existingEntity.setLastUpdateDate(new Date());

    }

    @Transactional
    public List<CrSqlextractionBindVarDtls> findAll() {
        log.info("findAll method");
        return bindVarDtlsRepository.findAll();
    }

    @Transactional
    public void deleteById(Long id) {
        log.info("deleteById method");
        if (!bindVarDtlsRepository.existsById(id)) {
            throw new EntityNotFoundException("Entity not found with id " + id);
        }
        bindVarDtlsRepository.deleteById(id);
    }
}
