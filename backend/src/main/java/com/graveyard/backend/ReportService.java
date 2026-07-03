package com.graveyard.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;

@Service
public class ReportService {

    @Autowired
    private DailyDataRepository dailyDataRepository;

    @Autowired
    private DailyReportRepository dailyReportRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserTaskRepository userTaskRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== 报告生成 ====================

    public DailyReport generateReport(Long userId, LocalDate date) {
        if (dailyReportRepository.existsByUserIdAndReportDate(userId, date)) {
            return dailyReportRepository.findByUserIdAndReportDate(userId, date).get();
        }

        Optional<DailyData> dataOpt = dailyDataRepository.findByUserIdAndRecordDate(userId, date);
        if (dataOpt.isEmpty()) {
            return generateEmptyReport(userId, date);
        }

        DailyData data = dataOpt.get();
        int score = calculateDeathScore(data);
        String reason = generateDeathReason(data);
        String epitaph = generateEpitaph(data, score);
        Map<String, Object> summaryMap = buildSummary(data, score, reason, epitaph);

        try {
            String summaryJson = objectMapper.writeValueAsString(summaryMap);
            DailyReport report = new DailyReport(userId, date, reason, epitaph, score, summaryJson);
            return dailyReportRepository.save(report);
        } catch (Exception e) {
            String fallback = "死亡指数：" + score + "%\n死亡原因：" + reason + "\n墓志铭：" + epitaph;
            DailyReport report = new DailyReport(userId, date, reason, epitaph, score, fallback);
            return dailyReportRepository.save(report);
        }
    }

    public int generateReportsForDate(LocalDate date) {
        List<Long> userIds = dailyDataRepository.findDistinctUserIdsByRecordDate(date);
        int count = 0;
        for (Long userId : userIds) {
            try {
                generateReport(userId, date);
                count++;
            } catch (Exception ignored) {}
        }
        return count;
    }

    // ==================== 任务分配 ====================

    public void assignDailyTasks(Long userId) {
        System.out.println("🔥🔥🔥 assignDailyTasks 被调用了！用户ID：" + userId);
        List<Task> allTasks = taskRepository.findByIsActiveTrue();
        if (allTasks.isEmpty()) return;

        LocalDate today = LocalDate.now();

        Collections.shuffle(allTasks);
        int count = 3 + new Random().nextInt(3);
        if (count > allTasks.size()) {
            count = allTasks.size();
        }
        List<Task> selected = allTasks.subList(0, count);

        for (Task task : selected) {
            Optional<UserTask> existing = userTaskRepository.findByUserIdAndTaskIdAndAssignDate(userId, task.getId(), today);
            if (existing.isEmpty()) {
                UserTask userTask = new UserTask(userId, task.getId(), today);
                userTaskRepository.save(userTask);
            }
        }
    }

    // ==================== 死亡指数计算 ====================

    private int calculateDeathScore(DailyData data) {
        int score = 0;
        if (data.getSteps() != null && data.getSteps() < 3000) score += 20;
        if (data.getScreenTimeMinutes() != null && data.getScreenTimeMinutes() > 480) score += 25;
        if (data.getSleepHours() != null && data.getSleepHours() < 6) score += 20;
        if (data.getLastActiveAt() != null) {
            try {
                int hour = Integer.parseInt(data.getLastActiveAt().split(":")[0]);
                if (hour >= 0 && hour < 4) score += 15;
            } catch (Exception ignored) {}
        }
        if (data.getAppOpens() != null && data.getAppOpens() > 30) score += 10;
        if (data.getMomentsViewed() != null && !data.getMomentsViewed()) score += 10;
        return Math.min(score, 100);
    }

    // ==================== 死亡原因（荒诞版） ====================

    private String generateDeathReason(DailyData data) {
        int steps = data.getSteps() != null ? data.getSteps() : 0;
        int screenTime = data.getScreenTimeMinutes() != null ? data.getScreenTimeMinutes() : 0;
        double sleep = data.getSleepHours() != null ? data.getSleepHours() : 8;
        String lastActive = data.getLastActiveAt() != null ? data.getLastActiveAt() : "00:00";
        int hour = 0;
        try {
            hour = Integer.parseInt(lastActive.split(":")[0]);
        } catch (Exception ignored) {}
        int appOpens = data.getAppOpens() != null ? data.getAppOpens() : 0;
        Boolean momentsViewed = data.getMomentsViewed();

        String[] reasons = {
                "走了" + steps + "步，活动范围约等于一张办公椅。恭喜，你活成了人类进化史上的一个括号。",
                "屏幕使用时间" + screenTime + "分钟。你的视网膜表示想辞职，但被你的拇指按住了。",
                "凌晨" + hour + "点还在刷手机。这个时间点，鬼都睡了，只有你还在给互联网当守夜人。",
                "睡了" + String.format("%.1f", sleep) + "个小时。其实你还可以更短，但你怕死得太明显。",
                "打开APP " + appOpens + "次。手机的电量比你的社交能量掉得更快。",
                "今天没看朋友圈。挺好，少知道点别人的好消息，对自己的心理健康有益。",
                "死于一种现代都市病——又想活又不想动，又想成功又不想努力，又想恋爱又不想出门。",
                "被算法推荐了太多你根本不感兴趣的东西，你忍着看完了，然后死了。"
        };

        if (steps < 2000) return reasons[0];
        if (screenTime > 480) return reasons[1];
        if (hour >= 0 && hour < 4) return reasons[2];
        if (sleep < 5) return reasons[3];
        if (appOpens > 20) return reasons[4];
        if (momentsViewed != null && !momentsViewed) return reasons[5];
        if (steps < 6000) return reasons[6];
        return reasons[7];
    }

    // ==================== 墓志铭（荒诞版） ====================

    private String generateEpitaph(DailyData data, int score) {
        int steps = data.getSteps() != null ? data.getSteps() : 0;
        int screenTime = data.getScreenTimeMinutes() != null ? data.getScreenTimeMinutes() : 0;
        int keyPresses = data.getKeyPresses() != null ? data.getKeyPresses() : 0;

        String[] epitaphs = {
                "他今天走了" + steps + "步。其中" + Math.max(1, steps / 2) + "步是在房间里找手机。",
                "盯着屏幕看了" + screenTime + "分钟。他看过别人的生活，唯独没过好自己的。",
                "今天敲了" + keyPresses + "次键盘。比昨天多，但依然改变不了任何事。",
                "他今天试图改变人生。失败了。明天继续试图改变人生。",
                "他的收藏夹里躺着127个以后再看视频，今天他又收藏了7个，然后一个都没看。",
                "他今天过得不好也不坏。就是那种——说不上来哪里不好，但总觉得哪里不太对的日子。",
                "别难过。他不是真的死了，他只是今天活够了。明天还会醒的，毕竟闹钟还没关。"
        };

        if (score >= 70) return epitaphs[1] + " " + epitaphs[2];
        if (score >= 40) return epitaphs[3] + " " + epitaphs[5];
        return epitaphs[6];
    }

    // ==================== 摘要组装 ====================

    private Map<String, Object> buildSummary(DailyData data, int score, String reason, String epitaph) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("deathScore", score);
        summary.put("deathReason", reason);
        summary.put("epitaph", epitaph);
        summary.put("steps", data.getSteps() != null ? data.getSteps() : "未记录");
        summary.put("screenTime", data.getScreenTimeMinutes() != null ? data.getScreenTimeMinutes() + " 分钟" : "未记录");
        summary.put("keyPresses", data.getKeyPresses() != null ? data.getKeyPresses() + " 次" : "未记录");
        summary.put("sleepHours", data.getSleepHours() != null ? data.getSleepHours() + " 小时" : "未记录");
        summary.put("lastActiveAt", data.getLastActiveAt() != null ? data.getLastActiveAt() : "未记录");
        summary.put("appOpens", data.getAppOpens() != null ? data.getAppOpens() + " 次" : "未记录");
        summary.put("momentsViewed", data.getMomentsViewed() != null ? (data.getMomentsViewed() ? "看了" : "没看") : "未记录");
        return summary;
    }

    // ==================== 空报告 ====================

    private DailyReport generateEmptyReport(Long userId, LocalDate date) {
        String summary = "{\"deathScore\":0,\"deathReason\":\"什么也没留下，仿佛不曾活过\",\"epitaph\":\"此人今天什么都没干，连死神都找不到他。\"}";
        return new DailyReport(userId, date, "死于毫无痕迹", "此人今天什么都没留下，仿佛不曾活过。", 0, summary);
    }
}