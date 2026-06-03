package com.example.AfriMarket_backend.dto;

import com.example.AfriMarket_backend.model.Message;

import java.time.LocalDateTime;

public class MessageDto {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private String content;
    private LocalDateTime sentAt;
    private boolean read;

    public static MessageDto from(Message m) {
        MessageDto d = new MessageDto();
        d.id             = m.getId();
        d.conversationId = m.getConversation() != null ? m.getConversation().getId() : null;
        if (m.getSender() != null) {
            d.senderId    = m.getSender().getId();
            d.senderName  = m.getSender().getFullName();
            d.senderAvatar = m.getSender().getAvatarUrl();
        }
        d.content = m.getContent();
        d.sentAt  = m.getSentAt();
        d.read    = m.getReadAt() != null;
        return d;
    }

    public Long getId() { return id; }
    public Long getConversationId() { return conversationId; }
    public Long getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getSenderAvatar() { return senderAvatar; }
    public String getContent() { return content; }
    public LocalDateTime getSentAt() { return sentAt; }
    public boolean isRead() { return read; }
}
