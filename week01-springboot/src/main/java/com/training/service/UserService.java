package com.training.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.training.dto.UserCreateDTO;
import com.training.dto.UserUpdateDTO;
import com.training.entity.User;

public interface UserService extends IService<User> {
    User createUser(UserCreateDTO dto);
    User updateUser(Long id, UserUpdateDTO dto);
}

/*
 和 Mapper 一样，IService<User> 自带全套 CRUD 方法。但 createUser 和 updateUser 有自定义逻辑（DTO 转 Entity +默认值设置），所以单独声明。
*/
