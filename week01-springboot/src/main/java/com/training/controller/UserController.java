package com.training.controller;
  
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.training.common.Result;
import com.training.dto.UserCreateDTO;
import com.training.dto.UserUpdateDTO;
import com.training.entity.User;
import com.training.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

      /* ===== 新增用户 =====
         @Valid 触发 DTO 参数校验，校验失败 → GlobalExceptionHandler 自动拦截
         @RequestBody 把请求体的 JSON 自动转为 UserCreateDTO 对象 */
    @PostMapping
    public Result<User> create(@Valid @RequestBody UserCreateDTO dto) {
        return Result.success(userService.createUser(dto));
    }

      /* ===== 根据 ID 查单个用户 =====
         @PathVariable 从 URL 路径中取 {id}，例如 /api/users/1 → id=1 */
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    /* ===== 分页查询 + 关键字搜索 =====
        LambdaQueryWrapper：用 Lambda 表达式写查询条件，避免字段名拼错
        相比字符串 "username"，User::getUsername 编译期就能发现错误 */
    @GetMapping
    public Result<IPage<User>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(User::getUsername, keyword)
                    .or()
                    .like(User::getEmail, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);
        return Result.success(userService.page(new Page<>(page, size), wrapper));
    }

      /* ===== 更新用户 =====
         先查再改：先 getById 确认用户存在，不存在抛异常 */
    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
        return Result.success(userService.updateUser(id, dto));
    }

      /* ===== 删除用户（逻辑删除） =====
         removeById 实际执行 UPDATE SET deleted=1，数据不真删
         因为 User 的 deleted 字段标记了 @TableLogic */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success();
    }
}