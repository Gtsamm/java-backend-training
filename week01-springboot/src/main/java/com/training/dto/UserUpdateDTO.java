package com.training.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserUpdateDTO {
    @Email(message = "邮箱格式不正确")
    private String email;
    private String phone;
    private Integer gender;
    private Integer status;
}

//对比 UserCreateDTO：更新时 username 和 password 不给改（那是独立接口），所以这里只有可更新的字段。