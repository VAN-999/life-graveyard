package com.graveyard.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/graves")
public class GraveVisitController {
    @Autowired private UserRepository users;
    @Autowired private GraveRepository graves;
    @Autowired private FriendRepository friends;
    @Autowired private UserDecorationRepository inventory;
    @Autowired private DecorationRepository decorations;
    @Autowired private DecorationStateRepository states;
    @Autowired private DailyReportRepository reports;

    @GetMapping("/{ownerId}")
    public Map<String, Object> visit(@PathVariable Long ownerId, @RequestParam Long visitorId) {
        User owner = users.findById(ownerId).orElse(null);
        if (owner == null) return Map.of("success", false, "message", "查无此坟。");
        if (!ownerId.equals(visitorId) && !friends.isFriend(visitorId, ownerId)) {
            return Map.of("success", false, "message", "先做邻坟，再来串门。");
        }
        List<UserDecoration> items = inventory.findEquippedByUserId(ownerId);
        Set<Long> decorationIds = new HashSet<>();
        Set<Long> itemIds = new HashSet<>();
        for (UserDecoration item : items) {
            decorationIds.add(item.getDecorationId());
            itemIds.add(item.getId());
        }
        Map<Long, Decoration> catalog = new HashMap<>();
        for (Decoration decoration : decorations.findAllById(decorationIds)) catalog.put(decoration.getId(), decoration);
        List<Map<String, Object>> visibleItems = new ArrayList<>();
        for (UserDecoration item : items) {
            Decoration decoration = catalog.get(item.getDecorationId());
            if (decoration != null) visibleItems.add(Map.of("id", item.getId(), "userDecorationId", item.getId(),
                    "name", decoration.getName(), "icon", decoration.getIcon()));
        }
        List<DecorationState> visibleStates = states.findByUserId(ownerId).stream()
                .filter(state -> itemIds.contains(state.getUserDecorationId())).toList();
        String style = graves.findByUserId(ownerId).map(Grave::getTombstoneStyle).orElse("default");
        String epitaph = reports.findFirstByUserIdOrderByReportDateDesc(ownerId)
                .map(DailyReport::getEpitaph).filter(text -> !text.isBlank()).orElse("此人还没想好最后说什么。");
        return Map.of("success", true, "data", Map.of("username", owner.getUsername(),
                "tombstoneStyle", style == null ? "default" : style, "epitaph", epitaph,
                "decorations", visibleItems, "states", visibleStates));
    }
}
