package com.qc.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BannerVO {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private String linkUrl;
    private Integer orderNum;
    private Integer status;
    private LocalDateTime createTime;
}

