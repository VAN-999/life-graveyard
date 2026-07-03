package com.graveyard.backend;

import org.springframework.beans.factory.annotation.Autowired;
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

    /**
     * 获取所有装饰品（按分类）
     */
    @GetMapping("/list")
    public Map<String, Object> listAll() {
        Map<String, Object> response = new HashMap<>();
        List<Decoration> all = decorationRepository.findAll();

        // 按分类分组
        Map<String, List<Decoration>> grouped = new HashMap<>();
        for (Decoration d : all) {
            grouped.computeIfAbsent(d.getCategory(), k -> new java.util.ArrayList<>()).add(d);
        }

        response.put("success", true);
        response.put("data", grouped);
        return response;
    }

    /**
     * 获取某个分类的装饰品
     */
    @GetMapping("/category/{category}")
    public Map<String, Object> listByCategory(@PathVariable String category) {
        Map<String, Object> response = new HashMap<>();
        List<Decoration> list = decorationRepository.findByCategory(category);
        response.put("success", true);
        response.put("data", list);
        return response;
    }

    /**
     * 购买装饰品
     */
    @PostMapping("/buy")
    public Map<String, Object> buy(@RequestParam Long userId, @RequestParam Long decorationId) {
        Map<String, Object> response = new HashMap<>();

        // 1. 查用户
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            response.put("success", false);
            response.put("message", "用户不存在，你是鬼吗？");
            return response;
        }

        // 2. 查装饰品
        Decoration decoration = decorationRepository.findById(decorationId).orElse(null);
        if (decoration == null) {
            response.put("success", false);
            response.put("message", "这个装饰品不存在，你是不是在盗墓？");
            return response;
        }

        // 3. 检查冥币够不够
        if (user.getHellMoney() < decoration.getPrice()) {
            response.put("success", false);
            response.put("message", "冥币不够，先去赚点再回来买。");
            response.put("yourMoney", user.getHellMoney());
            response.put("price", decoration.getPrice());
            return response;
        }

        // 4. 检查是否已经拥有
        if (userDecorationRepository.existsByUserIdAndDecorationId(userId, decorationId)) {
            response.put("success", false);
            response.put("message", "你已经拥有这个装饰品了，别买重复的。");
            return response;
        }

        // 5. 扣钱，给装饰品
        user.setHellMoney(user.getHellMoney() - decoration.getPrice());
        userRepository.save(user);

        UserDecoration userDeco = new UserDecoration(userId, decorationId);
        userDecorationRepository.save(userDeco);

        response.put("success", true);
        response.put("message", "购买成功！你拥有了 " + decoration.getName() + " ⚰️");
        response.put("remainingMoney", user.getHellMoney());
        return response;
    }

    /**
     * 查看用户拥有的装饰品
     */
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

        // 把装饰品详情拼进去
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
                item.put("quantity", ud.getQuantity());
                result.add(item);
            }
        }

        response.put("success", true);
        response.put("money", user.getHellMoney());
        response.put("data", result);
        return response;
    }

    /**
     * 装备装饰品
     */
    @PostMapping("/equip")
    public Map<String, Object> equip(@RequestParam Long userDecorationId) {
        Map<String, Object> response = new HashMap<>();

        UserDecoration ud = userDecorationRepository.findById(userDecorationId).orElse(null);
        if (ud == null) {
            response.put("success", false);
            response.put("message", "你没有这个装饰品");
            return response;
        }

        // 同一分类只能装备一个，先把同分类的卸下来
        Decoration d = decorationRepository.findById(ud.getDecorationId()).orElse(null);
        if (d == null) {
            response.put("success", false);
            response.put("message", "装饰品不存在");
            return response;
        }

        // 查找该用户同分类所有装备的，全部卸下
        List<UserDecoration> equipped = userDecorationRepository.findByUserIdAndIsEquippedTrue(ud.getUserId());
        for (UserDecoration e : equipped) {
            Decoration ed = decorationRepository.findById(e.getDecorationId()).orElse(null);
            if (ed != null && ed.getCategory().equals(d.getCategory())) {
                e.setIsEquipped(false);
                userDecorationRepository.save(e);
            }
        }

        // 装备当前这个
        ud.setIsEquipped(true);
        userDecorationRepository.save(ud);

        response.put("success", true);
        response.put("message", "已装备 " + d.getName() + " ⚰️");
        return response;
    }

    /**
     * 卸下装饰品
     */
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
}