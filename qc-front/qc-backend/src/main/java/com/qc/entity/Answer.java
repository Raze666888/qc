package com.qc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("answer")
public class Answer {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long questionnaireId;

    private String respondentName;

    private String respondentPhone;

    private String respondentIp;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
