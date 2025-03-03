package com.rite.products.convertrite.controller;

import com.rite.products.convertrite.model.CrSqlextractionBindVarDtls;
import com.rite.products.convertrite.po.BasicResponsePo;
import com.rite.products.convertrite.respository.CrSqlextractionBindVarDtlsRepository;
import com.rite.products.convertrite.service.CrQueryFilterService;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/convertritecore/filter")
@Slf4j
public class CrQueryFilterController {
    @Autowired
    CrQueryFilterService crQueryFilterService;
    @Autowired
    CrSqlextractionBindVarDtlsRepository sqlextractionBindVarDtlsRepository;
    @PostMapping("/findbindvar")
    public ResponseEntity<String> findingBindVariables() {
        try {
            crQueryFilterService.findingBindVariables();
            return ResponseEntity.ok("Bind variables processed successfully.");
        } catch (Exception e) {
            // Handling generic exceptions
            return handleGenericException(e);
        }
    }
    @ApiOperation(value = "This Api returns templates state")
    @GetMapping("/getbindvars")
    public ResponseEntity<?> getBindVarCols(@RequestParam("objectid") List<Long> ids) {
        try {
            return new ResponseEntity<>(crQueryFilterService.getBindVarCols(ids), HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(e.getCause(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    // Generic exception handler for unexpected exceptions
    private ResponseEntity<String> handleGenericException(Exception e) {
        return new ResponseEntity<>("An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); // 500
    }
    @PostMapping("/saveOrUpdate")
    public ResponseEntity<BasicResponsePo> saveOrUpdate(@RequestBody List<CrSqlextractionBindVarDtls> entity) {
        log.info("saveOrUpdate method called");
        BasicResponsePo response = new BasicResponsePo();
        try {
            List<CrSqlextractionBindVarDtls> savedEntity = crQueryFilterService.saveOrUpdate(entity);
            response.setPayload(savedEntity);
            response.setMessage("Successfully saved");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Unexpected error occurred", e);
            throw e;
        }
    }

}
