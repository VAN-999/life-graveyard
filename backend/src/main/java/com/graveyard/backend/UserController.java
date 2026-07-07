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

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();

        if (userRepository.existsByUsername(user.getUsername())) {
            response.put("success", false);
            response.put("message", "用户名已被占用，请换一个");
            return response;
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            response.put("success", false);
            response.put("message", "邮箱已被注册");
            return response;
        }

        User savedUser = userRepository.save(user);
        response.put("success", true);
        response.put("message", "注册成功！欢迎来到人生数据坟场 ⚰️");
        response.put("userId", savedUser.getId());
        response.put("username", savedUser.getUsername());
        return response;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        Map<String, Object> response = new HashMap<>();

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

        int level = user.getExperience() / 10 + 1;

        response.put("success", true);
        response.put("message", "登录成功！欢迎回到你的墓场 ⚰️");
        response.put("userId", user.getId());
        response.put("username", user.getUsername());
        response.put("graveStyle", user.getGraveStyle());
        response.put("level", level);
        response.put("hellMoney", user.getHellMoney());
        response.put("experience", user.getExperience());
        return response;
    }

    @GetMapping("/info")
    public Map<String, Object> getUserInfo(@RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return response;
        }
        int level = user.getExperience() / 10 + 1;
        int nextLevelExp = level * 10;
        int currentExp = user.getExperience() % 10;

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("hellMoney", user.getHellMoney());
        data.put("level", level);
        data.put("experience", user.getExperience());
        data.put("currentLevelExp", currentExp);
        data.put("nextLevelExp", nextLevelExp);

        response.put("success", true);
        response.put("data", data);
        return response;
    }
}