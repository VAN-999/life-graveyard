package com.graveyard.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/friends")
public class FriendController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private FriendRepository friendRepository;

    // ====== 搜索用户 ======
    @GetMapping("/search")
    public Map<String, Object> searchUsers(@RequestParam String keyword, @RequestParam Long currentUserId) {
        Map<String, Object> response = new HashMap<>();
        List<User> users = userRepository.findByUsernameContainingIgnoreCase(keyword);
        List<Map<String, Object>> result = new ArrayList<>();

        for (User u : users) {
            if (u.getId().equals(currentUserId)) continue;
            Map<String, Object> item = new HashMap<>();
            item.put("id", u.getId());
            item.put("username", u.getUsername());

            boolean isFriend = friendRepository.isFriend(currentUserId, u.getId());
            item.put("isFriend", isFriend);

            boolean hasRequest = friendRequestRepository.existsByFromUserIdAndToUserIdAndStatus(currentUserId, u.getId(), "pending");
            item.put("hasRequest", hasRequest);

            result.add(item);
        }

        response.put("success", true);
        response.put("data", result);
        return response;
    }

    // ====== 发送好友请求 ======
    @PostMapping("/request")
    public Map<String, Object> sendRequest(@RequestParam Long fromUserId, @RequestParam Long toUserId) {
        Map<String, Object> response = new HashMap<>();

        if (fromUserId.equals(toUserId)) {
            response.put("success", false);
            response.put("message", "不能添加自己为好友 💀");
            return response;
        }

        if (friendRepository.isFriend(fromUserId, toUserId)) {
            response.put("success", false);
            response.put("message", "已经是好友了");
            return response;
        }

        if (friendRequestRepository.existsByFromUserIdAndToUserIdAndStatus(fromUserId, toUserId, "pending")) {
            response.put("success", false);
            response.put("message", "已发送好友请求，等待对方同意");
            return response;
        }

        Optional<FriendRequest> reverseRequest = friendRequestRepository.findByFromUserIdAndToUserId(toUserId, fromUserId);
        if (reverseRequest.isPresent() && "pending".equals(reverseRequest.get().getStatus())) {
            Friend friend1 = new Friend(fromUserId, toUserId);
            Friend friend2 = new Friend(toUserId, fromUserId);
            friendRepository.save(friend1);
            friendRepository.save(friend2);

            FriendRequest req = reverseRequest.get();
            req.setStatus("accepted");
            friendRequestRepository.save(req);

            response.put("success", true);
            response.put("message", "你们已经成为好友了 ⚰️");
            return response;
        }

        FriendRequest request = new FriendRequest(fromUserId, toUserId);
        friendRequestRepository.save(request);

        response.put("success", true);
        response.put("message", "好友请求已发送 ⚰️");
        return response;
    }

    // ====== 获取好友请求列表 ======
    @GetMapping("/requests")
    public Map<String, Object> getRequests(@RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();

        List<FriendRequest> requests = friendRequestRepository.findByToUserIdAndStatus(userId, "pending");
        List<Map<String, Object>> result = new ArrayList<>();

        for (FriendRequest req : requests) {
            User fromUser = userRepository.findById(req.getFromUserId()).orElse(null);
            if (fromUser != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("requestId", req.getId());
                item.put("fromUserId", fromUser.getId());
                item.put("username", fromUser.getUsername());
                result.add(item);
            }
        }

        response.put("success", true);
        response.put("data", result);
        return response;
    }

    // ====== 同意好友请求 ======
    @PostMapping("/accept")
    public Map<String, Object> acceptRequest(@RequestParam Long requestId) {
        Map<String, Object> response = new HashMap<>();

        FriendRequest request = friendRequestRepository.findById(requestId).orElse(null);
        if (request == null) {
            response.put("success", false);
            response.put("message", "请求不存在");
            return response;
        }

        Friend friend1 = new Friend(request.getFromUserId(), request.getToUserId());
        Friend friend2 = new Friend(request.getToUserId(), request.getFromUserId());
        friendRepository.save(friend1);
        friendRepository.save(friend2);

        request.setStatus("accepted");
        friendRequestRepository.save(request);

        response.put("success", true);
        response.put("message", "已同意好友请求 ⚰️");
        return response;
    }

    // ====== 拒绝好友请求 ======
    @PostMapping("/reject")
    public Map<String, Object> rejectRequest(@RequestParam Long requestId) {
        Map<String, Object> response = new HashMap<>();

        FriendRequest request = friendRequestRepository.findById(requestId).orElse(null);
        if (request == null) {
            response.put("success", false);
            response.put("message", "请求不存在");
            return response;
        }

        request.setStatus("rejected");
        friendRequestRepository.save(request);

        response.put("success", true);
        response.put("message", "已拒绝好友请求");
        return response;
    }

    // ====== 获取好友列表 ======
    @GetMapping("/list")
    public Map<String, Object> getFriends(@RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();

        List<Friend> friends = friendRepository.findFriendsByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Friend f : friends) {
            User friendUser = userRepository.findById(f.getFriendId()).orElse(null);
            if (friendUser != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", friendUser.getId());
                item.put("username", friendUser.getUsername());
                result.add(item);
            }
        }

        response.put("success", true);
        response.put("data", result);
        return response;
    }

    // ====== 删除好友 ======
    @DeleteMapping("/delete")
    public Map<String, Object> deleteFriend(@RequestParam Long userId, @RequestParam Long friendId) {
        Map<String, Object> response = new HashMap<>();

        friendRepository.deleteByUserIdAndFriendId(userId, friendId);
        friendRepository.deleteByUserIdAndFriendId(friendId, userId);

        response.put("success", true);
        response.put("message", "已删除好友 ⚰️");
        return response;
    }
}