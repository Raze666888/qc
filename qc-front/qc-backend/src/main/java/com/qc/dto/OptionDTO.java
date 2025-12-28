package com.qc.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class OptionDTO {
    private Long id;

    @NotBlank(message = "选项内容不能为空")
    private String content;

    private Integer sort;

    private Long jumpQuestionId;
}
