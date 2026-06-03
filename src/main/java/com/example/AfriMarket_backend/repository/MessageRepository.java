package com.example.AfriMarket_backend.repository;

import com.example.AfriMarket_backend.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationIdOrderBySentAtAsc(Long conversationId);

    @Modifying
    @Query("UPDATE Message m SET m.readAt = :now WHERE m.conversation.id = :convId AND m.sender.id != :userId AND m.readAt IS NULL")
    void markAsRead(@Param("convId") Long convId, @Param("userId") Long userId, @Param("now") LocalDateTime now);
}
