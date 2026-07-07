package com.graveyard.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/daily-data")
public class DailyDataController {

    @Autowired
    private DailyDataRepository dailyDataRepository;

    @Autowired
    private UserRepository userRepository;

    // 提交每日数据
    @PostMapping("/submit")
    public Map<String, Object> submit(@RequestBody DailyData data) {
        Map<String, Object> response = new HashMap<>();

        if (data.getRecordDate() == null) {
            data.setRecordDate(LocalDate.now());
        }

        DailyData existing = dailyDataRepository
                .findByUserIdAndRecordDate(data.getUserId(), data.getRecordDate())
                .orElse(null);

        if (existing != null) {
            existing.setSteps(data.getSteps());
            existing.setScreenTimeMinutes(data.getScreenTimeMinutes());
            existing.setKeyPresses(data.getKeyPresses());
            existing.setSleepHours(data.getSleepHours());
            existing.setAppOpens(data.getAppOpens());
            existing.setLastActiveAt(data.getLastActiveAt());
            existing.setMomentsViewed(data.getMomentsViewed());
            dailyDataRepository.save(existing);

            // 加经验
            User user = userRepository.findById(data.getUserId()).orElse(null);
            if (user != null) {
                int oldLevel = user.getExperience() / 10 + 1;
                user.setExperience(user.getExperience() + 3);
                userRepository.save(user);
                int newLevel = user.getExperience() / 10 + 1;
                if (newLevel > oldLevel) {
                    user.setHellMoney(user.getHellMoney() + 15);
                    userRepository.save(user);
                }
            }

            response.put("success", true);
            response.put("message", "今日数据已更新 ⚰️");
            response.put("recordDate", existing.getRecordDate());
            return response;
        }

        dailyDataRepository.save(data);

        // 加经验
        User user = userRepository.findById(data.getUserId()).orElse(null);
        if (user != null) {
            int oldLevel = user.getExperience() / 10 + 1;
            user.setExperience(user.getExperience() + 3);
            userRepository.save(user);
            int newLevel = user.getExperience() / 10 + 1;
            if (newLevel > oldLevel) {
                user.setHellMoney(user.getHellMoney() + 15);
                userRepository.save(user);
            }
        }

        response.put("success", true);
        response.put("message", "今日数据提交成功 ⚰️");
        response.put("recordDate", data.getRecordDate());
        return response;
    }

    // 查询今日数据
    @GetMapping("/today")
    public Map<String, Object> getToday(@RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();

        DailyData data = dailyDataRepository
                .findByUserIdAndRecordDate(userId, LocalDate.now())
                .orElse(null);

        if (data == null) {
            response.put("success", false);
            response.put("message", "今天还没提交数据，你是死人吗？");
            return response;
        }

        response.put("success", true);
        response.put("data", data);
        return response;
    }
}