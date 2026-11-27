package com.graveyard.backend;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RobberyRulesTest {
    @InjectMocks DecorationController controller;
    @Mock EntityManager entityManager;
    @Mock FriendRepository friendRepository;
    @Mock RobberyLogRepository robberyLogRepository;
    @Mock UserDecorationRepository userDecorationRepository;
    @Mock DecorationRepository decorationRepository;
    @Mock DecorationStateRepository decorationStateRepository;

    @BeforeEach
    void players() {
        when(entityManager.find(eq(User.class), anyLong(), eq(LockModeType.PESSIMISTIC_WRITE)))
                .thenReturn(new User());
        when(friendRepository.isFriend(1L, 2L)).thenReturn(true);
    }

    @Test
    void repeatedAttemptDoesNotTransferAnything() {
        when(robberyLogRepository.existsByRobberIdAndVictimIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                eq(1L), eq(2L), any(), any())).thenReturn(true);
        assertEquals(false, controller.rob(1L, 2L).get("success"));
        verifyNoInteractions(userDecorationRepository, decorationStateRepository);
    }

    @Test
    void emptyInventoryCannotAttempt() {
        when(userDecorationRepository.findByUserId(1L)).thenReturn(List.of());
        assertEquals(false, controller.rob(1L, 2L).get("success"));
        verify(robberyLogRepository, never()).save(any());
    }

    @Test
    void failureGivesOwnItemToFriendAndRecordsItsName() {
        transfer(0.9, 1L, 2L, "GIFT_DECORATION");
    }

    @Test
    void successGivesFriendsItemToRobber() {
        transfer(0.1, 2L, 1L, null);
    }

    private void transfer(double roll, Long originalOwner, Long recipient, String penaltyType) {
        UserDecoration own = new UserDecoration(1L, 10L);
        own.setId(100L);
        UserDecoration friend = new UserDecoration(2L, 10L);
        friend.setId(200L);
        when(userDecorationRepository.findByUserId(1L)).thenReturn(List.of(own));
        when(userDecorationRepository.findByUserId(2L)).thenReturn(List.of(friend));
        Decoration decoration = mock(Decoration.class);
        when(decoration.getId()).thenReturn(10L);
        when(decoration.getName()).thenReturn("测试装饰");
        when(decorationRepository.findById(10L)).thenReturn(Optional.of(decoration));
        try (MockedConstruction<Random> random = Mockito.mockConstruction(Random.class,
                (mock, context) -> when(mock.nextDouble()).thenReturn(roll))) {
            Map<String, Object> result = controller.rob(1L, 2L);
            assertEquals(true, result.get("success"));
            assertEquals(roll < 0.7, result.get("robbed"));
        }
        UserDecoration transferred = originalOwner.equals(1L) ? own : friend;
        assertEquals(recipient, transferred.getUserId());
        assertEquals(false, transferred.getIsEquipped());
        verify(decorationStateRepository).deleteByUserDecorationId(transferred.getId());
        ArgumentCaptor<RobberyLog> log = ArgumentCaptor.forClass(RobberyLog.class);
        verify(robberyLogRepository).save(log.capture());
        assertEquals("测试装饰", log.getValue().getDecorationName());
        assertEquals(penaltyType, log.getValue().getPenaltyType());
    }
}
