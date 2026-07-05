package com.graveyard.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DecorationStateRepository extends JpaRepository<DecorationState, Long> {
    List<DecorationState> findByUserId(Long userId);
    Optional<DecorationState> findByUserDecorationId(Long userDecorationId);
    void deleteByUserDecorationId(Long userDecorationId);
}