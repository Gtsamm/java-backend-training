package com.training.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
  
@Data
@NoArgsConstructor
@AllArgsConstructor//@AllArgsConstructor + @NoArgsConstructor → Lombok 生成全参和无参构造函数，配合 JSON 序列化反序列化。
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }
}
// 💡 面试常考题：Result<T> 为什么用泛型 <T>？
// 答：为了让 data 可以是任意类型——Result<User> 时 data 是 User，Result<List<User>> 时 data 是 List，类型安全，不用强转。

