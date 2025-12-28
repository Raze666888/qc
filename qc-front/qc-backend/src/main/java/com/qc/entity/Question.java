package com.qc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("question")
public class Question {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long questionnaireId;

    private String content;

    private Integer type;

    private Integer isRequired;

    private Integer sort;

    private Integer maxSelectNum;

    private Integer inputType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
