package com.graveyard.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;

@Component
public class ScheduledTask {

    @Autowired
    private DailyDataRepository dailyDataRepository;

    @Autowired
    private ReportService reportService;

    /**
     * 每天凌晨 00:01 执行
     * 为前一天所有有数据的用户生成死亡报告
     */
    @Scheduled(cron = "0 1 0 * * ?")
    public void generateDailyReports() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        System.out.println("☠️ 开始生成 " + yesterday + " 的死亡报告...");

        List<Long> userIds = dailyDataRepository.findDistinctUserIdsByRecordDate(yesterday);

        if (userIds.isEmpty()) {
            System.out.println("💀 昨天没人提交数据，报告暂停生成");
            return;
        }

        int successCount = 0;
        for (Long userId : userIds) {
            try {
                reportService.generateReport(userId, yesterday);
                successCount++;
                System.out.println("✅ 用户 " + userId + " 报告生成成功");
            } catch (Exception e) {
                System.err.println("❌ 用户 " + userId + " 报告生成失败: " + e.getMessage());
            }
        }

        System.out.println("☠️ 报告生成完成！成功 " + successCount + "/" + userIds.size() + " 份");
    }
}