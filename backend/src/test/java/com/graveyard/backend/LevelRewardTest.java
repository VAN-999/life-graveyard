package com.graveyard.backend;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LevelRewardTest {
    @InjectMocks LevelRewardService service;
    @Mock UserRepository users;
    @Test void oldAccountReceivesCatchUpOnce() {
        User user = new User();
        user.setExperience(50);
        user.setRewardedLevel(null);
        assertEquals(150, service.settle(user));
        assertEquals(250, user.getHellMoney());
        assertEquals(6, user.getRewardedLevel());
        assertEquals(0, service.settle(user));
        verify(users, times(1)).save(user);
    }
    @Test void fifthLevelAddsMilestoneOnce() {
        User user = new User();
        user.setRewardedLevel(4);
        user.setExperience(40);
        assertEquals(70, service.settle(user));
        user.setExperience(50);
        assertEquals(20, service.settle(user));
    }
    @Test void startingLevelHasNoUpgradeReward() {
        assertEquals(0, service.settle(new User()));
        verifyNoInteractions(users);
    }
}
