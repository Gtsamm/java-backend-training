package com.training.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;    

@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {    
        MybatisPlusInterceptor interceptor = new
MybatisPlusInterceptor();
          /* 分页插件：MyBatis-Plus 默认不吃 LIMIT，
             必须加这个拦截器 IPage 才会自动拼接 LIMIT 和 COUNT */
        interceptor.addInnerInterceptor(new
PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}

/* 
面试常问：@Configuration 和 @Bean 的关系？@Configuration   
  ▎ 告诉 Spring "这是一个配置类"，@Bean 告诉 Spring
  ▎ "把这个方法的返回值放进容器"。这里 new
  ▎ 一个拦截器放进容器，MyBatis-Plus 自动检测到就能用分页了。
*/