package com.graveyard.backend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GraveVisitTest {
    @InjectMocks GraveVisitController controller;
    @Mock UserRepository users;
    @Mock GraveRepository graves;
    @Mock FriendRepository friends;
    @Mock UserDecorationRepository inventory;
    @Mock DecorationRepository decorations;
    @Mock DecorationStateRepository states;
    @Mock DailyReportRepository reports;

    @Test
    void nonFriendCannotViewGrave() {
        when(users.findById(2L)).thenReturn(Optional.of(new User()));
        assertEquals(false, controller.visit(2L, 1L).get("success"));
        verifyNoInteractions(inventory, reports, states);
    }

    @Test
    void friendSeesPublicDisplayAndLatestEpitaphOnly() {
        User owner = new User();
        owner.setUsername("测试墓园");
        when(users.findById(2L)).thenReturn(Optional.of(owner));
        when(friends.isFriend(1L, 2L)).thenReturn(true);
        when(graves.findByUserId(2L)).thenReturn(Optional.empty());
        DailyReport report = new DailyReport();
        report.setEpitaph("测试墓志铭");
        when(reports.findFirstByUserIdOrderByReportDateDesc(2L)).thenReturn(Optional.of(report));
        var response = controller.visit(2L, 1L);
        assertEquals(true, response.get("success"));
        Map<?, ?> data = (Map<?, ?>) response.get("data");
        assertEquals("测试墓志铭", data.get("epitaph"));
        assertEquals("default", data.get("tombstoneStyle"));
        assertEquals(Set.of("username", "tombstoneStyle", "epitaph", "decorations", "states"), data.keySet());
        verify(inventory).findEquippedByUserId(2L);
    }
}
