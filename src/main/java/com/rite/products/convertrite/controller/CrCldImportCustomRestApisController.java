package com.rite.products.convertrite.controller;

import com.rite.products.convertrite.exception.ConvertRiteException;
import com.rite.products.convertrite.po.AllowedColumns;
import com.rite.products.convertrite.po.CustomRestApiReqPo;
import com.rite.products.convertrite.service.BankAccountErrorService;
import com.rite.products.convertrite.service.CrCldImportCustomRestApisServiceImpl;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Set;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/convertritecore/cloudimport")
public class CrCldImportCustomRestApisController {

    private static final Logger log = LoggerFactory.getLogger(CrCldImportCustomRestApisController.class);
    @Autowired
    CrCldImportCustomRestApisServiceImpl cldImportCustomRestApisServiceImpl;
    @Autowired
    private BankAccountErrorService bankAccountErrorService;

    @ApiOperation(value = "This api is for creating bank & branches")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful Response"), @ApiResponse(code = 500, message = "Server Side Error"), @ApiResponse(code = 400, message = "Bad Request")})
    @PostMapping("/createbankandbranches")
    public ResponseEntity<?> createBankAndBranches(@RequestBody CustomRestApiReqPo customRestApiReqPo) throws Exception {
        cldImportCustomRestApisServiceImpl.createBankAndBranches(customRestApiReqPo);
        return new ResponseEntity<>("successful", HttpStatus.OK);
    }

    @ApiOperation(value = "This api is for creating banks")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful Response"), @ApiResponse(code = 500, message = "Server Side Error"), @ApiResponse(code = 400, message = "Bad Request")})
    @PostMapping("/createbank")
    public ResponseEntity<?> createOrUpdateBank(@RequestBody @Valid CustomRestApiReqPo customRestApiReqPo) throws Exception {
        // Validate and sanitize the input at the controller level
         String cloudUrl = validateAndSanitizeInput(customRestApiReqPo);
        cldImportCustomRestApisServiceImpl.createOrUpdateBank(customRestApiReqPo);
        return new ResponseEntity<>("successful", HttpStatus.OK);
    }

    @ApiOperation(value = "This api is for creating branches")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful Response"), @ApiResponse(code = 500, message = "Server Side Error"), @ApiResponse(code = 400, message = "Bad Request")})
    @PostMapping("/createbranch")
    public ResponseEntity<?> createOrUpdateBranch(@RequestBody CustomRestApiReqPo customRestApiReqPo) throws Exception {
        cldImportCustomRestApisServiceImpl.createOrUpdateBranch(customRestApiReqPo);
        return new ResponseEntity<>("successful", HttpStatus.OK);
    }

    @ApiOperation(value = "This api is for creating bank account")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful Response"), @ApiResponse(code = 500, message = "Server Side Error"), @ApiResponse(code = 400, message = "Bad Request")})
    @PostMapping("/createbankaccount")
    public ResponseEntity<?> createOrUpdateBankAccount(@RequestBody CustomRestApiReqPo customRestApiReqPo) throws Exception {
        cldImportCustomRestApisServiceImpl.createOrUpdateBankAccount(customRestApiReqPo);
        return new ResponseEntity<>("successful", HttpStatus.OK);
    }

    @ApiOperation(value = "This api is for updating project DFF fields")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful Response"), @ApiResponse(code = 500, message = "Server Side Error"), @ApiResponse(code = 400, message = "Bad Request")})
    @PostMapping("/updateprojectdff")
    public ResponseEntity<?> updateProjectDff(@RequestBody CustomRestApiReqPo customRestApiReqPo) throws Exception {
        cldImportCustomRestApisServiceImpl.updateProjectDff(customRestApiReqPo);
        return new ResponseEntity<>("successful", HttpStatus.OK);
    }

    @ApiOperation(value = "This api is for tax registrations")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful Response"), @ApiResponse(code = 500, message = "Server Side Error"), @ApiResponse(code = 400, message = "Bad Request")})
    @PostMapping("/supplierTaxProfileUpdate")
    public ResponseEntity<?> supplierTaxProfileUpdate(@RequestBody CustomRestApiReqPo customRestApiReqPo) throws Exception {
        cldImportCustomRestApisServiceImpl.supplierTaxProfileUpdate(customRestApiReqPo);
        return new ResponseEntity<>("successful", HttpStatus.OK);
    }

    @ApiOperation("This Api is for validate ccid")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful Response"),
            @ApiResponse(code = 500, message = "Server Side Error")})
    @PostMapping("/validateccid")
    public ResponseEntity<?> validateCcid(@RequestBody CustomRestApiReqPo customRestApiReqPo)
            throws Exception {

        String ccidColumn = validateColumnName(customRestApiReqPo.getCcidColumnName());
        String ledgerColumn = validateColumnName(customRestApiReqPo.getLedgerColumnName());
        log.info("ccidColumn:" + ccidColumn + "ledgerColumn:" + ledgerColumn);
        cldImportCustomRestApisServiceImpl.validateCcid(customRestApiReqPo);
        return new ResponseEntity<String>("successful", HttpStatus.OK);
    }

    private static String validateColumnName(String columnName) {
        try {
            return AllowedColumns.valueOf(columnName).name();
        } catch (IllegalArgumentException e) {
            throw new SecurityException("Invalid column name: " + columnName);
        }
    }

    @ApiOperation("This Api updates Bank Number,Branch Number,Bank Account Id")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful Response"),
            @ApiResponse(code = 500, message = "Server Side Error")})
    @PostMapping("/update/{updateType}")
    public ResponseEntity<?> updateCldStagingTable(@PathVariable String updateType, @RequestBody CustomRestApiReqPo customRestApiReqPo)
            throws Exception {
        cldImportCustomRestApisServiceImpl.updateCldStagingTable(updateType, customRestApiReqPo);
        return new ResponseEntity<String>("successful", HttpStatus.OK);
    }

    @GetMapping("/download-bank-account-error-records")
    public ResponseEntity<byte[]> downloadBankAccountErrorRecords(@RequestParam Long cldTempId, @RequestParam String batchName) {
        try {
            byte[] csvData = bankAccountErrorService.downloadBankAccountErrorRecords(cldTempId, batchName);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "text/csv");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bank_account_errors.csv");
            headers.setContentLength(csvData.length);

            return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/download-bank-or-branches-error-records")
    public ResponseEntity<byte[]> downloadBankOrBranchesErrorRecords(@RequestParam Long cldTempId, @RequestParam String batchName) {
        try {
            byte[] csvData = bankAccountErrorService.downloadBankOrBranchesErrorRecords(cldTempId, batchName);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "text/csv");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bank_errors.csv");
            headers.setContentLength(csvData.length);

            return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String validateAndSanitizeInput(CustomRestApiReqPo customRestApiReqPo) {
        // Validate and sanitize the cloudUrl
        String cloudUrl = customRestApiReqPo.getCloudUrl();
        if (cloudUrl == null || cloudUrl.isBlank()) {
            throw new IllegalArgumentException("Cloud URL cannot be null or blank");
        }

        try {
            URI uri = new URI(cloudUrl);

            // Allow only HTTPS protocol
            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                throw new IllegalArgumentException("Only HTTPS URLs are allowed: " + cloudUrl);
            }

            // Validate the host against a whitelist of allowed domains
            String host = uri.getHost();
            if (!isAllowedHost(host)) {
                throw new IllegalArgumentException("Access to the specified host is not allowed: " + host);
            }

            // Validate the IP address to prevent internal/private IP access
            validateIpAddress(host);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Malformed URL: " + cloudUrl, e);
        }

        if (cloudUrl.isBlank()) {
            throw new IllegalArgumentException("Bank Cloud URL cannot be null or blank");
        }

        if (!cloudUrl.startsWith("/") || cloudUrl.contains("..")) {
            throw new IllegalArgumentException("Invalid Bank Cloud URL: " + cloudUrl);
        }
        return cloudUrl;
    }

    private boolean isAllowedHost(String host) {
        // Define a whitelist of allowed hosts
        Set<String> allowedHosts = Set.of("trusted-domain.com", "api.trusted-domain.com");
        return allowedHosts.contains(host);
    }

    private void validateIpAddress(String host) {
        try {
            InetAddress inetAddress = InetAddress.getByName(host);
            if (inetAddress.isAnyLocalAddress() || inetAddress.isLoopbackAddress() || inetAddress.isSiteLocalAddress()) {
                throw new IllegalArgumentException("Access to internal or private IP addresses is not allowed: " + host);
            }
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid host: " + host, e);
        }
    }
}
