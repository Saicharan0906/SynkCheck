package com.rite.products.convertrite.po;

import javax.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class CustomRestApiReqPo {

    @NotNull(message = "cldTemplateId cannot be null")
    private long cldTemplateId;

    @NotBlank(message = "batchName cannot be blank")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "batchName must contain only letters, numbers, underscores, or hyphens")
    private String batchName;

    @NotBlank(message = "restApiUrl cannot be blank")
    @URL(message = "restApiUrl must be a valid URL")
    @Pattern(regexp = "^(https?://)[a-zA-Z0-9./_-]+$", message = "restApiUrl must start with http:// or https:// and contain only valid URL characters")
    private String restApiUrl;

    @NotBlank(message = "cldUserName cannot be blank")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "cldUserName must contain only letters, numbers, underscores, or hyphens")
    private String cldUserName;

    @NotBlank(message = "cldPassword cannot be blank")
    @Pattern(regexp = "^[a-zA-Z0-9@#$%^&+=!?_-]+$", message = "cldPassword contains invalid characters")
    private String cldPassword;

    @NotBlank(message = "objectName cannot be blank")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "objectName must contain only letters, numbers, underscores, or hyphens")
    private String objectName;

    @NotBlank(message = "cloudUrl cannot be blank")
    @URL(message = "cloudUrl must be a valid URL")
    @Pattern(regexp = "^(https?://)[a-zA-Z0-9./_-]+$", message = "cloudUrl must start with http:// or https:// and contain only valid URL characters")
    private String cloudUrl;

    @NotNull(message = "objectId cannot be null")
    private Long objectId;

    @NotNull(message = "podId cannot be null")
    private Long podId;

    @NotNull(message = "requestId cannot be null")
    private Long requestId;

    @NotBlank(message = "ccidColumnName cannot be blank")
    @Pattern(regexp = "^[a-zA-Z0-9 _-]+$", message = "ccidColumnName must contain only letters, numbers, spaces, underscores, or hyphens")
    private String ccidColumnName;

    @NotBlank(message = "ledgerColumnName cannot be blank")
    @Pattern(regexp = "^[a-zA-Z0-9 _-]+$", message = "ledgerColumnName must contain only letters, numbers, spaces, underscores, or hyphens")
    private String ledgerColumnName;

    @NotBlank(message = "createdBy cannot be blank")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "createdBy must contain only letters, numbers, underscores, or hyphens")
    private String createdBy;
}
