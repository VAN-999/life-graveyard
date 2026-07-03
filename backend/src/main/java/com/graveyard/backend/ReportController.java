package com.graveyard.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private DailyReportRepository dailyReportRepository;

    @GetMapping("/today")
    public Map<String, Object> getTodayReport(@RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();
        LocalDate today = LocalDate.now();

        var reportOpt = dailyReportRepository.findByUserIdAndReportDate(userId, today);

        if (reportOpt.isPresent()) {
            response.put("success", true);
            response.put("report", reportOpt.get());
            return response;
        }

        DailyReport report = reportService.generateReport(userId, today);
        response.put("success", true);
        response.put("message", "报告已生成 ☠️");
        response.put("report", report);
        return response;
    }

    @PostMapping("/generate")
    public Map<String, Object> generateReport(@RequestParam Long userId, @RequestParam(required = false) String date) {
        Map<String, Object> response = new HashMap<>();
        LocalDate targetDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        DailyReport report = reportService.generateReport(userId, targetDate);

        response.put("success", true);
        response.put("message", "报告生成成功 ⚰️");
        response.put("report", report);
        return response;
    }

    @PostMapping("/generate-all")
    public Map<String, Object> generateAll(@RequestParam String date) {
        LocalDate targetDate = LocalDate.parse(date);
        int count = reportService.generateReportsForDate(targetDate);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "生成了 " + count + " 份报告");
        response.put("date", date);
        return response;
    }
}