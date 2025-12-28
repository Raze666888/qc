package com.qc.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class QuestionDTO {
    private Long id;

    private Long questionnaireId;

    @NotBlank(message = "问题内容不能为空")
    private String content;

    @NotNull(message = "问题类型不能为空")
    private Integer type;

    @NotNull(message = "是否必填不能为空")
    private Integer isRequired;

    private Integer sort;

    private Integer maxSelectNum;

    private Integer inputType;
}
