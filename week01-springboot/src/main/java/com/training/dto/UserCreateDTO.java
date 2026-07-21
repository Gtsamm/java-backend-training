package com.training.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
  
@Data
public class UserCreateDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
      private String password;

    @Email(message = "邮箱格式不正确")
      private String email;

    private String phone;
    private Integer gender;
  }

/*
💡 DTO 只包含「前端传过来的字段」，没有 id、createTime、deleted 这些后端自动处理的字段。@NotBlank 和 @Email 是 JSR 380 校验注解，配合
Controller 的 @Valid 使用——参数不合格时直接返回 400，请求压根到不了 Service 层。
 */
