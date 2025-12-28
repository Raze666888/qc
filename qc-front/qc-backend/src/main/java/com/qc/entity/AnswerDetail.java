package com.qc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("answer_detail")
public class AnswerDetail {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long answerId;

    private Long questionId;

    private Long optionId;

    private String textContent;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
