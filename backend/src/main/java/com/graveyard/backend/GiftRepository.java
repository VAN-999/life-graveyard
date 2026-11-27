package com.graveyard.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface GiftRepository extends JpaRepository<Gift, Long> {
    Optional<Gift> findByRequestId(String requestId);
    List<Gift> findBySenderIdOrRecipientIdOrderByCreatedAtDescIdDesc(Long senderId, Long recipientId, Pageable pageable);
}
