package com.qc.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuestionnaireDetailVO {
    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private String title;
    private String description;
    private Integer category;
    private LocalDateTime deadline;
    private Integer isAnonymous;
    private Integer status;
    private Integer auditStatus;
    private String link;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer viewCount;
    private Integer answerCount;
    private List<QuestionVO> questions;
}
