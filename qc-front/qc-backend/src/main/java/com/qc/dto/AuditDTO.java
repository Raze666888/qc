package com.qc.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class AuditDTO {
    @NotNull(message = "问卷ID不能为空")
    private Long id;

    @NotNull(message = "审核状态不能为空")
    private Integer auditStatus;

    private String reason;
}
