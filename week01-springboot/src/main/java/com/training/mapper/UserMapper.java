package com.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.training.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

  }
/*
💡 注意：这是一个 interface（接口），不是 class。BaseMapper<User> 是 MyBatis-Plus
的核心魔法——你只需要继承它，增删改查的全部方法（insert、deleteById、selectById、selectList...）自动就有了，一行 SQL都不用写。面试官问 MyBatis-Plus 原理时，这句话就是标准答案。
 */
