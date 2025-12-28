package com.qc.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class QuestionnaireDTO {
    private Long id;

    @NotBlank(message = "问卷标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "问卷类型不能为空")
    private Integer category;

    private LocalDateTime deadline;

    @NotNull(message = "是否匿名不能为空")
    private Integer isAnonymous;

    private Integer status;
}
