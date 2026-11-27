package com.graveyard.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZoneId;
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
        List<RobberyLog> logs = robberyLogRepository.findByRobberIdOrVictimIdOrderByCreatedAtDescIdDesc(userId, userId);
        Set<Long> playerIds = new HashSet<>();
        for (RobberyLog log : logs) {
            playerIds.add(log.getRobberId());
            playerIds.add(log.getVictimId());
        }
        Map<Long, User> players = new HashMap<>();
        for (User player : userRepository.findAllById(playerIds)) {
            players.put(player.getId(), player);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (RobberyLog log : logs) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", log.getId());
            User victim = players.get(log.getVictimId());
            item.put("victimName", victim != null ? victim.getUsername() : "已注销用户");
            User robber = players.get(log.getRobberId());
            item.put("robberName", robber != null ? robber.getUsername() : "已注销用户");
            item.put("direction", userId.equals(log.getRobberId()) ? "OUTGOING" : "INCOMING");
            item.put("decorationName", log.getDecorationName());
            item.put("success", log.getSuccess());
            item.put("penaltyType", log.getPenaltyType());
            item.put("penaltyAmount", log.getPenaltyAmount());
            item.put("createdAt", log.getCreatedAt().atOffset(ZoneOffset.UTC));
            result.add(item);
        }
        response.put("success", true);
        response.put("data", result);
        return response;
    }

    // Lock both players in ID order so simultaneous attempts cannot bypass the daily limit.
    @PostMapping("/rob")
    @Transactional
    public Map<String, Object> rob(@RequestParam Long robberId, @RequestParam Long victimId) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        if (robberId.equals(victimId)) {
            response.put("message", "不能偷自己的墓园");
            return response;
        }

        User first = entityManager.find(User.class, Math.min(robberId, victimId), LockModeType.PESSIMISTIC_WRITE);
        User second = entityManager.find(User.class, Math.max(robberId, victimId), LockModeType.PESSIMISTIC_WRITE);
        if (first == null || second == null) {
            response.put("message", "用户不存在");
            return response;
        }
        if (!friendRepository.isFriend(robberId, victimId)) {
            response.put("message", "只能对好友发起盗墓");
            return response;
        }

        // Existing timestamps were written in Railway's UTC timezone.
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        LocalDateTime start = today.atStartOfDay(ZoneId.of("Asia/Shanghai"))
                .withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        if (robberyLogRepository.existsByRobberIdAndVictimIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                robberId, victimId, start, start.plusDays(1))) {
            response.put("message", "同一座坟一天只刨一次，给死者一点隐私。");
            return response;
        }

        List<UserDecoration> ownItems = userDecorationRepository.findByUserId(robberId);
        if (ownItems.isEmpty()) {
            response.put("message", "你比墓还干净，拿什么赔？");
            return response;
        }
        List<UserDecoration> friendItems = userDecorationRepository.findByUserId(victimId);
        if (friendItems.isEmpty()) {
            response.put("message", "进去看了，耗子都留了两粒米。🐀");
            return response;
        }

        Random random = new Random();
        boolean won = random.nextDouble() < 0.7;
        List<UserDecoration> candidates = won ? friendItems : ownItems;
        UserDecoration item = candidates.get(random.nextInt(candidates.size()));
        // Refresh and lock the item in case another inventory operation changed it.
        entityManager.refresh(item, LockModeType.PESSIMISTIC_WRITE);
        Long expectedOwner = won ? victimId : robberId;
        if (!expectedOwner.equals(item.getUserId())) {
            response.put("message", "装饰品归属已变化，请重试");
            return response;
        }
        Decoration decoration = decorationRepository.findById(item.getDecorationId()).orElseThrow();
        Long recipientId = won ? robberId : victimId;

        decorationStateRepository.deleteByUserDecorationId(item.getId());
        decorationStateRepository.flush();
        item.setUserId(recipientId);
        item.setIsEquipped(false);
        userDecorationRepository.save(item);
        decorationStateRepository.save(new DecorationState(recipientId, item.getId()));

        RobberyLog log = new RobberyLog(robberId, victimId, decoration.getId(),
                decoration.getName(), won, won ? null : "GIFT_DECORATION", 0);
        robberyLogRepository.save(log);

        response.put("success", true);
        response.put("robbed", won);
        response.put("decorationName", decoration.getName());
        response.put("message", won
                ? "「" + decoration.getName() + "」已过户。原主人暂未发表遗言。🥀"
                : "被抓现行。「" + decoration.getName() + "」赔给对方，你获准保留全尸。");
        return response;
    }
}
