package com.rite.products.convertrite.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Data
@Entity
@Table(name="CR_OBJECTS")
public class CrObjects {

    @Id
    @Column(name="OBJECT_ID")
    private long objectId;
    @Column(name="OBJECT_NAME")
    private String objectName;
    @Column(name="OBJECT_CODE")
    private String objectCode;
    @Column(name="PARENT_OBJECT_ID")
    private Long parentObjectId;

}
