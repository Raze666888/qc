package com.qc.vo;

import lombok.Data;

import java.util.List;

@Data
public class StatisticsVO {
    private Long questionnaireId;
    private String title;
    private Integer totalAnswers;
    private List<QuestionStatisticsVO> questionStatistics;
}
