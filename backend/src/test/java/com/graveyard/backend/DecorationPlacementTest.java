package com.graveyard.backend;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DecorationPlacementTest {
    @InjectMocks DecorationController controller;
    @Mock DecorationRepository decorationRepository;
    @Mock UserDecorationRepository userDecorationRepository;
    @Mock DecorationStateRepository decorationStateRepository;

    @Test
    void newDecorationStartsAtStableAccessiblePosition() {
        DecorationState state = new DecorationState(1L, 10L);
        assertEquals(28.0, state.getX());
        assertEquals(70.0, state.getY());
    }

    @Test
    void equipKeepsExistingPosition() {
        UserDecoration item = new UserDecoration(1L, 2L);
        item.setId(10L);
        Decoration decoration = mock(Decoration.class);
        when(decoration.getName()).thenReturn("装饰");
        when(userDecorationRepository.findById(10L)).thenReturn(Optional.of(item));
        when(decorationRepository.findById(2L)).thenReturn(Optional.of(decoration));
        DecorationState state = new DecorationState(1L, 10L);
        state.setX(80.0);
        state.setY(20.0);
        when(decorationStateRepository.findByUserDecorationId(10L)).thenReturn(Optional.of(state));
        assertEquals(true, controller.equip(10L).get("success"));
        assertEquals(80.0, state.getX());
        verify(decorationStateRepository, never()).save(any());
    }
}
