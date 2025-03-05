package com.rite.products.convertrite.po;

import javax.validation.constraints.Pattern;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class LoadCustomDataReqPo {
    @NotNull(message = "customTableId cannot be null")
    private Long customTableId;
    @NotBlank(message = "crBatchName cannot be blank")
    private String crBatchName;
    @NotBlank(message = "fileName cannot be blank")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Invalid file name format")
    private String fileName;
}
