package org.example.springbootproject.pojo;

import lombok.Data;

//修改密码实体
@Data
public class ChangePasswordRequest {
    private Integer userId;
    private String oldPassword;
    private String newPassword;
}

