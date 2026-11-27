package com.graveyard.backend;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.ZoneOffset;
import java.util.*;

@RestController
@RequestMapping("/api/v1/gifts")
public class GiftController {
    @Autowired private GiftRepository gifts;
    @Autowired private FriendRepository friends;
    @Autowired private UserRepository users;
    @Autowired private DecorationRepository decorations;
    @Autowired private UserDecorationRepository inventory;
    @Autowired private DecorationStateRepository states;
    @PersistenceContext private EntityManager entityManager;

    public record GiftRequest(Long senderId, Long recipientId, Long userDecorationId, String message, String requestId) {}

    @PostMapping
    @Transactional
    public Map<String, Object> send(@RequestBody GiftRequest request) {
        if (request.senderId() == null || request.recipientId() == null || request.userDecorationId() == null
                || request.requestId() == null || !request.requestId().matches("[0-9a-fA-F-]{36}")) {
            return Map.of("success", false, "message", "赠礼信息不完整，请重新选择");
        }
        String message = request.message() == null ? "" : request.message().strip();
        if (message.length() > 200 || request.senderId().equals(request.recipientId())) {
            return Map.of("success", false, "message", "请选择好友，留言最多 200 字");
        }
        User first = entityManager.find(User.class, Math.min(request.senderId(), request.recipientId()), LockModeType.PESSIMISTIC_WRITE);
        User second = entityManager.find(User.class, Math.max(request.senderId(), request.recipientId()), LockModeType.PESSIMISTIC_WRITE);
        if (first == null || second == null || !friends.isFriend(request.senderId(), request.recipientId())) {
            return Map.of("success", false, "message", "只能给好友送礼");
        }
        Optional<Gift> previous = gifts.findByRequestId(request.requestId());
        if (previous.isPresent()) {
            Gift gift = previous.get();
            boolean matches = gift.senderId.equals(request.senderId()) && gift.recipientId.equals(request.recipientId())
                    && gift.userDecorationId.equals(request.userDecorationId()) && gift.message.equals(message);
            return Map.of("success", matches, "message", matches ? "这份礼物已经送到了" : "请重新确认赠礼");
        }
        UserDecoration item = entityManager.find(UserDecoration.class, request.userDecorationId(), LockModeType.PESSIMISTIC_WRITE);
        if (item == null || !request.senderId().equals(item.getUserId())) {
            return Map.of("success", false, "message", "这件装饰品已经不在你的背包里了");
        }
        Decoration decoration = decorations.findById(item.getDecorationId()).orElseThrow();
        states.deleteByUserDecorationId(item.getId());
        states.flush();
        item.setUserId(request.recipientId());
        item.setIsEquipped(false);
        inventory.save(item);
        states.save(new DecorationState(request.recipientId(), item.getId()));
        Gift gift = new Gift();
        gift.senderId = request.senderId();
        gift.recipientId = request.recipientId();
        gift.userDecorationId = item.getId();
        gift.decorationName = decoration.getName();
        gift.message = message;
        gift.requestId = request.requestId();
        gifts.save(gift);
        return Map.of("success", true, "message", "「" + decoration.getName() + "」送到了。对方迟早用得上。🎁");
    }

    @GetMapping
    public Map<String, Object> history(@RequestParam Long userId, @RequestParam(defaultValue = "0") int page) {
        if (page < 0) return Map.of("success", false, "message", "页码无效");
        List<Gift> records = gifts.findBySenderIdOrRecipientIdOrderByCreatedAtDescIdDesc(userId, userId, PageRequest.of(page, 30));
        Set<Long> ids = new HashSet<>();
        for (Gift gift : records) {
            ids.add(gift.senderId);
            ids.add(gift.recipientId);
        }
        Map<Long, String> names = new HashMap<>();
        for (User player : users.findAllById(ids)) names.put(player.getId(), player.getUsername());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Gift gift : records) {
            result.add(Map.of("id", gift.id, "direction", userId.equals(gift.senderId) ? "SENT" : "RECEIVED",
                    "senderName", names.getOrDefault(gift.senderId, "已注销用户"),
                    "recipientName", names.getOrDefault(gift.recipientId, "已注销用户"),
                    "decorationName", gift.decorationName, "message", gift.message,
                    "createdAt", gift.createdAt.atOffset(ZoneOffset.UTC)));
        }
        return Map.of("success", true, "data", result, "hasMore", records.size() == 30);
    }
}
