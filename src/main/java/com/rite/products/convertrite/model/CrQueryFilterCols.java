package com.rite.products.convertrite.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "cr_query_bind_var_cols")
@Data
@IdClass(CrQueryFilterId.class)
public class CrQueryFilterCols implements Serializable {
    @Id
    @Column(name = "object_id")
    private Long objectId;
    @Id
    @Column(name = "bind_var_col_name", length = 240)
    private String bindVarColName;
}
