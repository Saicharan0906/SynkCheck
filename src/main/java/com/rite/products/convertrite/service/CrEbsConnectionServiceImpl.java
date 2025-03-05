package com.rite.products.convertrite.service;

import com.rite.products.convertrite.utils.DataSourceUtil;
import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import com.rite.products.convertrite.model.*;
import com.rite.products.convertrite.po.*;
import com.rite.products.convertrite.respository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.rite.products.convertrite.Validations.Validations;
import com.rite.products.convertrite.exception.ValidationException;
import com.rite.products.convertrite.multitenancy.config.tenant.hibernate.DynamicDataSourceBasedMultiTenantConnectionProvider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

@Service
@Slf4j
public class CrEbsConnectionServiceImpl {

    @Autowired
    CrEbsConnectionDetailsRepository crEbsConnectionDetailsRepository;

    @Autowired
    CrBatchProcessingDetailsRepo crBatchProcessingDetailsRepo;
    @Autowired
    CrSourceTemplateHeadersRepo crSourceTemplateHeadersRepo;
    @Autowired
    DynamicDataSourceBasedMultiTenantConnectionProvider dynamicDataSourceBasedMultiTenantConnectionProvider;
    @Autowired
    CrProcessRequestsRepository crProcessRequestsRepository;
    @Autowired
    CrSourceTemplateHeadersViewRepo crSourceTemplateHeadersViewRepo;
    @Autowired
    GenerateOrigTransRefDaoImpl generateOrigTransRefDaoImpl;
    @Autowired
    DataSourceUtil dataSourceUtil;

    @Autowired
    @Qualifier("masterDataSource")
    private DataSource masterDataSource;

    @Autowired
    CrSourceTableRepo crSourceTableRepo;


    @Autowired
    CrBatchWiseProcessingDaoImpl batchWiseProcessingDaoImpl;

    @Value("${spring.profiles.active}")
    private String env;

    @Value("${ebs.batch.size}")
    private int ebsBatchSize;

    @Value("${ebs.dblink.enabled:false}")
    private boolean dbLinkEnabled;
    @Autowired
    CrSqlextractionBindVarDtlsRepository sqlextractionBindVarDtlsRepository;


    /**
     * @param ebsConnectnDtlsReqPo
     * @param request
     * @return
     * @throws ValidationException
     * @throws Exception
     */
//    public BasicResponsePo saveEbsConnectionDtls(CrSaveEbsConnectionDetailsReqPo ebsConnectnDtlsReqPo,
//                                                 HttpServletRequest request) throws ValidationException, Exception {
//        Connection con = null;
//        PreparedStatement stmnt = null;
//        BasicResponsePo responsePo = new BasicResponsePo();
//        try {
//            // create database connection
//            log.info("TENANT-->" + request.getHeader("X-TENANT-ID"));
//            con = dynamicDataSourceBasedMultiTenantConnectionProvider.getConnection(request.getHeader("X-TENANT-ID"));
//            //save or update ebs connection details
//            //add _PodID
//            CrEbsConnectionDetails ebsConnectnDtlsResp = saveConnectionDtls(ebsConnectnDtlsReqPo, con);
//
//            if (dbLinkEnabled) {
//                String dbLinkName = ebsConnectnDtlsResp.getServiceName() + ebsConnectnDtlsResp.getConnectionId() + env + "_" + request.getHeader("X-TENANT-ID");
//                //create ebs dblink
//                String dbLinkQuery = dbLinkQuery(dbLinkName, ebsConnectnDtlsResp);
//                // create Prepared Statement
//                stmnt = con.prepareStatement(dbLinkQuery);
//                int count = stmnt.executeUpdate();
//                // updating connection details with dblink
//                ebsConnectnDtlsResp.setDabaseLink(dbLinkName);
//            }
//
//            CrEbsConnectionDetails updateCntResp = crEbsConnectionDetailsRepository.save(ebsConnectnDtlsResp);
//            responsePo.setPayload(updateCntResp);
//            responsePo.setMessage("Ebs connection details saved successfully");
//        } finally {
//            if (stmnt != null)
//                stmnt.close();
//            if (con != null)
//                con.close();
//        }
//        return responsePo;
//    }
    public BasicResponsePo saveEbsConnectionDtls(CrSaveEbsConnectionDetailsReqPo ebsConnectnDtlsReqPo,
                                                 HttpServletRequest request) throws ValidationException, Exception {
        Connection con = null;
        PreparedStatement stmnt = null;
        BasicResponsePo responsePo = new BasicResponsePo();

        try {
            log.info("TENANT-->" + request.getHeader("X-TENANT-ID"));
            con = dynamicDataSourceBasedMultiTenantConnectionProvider.getConnection(request.getHeader("X-TENANT-ID"));

            // Save or update EBS connection details
            CrEbsConnectionDetails ebsConnectnDtlsResp = saveConnectionDtls(ebsConnectnDtlsReqPo, con);

            if (dbLinkEnabled) {
                // Securely construct DB link name
                String dbLinkName = sanitizeDbLinkName(
                        ebsConnectnDtlsResp.getServiceName() +
                                ebsConnectnDtlsResp.getConnectionId() +
                                env + "_" +
                                request.getHeader("X-TENANT-ID")
                );

                // Validate dbLinkName
                if (dbLinkName == null) {
                    throw new ValidationException("Invalid database link name");
                }

                // Securely generate the DB Link Query
                String dbLinkQuery = dbLinkQuery(dbLinkName, ebsConnectnDtlsResp);

                // Execute safely
                stmnt = con.prepareStatement(dbLinkQuery);
                int count = stmnt.executeUpdate();

                ebsConnectnDtlsResp.setDabaseLink(dbLinkName);
            }

            CrEbsConnectionDetails updateCntResp = crEbsConnectionDetailsRepository.save(ebsConnectnDtlsResp);
            responsePo.setPayload(updateCntResp);
            responsePo.setMessage("EBS connection details saved successfully");
        } finally {
            if (stmnt != null) stmnt.close();
            if (con != null) con.close();
        }
        return responsePo;
    }


    private String sanitizeDbLinkName(String dbLinkName) {
        if (dbLinkName == null || !dbLinkName.matches("^[a-zA-Z0-9_]+$")) {
            log.error("Invalid database link name: " + dbLinkName);
            return null;
        }
        return dbLinkName;
    }

    private CrEbsConnectionDetails saveConnectionDtls(CrSaveEbsConnectionDetailsReqPo ebsConnectnDtlsReqPo, Connection con) throws ValidationException, Exception {
        CrEbsConnectionDetails ebsConnectionDetails = new CrEbsConnectionDetails();
        Long connectionId = ebsConnectnDtlsReqPo.getConnectionId();
        Connection ebsCon = null;
        try {
            if ("Azure".equalsIgnoreCase(ebsConnectnDtlsReqPo.getConnectionType())) {
                EbsConnectionReqPo ebsConnectionReqPo = new EbsConnectionReqPo();
                ebsConnectionReqPo.setDatabaseName(ebsConnectnDtlsReqPo.getServiceName());
                ebsConnectionReqPo.setPortNumber(ebsConnectnDtlsReqPo.getPort());
                ebsConnectionReqPo.setHostName(ebsConnectnDtlsReqPo.getHostName());
                ebsConnectionReqPo.setTargetUserName(ebsConnectnDtlsReqPo.getUserName());
                ebsConnectionReqPo.setTargetPassword(ebsConnectnDtlsReqPo.getPassword());
                ebsCon = dataSourceUtil.createMsSQLConnection(ebsConnectionReqPo);

            } else {
                String ebsConnectionUrl = "jdbc:oracle:thin:@//" + ebsConnectnDtlsReqPo.getHostName() + ":" + ebsConnectnDtlsReqPo.getPort() + "/" + ebsConnectnDtlsReqPo.getServiceName();
                // Driver name
                Class.forName(
                        "oracle.jdbc.driver.OracleDriver");
                ebsCon = DriverManager.getConnection(
                        ebsConnectionUrl, ebsConnectnDtlsReqPo.getUserName(), ebsConnectnDtlsReqPo.getPassword());
            }
        } catch (Exception e) {
            log.error("Error getting EBS connection: " + e.getMessage());
            throw new ValidationException("Please cross verify ebs connection details");
        }
        if (connectionId != null) {
            String databaseLink = crEbsConnectionDetailsRepository.findById(connectionId).get().getDabaseLink();
            if (!Validations.isNullOrEmpty(databaseLink)) {
                PreparedStatement statement = con.prepareStatement("DROP PUBLIC DATABASE LINK " + databaseLink);
                statement.execute();
            }
            ebsConnectionDetails.setConnectionId(connectionId);
        } else {
            CrEbsConnectionDetails ebsDtlsWithConnectnName = crEbsConnectionDetailsRepository
                    .findByConnectionName(ebsConnectnDtlsReqPo.getConnectionName());
            if (ebsDtlsWithConnectnName != null)
                throw new ValidationException("ConnectionName already exists");
        }
        ebsConnectionDetails.setConnectionName(ebsConnectnDtlsReqPo.getConnectionName());
        ebsConnectionDetails.setHostName(ebsConnectnDtlsReqPo.getHostName());
        ebsConnectionDetails.setUserName(ebsConnectnDtlsReqPo.getUserName());
        ebsConnectionDetails.setPassword(ebsConnectnDtlsReqPo.getPassword());
        ebsConnectionDetails.setPort(ebsConnectnDtlsReqPo.getPort());
        ebsConnectionDetails.setServiceName(ebsConnectnDtlsReqPo.getServiceName());
        ebsConnectionDetails.setCreationDate(new Date());
        ebsConnectionDetails.setCreatedBy("ConvertRite");
        ebsConnectionDetails.setLastUpdatedDate(new Date());
        ebsConnectionDetails.setLastUpdateBy("ConvertRite");
        ebsConnectionDetails.setConnectionType(ebsConnectnDtlsReqPo.getConnectionType());
        return crEbsConnectionDetailsRepository.save(ebsConnectionDetails);
    }

    private String dbLinkQuery(String dbLinkName, CrEbsConnectionDetails ebsConnectnDtlsResp) {
        StringBuilder sb = new StringBuilder("CREATE PUBLIC DATABASE LINK ");
        sb.append(dbLinkName);
        sb.append("  CONNECT TO " + ebsConnectnDtlsResp.getUserName() + " IDENTIFIED BY "
                + ebsConnectnDtlsResp.getPassword());
        sb.append(" USING '(DESCRIPTION=\r\n" + " (ADDRESS=(PROTOCOL=TCP)(HOST=" + ebsConnectnDtlsResp.getHostName()
                + ")(PORT=" + ebsConnectnDtlsResp.getPort() + "))");
        sb.append("(CONNECT_DATA=(SERVICE_NAME=" + ebsConnectnDtlsResp.getServiceName() + ")))'");
        log.info("DBLinkQuery:" + sb.toString());
        return sb.toString();
    }

    public BasicResponsePo getEbsConnectionDtls() {
        BasicResponsePo responsePo = new BasicResponsePo();
        responsePo.setPayload(crEbsConnectionDetailsRepository.findAll());
        responsePo.setMessage("Successfully retrieved Ebs connection details");
        return responsePo;
    }

    public Connection getEbsConnection(String connectionName, CrEbsConnectionDetails ebsDtlsWithConnectnName) throws ValidationException {
        Connection ebsCon = null;
        try {
            log.info("======getEbsConnection======");
            if ("azure".equalsIgnoreCase(ebsDtlsWithConnectnName.getConnectionType())) {
                EbsConnectionReqPo ebsConnectionReqPo = new EbsConnectionReqPo();
                ebsConnectionReqPo.setDatabaseName(ebsDtlsWithConnectnName.getServiceName());
                ebsConnectionReqPo.setPortNumber(ebsDtlsWithConnectnName.getPort());
                ebsConnectionReqPo.setHostName(ebsDtlsWithConnectnName.getHostName());
                ebsConnectionReqPo.setTargetUserName(ebsDtlsWithConnectnName.getUserName());
                ebsConnectionReqPo.setTargetPassword(ebsDtlsWithConnectnName.getPassword());
                ebsCon = dataSourceUtil.createMsSQLConnection(ebsConnectionReqPo);
            } else {
                String ebsConnectionUrl = "jdbc:oracle:thin:@//" + ebsDtlsWithConnectnName.getHostName() + ":" + ebsDtlsWithConnectnName.getPort() + "/" + ebsDtlsWithConnectnName.getServiceName();
                log.info("ebsConnectionUrl----> {} ", ebsConnectionUrl);
                // Driver name
                Class.forName("oracle.jdbc.driver.OracleDriver");
                ebsCon = DriverManager.getConnection(
                        ebsConnectionUrl, ebsDtlsWithConnectnName.getUserName(), ebsDtlsWithConnectnName.getPassword());
            }
        } catch (Exception e) {
            log.error("Error getting EBS connection: {} ", e.getMessage());
            throw new ValidationException("Please cross verify ebs connection details");
        }
        return ebsCon;
    }

    //    public BasicResponsePo loadSrcMetaDataFromEbs(CrLoadMetaDataFromEbsReqPo crLoadMetaDataFromEbsReqPo,
//                                                  HttpServletRequest request) throws ValidationException, Exception {
//        log.info("======loadSrcMetaDataFromEbs======");
//        log.info("DBLink Enabled: " + dbLinkEnabled);
//        Connection con = null;
//        Connection ebsCon = null;
//        PreparedStatement stmnt = null;
//        ResultSet rs = null;
//        ResultSetMetaData rsmd = null;
//        BasicResponsePo responsePo = new BasicResponsePo();
//        String ebsQuery = "";
//        try {
//            Long objectId = crLoadMetaDataFromEbsReqPo.getObjectId();
//            String metaDataTableName = crLoadMetaDataFromEbsReqPo.getMetaDataTableName();
//            Long metaDataTableId = crSourceTableRepo.getTableId(metaDataTableName);
//            CrEbsConnectionDetails ebsDtlsWithConnectnName = crEbsConnectionDetailsRepository
//                    .findByConnectionName(crLoadMetaDataFromEbsReqPo.getConnectionName());
//            if (ebsDtlsWithConnectnName == null) {
//                throw new ValidationException("EBS connection doesn't exist");
//            }
//            if (metaDataTableId != null)
//                throw new ValidationException("MetaDataTableName already exists");
//            // Retrieve Ebs View query
//            if (dbLinkEnabled) {
//                ebsQuery = replaceEbsQueryWithDbLink(crLoadMetaDataFromEbsReqPo.getObjectId(), crLoadMetaDataFromEbsReqPo.getConnectionName(), ebsDtlsWithConnectnName.getConnectionType());
//            } else {
//                ebsQuery = getEbsQueryWithoutDbLink(crLoadMetaDataFromEbsReqPo.getObjectId(), ebsDtlsWithConnectnName.getConnectionType());
//            }
//
//            log.info("EBS Query: " + ebsQuery);
//
//            // create database connection
//            log.info("TENANT-->" + request.getHeader("X-TENANT-ID"));
//            con = dynamicDataSourceBasedMultiTenantConnectionProvider.getConnection(request.getHeader("X-TENANT-ID"));
//
//            String ebcConnectionName = crLoadMetaDataFromEbsReqPo.getConnectionName();
//            ebsCon = getEbsConnection(ebcConnectionName, ebsDtlsWithConnectnName);
//
//            //If DBLink is enabled then get metadata using DBLink else use EBS jdbc connection
//            if (dbLinkEnabled) {
//                stmnt = con.prepareStatement(ebsQuery);
//                rs = stmnt.executeQuery();
//                rsmd = rs.getMetaData();
//            } else {
//                stmnt = ebsCon.prepareStatement(ebsQuery);
//                rs = stmnt.executeQuery();
//                rsmd = rs.getMetaData();
//            }
//
//            // insert metadata of table into CR_SOURCE_TABLES
//            metaDataTableName = insertTableMetaData(objectId, con, metaDataTableName);
//            metaDataTableId = crSourceTableRepo.getTableId(metaDataTableName);
//            log.info("metaDataTableId-->" + metaDataTableId);
//
//            // To insert MetaData columns into cr_source_columns
//            insertColumnMetaData(rsmd, metaDataTableId, con);
//            responsePo.setMessage("Loaded metadata successfully through EBS");
//            responsePo.setPayload(metaDataTableId);
//        } finally {
//            if (rs != null)
//                rs.close();
//            if (stmnt != null)
//                stmnt.close();
//            if (con != null)
//                con.close();
//            if (ebsCon != null)
//                ebsCon.close();
//        }
//        return responsePo;
//    }
    public BasicResponsePo loadSrcMetaDataFromEbs(CrLoadMetaDataFromEbsReqPo crLoadMetaDataFromEbsReqPo,
                                                  HttpServletRequest request) throws ValidationException {
        log.info("======loadSrcMetaDataFromEbs======");
        log.info("DBLink Enabled: " + dbLinkEnabled);

        BasicResponsePo responsePo = new BasicResponsePo();
        String ebsQuery = "";

        // Validate Input Parameters
        validateLoadMetaDataRequest(crLoadMetaDataFromEbsReqPo);

        try (Connection con = dynamicDataSourceBasedMultiTenantConnectionProvider.getConnection(request.getHeader("X-TENANT-ID"));
             Connection ebsCon = dbLinkEnabled ? null : getEbsConnection(crLoadMetaDataFromEbsReqPo.getConnectionName(),
                     crEbsConnectionDetailsRepository.findByConnectionName(crLoadMetaDataFromEbsReqPo.getConnectionName()));
             PreparedStatement stmnt = dbLinkEnabled ? con.prepareStatement(ebsQuery) : ebsCon.prepareStatement(ebsQuery);
             ResultSet rs = stmnt.executeQuery()) {

            Long objectId = crLoadMetaDataFromEbsReqPo.getObjectId();
            String metaDataTableName = sanitizeTableName(crLoadMetaDataFromEbsReqPo.getMetaDataTableName());
            Long metaDataTableId = crSourceTableRepo.getTableId(metaDataTableName);

            if (metaDataTableId != null) {
                throw new ValidationException("MetaDataTableName already exists");
            }

            // Retrieve and Validate EBS Query
            ebsQuery = dbLinkEnabled
                    ? replaceEbsQueryWithDbLink(objectId, crLoadMetaDataFromEbsReqPo.getConnectionName(),
                    crEbsConnectionDetailsRepository.findByConnectionName(crLoadMetaDataFromEbsReqPo.getConnectionName()).getConnectionType())
                    : getEbsQueryWithoutDbLink(objectId, crEbsConnectionDetailsRepository.findByConnectionName(crLoadMetaDataFromEbsReqPo.getConnectionName()).getConnectionType());

            if (Validations.isNullOrEmpty(ebsQuery)) {
                throw new ValidationException("EBS SQL extraction query is not present");
            }

            log.info("EBS Query Retrieved Successfully");

            // Process ResultSet Metadata
            ResultSetMetaData rsmd = rs.getMetaData();

            // Insert Metadata into CR_SOURCE_TABLES
            metaDataTableName = insertTableMetaData(objectId, con, metaDataTableName);
            metaDataTableId = crSourceTableRepo.getTableId(metaDataTableName);

            log.info("metaDataTableId --> " + metaDataTableId);

            // Insert Column Metadata
            insertColumnMetaData(rsmd, metaDataTableId, con);

            responsePo.setMessage("Loaded metadata successfully through EBS");
            responsePo.setPayload(metaDataTableId);

        } catch (SQLException e) {
            log.error("Database error while loading metadata", e);
            throw new RuntimeException("Error loading metadata from EBS", e);
        } catch (Exception e) {
            log.error("Unexpected error while loading metadata", e);
            throw new RuntimeException("Unexpected error", e);
        }

        return responsePo;
    }


    private String insertTableMetaData(Long objectId, Connection con, String metaDataTableName) throws Exception {
        PreparedStatement insertStmnt = null;
        try {
            int count = 0;
            log.info("-------insertTableMetaData-----objectId-->" + objectId);
            StringBuilder sb = new StringBuilder(
                    "insert into CR_SOURCE_TABLES(table_id,table_name,user_table_name,description,application_id,auto_size,table_type,initial_extent,next_extent,min_extents,max_extents,pct_increase,ini_trans,max_trans,pct_free,pct_used,hosted_support_style,irep_comments,irep_annotations,object_id,attribute1,attribute2,attribute3,attribute4,attribute5,last_update_date,last_updated_by,creation_date,created_by) values ");
            sb.append("(CR_SRC_TABLE_ID_S.nextval,'" + metaDataTableName + "','" + metaDataTableName + "','"
                    + metaDataTableName + "',200,'Y','T',4,8,1,50,0,3,255,5,80,'Local',null,null," + objectId
                    + ",null,null,null,null,null,sysdate,'CONVRITE',sysdate,'CONVRITE')");
            insertStmnt = con.prepareStatement(sb.toString());
            count = insertStmnt.executeUpdate();
            log.info("metadata insert count-->" + count);
        } finally {
            if (insertStmnt != null)
                insertStmnt.close();
        }

        return metaDataTableName;
    }

    private void insertColumnMetaData(ResultSetMetaData rsmd, Long metaDataTableId, Connection con) throws Exception {
        PreparedStatement insertColumnsStmnt = null;
        int count = 0;
        try {
            log.info("======insertColumnMetaData======");
            StringBuffer sb = new StringBuffer("insert all ");

            int numColumns = rsmd.getColumnCount();

            String nullableFlag = "";
            for (int i = 1; i < numColumns + 1; i++) {
                String columnName = rsmd.getColumnName(i);
                String columnType = rsmd.getColumnTypeName(i).toUpperCase();

                int isNullable = rsmd.isNullable(numColumns);
                if (columnType.contains("VARCHAR") || columnType.contains("CHAR") ||
                        columnType.contains("NVARCHAR"))
                    columnType = "V";
                else if (columnType.equalsIgnoreCase("NUMBER") ||
                        columnType.contains("NUMERIC") ||
                        columnType.equalsIgnoreCase("DECIMAL") ||
                        columnType.equalsIgnoreCase("FLOAT") ||
                        columnType.equalsIgnoreCase("BIGINT") ||
                        columnType.equalsIgnoreCase("BIT") ||
                        columnType.equalsIgnoreCase("INT"))
                    columnType = "N";
                else if (columnType.equalsIgnoreCase("DATE"))
                    columnType = "D";
                else if (columnType.equalsIgnoreCase("TIMESTAMP") ||
                        columnType.contains("TIME"))
                    columnType = "T";
                int columnSize = rsmd.getColumnDisplaySize(i);
                if (columnSize == 0)
                    columnSize = 1000;
                if (isNullable == ResultSetMetaData.columnNullable)
                    nullableFlag = "Y";
                else if (isNullable == ResultSetMetaData.columnNoNulls)
                    nullableFlag = "N";

                sb.append(
                        "into cr_source_columns (table_id,column_id,column_name,user_column_name,description,application_id,column_sequence,column_type,width,null_allowed_flag,translate_flag,flexfield_usage_code,flexfield_application_id,flexfield_name,flex_value_set_application_id,flex_value_set_id,default_value,precision,scale,irep_comments,attribute1,attribute2,attribute3,attribute4,attribute5,last_update_date,last_updated_by,creation_date,created_by) values ("
                                + metaDataTableId + "," + i + ",'" + columnName + "','" + columnName + "','"
                                + columnName + "',200," + i + ",'" + columnType + "'," + columnSize + ",'"
                                + nullableFlag
                                + "','N','N',null,null,null,null,null,null,null,null,null,null,null,null,null,sysdate,'CONVRITE',sysdate,'CONVRITE')");
                log.debug(columnName + "-->column_name" + columnType + "-->column_type" + nullableFlag
                        + "-->nullableFlag" + "-->columnSize:::::" + columnSize);
            }
            sb.append("SELECT 1 FROM DUAL");
            log.info("sqlquery for inserting into cr_source_columns:" + sb.toString());
            insertColumnsStmnt = con.prepareStatement(sb.toString());
            count = insertColumnsStmnt.executeUpdate();
            log.info("Count of metadata columns -->" + count);
        } finally {
            if (insertColumnsStmnt != null)
                insertColumnsStmnt.close();
        }
    }

    private String getEbsQuery(Long objectId, String connectionType) throws Exception {
        Connection con = null;
        PreparedStatement stmnt = null;
        ResultSet rs = null;
        String ebsQuery = null;
        try {
            con = masterDataSource.getConnection();
            String query = "";
            if ("Azure".equalsIgnoreCase(connectionType)) {
                query = "select info_value from cr_object_information where object_id=? and info_type='AZURE_EXTRACTION_QUERY' ";
            } else {
                query = "select info_value from cr_object_information where object_id=? and info_type='SQL_EXTRACTION_QUERY' ";

            }
            // create Prepared Statement
            stmnt = con.prepareStatement(query);
            stmnt.setLong(1, objectId);
            rs = stmnt.executeQuery();
            if (rs.next())
                ebsQuery = rs.getString("info_value");

        } finally {
            if (rs != null)
                rs.close();
            if (stmnt != null)
                stmnt.close();
            if (con != null)
                con.close();
        }
        return ebsQuery;
    }

    public BasicResponsePo loadSrcDataFromEbs(CrLoadDataFromEbsReqPo crLoadDataFromEbsReqPo, HttpServletRequest request)
            throws Exception {
        BasicResponsePo basicResponsePo = new BasicResponsePo();
        CrSourceTemplateHeadersView crSourceTemplateHeaders = null;
        Long srcTemplateId = crLoadDataFromEbsReqPo.getSrcTemplateId();
        Connection con = null;
        Connection ebsCon = null;
        PreparedStatement stmnt = null;
        try {
            log.info("======loadSrcDataFromEbs======");
            log.info("DBLink Enabled: " + dbLinkEnabled);
            Long batchCap = crLoadDataFromEbsReqPo.getBatchSize();
            if (crLoadDataFromEbsReqPo.getBatchName().toLowerCase().indexOf("cr20") != -1)
                throw new ValidationException("Please don't use CR20 value in batchName");
            Optional<CrSourceTemplateHeadersView> srcTempHrdOpt = crSourceTemplateHeadersViewRepo
                    .findById(srcTemplateId);
            if (!srcTempHrdOpt.isPresent())
                throw new ValidationException("SrcTemplateId is not present");
            else
                crSourceTemplateHeaders = srcTempHrdOpt.get();
            String srcStagingTable = crSourceTemplateHeaders.getStagingTableName();
            CrEbsConnectionDetails ebsDtlsWithConnectnName = crEbsConnectionDetailsRepository
                    .findByConnectionName(crLoadDataFromEbsReqPo.getConnectionName());
            if (ebsDtlsWithConnectnName == null) {
                throw new ValidationException("EBS connection doesn't exist");
            }
            // create database connection
            log.info("TENANT-->" + request.getHeader("X-TENANT-ID"));
            con = dynamicDataSourceBasedMultiTenantConnectionProvider
                    .getConnection(request.getHeader("X-TENANT-ID"));
            PreparedStatement countStmnt = con
                    .prepareStatement("select count(*) from " + srcStagingTable
                            + " where cr_batch_name='" + crLoadDataFromEbsReqPo.getBatchName() + "'");
            ResultSet rs = countStmnt.executeQuery();
            int recCount = 0;
            if (rs.next()) {
                recCount = rs.getInt("count(*)");
                log.info("Record Count in " + srcStagingTable + ": " + recCount);
            }
            countStmnt.close();
            boolean existingBatchDetails = crBatchProcessingDetailsRepo.existsBySrcTempIdAndBatchName(
                    crLoadDataFromEbsReqPo.getSrcTemplateId(),
                    crLoadDataFromEbsReqPo.getBatchName()
            );
            if (recCount > 0 || existingBatchDetails == true) {
                throw new ValidationException("BatchName already exists");
            }

            // saving to processrequest
            CrProcessRequests crProcessRequestsRes = saveToProcessRequests(crSourceTemplateHeaders,
                    crLoadDataFromEbsReqPo.getBatchName());

            StopWatch watch = new StopWatch();

            int count = 0;
            try {
                if (dbLinkEnabled) {
                    count = insertEbsSourceDataWithDBLink(crLoadDataFromEbsReqPo, crSourceTemplateHeaders, con, srcStagingTable, srcTemplateId, ebsDtlsWithConnectnName);
                } else {
                    count = insertEbsSourceDataWithoutDBLink(crLoadDataFromEbsReqPo, crSourceTemplateHeaders, con, srcStagingTable, srcTemplateId, ebsDtlsWithConnectnName);
                }
                log.info("Total records inserted into EBS source staging table: " + count);

                // generating origtransref
                generateOrigTransRefDaoImpl.generateOrigTranRef(srcTemplateId, srcStagingTable, request,
                        crLoadDataFromEbsReqPo.getBatchName());

                CrLoadDataResPo crLoadDataResPo = new CrLoadDataResPo();
                if (batchCap != null && batchCap > 0) {
                    crLoadDataResPo = batchWiseProcessingDaoImpl.srcDataBatchProcessing(crLoadDataFromEbsReqPo.getBatchName(), crLoadDataFromEbsReqPo.getBatchSize(), crLoadDataFromEbsReqPo.getParentStgTableName(), crLoadDataFromEbsReqPo.getParentColumn(), srcStagingTable);
                    crLoadDataResPo.setCount(count);
                } else {
                    crLoadDataResPo.setCount(count);
                    basicResponsePo.setPayload(crLoadDataResPo);
                }
                // update process_requests table
                crProcessRequestsRes.setStatus("C");
                crProcessRequestsRes.setTotalRecords(count);
                crProcessRequestsRes.setEndDate(new java.sql.Date(new Date().getTime()));
                crProcessRequestsRes.setLastUpdatedDate(new java.sql.Date(new Date().getTime()));
                crProcessRequestsRes.setLastUpdateBy("ConvertRite");
                crProcessRequestsRepository.save(crProcessRequestsRes);
                basicResponsePo.setPayload(crLoadDataResPo);
                basicResponsePo.setMessage(count + " records loaded successfully into " + srcStagingTable);
            } catch (Exception e) {
                log.error("Error in loadSrcDataFromEbs----->" + e.getMessage());
                crProcessRequestsRes.setStatus("E");
                crProcessRequestsRes.setTotalRecords(count);
                crProcessRequestsRes.setErrorMsg(e.getMessage());
                crProcessRequestsRes.setLastUpdatedDate(new java.sql.Date(new Date().getTime()));
                crProcessRequestsRes.setLastUpdateBy("ConvertRite");
                crProcessRequestsRepository.save(crProcessRequestsRes);
                throw new Exception(e.getMessage());
            }
        } finally {
            if (stmnt != null)
                stmnt.close();
            if (con != null)
                con.close();
            if (ebsCon != null)
                ebsCon.close();
        }
        return basicResponsePo;
    }

    private String getEbsQueryWithoutDbLink(Long objectId, String connectionType) throws Exception {
        log.info("======getEbsQueryWithoutDbLink======");
        // Retrieve EBS Extraction Query
        String ebsQuery = getEbsQuery(objectId, connectionType);
        if (Validations.isNullOrEmpty(ebsQuery))
            throw new ValidationException("EBS sql extraction query is not present");
        ebsQuery = ebsQuery.replaceAll("\\@\\{0\\}", "");
        return ebsQuery;
    }

    //    private String replaceEbsQueryWithDbLink(Long objectId, String connectionName, String connectionType) throws Exception {
//        log.info("======replaceEbsQueryWithDbLink======");
//        // Retrieve EBS Extraction Query
//        String ebsQuery = getEbsQuery(objectId, connectionType);
//        if (Validations.isNullOrEmpty(ebsQuery))
//            throw new ValidationException("EBS sql extraction query is not present");
//        CrEbsConnectionDetails ebsConnectionDetails = crEbsConnectionDetailsRepository
//                .findByConnectionName(connectionName);
//        if (!Validations.isNullOrEmpty(ebsConnectionDetails.getDabaseLink()))
//            ebsQuery = ebsQuery.replaceAll("\\{0\\}", ebsConnectionDetails.getDabaseLink());
//        return ebsQuery;
//    }
    private String replaceEbsQueryWithDbLink(Long objectId, String connectionName, String connectionType) throws Exception {
        log.info("======replaceEbsQueryWithDbLink======");

        // Retrieve EBS Extraction Query
        String ebsQuery = getEbsQuery(objectId, connectionType);
        if (Validations.isNullOrEmpty(ebsQuery)) {
            throw new ValidationException("EBS SQL extraction query is not present");
        }

        // Fetch Connection Details
        CrEbsConnectionDetails ebsConnectionDetails = crEbsConnectionDetailsRepository.findByConnectionName(connectionName);
        if (ebsConnectionDetails == null || Validations.isNullOrEmpty(ebsConnectionDetails.getDabaseLink())) {
            throw new ValidationException("Database link is missing for the given connection.");
        }

        // Validate Database Link (Only allow safe characters)
        String dbLink = ebsConnectionDetails.getDabaseLink();
        if (!dbLink.matches("^[a-zA-Z0-9_.]+$")) {
            throw new ValidationException("Invalid database link format.");
        }

        // Use Named Parameter Instead of String Replacement
        return ebsQuery.replace("{0}", dbLink); // Ensure this replacement is safe
    }


    private int insertEbsSourceDataWithDBLink(CrLoadDataFromEbsReqPo crLoadDataFromEbsReqPo, CrSourceTemplateHeadersView crSourceTemplateHeaders, Connection conn, String srcStagingTable, Long srcTemplateId, CrEbsConnectionDetails ebsDtlsWithConnectnName) throws Exception {
        log.info("Start of EBS source data insertion using DBLink.");
        StopWatch watch = new StopWatch();
        watch.start();

        PreparedStatement stmnt = null;

        // Retrieve Ebs View query
        String ebsQuery = replaceEbsQueryWithDbLink(crSourceTemplateHeaders.getObjectId(),
                crLoadDataFromEbsReqPo.getConnectionName(), ebsDtlsWithConnectnName.getConnectionType());
        //replace bind variables
        ebsQuery = replaceWithBindVals(ebsQuery, crLoadDataFromEbsReqPo);
        log.info("ebsQuery bind" + ebsQuery);
        // Inserting data into staging table
        StringBuffer sb = new StringBuffer("INSERT INTO " + srcStagingTable + "   SELECT '" + srcTemplateId
                + "',NULL,NULL,ROWNUM,b.* ,ROWNUM,'" + crLoadDataFromEbsReqPo.getBatchName() + "' FROM ( ");
        sb.append(ebsQuery);
        sb.append(") b");
        String sqlQuery = sb.toString();
        log.info("SQL Query to insert data into EBS source staging table: " + sqlQuery);
        int count = 0;
        try {
            // create Prepared Statement
            stmnt = conn.prepareStatement(sqlQuery);
            count = stmnt.executeUpdate();
        } catch (SQLException e) {
            log.error("Rolling back ebs data inserts due to SQL exception: " + e.getMessage());
            throw e;
        } finally {
            if (stmnt != null)
                stmnt.close();
        }
        watch.stop();
        log.info("EBS source data insertion using DBLink completed in " + watch.getTotalTimeSeconds() + " seconds");
        return count;
    }

    private int insertEbsSourceDataWithoutDBLink(CrLoadDataFromEbsReqPo crLoadDataFromEbsReqPo, CrSourceTemplateHeadersView crSourceTemplateHeaders, Connection conn, String srcStagingTable, Long srcTemplateId, CrEbsConnectionDetails ebsDtlsWithConnectnName) throws Exception {
        log.info("Start of EBS source data insertion without using DBLink.");
        StopWatch watch = new StopWatch();
        watch.start();

        Connection ebsCon = null;
        PreparedStatement stmnt = null;

        // Retrieve Ebs View query
        String ebsQuery = getEbsQueryWithoutDbLink(crSourceTemplateHeaders.getObjectId(), ebsDtlsWithConnectnName.getConnectionType());
        log.info("EBS Query: " + ebsQuery);
        //replace bind variables
        ebsQuery = replaceWithBindVals(ebsQuery, crLoadDataFromEbsReqPo);
        log.info("ebsQuery bind" + ebsQuery);
        String ebcConnectionName = crLoadDataFromEbsReqPo.getConnectionName();
        log.info("EBS connection Name: " + ebcConnectionName);

        String batchName = crLoadDataFromEbsReqPo.getBatchName();

        ebsCon = getEbsConnection(ebcConnectionName, ebsDtlsWithConnectnName);

        // create EBS Prepared Statement
        stmnt = ebsCon.prepareStatement(ebsQuery);
        stmnt.setFetchSize(200);
        ResultSet rsEbs = stmnt.executeQuery();

        int totalCount = 0;
        int batchSize = 1000;

        if (ebsBatchSize > 0) {
            batchSize = ebsBatchSize;
        }

        ResultSetMetaData meta = rsEbs.getMetaData();

        List<String> columns = new ArrayList<>();
        for (int i = 1; i <= meta.getColumnCount(); i++)
            columns.add(meta.getColumnName(i));

        String insertSQL = "INSERT INTO " + srcStagingTable + " VALUES ('" + srcTemplateId
                + "',NULL,NULL,?,"
                + columns.stream().map(c -> "?").collect(Collectors.joining(", "))
                + ",?,'" + batchName + "')";
        log.info("SQL stmt for inserting EBS dta into Source Data Staging :" + insertSQL);

        try {

            conn.setAutoCommit(false);
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                while (rsEbs.next()) {
                    //Setting Row Number for ORIG_TRANS_ID column
                    insertStmt.setObject(1, totalCount + 1);
                    int totalColumns = meta.getColumnCount();
                    //loop to simulate load
                    //for (int j=1; j <=445; j++) {
                    for (int i = 1; i <= totalColumns; i++)
                        insertStmt.setObject(i + 1, rsEbs.getObject(i));

                    //Setting Row Number for CR_LOAD_ID column
                    insertStmt.setObject(totalColumns + 2, totalCount + 1);
                    insertStmt.addBatch();

                    //execute the batch after the specified batch size is reached
                    if (++totalCount % batchSize == 0) {
                        log.debug("Total records inserted :" + totalCount);
                        insertStmt.executeBatch();
                    }
                    //}
                }

                //execute the final batch
                insertStmt.executeBatch();

                //commiting all the inserts at the end
                conn.commit();
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            // Roll back all the inserts
            if (conn != null)
                conn.rollback();
            log.error("Rolling back ebs data inserts due to SQL exception: " + e.getMessage());
            throw e;
        } finally {
            if (ebsCon != null)
                ebsCon.close();
            if (stmnt != null)
                stmnt.close();
        }
        watch.stop();
        log.info("EBS source data insertion without DBLink completed in " + watch.getTotalTimeSeconds() + " seconds");
        return totalCount;
    }


    private CrProcessRequests saveToProcessRequests(CrSourceTemplateHeadersView sourceTemplateHeaders,
                                                    String batchName) {

        log.info("======saveToProcessRequests======");
        CrProcessRequests crProcessRequests = new CrProcessRequests();
        //crProcessRequests.setCldTemplateId(sourceTemplateHeaders.getTemplateId());
        crProcessRequests.setSrcTemplateId(sourceTemplateHeaders.getTemplateId());
        crProcessRequests.setPercentage(100);
        crProcessRequests.setRequestType("LOADING");
        crProcessRequests.setStatus("I");
        crProcessRequests.setStartDate(new java.sql.Date(new Date().getTime()));
        crProcessRequests.setCrBatchName(batchName);
        crProcessRequests.setCreationDate(new java.sql.Date(new Date().getTime()));
        crProcessRequests.setCreatedBy("ConvertRite");
        crProcessRequests.setLastUpdatedDate(new java.sql.Date(new Date().getTime()));
        crProcessRequests.setLastUpdateBy("ConvertRite");
        CrProcessRequests crProcessRequestsRes = crProcessRequestsRepository.save(crProcessRequests);
        return crProcessRequestsRes;
    }

    public BasicResponsePo getEbsAdaptorEnableFlag(Long objectId, String connectionType) throws Exception {
        BasicResponsePo responsePo = new BasicResponsePo();
        String ebsQuery = getEbsQuery(objectId, connectionType);
        responsePo.setMessage("Successfully retrieved Ebs Adaptor EnableFlag");
        responsePo.setPayload(Map.of("ebsAdaptorFlag", Validations.isNullOrEmpty(ebsQuery) ? "N" : "Y"));
        return responsePo;
    }

    public BasicResponsePo deleteEbsConnectionDtls(Long connectionId, HttpServletRequest request) throws ValidationException, Exception {
        BasicResponsePo basicResponsePo = new BasicResponsePo();
        if (!crEbsConnectionDetailsRepository.existsById(connectionId))
            throw new ValidationException("Delete failed connection details doesn't exist");
        String databaseLink = crEbsConnectionDetailsRepository.findById(connectionId).get().getDabaseLink();
        if (!Validations.isNullOrEmpty(databaseLink)) {
            log.info("TENANT-->" + request.getHeader("X-TENANT-ID"));
            try (
                    Connection con = dynamicDataSourceBasedMultiTenantConnectionProvider.getConnection(request.getHeader("X-TENANT-ID"));
            ) {
                PreparedStatement stmnt = con.prepareStatement("DROP PUBLIC DATABASE LINK " + databaseLink);
                stmnt.execute();
            }
        }
        crEbsConnectionDetailsRepository.deleteById(connectionId);
        basicResponsePo.setMessage("Deleted successfully ebs connection details ");
        basicResponsePo.setPayload("Deleted successfully ebs connection details for connectionId :" + connectionId);
        return basicResponsePo;
    }

    private String replaceWithBindVals(String ebsQuery, CrLoadDataFromEbsReqPo crLoadDataFromEbsReqPo) {
        List<CrSqlextractionBindVarDtls> crSqlextractionBindVarDtls = sqlextractionBindVarDtlsRepository.findByBatchNameAndSrcTemplateId(crLoadDataFromEbsReqPo.getBatchName(), crLoadDataFromEbsReqPo.getSrcTemplateId());

        if (crSqlextractionBindVarDtls != null) {
            for (CrSqlextractionBindVarDtls bindVarDtls : crSqlextractionBindVarDtls) {
                if (bindVarDtls.getBindVariableValue() != null) {
                    ebsQuery = ebsQuery.replace(":" + bindVarDtls.getBindVariable(), "'" + bindVarDtls.getBindVariableValue() + "'");
                } else if (bindVarDtls.getBindWhereClob() != null) {
                    ebsQuery = ebsQuery.replace(bindVarDtls.getBindVariable(), bindVarDtls.getBindWhereClob());
                }
            }
        }
        return ebsQuery;
    }
    private void validateLoadMetaDataRequest(CrLoadMetaDataFromEbsReqPo request) throws ValidationException {
        if (request.getObjectId() == null) {
            throw new ValidationException("ObjectId cannot be null");
        }
        if (Validations.isNullOrEmpty(request.getMetaDataTableName())) {
            throw new ValidationException("MetaDataTableName cannot be blank");
        }
        if (!request.getMetaDataTableName().matches("^[a-zA-Z0-9_]+$")) {
            throw new ValidationException("Invalid MetaDataTableName format");
        }
    }

    private String sanitizeTableName(String tableName) {
        if (tableName == null) return null;
        return tableName.replaceAll("[^a-zA-Z0-9_]", "_");
    }
}