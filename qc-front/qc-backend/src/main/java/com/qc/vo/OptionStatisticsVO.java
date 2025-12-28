package com.qc.vo;

import lombok.Data;

@Data
public class OptionStatisticsVO {
    private Long optionId;
    private String content;
    private Integer count;
    private Double percentage;
}
