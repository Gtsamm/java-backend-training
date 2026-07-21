package com.training.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.training.dto.UserCreateDTO;
import com.training.dto.UserUpdateDTO;
import com.training.entity.User;
import com.training.mapper.UserMapper;
import com.training.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User createUser(UserCreateDTO dto) {
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setStatus(1);
        save(user);
        return user;
    }

@Override
public User updateUser(Long id, UserUpdateDTO dto) {
    User user = getById(id);
    if (user == null) {
        throw new RuntimeException("用户不存在");
    }
    /* 只拷贝 DTO 中不为 null 的字段，避免把 null 覆盖到 Entity */
    if (dto.getEmail() != null) {
        user.setEmail(dto.getEmail());
    }
    if (dto.getPhone() != null) {
        user.setPhone(dto.getPhone());
    }
    if (dto.getGender() != null) {
        user.setGender(dto.getGender());
    }
    if (dto.getStatus() != null) {
        user.setStatus(dto.getStatus());
    }
    updateById(user);
    return user;
  }
}

/*
💡关键方法：ServiceImpl<UserMapper, User> — 泛型第一个是 Mapper，第二个是 Entity。
BeanUtils.copyProperties 把 DTO 的同名字段拷贝到 Entity（字段名不一致的话会跳过，不报错）。
  ▎ 💡 Spring 的 BeanUtils.copyProperties 是全量拷贝，不区分 null。生产环境一般用 Hutool 的 BeanUtil.copyProperties(source, target,     
  ▎ CopyOptions.create().ignoreNullValue()) 一步搞定。
 */