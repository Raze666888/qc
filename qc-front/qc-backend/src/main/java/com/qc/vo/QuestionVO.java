package com.qc.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuestionVO {
    private Long id;
    private Long questionnaireId;
    private String content;
    private Integer type;
    private String typeName;
    private Integer isRequired;
    private Integer sort;
    private Integer maxSelectNum;
    private Integer inputType;
    private LocalDateTime createTime;
    private List<OptionVO> options;
}
