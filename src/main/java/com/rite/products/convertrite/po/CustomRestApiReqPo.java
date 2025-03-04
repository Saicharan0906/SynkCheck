package com.rite.products.convertrite.po;

import javax.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class CustomRestApiReqPo {
    @NotNull(message = "cldTemplateId cannot be null")
    private long cldTemplateId;

    @NotBlank(message = "batchName cannot be blank")
    private String batchName;


    private String restApiUrl;

    @NotBlank(message = "cldUserName cannot be blank")
    private String cldUserName;

    @NotBlank(message = "cldPassword cannot be blank")
    private String cldPassword;

    @NotBlank(message = "objectName cannot be blank")
    private String objectName;

    @NotBlank(message = "cloudUrl cannot be blank")
    @URL(message = "cloudUrl must be a valid URL")
    private String cloudUrl;

    @NotNull(message = "objectId cannot be null")
    private Long objectId;

    @NotNull(message = "podId cannot be null")
    private Long podId;

    @NotNull(message = "requestId cannot be null")
    private Long requestId;

    @NotBlank(message = "ccidColumnName cannot be blank")
    private String ccidColumnName;

    @NotBlank(message = "ledgerColumnName cannot be blank")
    private String ledgerColumnName;

    @NotBlank(message = "createdBy cannot be blank")
    private String createdBy;
}