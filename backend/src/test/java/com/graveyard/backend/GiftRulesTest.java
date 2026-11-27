package com.graveyard.backend;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GiftRulesTest {
    @InjectMocks GiftController controller;
    @Mock GiftRepository gifts;
    @Mock FriendRepository friends;
    @Mock UserRepository users;
    @Mock DecorationRepository decorations;
    @Mock UserDecorationRepository inventory;
    @Mock DecorationStateRepository states;
    @Mock EntityManager entityManager;

    @Test
    void transfersItemAndSavesMessage() {
        when(entityManager.find(eq(User.class), anyLong(), eq(LockModeType.PESSIMISTIC_WRITE))).thenReturn(new User());
        when(friends.isFriend(1L, 2L)).thenReturn(true);
        UserDecoration item = new UserDecoration(1L, 10L);
        item.setId(100L);
        item.setIsEquipped(true);
        when(entityManager.find(UserDecoration.class, 100L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(item);
        Decoration decoration = mock(Decoration.class);
        when(decoration.getName()).thenReturn("菊花");
        when(decorations.findById(10L)).thenReturn(Optional.of(decoration));
        var request = new GiftController.GiftRequest(1L, 2L, 100L, " 留言 ", UUID.randomUUID().toString());
        assertEquals(true, controller.send(request).get("success"));
        assertEquals(2L, item.getUserId());
        assertEquals(false, item.getIsEquipped());
        ArgumentCaptor<Gift> saved = ArgumentCaptor.forClass(Gift.class);
        verify(gifts).save(saved.capture());
        assertEquals("留言", saved.getValue().message);
        when(gifts.findByRequestId(request.requestId())).thenReturn(Optional.of(saved.getValue()));
        assertEquals(true, controller.send(request).get("success"));
        verify(gifts, times(1)).save(any());
        verify(states, times(1)).deleteByUserDecorationId(100L);
    }

    @Test
    void rejectsLongMessagesBeforeChangingAnything() {
        assertEquals(false, controller.send(new GiftController.GiftRequest(1L, 2L, 100L,
                "字".repeat(201), UUID.randomUUID().toString())).get("success"));
        verifyNoInteractions(entityManager, inventory, states, gifts);
    }
}
