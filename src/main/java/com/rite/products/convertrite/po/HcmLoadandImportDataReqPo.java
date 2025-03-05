package com.rite.products.convertrite.po;

import lombok.Data;
import javax.validation.constraints.*;

@Data
public class HcmLoadandImportDataReqPo {

	@NotNull(message = "podId cannot be null")
	private Long podId;

	@NotNull(message = "projectId cannot be null")
	private Long projectId;

	@Positive(message = "parentObjectId must be a positive integer")
	private Integer parentObjectId;

	@NotBlank(message = "cloudTemplateId cannot be blank")
	@Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "cloudTemplateId must contain only letters, numbers, underscores, or hyphens")
	private String cloudTemplateId;

	@NotBlank(message = "documentTitle cannot be blank")
	@Pattern(regexp = "^[a-zA-Z0-9 _-]+$", message = "documentTitle must contain only letters, numbers, spaces, underscores, or hyphens")
	private String documentTitle;

	@NotBlank(message = "documentAuthor cannot be blank")
	@Pattern(regexp = "^[a-zA-Z ]+$", message = "documentAuthor must contain only letters and spaces")
	private String documentAuthor;

	@NotBlank(message = "documentSecurityGroup cannot be blank")
	@Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "documentSecurityGroup must contain only letters, numbers, underscores, or hyphens")
	private String documentSecurityGroup;

	@NotBlank(message = "documentAccount cannot be blank")
	@Pattern(regexp = "^[a-zA-Z0-9]+$", message = "documentAccount must contain only letters and numbers")
	private String documentAccount;

	@NotBlank(message = "batchName cannot be blank")
	@Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "batchName must contain only letters, numbers, underscores, or hyphens")
	private String batchName;

	@NotBlank(message = "isIntialLoad cannot be blank")
	@Pattern(regexp = "^[YN]$", message = "isIntialLoad must be 'Y' or 'N'")
	private String isIntialLoad;
}
