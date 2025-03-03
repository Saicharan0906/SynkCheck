package com.rite.products.convertrite.model;


import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "cr_sqlextraction_bind_var_dtls")
@Data
public class CrSqlextractionBindVarDtls {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bind_variable_id")
    private Long bindVariableId;

    @Column(name = "module", length = 100)
    private String module;

    @Column(name = "Parent_object_id")
    private Long parentObjectId;

    @Column(name = "Object_id")
    private Long objectId;

    @Column(name = "template_name", length = 100)
    private String templateName;

    @Column(name = "cld_template_id")
    private Long cldTemplateId;
    @Column(name = "src_template_id")
    private Long srcTemplateId;

    @Column(name = "Batch_name", length = 100)
    private String batchName;

    @Column(name = "Bind_variable", length = 100)
    private String bindVariable;

    @Column(name = "Bind_variable_value", length = 100)
    private String bindVariableValue;

    @Column(name = "Bind_Where_clob")
    private String bindWhereClob;

    @Column(name = "attribute1", length = 100)
    private String attribute1;

    @Column(name = "attribute2", length = 100)
    private String attribute2;

    @Column(name = "attribute3", length = 100)
    private String attribute3;

    @Column(name = "attribute4", length = 100)
    private String attribute4;

    @Column(name = "attribute5", length = 100)
    private String attribute5;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "creation_date")
    private Date creationDate;

    @Column(name = "last_updated_by", length = 50)
    private String lastUpdatedBy;

    @Column(name = "last_update_date")
    private Date lastUpdateDate;


}
