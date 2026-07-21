package com.training.common;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
  
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        return Result.fail(400, msg);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        return Result.fail(500, "服务器内部错误: " + e.getMessage());
    }
}

/*
💡原理：@RestControllerAdvice = AOP 切面。所有 @RestController 抛出的异常都会被这里拦截，统一返回 {"code": 500, "message": "...",  "data": null} 
而不是一大堆 Tomcat 报错的 HTML 页面。
两个 @ExceptionHandler 分别处理参数校验异常和通用异常——面试官可能会问"优先级问题"：越具体的异常匹配优先级越高。
 */
