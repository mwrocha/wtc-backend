package br.com.wtc.domain.service;

import br.com.wtc.domain.model.Campaign;
import br.com.wtc.domain.model.Conversation;
import br.com.wtc.domain.model.Message;
import br.com.wtc.domain.repository.MessageRepository;
import br.com.wtc.domain.repository.UserRepository;
import br.com.wtc.infra.FirebasePushService;
import br.com.wtc.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final FirebasePushService firebasePushService;
    private final ConversationService conversationService;

    public MessageService(MessageRepository messageRepository, UserRepository userRepository, FirebasePushService firebasePushService, ConversationService conversationService) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.firebasePushService = firebasePushService;
        this.conversationService = conversationService;
    }

    // ── Mensagem direta 1:1 ───────────────────────────────────────────────────
    public Message sendDirect(String senderId, String recipientId, String title, String body) {

        String recipientEmail = recipientId;
        if (!recipientId.contains("@")) {
            recipientEmail = userRepository.findById(recipientId).map(u -> u.getEmail()).orElse(recipientId);
        }

        String senderEmail = senderId;
        if (!senderId.contains("@")) {
            senderEmail = userRepository.findById(senderId).map(u -> u.getEmail()).orElse(senderId);
        }

        if (senderEmail.equals(recipientEmail)) {
            final String clientEmail = senderEmail;
            String operatorEmail = userRepository.findByRole("OPERATOR").stream().filter(u -> u.isActive()).findFirst().map(u -> u.getEmail()).orElse(null);

            if (operatorEmail != null) {
                recipientEmail = operatorEmail;
                log.info("Distribuição automática: cliente={} → operador={}", clientEmail, operatorEmail);
            } else {
                log.warn("Nenhum operador ativo encontrado para atender: {}", clientEmail);
            }
        }

        String conversationId = senderEmail.compareTo(recipientEmail) < 0 ? senderEmail + "_" + recipientEmail : recipientEmail + "_" + senderEmail;

        Message message = Message.builder().conversationId(conversationId).senderId(senderEmail).recipientId(recipientEmail).title(title).body(body).type(Message.MessageType.CHAT).status(Message.MessageStatus.SENT).read(false).createdAt(LocalDateTime.now()).build();

        Message saved = messageRepository.save(message);

        final String finalSenderEmail = senderEmail;
        final String finalRecipientEmail = recipientEmail;
        final String finalConversationId = conversationId;
        final String finalMessageId = saved.getId();

        log.info("DEBUG sendDirect: sender={} recipient={} conversationId={}", finalSenderEmail, finalRecipientEmail, finalConversationId);

        boolean senderIsClient = userRepository.findByEmail(finalSenderEmail).map(u -> "CLIENT".equals(u.getRole())).orElse(false);

        if (senderIsClient) {
            log.info("Cliente detectado, adicionando à fila: {}", finalConversationId);
            conversationService.onClientMessageSent(finalConversationId, finalSenderEmail, body.length() > 60 ? body.substring(0, 60) + "..." : body);

            conversationService.getByConversationId(finalConversationId).ifPresentOrElse(conv -> {
                log.info("DEBUG conversa encontrada: id={} status={} operador={}", conv.getConversationId(), conv.getStatus(), conv.getAssignedOperatorEmail());

                if (conv.getStatus() == Conversation.ConversationStatus.IN_PROGRESS && conv.getAssignedOperatorEmail() != null) {
                    log.info("Conversa IN_PROGRESS → push para operador: {}", conv.getAssignedOperatorEmail());
                    userRepository.findByEmail(conv.getAssignedOperatorEmail()).ifPresent(operator -> {
                        if (operator.getFcmToken() != null) {
                            firebasePushService.sendMessagePush(operator.getFcmToken(), finalSenderEmail, body, finalConversationId, finalMessageId);
                        }
                    });
                } else {
                    log.info("Conversa {} → sem push individual, fila notifica", conv.getStatus());
                }
            }, () -> {
                log.info("Conversa nova → sem push individual, fila notifica");
            });

        } else {
            userRepository.findByEmail(finalRecipientEmail).ifPresent(recipient -> {
                if (recipient.getFcmToken() != null) {
                    firebasePushService.sendMessagePush(recipient.getFcmToken(), finalSenderEmail, body, finalConversationId, finalMessageId);
                }
            });
        }

        return saved;
    }

    // ── Mensagem de grupo ─────────────────────────────────────────────────────
    public Message sendGroup(String senderId, String groupId, String title, String body) {

        Message message = Message.builder().conversationId(groupId).senderId(senderId).groupId(groupId).title(title).body(body).type(Message.MessageType.CHAT).status(Message.MessageStatus.SENT).read(false).createdAt(LocalDateTime.now()).build();

        Message saved = messageRepository.save(message);

        // ── FCM para todos os membros do grupo (exceto o remetente) ──────────
        userRepository.findByGroupId(groupId).forEach(member -> {
            if (!member.getEmail().equals(senderId) && member.getFcmToken() != null) {
                // Busca nome do grupo para exibir no push
                // Usa senderId como remetente no título
                firebasePushService.sendGroupPush(member.getFcmToken(), senderId, "Grupo",   // nome do grupo — ajuste se tiver GroupService disponível
                        body, groupId);
                log.info("Push de grupo enviado para: {}", member.getEmail());
            }
        });

        return saved;
    }

    // ── Campanha — disparo inicial ────────────────────────────────────────────
    public Message sendCampaignMessage(Campaign campaign, String recipientId) {

        Message message = Message.builder().conversationId("campaign_" + campaign.getId()).senderId(campaign.getCreatedByUserId()).recipientId(recipientId).title(campaign.getTitle()).body(campaign.getBody()).url(campaign.getUrl()).actions(campaign.getActions()).actionUrls(campaign.getActionUrls()).type(Message.MessageType.CAMPAIGN).status(Message.MessageStatus.SENT).read(false).createdAt(LocalDateTime.now()).build();

        Message saved = messageRepository.save(message);

        userRepository.findByEmail(recipientId).ifPresent(recipient -> {
            if (recipient.getFcmToken() != null) {
                firebasePushService.sendCampaignPush(recipient.getFcmToken(), campaign.getTitle(), campaign.getBody(), campaign.getUrl(), campaign.getId());
            }
        });

        userRepository.findById(recipientId).ifPresent(recipient -> {
            if (recipient.getFcmToken() != null) {
                firebasePushService.sendCampaignPush(recipient.getFcmToken(), campaign.getTitle(), campaign.getBody(), campaign.getUrl(), campaign.getId());
            }
        });

        return saved;
    }

    // ── Campanha — propagar edição ────────────────────────────────────────────
    public int propagateCampaignUpdate(Campaign campaign) {
        String conversationId = "campaign_" + campaign.getId();

        List<Message> existingMessages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);

        if (existingMessages.isEmpty()) return 0;

        for (Message msg : existingMessages) {
            msg.setTitle(campaign.getTitle());
            msg.setBody(campaign.getBody());
            msg.setUrl(campaign.getUrl());
            msg.setActions(campaign.getActions());
            msg.setActionUrls(campaign.getActionUrls());
            messageRepository.save(msg);

            String recipientId = msg.getRecipientId();
            if (recipientId != null) {
                userRepository.findByEmail(recipientId).ifPresent(recipient -> {
                    if (recipient.getFcmToken() != null) {
                        firebasePushService.sendCampaignUpdatedPush(recipient.getFcmToken(), campaign.getTitle(), campaign.getBody(), campaign.getId());
                    }
                });
                userRepository.findById(recipientId).ifPresent(recipient -> {
                    if (recipient.getFcmToken() != null) {
                        firebasePushService.sendCampaignUpdatedPush(recipient.getFcmToken(), campaign.getTitle(), campaign.getBody(), campaign.getId());
                    }
                });
            }
        }

        return existingMessages.size();
    }

    // ── Histórico de conversa → marca como DELIVERED ──────────────────────────
    public List<Message> getConversation(String conversationId, String requestingUserEmail) {
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);

        messages.stream().filter(m -> requestingUserEmail.equals(m.getRecipientId()) && m.getStatus() == Message.MessageStatus.SENT).forEach(m -> {
            m.setStatus(Message.MessageStatus.DELIVERED);
            messageRepository.save(m);
        });

        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    public List<Message> getConversation(String conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    // ── Todas as mensagens do usuário (1:1) ───────────────────────────────────
    public List<Message> getMyConversations(String userEmail, String userId) {
        return messageRepository.findBySenderIdOrRecipientIdOrderByCreatedAtAsc(userEmail, userId);
    }

    // ── Mensagens não lidas ───────────────────────────────────────────────────
    public List<Message> getUnread(String userId) {
        return messageRepository.findByRecipientIdAndReadFalse(userId);
    }

    /**
     * Conta mensagens CHAT não lidas — 1:1 + grupo do cliente.
     */
    public long countUnread(String userId) {
        // 1:1 não lidas
        long direct = messageRepository.countByRecipientIdAndReadFalseAndType(userId, Message.MessageType.CHAT);

        // Grupo do cliente (um único groupId no User)
        long group = userRepository.findByEmail(userId).map(user -> user.getGroupId()).filter(gid -> gid != null && !gid.isBlank()).map(gid -> messageRepository.countByGroupIdInAndReadFalseAndTypeAndSenderIdNot(List.of(gid), Message.MessageType.CHAT, userId)).orElse(0L);

        log.info("countUnread userId={} direct={} group={}", userId, direct, group);

        return direct + group;
    }

    // ── Marcar como lida → READ ───────────────────────────────────────────────
    public Message markAsRead(String messageId) {
        br.com.wtc.domain.model.Message message = messageRepository.findById(messageId).orElseThrow(() -> new ResourceNotFoundException("Mensagem", messageId));
        message.setRead(true);
        message.setStatus(Message.MessageStatus.READ);
        return messageRepository.save(message);
    }

    public void markConversationAsRead(String conversationId, String userId) {
        // Resolve o ID do usuário a partir do email (para campanhas)
        String userIdResolved = userRepository.findByEmail(userId).map(u -> u.getId()).orElse(userId);

        messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId).stream().filter(m -> !m.isRead() && (
                // 1:1 — recipientId bate com o email do usuário
                userId.equals(m.getRecipientId())
                        // campanha — recipientId bate com o ID do usuário
                        || userIdResolved.equals(m.getRecipientId())
                        // grupo — groupId bate com conversationId e não foi o próprio usuário que enviou
                        || (m.getGroupId() != null && m.getGroupId().equals(conversationId) && !userId.equals(m.getSenderId())))).forEach(m -> {
            m.setRead(true);
            m.setStatus(Message.MessageStatus.READ);
            messageRepository.save(m);
        });
    }
}