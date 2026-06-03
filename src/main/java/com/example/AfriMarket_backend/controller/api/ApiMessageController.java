package com.example.AfriMarket_backend.controller.api;

import com.example.AfriMarket_backend.dto.ConversationDto;
import com.example.AfriMarket_backend.dto.MessageDto;
import com.example.AfriMarket_backend.model.Conversation;
import com.example.AfriMarket_backend.model.Message;
import com.example.AfriMarket_backend.model.Offer;
import com.example.AfriMarket_backend.model.User;
import com.example.AfriMarket_backend.repository.ConversationRepository;
import com.example.AfriMarket_backend.repository.MessageRepository;
import com.example.AfriMarket_backend.repository.OfferRepository;
import com.example.AfriMarket_backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/messages")
public class ApiMessageController {

    private final ConversationRepository conversationRepo;
    private final MessageRepository messageRepo;
    private final UserRepository userRepo;
    private final OfferRepository offerRepo;

    public ApiMessageController(ConversationRepository conversationRepo,
                                  MessageRepository messageRepo,
                                  UserRepository userRepo,
                                  OfferRepository offerRepo) {
        this.conversationRepo = conversationRepo;
        this.messageRepo = messageRepo;
        this.userRepo = userRepo;
        this.offerRepo = offerRepo;
    }

    // ─── List conversations for current user ────────────────────────────────

    @GetMapping("/conversations")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getConversations(@AuthenticationPrincipal User user) {
        if (user == null) return unauthorized();
        List<ConversationDto> dtos = conversationRepo.findByUserId(user.getId())
                .stream()
                .map(c -> {
                    // Force-load lazy fields
                    if (c.getConsumer() != null) c.getConsumer().getFullName();
                    if (c.getProducer() != null) c.getProducer().getFullName();
                    if (c.getOffer() != null) c.getOffer().getTitle();
                    return ConversationDto.from(c, user.getId());
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // ─── Get or create conversation with a producer ─────────────────────────

    @PostMapping("/conversations/start")
    @Transactional
    public ResponseEntity<?> startConversation(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal User user) {
        if (user == null) return unauthorized();

        Long producerId = getLong(body, "producerId");
        Long offerId    = getLong(body, "offerId");   // optional

        if (producerId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "producerId requis."));
        }

        User producer = userRepo.findById(producerId).orElse(null);
        if (producer == null) return ResponseEntity.notFound().build();

        // Determine consumer — the current user (must be CONSUMER role)
        // Producers can also contact other producers (for now allow both)
        Long consumerId = user.getId();

        // Check if conversation already exists
        Conversation conv = conversationRepo
                .findByConsumerAndProducerAndOffer(consumerId, producerId, offerId)
                .orElse(null);

        if (conv == null) {
            conv = new Conversation();
            conv.setConsumer(user);
            conv.setProducer(producer);
            if (offerId != null) {
                offerRepo.findById(offerId).ifPresent(conv::setOffer);
            }
            conv = conversationRepo.save(conv);
        }

        ConversationDto dto = ConversationDto.from(conv, user.getId());
        return ResponseEntity.ok(dto);
    }

    // ─── Get messages in a conversation ──────────────────────────────────────

    @GetMapping("/conversations/{id}")
    @Transactional
    public ResponseEntity<?> getMessages(@PathVariable Long id,
                                          @AuthenticationPrincipal User user) {
        if (user == null) return unauthorized();
        Conversation conv = conversationRepo.findById(id).orElse(null);
        if (conv == null) return ResponseEntity.notFound().build();

        // Check access
        boolean isParticipant = user.getId().equals(conv.getConsumer().getId())
                             || user.getId().equals(conv.getProducer().getId());
        if (!isParticipant) return forbidden();

        // Mark messages as read
        messageRepo.markAsRead(id, user.getId(), LocalDateTime.now());

        // Reset unread counter for this user
        if (user.getId().equals(conv.getConsumer().getId())) {
            conv.setUnreadConsumer(0);
        } else {
            conv.setUnreadProducer(0);
        }
        conversationRepo.save(conv);

        List<MessageDto> messages = messageRepo.findByConversationIdOrderBySentAtAsc(id)
                .stream().map(MessageDto::from).collect(Collectors.toList());

        ConversationDto dto = ConversationDto.from(conv, user.getId());
        dto.setMessages(messages);
        return ResponseEntity.ok(dto);
    }

    // ─── Send a message ───────────────────────────────────────────────────────

    @PostMapping("/conversations/{id}/send")
    @Transactional
    public ResponseEntity<?> sendMessage(@PathVariable Long id,
                                          @RequestBody Map<String, String> body,
                                          @AuthenticationPrincipal User user) {
        if (user == null) return unauthorized();

        String content = body.get("content");
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message vide."));
        }

        Conversation conv = conversationRepo.findById(id).orElse(null);
        if (conv == null) return ResponseEntity.notFound().build();

        boolean isParticipant = user.getId().equals(conv.getConsumer().getId())
                             || user.getId().equals(conv.getProducer().getId());
        if (!isParticipant) return forbidden();

        Message msg = new Message();
        msg.setConversation(conv);
        msg.setSender(user);
        msg.setContent(content.trim());
        msg = messageRepo.save(msg);

        // Update conversation summary
        conv.setLastMessage(content.length() > 60 ? content.substring(0, 60) + "…" : content);
        conv.setLastMessageAt(msg.getSentAt());

        // Increment unread for the OTHER participant
        if (user.getId().equals(conv.getConsumer().getId())) {
            conv.setUnreadProducer(conv.getUnreadProducer() + 1);
        } else {
            conv.setUnreadConsumer(conv.getUnreadConsumer() + 1);
        }
        conversationRepo.save(conv);

        return ResponseEntity.status(HttpStatus.CREATED).body(MessageDto.from(msg));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private ResponseEntity<?> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Non authentifié."));
    }
    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Accès refusé."));
    }
    private Long getLong(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) return null;
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return null; }
    }
}
