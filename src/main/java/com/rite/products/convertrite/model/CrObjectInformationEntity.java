package com.rite.products.convertrite.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "cr_object_information")
public class CrObjectInformationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "obj_info_id", columnDefinition = "serial")
    private Long objInfoId;
    @Column(name = "object_id")
    private Long objectId;
    @Column(name = "info_type")
    private String infoType;
    @Column(name = "info_value")
    private String infoValue;
    @Column(name = "info_description")
    private String infoDescription;
    @Column(name = "additional_information1")
    private String additionalInformation1;
    @Column(name = "additional_information2")
    private String additionalInformation2;
    @Column(name = "additional_information3")
    private String additionalInformation3;
    @Column(name = "additional_information4")
    private String additionalInformation4;
    @Column(name = "additional_information5")
    private String additionalInformation5;
    @Column(name = "creation_date")
    private Date creationDate;
    @Column(name = "created_by")
    private String createdBy;
    @Column(name = "last_update_date")
    private Date lastUpdateDate;
    @Column(name = "last_update_by")
    private String lastUpdateBy;

}
