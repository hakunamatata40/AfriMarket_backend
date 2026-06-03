package com.example.AfriMarket_backend.repository;

import com.example.AfriMarket_backend.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    // All conversations for a user (as consumer or producer)
    @Query("SELECT c FROM Conversation c WHERE c.consumer.id = :userId OR c.producer.id = :userId ORDER BY c.lastMessageAt DESC NULLS LAST")
    List<Conversation> findByUserId(@Param("userId") Long userId);

    // Find existing conversation between two users (for a specific offer or any)
    @Query("SELECT c FROM Conversation c WHERE c.consumer.id = :consumerId AND c.producer.id = :producerId AND (:offerId IS NULL OR c.offer.id = :offerId)")
    Optional<Conversation> findByConsumerAndProducerAndOffer(
            @Param("consumerId") Long consumerId,
            @Param("producerId") Long producerId,
            @Param("offerId") Long offerId);
}
