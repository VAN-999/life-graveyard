package com.graveyard.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    List<FriendRequest> findByToUserIdAndStatus(Long toUserId, String status);
    List<FriendRequest> findByFromUserIdAndStatus(Long fromUserId, String status);
    Optional<FriendRequest> findByFromUserIdAndToUserId(Long fromUserId, Long toUserId);
    boolean existsByFromUserIdAndToUserIdAndStatus(Long fromUserId, Long toUserId, String status);
    void deleteByFromUserIdAndToUserId(Long fromUserId, Long toUserId);
}