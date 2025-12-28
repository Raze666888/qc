package com.qc.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class AnswerDetailDTO {
    @NotNull(message = "问题ID不能为空")
    private Long questionId;

    private Long optionId;

    private String textContent;
}
