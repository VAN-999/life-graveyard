package com.graveyard.backend;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LevelRewardService {
    @Autowired private UserRepository users;

    // Level 1 is the starting level. The level-5 bonus is a one-time milestone.
    public int settle(User user) {
        int level = user.getExperience() / 10 + 1;
        int paid = user.getRewardedLevel() == null ? 1 : user.getRewardedLevel();
        int reward = Math.max(0, level - paid) * 20;
        if (paid < 5 && level >= 5) reward += 50;
        if (reward > 0) {
            user.setHellMoney(user.getHellMoney() + reward);
            user.setRewardedLevel(level);
            users.save(user);
        }
        return reward;
    }

    @Transactional
    public User sync(Long userId) {
        User user = users.findLockedById(userId).orElseThrow();
        settle(user);
        return user;
    }
}
