package com.rite.products.convertrite.po;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class CrLoadDataFromEbsReqPo {

	@NotEmpty(message = "connectionName cannot be empty")
	@Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "connectionName must contain only letters, numbers, and underscores")
	private String connectionName;

	@NotNull(message = "srcTemplateId cannot be null")
	@Positive(message = "srcTemplateId must be a positive number")
	private Long srcTemplateId;

	@NotEmpty(message = "batchName cannot be empty")
	@Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "batchName must contain only letters, numbers, underscores, or hyphens")
	@Size(max = 50, message = "batchName must not exceed 50 characters")
	private String batchName;

	@NotNull(message = "batchSize cannot be null")
	@Positive(message = "batchSize must be a positive number")
	private Long batchSize;

	@Pattern(regexp = "^[a-zA-Z0-9_]*$", message = "parentStgTableName must contain only letters, numbers, and underscores")
	private String parentStgTableName;

	@Pattern(regexp = "^[a-zA-Z0-9_]*$", message = "parentColumn must contain only letters, numbers, and underscores")
	private String parentColumn;
}
