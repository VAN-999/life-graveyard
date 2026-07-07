package com.graveyard.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserTaskRepository userTaskRepository;

    @Autowired
    private DailyDataRepository dailyDataRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportService reportService;

    @GetMapping("/today")
    public Map<String, Object> getTodayTasks(@RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();

        if (!userRepository.existsById(userId)) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return response;
        }

        LocalDate today = LocalDate.now();
        List<UserTask> userTasks = userTaskRepository.findByUserIdAndAssignDate(userId, today);

        if (userTasks.isEmpty()) {
            reportService.assignDailyTasks(userId);
            userTasks = userTaskRepository.findByUserIdAndAssignDate(userId, today);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (UserTask ut : userTasks) {
            Task task = taskRepository.findById(ut.getTaskId()).orElse(null);
            if (task != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("taskId", task.getId());
                item.put("name", task.getName());
                item.put("description", task.getDescription());
                item.put("rewardMoney", task.getRewardMoney());
                item.put("conditionType", task.getConditionType());
                item.put("conditionValue", task.getConditionValue());
                item.put("isCompleted", ut.getIsCompleted());
                item.put("isClaimed", ut.getIsClaimed());
                item.put("userTaskId", ut.getId());
                result.add(item);
            }
        }

        int completedToday = userTaskRepository.countCompletedToday(userId, today);

        response.put("success", true);
        response.put("data", result);
        response.put("completedCount", completedToday);
        response.put("totalCount", result.size());
        return response;
    }

    @PostMapping("/assign")
    public Map<String, Object> assignTasks(@RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();
        reportService.assignDailyTasks(userId);
        response.put("success", true);
        response.put("message", "任务已分配 ☠️");
        return response;
    }

    @PostMapping("/claim")
    public Map<String, Object> claimReward(@RequestParam Long userTaskId) {
        Map<String, Object> response = new HashMap<>();

        UserTask userTask = userTaskRepository.findById(userTaskId).orElse(null);
        if (userTask == null) {
            response.put("success", false);
            response.put("message", "任务不存在");
            return response;
        }

        if (!userTask.getIsCompleted()) {
            response.put("success", false);
            response.put("message", "任务还没完成，继续努力吧 ☠️");
            return response;
        }

        if (userTask.getIsClaimed()) {
            response.put("success", false);
            response.put("message", "奖励已经领过了，别贪心");
            return response;
        }

        Task task = taskRepository.findById(userTask.getTaskId()).orElse(null);
        if (task == null) {
            response.put("success", false);
            response.put("message", "任务配置异常");
            return response;
        }

        User user = userRepository.findById(userTask.getUserId()).orElse(null);
        if (user == null) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return response;
        }

        user.setHellMoney(user.getHellMoney() + task.getRewardMoney());

        // 加经验
        int oldLevel = user.getExperience() / 10 + 1;
        user.setExperience(user.getExperience() + 5);
        userRepository.save(user);
        int newLevel = user.getExperience() / 10 + 1;

        boolean levelUp = newLevel > oldLevel;
        if (levelUp) {
            user.setHellMoney(user.getHellMoney() + 15);
            userRepository.save(user);
        }

        userTask.setIsClaimed(true);
        userTask.setClaimedAt(java.time.LocalDateTime.now());
        userTaskRepository.save(userTask);

        response.put("success", true);
        response.put("message", "领取成功！获得了 " + task.getRewardMoney() + " 冥币 💀");
        response.put("rewardMoney", task.getRewardMoney());
        response.put("currentMoney", user.getHellMoney());
        response.put("experience", user.getExperience());
        response.put("level", newLevel);
        response.put("levelUp", levelUp);
        if (levelUp) {
            response.put("levelUpMessage", "🎉 恭喜升级到 Lv." + newLevel + "！获得 15 冥币奖励！");
        }
        return response;
    }

    @PostMapping("/check")
    public Map<String, Object> checkTasks(@RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();

        LocalDate today = LocalDate.now();

        DailyData data = dailyDataRepository.findByUserIdAndRecordDate(userId, today).orElse(null);
        if (data == null) {
            response.put("success", false);
            response.put("message", "今天还没有数据，先提交数据再来检查任务吧");
            return response;
        }

        List<UserTask> userTasks = userTaskRepository.findByUserIdAndAssignDate(userId, today);
        if (userTasks.isEmpty()) {
            reportService.assignDailyTasks(userId);
            userTasks = userTaskRepository.findByUserIdAndAssignDate(userId, today);
        }

        int completed = 0;
        for (UserTask ut : userTasks) {
            if (ut.getIsCompleted()) continue;

            Task task = taskRepository.findById(ut.getTaskId()).orElse(null);
            if (task == null) continue;

            boolean isDone = checkCondition(data, task);
            if (isDone) {
                ut.setIsCompleted(true);
                ut.setCompletedAt(java.time.LocalDateTime.now());
                userTaskRepository.save(ut);
                completed++;
            }
        }

        response.put("success", true);
        response.put("message", "检查完成，" + completed + " 个任务已完成");
        response.put("completedCount", completed);
        return response;
    }

    private boolean checkCondition(DailyData data, Task task) {
        String type = task.getConditionType();
        int value = task.getConditionValue();

        switch (type) {
            case "STEPS":
                return data.getSteps() != null && data.getSteps() >= value;
            case "SCREEN_TIME":
                return data.getScreenTimeMinutes() != null && data.getScreenTimeMinutes() >= value;
            case "SLEEP":
                return data.getSleepHours() != null && data.getSleepHours() <= value;
            case "LAST_ACTIVE":
                if (data.getLastActiveAt() == null) return false;
                try {
                    int hour = Integer.parseInt(data.getLastActiveAt().split(":")[0]);
                    // 凌晨 0 点到 value 点（如 value=4 表示 0-4 点）
                    return hour >= 0 && hour < value;
                } catch (Exception e) {
                    return false;
                }
            case "APP_OPENS":
                return data.getAppOpens() != null && data.getAppOpens() >= value;
            case "KEY_PRESSES":
                return data.getKeyPresses() != null && data.getKeyPresses() >= value;
            case "MOMENTS_VIEWED":
                return data.getMomentsViewed() != null && !data.getMomentsViewed();
            default:
                return false;
        }
    }
}