package com.qc.vo;

import lombok.Data;

import java.util.List;

@Data
public class QuestionStatisticsVO {
    private Long questionId;
    private String content;
    private Integer type;
    private List<OptionStatisticsVO> options;
}
