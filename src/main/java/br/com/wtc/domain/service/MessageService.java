package br.com.wtc.domain.service;

import br.com.wtc.domain.model.Campaign;
import br.com.wtc.domain.model.Message;
import br.com.wtc.domain.repository.MessageRepository;
import br.com.wtc.domain.repository.UserRepository;
import br.com.wtc.infra.FirebasePushService;
import br.com.wtc.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    private final MessageRepository  messageRepository;
    private final UserRepository     userRepository;
    private final FirebasePushService firebasePushService;

    public MessageService(MessageRepository messageRepository,
                          UserRepository userRepository,
                          FirebasePushService firebasePushService) {
        this.messageRepository   = messageRepository;
        this.userRepository      = userRepository;
        this.firebasePushService = firebasePushService;
    }

    // ── Mensagem direta 1:1 ───────────────────────────────────────────────────
    public Message sendDirect(String senderId, String recipientId,
                              String title, String body) {

        String recipientEmail = recipientId;
        if (!recipientId.contains("@")) {
            recipientEmail = userRepository.findById(recipientId)
                    .map(u -> u.getEmail()).orElse(recipientId);
        }

        String senderEmail = senderId;
        if (!senderId.contains("@")) {
            senderEmail = userRepository.findById(senderId)
                    .map(u -> u.getEmail()).orElse(senderId);
        }

        String conversationId = senderEmail.compareTo(recipientEmail) < 0
                ? senderEmail + "_" + recipientEmail
                : recipientEmail + "_" + senderEmail;

        Message message = Message.builder()
                .conversationId(conversationId)
                .senderId(senderEmail)
                .recipientId(recipientEmail)
                .title(title)
                .body(body)
                .type(Message.MessageType.CHAT)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        Message saved = messageRepository.save(message);

        final String finalRecipientEmail = recipientEmail;
        final String finalSenderEmail    = senderEmail;
        final String finalConversationId = conversationId;
        userRepository.findByEmail(finalRecipientEmail).ifPresent(recipient -> {
            if (recipient.getFcmToken() != null) {
                firebasePushService.sendMessagePush(
                        recipient.getFcmToken(), finalSenderEmail, body, finalConversationId);
            }
        });

        return saved;
    }

    // ── Mensagem de grupo ─────────────────────────────────────────────────────
    public Message sendGroup(String senderId, String groupId,
                             String title, String body) {

        Message message = Message.builder()
                .conversationId(groupId)
                .senderId(senderId)
                .groupId(groupId)
                .title(title)
                .body(body)
                .type(Message.MessageType.CHAT)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        return messageRepository.save(message);
    }

    // ── Campanha — disparo inicial ────────────────────────────────────────────
    public Message sendCampaignMessage(Campaign campaign, String recipientId) {

        Message message = Message.builder()
                .conversationId("campaign_" + campaign.getId())
                .senderId(campaign.getCreatedByUserId())
                .recipientId(recipientId)
                .title(campaign.getTitle())
                .body(campaign.getBody())
                .url(campaign.getUrl())
                .actions(campaign.getActions())
                .type(Message.MessageType.CAMPAIGN)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        Message saved = messageRepository.save(message);

        userRepository.findByEmail(recipientId).ifPresent(recipient -> {
            if (recipient.getFcmToken() != null) {
                firebasePushService.sendCampaignPush(
                        recipient.getFcmToken(),
                        campaign.getTitle(), campaign.getBody(),
                        campaign.getUrl(), campaign.getId());
            }
        });

        // Fallback por ID
        userRepository.findById(recipientId).ifPresent(recipient -> {
            if (recipient.getFcmToken() != null) {
                firebasePushService.sendCampaignPush(
                        recipient.getFcmToken(),
                        campaign.getTitle(), campaign.getBody(),
                        campaign.getUrl(), campaign.getId());
            }
        });

        return saved;
    }

    // ── Campanha — propagar edição para mensagens já enviadas ─────────────────
    public int propagateCampaignUpdate(Campaign campaign) {
        String conversationId = "campaign_" + campaign.getId();

        List<Message> existingMessages = messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversationId);

        if (existingMessages.isEmpty()) return 0;

        for (Message msg : existingMessages) {
            msg.setTitle(campaign.getTitle());
            msg.setBody(campaign.getBody());
            msg.setUrl(campaign.getUrl());
            msg.setActions(campaign.getActions());
            messageRepository.save(msg);

            // Push de atualização para cada destinatário
            String recipientId = msg.getRecipientId();
            if (recipientId != null) {
                // Tenta por email
                userRepository.findByEmail(recipientId).ifPresent(recipient -> {
                    if (recipient.getFcmToken() != null) {
                        firebasePushService.sendCampaignUpdatedPush(
                                recipient.getFcmToken(),
                                campaign.getTitle(),
                                campaign.getBody(),
                                campaign.getId());
                    }
                });
                // Fallback por ID
                userRepository.findById(recipientId).ifPresent(recipient -> {
                    if (recipient.getFcmToken() != null) {
                        firebasePushService.sendCampaignUpdatedPush(
                                recipient.getFcmToken(),
                                campaign.getTitle(),
                                campaign.getBody(),
                                campaign.getId());
                    }
                });
            }
        }

        return existingMessages.size();
    }

    // ── Histórico de conversa ─────────────────────────────────────────────────
    public List<Message> getConversation(String conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    // ── Todas as mensagens do usuário (1:1) ───────────────────────────────────
    public List<Message> getMyConversations(String userEmail, String userId) {
        return messageRepository
                .findBySenderIdOrRecipientIdOrderByCreatedAtAsc(userEmail, userId);
    }

    // ── Mensagens não lidas ───────────────────────────────────────────────────
    public List<Message> getUnread(String userId) {
        return messageRepository.findByRecipientIdAndReadFalse(userId);
    }

    public long countUnread(String userId) {
        return messageRepository.countByRecipientIdAndReadFalse(userId);
    }

    // ── Marcar como lida ──────────────────────────────────────────────────────
    public Message markAsRead(String messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Mensagem", messageId));
        message.setRead(true);
        return messageRepository.save(message);
    }

    public void markConversationAsRead(String conversationId, String userId) {
        messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .filter(m -> userId.equals(m.getRecipientId()) && !m.isRead())
                .forEach(m -> {
                    m.setRead(true);
                    messageRepository.save(m);
                });
    }
}