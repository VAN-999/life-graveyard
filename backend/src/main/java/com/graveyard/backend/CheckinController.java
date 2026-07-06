package com.graveyard.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/v1/checkin")
public class CheckinController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CheckinRepository checkinRepository;

    // ====== 签到 ======
    @PostMapping("/do")
    public Map<String, Object> doCheckin(@RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return response;
        }

        LocalDate today = LocalDate.now();

        // 检查今天是否已签到
        if (checkinRepository.existsByUserIdAndCheckinDate(userId, today)) {
            response.put("success", false);
            response.put("message", "今天已经签到过了 💀");
            response.put("alreadyChecked", true);
            return response;
        }

        // 计算连续签到天数
        LocalDate yesterday = today.minusDays(1);
        int consecutiveDays = 1;
        Optional<Checkin> yesterdayCheckin = checkinRepository.findByUserIdAndCheckinDate(userId, yesterday);
        if (yesterdayCheckin.isPresent()) {
            consecutiveDays = yesterdayCheckin.get().getConsecutiveDays() + 1;
        }

        // 保存签到记录
        Checkin checkin = new Checkin();
        checkin.setUserId(userId);
        checkin.setCheckinDate(today);
        checkin.setConsecutiveDays(consecutiveDays);
        checkinRepository.save(checkin);

        // 发冥币
        int reward = 15;
        user.setHellMoney(user.getHellMoney() + reward);
        userRepository.save(user);

        // 顺便检查"墓园常客"任务
        // 前端会调用 /tasks/check 来触发任务检查

        response.put("success", true);
        response.put("message", "签到成功！获得了 " + reward + " 冥币 ⚰️");
        response.put("reward", reward);
        response.put("consecutiveDays", consecutiveDays);
        response.put("totalMoney", user.getHellMoney());
        return response;
    }

    // ====== 获取签到记录（月历） ======
    @GetMapping("/calendar")
    public Map<String, Object> getCalendar(@RequestParam Long userId, @RequestParam(required = false) String month) {
        Map<String, Object> response = new HashMap<>();

        YearMonth yearMonth;
        if (month != null) {
            yearMonth = YearMonth.parse(month);
        } else {
            yearMonth = YearMonth.now();
        }

        // 获取该月所有签到日期
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        List<Checkin> checkins = checkinRepository.findByUserIdAndCheckinDateBetween(userId, start, end);

        Set<String> checkedDates = new HashSet<>();
        for (Checkin c : checkins) {
            checkedDates.add(c.getCheckinDate().toString());
        }

        // 构建月历数据
        List<Map<String, Object>> days = new ArrayList<>();
        int daysInMonth = yearMonth.lengthOfMonth();
        int firstDayOfWeek = yearMonth.atDay(1).getDayOfWeek().getValue(); // 1=周一, 7=周日

        for (int i = 1; i <= daysInMonth; i++) {
            LocalDate date = yearMonth.atDay(i);
            Map<String, Object> day = new HashMap<>();
            day.put("day", i);
            day.put("date", date.toString());
            day.put("checked", checkedDates.contains(date.toString()));
            day.put("isToday", date.equals(LocalDate.now()));
            days.add(day);
        }

        response.put("success", true);
        response.put("year", yearMonth.getYear());
        response.put("month", yearMonth.getMonthValue());
        response.put("days", days);
        response.put("firstDayOfWeek", firstDayOfWeek);
        response.put("checkedCount", checkedDates.size());
        return response;
    }

    // ====== 获取签到统计 ======
    @GetMapping("/stats")
    public Map<String, Object> getStats(@RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();

        LocalDate today = LocalDate.now();

        // 本月签到天数
        YearMonth yearMonth = YearMonth.now();
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        int monthCount = checkinRepository.countByUserIdAndCheckinDateBetween(userId, start, end);

        // 总签到天数
        int totalCount = checkinRepository.countByUserId(userId);

        // 今天是否已签到
        boolean todayChecked = checkinRepository.existsByUserIdAndCheckinDate(userId, today);

        // 连续签到天数
        int consecutiveDays = 0;
        Optional<Checkin> todayCheckin = checkinRepository.findByUserIdAndCheckinDate(userId, today);
        if (todayCheckin.isPresent()) {
            consecutiveDays = todayCheckin.get().getConsecutiveDays();
        } else {
            // 检查昨天是否签到
            Optional<Checkin> yesterdayCheckin = checkinRepository.findByUserIdAndCheckinDate(userId, today.minusDays(1));
            if (yesterdayCheckin.isPresent()) {
                consecutiveDays = yesterdayCheckin.get().getConsecutiveDays();
            }
        }

        response.put("success", true);
        response.put("monthCount", monthCount);
        response.put("totalCount", totalCount);
        response.put("todayChecked", todayChecked);
        response.put("consecutiveDays", consecutiveDays);
        return response;
    }
}