package com.graveyard.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
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

    // ====== 装备装饰品 ======
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

    // ====== 删除装饰品 ======
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

    // ====== 从墓场移除装饰 ======
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

    // ====== 切换墓碑款式 ======
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

    // ====== 获取用户墓碑款式 ======
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

    // ====== 盗墓 ======
    @PostMapping("/rob")
    public Map<String, Object> rob(@RequestParam Long robberId, @RequestParam Long victimId) {
        Map<String, Object> response = new HashMap<>();

        // 1. 不能偷自己
        if (robberId.equals(victimId)) {
            response.put("success", false);
            response.put("message", "不能偷自己 💀");
            return response;
        }

        // 2. 检查是否是好友
        if (!friendRepository.existsByUserIdAndFriendId(robberId, victimId)) {
            response.put("success", false);
            response.put("message", "不是你的好友，不能盗墓 💀");
            return response;
        }

        // 3. 检查是否被穷鬼豁免锁定（今天内触发过穷鬼豁免）
        LocalDate today = LocalDate.now();
        if (robberyLogRepository.hasPoorExemptToday(robberId, today)) {
            response.put("success", false);
            response.put("message", "你今天已经触发过穷鬼豁免，盗墓功能已被锁定，明天再来吧 ☠️");
            return response;
        }

        // 4. 检查今天是否已经偷过这个好友
        if (robberyLogRepository.hasRobbedToday(robberId, victimId, today)) {
            response.put("success", false);
            response.put("message", "今天已经偷过这个好友了，明天再来吧 💀");
            return response;
        }

        // 5. 获取受害者的装备装饰品（不包括墓碑）
        List<UserDecoration> victimDecorations = userDecorationRepository.findByUserIdAndIsEquippedTrue(victimId);

        // 过滤掉墓碑分类
        List<UserDecoration> robTargets = new ArrayList<>();
        for (UserDecoration ud : victimDecorations) {
            Decoration d = decorationRepository.findById(ud.getDecorationId()).orElse(null);
            if (d != null && !"TOMBSTONE".equals(d.getCategory())) {
                robTargets.add(ud);
            }
        }

        // 6. 检查是否有可偷的装饰品
        if (robTargets.isEmpty()) {
            response.put("success", false);
            response.put("message", "墓场太寒酸了，没什么好偷的 💀");
            return response;
        }

        // 7. 检查好友是否至少保留1件（不能偷光）
        if (robTargets.size() <= 1) {
            response.put("success", false);
            response.put("message", "好友只剩一件装饰品了，做人留一线 ☠️");
            return response;
        }

        // 8. 随机选一件要偷的装饰品
        Random random = new Random();
        int targetIndex = random.nextInt(robTargets.size());
        UserDecoration target = robTargets.get(targetIndex);
        Decoration targetDeco = decorationRepository.findById(target.getDecorationId()).orElse(null);
        if (targetDeco == null) {
            response.put("success", false);
            response.put("message", "装饰品不存在");
            return response;
        }

        // 9. 判断成功率（70%）
        boolean success = random.nextDouble() < 0.7;

        if (success) {
            // ====== 偷盗成功 ======
            // 从受害者移除装饰品
            userDecorationRepository.deleteById(target.getId());
            decorationStateRepository.deleteByUserDecorationId(target.getId());

            // 给盗墓者添加该装饰品
            UserDecoration newDeco = new UserDecoration(robberId, targetDeco.getId());
            userDecorationRepository.save(newDeco);
            // 创建默认状态
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

        // ====== 偷盗失败 ======
        // 计算罚款金额（装饰品价格的40%）
        int penalty = (int) Math.round(targetDeco.getPrice() * 0.4);
        if (penalty < 1) penalty = 1;

        // 获取盗墓者信息
        User robber = userRepository.findById(robberId).orElse(null);
        if (robber == null) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return response;
        }

        // 检查盗墓者是否有装饰品
        List<UserDecoration> robberDecorations = userDecorationRepository.findByUserId(robberId);
        boolean hasDecoration = !robberDecorations.isEmpty();

        String penaltyType;
        int penaltyAmount = 0;
        String penaltyMessage = "";

        if (hasDecoration) {
            // 有装饰品：随机丢一件
            int randomIndex = random.nextInt(robberDecorations.size());
            UserDecoration toRemove = robberDecorations.get(randomIndex);
            Decoration removedDeco = decorationRepository.findById(toRemove.getDecorationId()).orElse(null);
            String removedName = removedDeco != null ? removedDeco.getName() : "装饰品";

            userDecorationRepository.deleteById(toRemove.getId());
            decorationStateRepository.deleteByUserDecorationId(toRemove.getId());

            penaltyType = "LOST_DECORATION";
            penaltyAmount = 0;
            penaltyMessage = "墓场诅咒！你丢失了 " + removedName + " ☠️";

            RobberyLog log = new RobberyLog(robberId, victimId, null, null, false, penaltyType, 0);
            robberyLogRepository.save(log);

            response.put("success", true);
            response.put("robbed", false);
            response.put("message", penaltyMessage);
            response.put("penaltyType", penaltyType);
            response.put("lostDecoration", removedName);
            return response;
        }

        // 没有装饰品：检查冥币是否够扣
        if (robber.getHellMoney() >= penalty) {
            // 扣冥币
            robber.setHellMoney(robber.getHellMoney() - penalty);
            userRepository.save(robber);

            penaltyType = "LOST_MONEY";
            penaltyAmount = penalty;
            penaltyMessage = "墓场诅咒！你被扣除了 " + penalty + " 冥币 ☠️";

            RobberyLog log = new RobberyLog(robberId, victimId, null, null, false, penaltyType, penalty);
            robberyLogRepository.save(log);

            response.put("success", true);
            response.put("robbed", false);
            response.put("message", penaltyMessage);
            response.put("penaltyType", penaltyType);
            response.put("penaltyAmount", penalty);
            return response;
        }

        // 穷鬼豁免：没钱没装饰品
        penaltyType = "POOR_EXEMPT";
        penaltyMessage = "你穷得连诅咒都懒得理你，今天盗墓功能已锁定，明天再来吧。";

        RobberyLog log = new RobberyLog(robberId, victimId, null, null, false, penaltyType, 0);
        robberyLogRepository.save(log);

        response.put("success", true);
        response.put("robbed", false);
        response.put("message", penaltyMessage);
        response.put("penaltyType", penaltyType);
        response.put("poorExempt", true);
        return response;
    }
}