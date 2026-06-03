package com.example.AfriMarket_backend.dto;

import com.example.AfriMarket_backend.model.Conversation;

import java.time.LocalDateTime;
import java.util.List;

public class ConversationDto {
    private Long id;
    private Long consumerId;
    private String consumerName;
    private String consumerAvatar;
    private Long producerId;
    private String producerName;
    private String producerAvatar;
    private Long offerId;
    private String offerTitle;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private int unreadCount;    // from current user's perspective
    private LocalDateTime createdAt;
    private List<MessageDto> messages; // optional, loaded on detail

    public static ConversationDto from(Conversation c, Long currentUserId) {
        ConversationDto d = new ConversationDto();
        d.id = c.getId();
        if (c.getConsumer() != null) {
            d.consumerId   = c.getConsumer().getId();
            d.consumerName  = c.getConsumer().getFullName();
            d.consumerAvatar = c.getConsumer().getAvatarUrl();
        }
        if (c.getProducer() != null) {
            d.producerId   = c.getProducer().getId();
            d.producerName  = c.getProducer().getFullName();
            d.producerAvatar = c.getProducer().getAvatarUrl();
        }
        if (c.getOffer() != null) {
            d.offerId    = c.getOffer().getId();
            d.offerTitle = c.getOffer().getTitle();
        }
        d.lastMessage   = c.getLastMessage();
        d.lastMessageAt = c.getLastMessageAt();
        d.createdAt     = c.getCreatedAt();
        // Unread count depends on who is reading
        if (currentUserId != null && c.getConsumer() != null
                && currentUserId.equals(c.getConsumer().getId())) {
            d.unreadCount = c.getUnreadConsumer();
        } else {
            d.unreadCount = c.getUnreadProducer();
        }
        return d;
    }

    // Getters
    public Long getId() { return id; }
    public Long getConsumerId() { return consumerId; }
    public String getConsumerName() { return consumerName; }
    public String getConsumerAvatar() { return consumerAvatar; }
    public Long getProducerId() { return producerId; }
    public String getProducerName() { return producerName; }
    public String getProducerAvatar() { return producerAvatar; }
    public Long getOfferId() { return offerId; }
    public String getOfferTitle() { return offerTitle; }
    public String getLastMessage() { return lastMessage; }
    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public int getUnreadCount() { return unreadCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<MessageDto> getMessages() { return messages; }
    public void setMessages(List<MessageDto> m) { this.messages = m; }
}
