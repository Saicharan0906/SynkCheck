package com.rite.products.convertrite.model;

import java.io.Serializable;
import lombok.Data;

@Data
public class CrQueryFilterId implements Serializable {

    private Long objectId;
    private String bindVarColName;
}
