package com.training.entity;

  import com.baomidou.mybatisplus.annotation.*;
  import lombok.Data;
  import java.time.LocalDateTime;

  @Data//Lombok 自动生成 getter/setter/toString/equals/hashCode，编译后会看到
  @TableName("user")//告诉 MyBatis-Plus 这个类对应 user 表
  public class User {
      @TableId(type = IdType.AUTO)//主键自增
      private Long id;
      private String username;
      private String password;
      private String email;
      private String phone;
      private Integer gender;
      private String avatar;
      private Integer status;
      @TableField(fill = FieldFill.INSERT)//插入时自动填充时间
      private LocalDateTime createTime;
      @TableField(fill = FieldFill.INSERT_UPDATE)//更新时自动填充时间
      private LocalDateTime updateTime;
      @TableLogic//逻辑删除标记,userService.removeById() 会变成 UPDATE SET deleted=1 而不是真的 DELETE
      private Integer deleted;
  }