package com.graveyard.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // 注册接口
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            response.put("success", false);
            response.put("message", "用户名已被占用，请换一个");
            return response;
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(user.getEmail())) {
            response.put("success", false);
            response.put("message", "邮箱已被注册");
            return response;
        }

        // 保存用户
        User savedUser = userRepository.save(user);
        response.put("success", true);
        response.put("message", "注册成功！欢迎来到人生数据坟场 ⚰️");
        response.put("userId", savedUser.getId());
        response.put("username", savedUser.getUsername());
        return response;
    }

    // 登录接口
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        Map<String, Object> response = new HashMap<>();

        // 根据用户名查用户
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            response.put("success", false);
            response.put("message", "用户不存在，要不要先注册一个？⚰️");
            return response;
        }

        if (!user.getPassword().equals(password)) {
            response.put("success", false);
            response.put("message", "密码错误，你是来盗墓的吗？");
            return response;
        }

        response.put("success", true);
        response.put("message", "登录成功！欢迎回到你的墓场 ⚰️");
        response.put("userId", user.getId());
        response.put("username", user.getUsername());
        response.put("graveStyle", user.getGraveStyle());
        response.put("level", user.getLevel());
        return response;
    }
}