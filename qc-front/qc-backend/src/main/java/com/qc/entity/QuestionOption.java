package com.qc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("question_option")
public class QuestionOption {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long questionId;

    private String content;

    private Integer sort;

    private Long jumpQuestionId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
