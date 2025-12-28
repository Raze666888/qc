package com.qc.dto;

import lombok.Data;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class AnswerDTO {
    @NotNull(message = "问卷ID不能为空")
    private Long questionnaireId;

    private String respondentName;

    private String respondentPhone;

    private String respondentIp;

    @NotEmpty(message = "答题详情不能为空")
    private List<AnswerDetailDTO> details;
}
