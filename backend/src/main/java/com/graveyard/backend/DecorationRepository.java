package com.graveyard.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DecorationRepository extends JpaRepository<Decoration, Long> {
    List<Decoration> findByCategory(String category);
    List<Decoration> findByIsDefaultTrue();
}