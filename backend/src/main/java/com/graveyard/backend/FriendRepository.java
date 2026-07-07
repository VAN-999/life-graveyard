package com.graveyard.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    @Query("SELECT f FROM Friend f WHERE f.userId = :userId")
    List<Friend> findFriendsByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(f) > 0 FROM Friend f WHERE f.userId = :userId AND f.friendId = :friendId")
    boolean isFriend(@Param("userId") Long userId, @Param("friendId") Long friendId);

    // 删除好友（双向删除）
    void deleteByUserIdAndFriendId(Long userId, Long friendId);
}