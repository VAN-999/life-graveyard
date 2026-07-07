package com.graveyard.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface RobberyLogRepository extends JpaRepository<RobberyLog, Long> {

    // 查询今天某用户是否偷过某好友
    @Query("SELECT COUNT(r) > 0 FROM RobberyLog r WHERE r.robberId = :robberId AND r.victimId = :victimId AND DATE(r.createdAt) = :today")
    boolean hasRobbedToday(@Param("robberId") Long robberId, @Param("victimId") Long victimId, @Param("today") LocalDate today);

    // 查询今天某用户是否触发过穷鬼豁免
    @Query("SELECT COUNT(r) > 0 FROM RobberyLog r WHERE r.robberId = :robberId AND r.penaltyType = 'POOR_EXEMPT' AND DATE(r.createdAt) = :today")
    boolean hasPoorExemptToday(@Param("robberId") Long robberId, @Param("today") LocalDate today);

    // 查询某用户的盗墓记录
    List<RobberyLog> findByRobberIdOrderByCreatedAtDesc(Long robberId);

    // 查询某用户被偷记录
    List<RobberyLog> findByVictimIdOrderByCreatedAtDesc(Long victimId);
}