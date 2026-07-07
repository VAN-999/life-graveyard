package com.graveyard.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.time.LocalDate;
import java.util.*;

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

    @Autowired
    private GraveRepository graveRepository;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private RobberyLogRepository robberyLogRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/list")
    public Map<String, Object> listAll() {
        Map<String, Object> response = new HashMap<>();
        List<Decoration> all = decorationRepository.findAll();
        Map<String, List<Decoration>> grouped = new HashMap<>();
        for (Decoration d : all) {
            grouped.computeIfAbsent(d.getCategory(), k -> new ArrayList<>()).add(d);
        }
        response.put("success", true);
        response.put("data", grouped);
        return response;
    }

    @GetMapping("/category/{category}")
    public Map<String, Object> listByCategory(@PathVariable String category) {
        Map<String, Object> response = new HashMap<>();
        List<Decoration> list = decorationRepository.findByCategory(category);
        response.put("success", true);
        response.put("data", list);
        return response;
    }

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
        user.setHellMoney(user.getHellMoney() - decoration.getPrice());
        userRepository.save(user);
        UserDecoration userDeco = new UserDecoration(userId, decorationId);
        UserDecoration saved = userDecorationRepository.save(userDeco);
        DecorationState state = new DecorationState(userId, saved.getId());
        decorationStateRepository.save(state);
        response.put("success", true);
        response.put("message", "购买成功！你拥有了 " + decoration.getName() + " ⚰️");
        response.put("remainingMoney", user.getHellMoney());
        return response;
    }

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
        List<Map<String, Object>> result = new ArrayList<>();
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
        ud.setIsEquipped(true);
        userDecorationRepository.save(ud);
        response.put("success", true);
        response.put("message", "已装备 " + d.getName() + " ⚰️");
        return response;
    }

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

    @GetMapping("/states")
    public Map<String, Object> getStates(@RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();
        List<DecorationState> states = decorationStateRepository.findByUserId(userId);
        response.put("success", true);
        response.put("data", states);
        return response;
    }

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

    @PostMapping("/tombstone/switch")
    public Map<String, Object> switchTombstone(@RequestParam Long userId, @RequestParam String style) {
        Map<String, Object> response = new HashMap<>();
        try {
            Grave grave = graveRepository.findByUserId(userId).orElse(new Grave(userId));
            grave.setTombstoneStyle(style);
            graveRepository.save(grave);
            response.put("success", true);
            response.put("message", "墓碑已更换 ⚰️");
            response.put("style", style);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更换失败: " + e.getMessage());
        }
        return response;
    }

    @GetMapping("/tombstone/current")
    public Map<String, Object> getCurrentTombstone(@RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();
        Grave grave = graveRepository.findByUserId(userId).orElse(new Grave(userId));
        String style = grave.getTombstoneStyle();
        if (style == null || style.isEmpty()) {
            style = "default";
        }
        response.put("success", true);
        response.put("style", style);
        return response;
    }

    // ====== 获取盗墓日志 ======
    @GetMapping("/robbery-logs")
    public Map<String, Object> getRobberyLogs(@RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();
        List<RobberyLog> logs = robberyLogRepository.findByRobberIdOrderByCreatedAtDesc(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (RobberyLog log : logs) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", log.getId());
            User victim = userRepository.findById(log.getVictimId()).orElse(null);
            item.put("victimName", victim != null ? victim.getUsername() : "未知");
            item.put("decorationName", log.getDecorationName());
            item.put("success", log.getSuccess());
            item.put("penaltyType", log.getPenaltyType());
            item.put("penaltyAmount", log.getPenaltyAmount());
            item.put("createdAt", log.getCreatedAt());
            result.add(item);
        }
        response.put("success", true);
        response.put("data", result);
        return response;
    }

    // ====== 盗墓（简化版） ======
    @PostMapping("/rob")
    public Map<String, Object> rob(@RequestParam Long robberId, @RequestParam Long victimId) {
        Map<String, Object> response = new HashMap<>();

        if (robberId.equals(victimId)) {
            response.put("success", false);
            response.put("message", "不能偷自己 💀");
            return response;
        }

        // 直接查受害者所有装饰品（不管装备、不管分类）
        List<UserDecoration> allDecos = userDecorationRepository.findByUserId(victimId);

        if (allDecos.isEmpty()) {
            response.put("success", false);
            response.put("message", "墓场太寒酸了，没什么好偷的 💀");
            return response;
        }

        // 随机选一件
        Random random = new Random();
        int targetIndex = random.nextInt(allDecos.size());
        UserDecoration target = allDecos.get(targetIndex);
        Decoration targetDeco = decorationRepository.findById(target.getDecorationId()).orElse(null);

        if (targetDeco == null) {
            response.put("success", false);
            response.put("message", "装饰品不存在");
            return response;
        }

        // 直接偷，70% 成功率
        boolean success = random.nextDouble() < 0.7;

        if (success) {
            // 从受害者移除
            userDecorationRepository.deleteById(target.getId());
            decorationStateRepository.deleteByUserDecorationId(target.getId());

            // 给盗墓者
            UserDecoration newDeco = new UserDecoration(robberId, targetDeco.getId());
            userDecorationRepository.save(newDeco);
            DecorationState state = new DecorationState(robberId, newDeco.getId());
            decorationStateRepository.save(state);

            // 记录日志
            RobberyLog log = new RobberyLog(robberId, victimId, targetDeco.getId(), targetDeco.getName(), true, null, 0);
            robberyLogRepository.save(log);

            response.put("success", true);
            response.put("robbed", true);
            response.put("message", "盗墓成功！你偷到了 " + targetDeco.getName() + " ⚰️");
            response.put("decorationName", targetDeco.getName());
            response.put("decorationIcon", targetDeco.getIcon());
            return response;
        }

        // 失败：扣钱或丢装饰品
        int penalty = (int) Math.round(targetDeco.getPrice() * 0.4);
        if (penalty < 1) penalty = 1;

        User robber = userRepository.findById(robberId).orElse(null);
        if (robber == null) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return response;
        }

        List<UserDecoration> robberDecos = userDecorationRepository.findByUserId(robberId);

        if (!robberDecos.isEmpty()) {
            int idx = random.nextInt(robberDecos.size());
            UserDecoration toRemove = robberDecos.get(idx);
            Decoration removedDeco = decorationRepository.findById(toRemove.getDecorationId()).orElse(null);
            String removedName = removedDeco != null ? removedDeco.getName() : "装饰品";

            userDecorationRepository.deleteById(toRemove.getId());
            decorationStateRepository.deleteByUserDecorationId(toRemove.getId());

            RobberyLog log = new RobberyLog(robberId, victimId, null, null, false, "LOST_DECORATION", 0);
            robberyLogRepository.save(log);

            response.put("success", true);
            response.put("robbed", false);
            response.put("message", "墓场诅咒！你丢失了 " + removedName + " ☠️");
            response.put("penaltyType", "LOST_DECORATION");
            response.put("lostDecoration", removedName);
            return response;
        }

        if (robber.getHellMoney() >= penalty) {
            robber.setHellMoney(robber.getHellMoney() - penalty);
            userRepository.save(robber);

            RobberyLog log = new RobberyLog(robberId, victimId, null, null, false, "LOST_MONEY", penalty);
            robberyLogRepository.save(log);

            response.put("success", true);
            response.put("robbed", false);
            response.put("message", "墓场诅咒！你被扣除了 " + penalty + " 冥币 ☠️");
            response.put("penaltyType", "LOST_MONEY");
            response.put("penaltyAmount", penalty);
            return response;
        }

        RobberyLog log = new RobberyLog(robberId, victimId, null, null, false, "POOR_EXEMPT", 0);
        robberyLogRepository.save(log);

        response.put("success", true);
        response.put("robbed", false);
        response.put("message", "你穷得连诅咒都懒得理你，今天盗墓功能已锁定，明天再来吧。");
        response.put("penaltyType", "POOR_EXEMPT");
        response.put("poorExempt", true);
        return response;
    }
}