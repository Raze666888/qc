package com.qc.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuestionnaireVO {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private Integer category;
    private String categoryName;

    @com.baomidou.mybatisplus.annotation.TableField("deadline")
    private LocalDateTime deadline;

    private Integer isAnonymous;
    private Integer status;
    private Integer auditStatus;
    private String link;
    private LocalDateTime createTime;
    private LocalDateTime answerTime;  // 添加填写时间字段
    private Integer answerCount;
    private List<QuestionVO> questions;
}
