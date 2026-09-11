package org.example.springbootproject.pojo;

import lombok.Data;


//登录
@Data
public class LoginRequest {
    private String username;
    private String password;
}

