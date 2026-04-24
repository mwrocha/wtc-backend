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

    private final MessageRepository   messageRepository;
    private final UserRepository      userRepository;
    private final FirebasePushService firebasePushService;
    private final ConversationService conversationService;

    public MessageService(MessageRepository messageRepository,
                          UserRepository userRepository,
                          FirebasePushService firebasePushService,
                          ConversationService conversationService) {
        this.messageRepository   = messageRepository;
        this.userRepository      = userRepository;
        this.firebasePushService = firebasePushService;
        this.conversationService = conversationService;
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

        // ── Distribuição automática ───────────────────────────────────────────
        // Se o cliente está enviando para si mesmo (sem operador definido),
        // busca qualquer operador ativo no sistema automaticamente
        if (senderEmail.equals(recipientEmail)) {
            final String clientEmail = senderEmail;
            String operatorEmail = userRepository.findByRole("OPERATOR")
                    .stream()
                    .filter(u -> u.isActive())
                    .findFirst()
                    .map(u -> u.getEmail())
                    .orElse(null);

            if (operatorEmail != null) {
                recipientEmail = operatorEmail;
                log.info("Distribuição automática: cliente={} → operador={}", clientEmail, operatorEmail);
            } else {
                log.warn("Nenhum operador ativo encontrado para atender: {}", clientEmail);
            }
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
                .status(Message.MessageStatus.SENT)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        Message saved = messageRepository.save(message);

        final String finalSenderEmail    = senderEmail;
        final String finalRecipientEmail = recipientEmail;
        final String finalConversationId = conversationId;
        final String finalMessageId      = saved.getId();

        // log pra achar erro de direcionamento errado usuário
        log.info("DEBUG sendDirect: sender={} recipient={} conversationId={}",
                finalSenderEmail, finalRecipientEmail, finalConversationId);


        // ── Verifica role do remetente ────────────────────────────────────────
        boolean senderIsClient = userRepository.findByEmail(finalSenderEmail)
                .map(u -> "CLIENT".equals(u.getRole()))
                .orElse(false);

        if (senderIsClient) {
            // ── Remetente é CLIENT ────────────────────────────────────────────

            // 1. Atualiza fila de atendimento
            log.info("Cliente detectado, adicionando à fila: {}", finalConversationId);
            conversationService.onClientMessageSent(
                    finalConversationId, finalSenderEmail,
                    body.length() > 60 ? body.substring(0, 60) + "..." : body
            );

            // 2. Push inteligente baseado no status da conversa
            conversationService.getByConversationId(finalConversationId)
                    .ifPresentOrElse(conv -> {
                        // ← ADICIONAR AQUI
                        log.info("DEBUG conversa encontrada: id={} status={} operador={}",
                                conv.getConversationId(), conv.getStatus(), conv.getAssignedOperatorEmail());

                        if (conv.getStatus() == Conversation.ConversationStatus.IN_PROGRESS
                                && conv.getAssignedOperatorEmail() != null) {
                            // Conversa já assumida → push só para o operador responsável
                            log.info("Conversa IN_PROGRESS → push para operador: {}",
                                    conv.getAssignedOperatorEmail());
                            userRepository.findByEmail(conv.getAssignedOperatorEmail())
                                    .ifPresent(operator -> {
                                        if (operator.getFcmToken() != null) {
                                            firebasePushService.sendMessagePush(
                                                    operator.getFcmToken(), finalSenderEmail,
                                                    body, finalConversationId, finalMessageId);
                                        }
                                    });
                        } else {
                            // Conversa OPEN ou CLOSED → sem push individual
                            // (fila de atendimento já notifica visualmente no dashboard)
                            log.info("Conversa {} → sem push individual, fila notifica",
                                    conv.getStatus());
                        }
                    }, () -> {
                        // Conversa nova (ainda não existe no banco) → sem push
                        // será criada como OPEN pelo onClientMessageSent acima
                        log.info("Conversa nova → sem push individual, fila notifica");
                    });

        } else {
            // ── Remetente é OPERATOR → push normal para o cliente ─────────────
            // Não altera nada no fluxo do cliente
            userRepository.findByEmail(finalRecipientEmail).ifPresent(recipient -> {
                if (recipient.getFcmToken() != null) {
                    firebasePushService.sendMessagePush(
                            recipient.getFcmToken(), finalSenderEmail, body,
                            finalConversationId, finalMessageId);
                }
            });
        }

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
                .status(Message.MessageStatus.SENT)
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
                .actionUrls(campaign.getActionUrls())
                .type(Message.MessageType.CAMPAIGN)
                .status(Message.MessageStatus.SENT)
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

    // ── Campanha — propagar edição ────────────────────────────────────────────
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
            msg.setActionUrls(campaign.getActionUrls());
            messageRepository.save(msg);

            String recipientId = msg.getRecipientId();
            if (recipientId != null) {
                userRepository.findByEmail(recipientId).ifPresent(recipient -> {
                    if (recipient.getFcmToken() != null) {
                        firebasePushService.sendCampaignUpdatedPush(
                                recipient.getFcmToken(),
                                campaign.getTitle(), campaign.getBody(),
                                campaign.getId());
                    }
                });
                userRepository.findById(recipientId).ifPresent(recipient -> {
                    if (recipient.getFcmToken() != null) {
                        firebasePushService.sendCampaignUpdatedPush(
                                recipient.getFcmToken(),
                                campaign.getTitle(), campaign.getBody(),
                                campaign.getId());
                    }
                });
            }
        }

        return existingMessages.size();
    }

    // ── Histórico de conversa → marca como DELIVERED ──────────────────────────
    public List<Message> getConversation(String conversationId, String requestingUserEmail) {
        List<Message> messages = messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversationId);

        messages.stream()
                .filter(m -> requestingUserEmail.equals(m.getRecipientId())
                        && m.getStatus() == Message.MessageStatus.SENT)
                .forEach(m -> {
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

    // ── Marcar como lida → READ ───────────────────────────────────────────────
    public Message markAsRead(String messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Mensagem", messageId));
        message.setRead(true);
        message.setStatus(Message.MessageStatus.READ);
        return messageRepository.save(message);
    }

    public void markConversationAsRead(String conversationId, String userId) {
        messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .filter(m -> userId.equals(m.getRecipientId()) && !m.isRead())
                .forEach(m -> {
                    m.setRead(true);
                    m.setStatus(Message.MessageStatus.READ);
                    messageRepository.save(m);
                });
    }
}