package com.qc.dto;

import lombok.Data;

@Data
public class BannerDTO {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private String linkUrl;
    private Integer orderNum;
    private Integer status;
}
