package com.qc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("questionnaire")
public class Questionnaire {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String description;

    private Integer category;

    @TableField("deadline")
    private LocalDateTime deadline;

    private Integer isAnonymous;

    private Integer status;

    private Integer auditStatus;

    private String link;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private Integer answerCount;
}
