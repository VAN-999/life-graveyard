package com.graveyard.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/decorations")
public class DecorationController {

    @Autowired
    private DecorationRepository decorationRepository;

    @Autowired
    private UserDecorationRepository userDecorationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DecorationStateRepository decorationStateRepository;

    // ====== 获取所有装饰品 ======
    @GetMapping("/list")
    public Map<String, Object> listAll() {
        Map<String, Object> response = new HashMap<>();
        List<Decoration> all = decorationRepository.findAll();

        Map<String, List<Decoration>> grouped = new HashMap<>();
        for (Decoration d : all) {
            grouped.computeIfAbsent(d.getCategory(), k -> new java.util.ArrayList<>()).add(d);
        }

        response.put("success", true);
        response.put("data", grouped);
        return response;
    }

    // ====== 获取分类装饰品 ======
    @GetMapping("/category/{category}")
    public Map<String, Object> listByCategory(@PathVariable String category) {
        Map<String, Object> response = new HashMap<>();
        List<Decoration> list = decorationRepository.findByCategory(category);
        response.put("success", true);
        response.put("data", list);
        return response;
    }

    // ====== 购买装饰品 ======
    @PostMapping("/buy")
    public Map<String, Object> buy(@RequestParam Long userId, @RequestParam Long decorationId) {
        Map<String, Object> response = new HashMap<>();

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            response.put("success", false);
            response.put("message", "用户不存在，你是鬼吗？");
            return response;
        }

        Decoration decoration = decorationRepository.findById(decorationId).orElse(null);
        if (decoration == null) {
            response.put("success", false);
            response.put("message", "这个装饰品不存在，你是不是在盗墓？");
            return response;
        }

        if (user.getHellMoney() < decoration.getPrice()) {
            response.put("success", false);
            response.put("message", "冥币不够，先去赚点再回来买。");
            response.put("yourMoney", user.getHellMoney());
            response.put("price", decoration.getPrice());
            return response;
        }

        // 扣钱
        user.setHellMoney(user.getHellMoney() - decoration.getPrice());
        userRepository.save(user);

        // 每次购买都插入一条新记录
        UserDecoration userDeco = new UserDecoration(userId, decorationId);
        UserDecoration saved = userDecorationRepository.save(userDeco);

        // 创建默认状态
        DecorationState state = new DecorationState(userId, saved.getId());
        decorationStateRepository.save(state);

        response.put("success", true);
        response.put("message", "购买成功！你拥有了 " + decoration.getName() + " ⚰️");
        response.put("remainingMoney", user.getHellMoney());
        return response;
    }

    // ====== 查看用户拥有的装饰品 ======
    @GetMapping("/my")
    public Map<String, Object> myDecorations(@RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return response;
        }

        List<UserDecoration> myList = userDecorationRepository.findByUserId(userId);

        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (UserDecoration ud : myList) {
            Decoration d = decorationRepository.findById(ud.getDecorationId()).orElse(null);
            if (d != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", ud.getId());
                item.put("name", d.getName());
                item.put("icon", d.getIcon());
                item.put("category", d.getCategory());
                item.put("isEquipped", ud.getIsEquipped());
                item.put("userDecorationId", ud.getId());
                result.add(item);
            }
        }

        response.put("success", true);
        response.put("money", user.getHellMoney());
        response.put("data", result);
        return response;
    }

    // ====== 装备装饰品（不互斥，可同时装备多个） ======
    @PostMapping("/equip")
    public Map<String, Object> equip(@RequestParam Long userDecorationId) {
        Map<String, Object> response = new HashMap<>();

        UserDecoration ud = userDecorationRepository.findById(userDecorationId).orElse(null);
        if (ud == null) {
            response.put("success", false);
            response.put("message", "你没有这个装饰品");
            return response;
        }

        Decoration d = decorationRepository.findById(ud.getDecorationId()).orElse(null);
        if (d == null) {
            response.put("success", false);
            response.put("message", "装饰品不存在");
            return response;
        }

        // 直接装备，不卸下同分类的其他装饰品
        ud.setIsEquipped(true);
        userDecorationRepository.save(ud);

        response.put("success", true);
        response.put("message", "已装备 " + d.getName() + " ⚰️");
        return response;
    }

    // ====== 卸下装饰品 ======
    @PostMapping("/unequip")
    public Map<String, Object> unequip(@RequestParam Long userDecorationId) {
        Map<String, Object> response = new HashMap<>();

        UserDecoration ud = userDecorationRepository.findById(userDecorationId).orElse(null);
        if (ud == null) {
            response.put("success", false);
            response.put("message", "你没有这个装饰品");
            return response;
        }

        ud.setIsEquipped(false);
        userDecorationRepository.save(ud);

        response.put("success", true);
        response.put("message", "已卸下");
        return response;
    }

    // ====== 获取所有装饰位置 ======
    @GetMapping("/states")
    public Map<String, Object> getStates(@RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();
        List<DecorationState> states = decorationStateRepository.findByUserId(userId);
        response.put("success", true);
        response.put("data", states);
        return response;
    }

    // ====== 保存单个装饰位置 ======
    @PostMapping("/state/save")
    public Map<String, Object> saveState(@RequestBody DecorationState state) {
        Map<String, Object> response = new HashMap<>();
        try {
            DecorationState saved = decorationStateRepository.save(state);
            response.put("success", true);
            response.put("data", saved);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "保存失败: " + e.getMessage());
        }
        return response;
    }

    // ====== 创建装饰位置状态 ======
    @PostMapping("/state/create")
    public Map<String, Object> createState(@RequestParam Long userId, @RequestParam Long userDecorationId) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (decorationStateRepository.findByUserDecorationId(userDecorationId).isPresent()) {
                response.put("success", true);
                response.put("message", "状态已存在");
                return response;
            }
            DecorationState state = new DecorationState(userId, userDecorationId);
            decorationStateRepository.save(state);
            response.put("success", true);
            response.put("message", "状态创建成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "创建失败: " + e.getMessage());
        }
        return response;
    }

    // ====== 删除装饰品（有事务注解，可正常删除） ======
    @DeleteMapping("/delete")
    @Transactional
    public Map<String, Object> delete(@RequestParam Long userDecorationId) {
        Map<String, Object> response = new HashMap<>();
        try {
            decorationStateRepository.deleteByUserDecorationId(userDecorationId);
            userDecorationRepository.deleteById(userDecorationId);
            response.put("success", true);
            response.put("message", "已删除");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
        }
        return response;
    }

    // ====== 从墓场移除装饰（不删记录，只删位置） ======
    @PostMapping("/state/delete")
    public Map<String, Object> deleteState(@RequestParam Long userDecorationId) {
        Map<String, Object> response = new HashMap<>();
        try {
            decorationStateRepository.deleteByUserDecorationId(userDecorationId);
            response.put("success", true);
            response.put("message", "已从墓场移除");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "移除失败");
        }
        return response;
    }
}