package com.qc.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private Integer role;
    private Integer points;
    private LocalDateTime createTime;
}
