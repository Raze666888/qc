package com.qc.vo;

import lombok.Data;

@Data
public class OptionVO {
    private Long id;
    private Long questionId;
    private String content;
    private Integer sort;
    private Long jumpQuestionId;
    private Integer selectCount;
}
