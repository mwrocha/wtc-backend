package br.com.wtc.infra;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FirebasePushService {

    private static final Logger log = LoggerFactory.getLogger(FirebasePushService.class);

    // Mensagem direta 1:1 — agora inclui messageId para o app atualizar o status
    public void sendMessagePush(String fcmToken, String senderName, String body, String conversationId, String messageId) {
        if (fcmToken == null || fcmToken.isBlank()) return;
        try {
            Message message = Message.builder().setToken(fcmToken).setNotification(Notification.builder().setTitle("Nova mensagem de " + senderName).setBody(body).build()).putData("type", "DIRECT").putData("title", "Nova mensagem de " + senderName).putData("body", body).putData("senderId", senderName).putData("conversationId", conversationId != null ? conversationId : "").putData("messageId", messageId != null ? messageId : "").build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Push direto enviado: {}", response);
        } catch (Exception e) {
            log.error("Erro ao enviar push direto: {}", e.getMessage());
        }
    }

    // Mensagem de grupo
    public void sendGroupPush(String fcmToken, String senderName, String groupName, String body, String groupId) {
        if (fcmToken == null || fcmToken.isBlank()) return;
        try {
            String title = groupName + " — " + senderName;
            Message message = Message.builder().setToken(fcmToken).setNotification(Notification.builder().setTitle(title).setBody(body).build()).putData("type", "GROUP").putData("title", title).putData("body", body).putData("groupId", groupId != null ? groupId : "").build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Push de grupo enviado: {}", response);
        } catch (Exception e) {
            log.error("Erro ao enviar push de grupo: {}", e.getMessage());
        }
    }

    // Campanha — disparo inicial
    public void sendCampaignPush(String fcmToken, String title, String body, String url, String campaignId) {
        if (fcmToken == null || fcmToken.isBlank()) return;
        try {
            Message message = Message.builder().setToken(fcmToken).setNotification(Notification.builder().setTitle(title).setBody(body).build()).putData("type", "CAMPAIGN").putData("title", title).putData("body", body).putData("campaignId", campaignId != null ? campaignId : "").putData("url", url != null ? url : "").build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Push de campanha enviado: {}", response);
        } catch (Exception e) {
            log.error("Erro ao enviar push de campanha: {}", e.getMessage());
        }
    }

    // Solicitação de troca de grupo — notifica operadores
    public void sendGroupChangeRequestPush(String fcmToken, String clientName, String currentGroup, String requestedGroup) {
        if (fcmToken == null || fcmToken.isBlank()) return;
        try {
            String title = "Nova solicitação de grupo";
            String body = clientName + " quer trocar de \"" + currentGroup + "\" para \"" + requestedGroup + "\"";
            Message message = Message.builder().setToken(fcmToken).setNotification(Notification.builder().setTitle(title).setBody(body).build()).putData("type", "GROUP_REQUEST").putData("title", title).putData("body", body).putData("clientName", clientName != null ? clientName : "").build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Push de solicitação de grupo enviado: {}", response);
        } catch (Exception e) {
            log.error("Erro ao enviar push de solicitação de grupo: {}", e.getMessage());
        }
    }

    // Campanha atualizada — notifica clientes
    public void sendCampaignUpdatedPush(String fcmToken, String title, String body, String campaignId) {
        if (fcmToken == null || fcmToken.isBlank()) return;
        try {
            String notifTitle = "Campanha atualizada: " + title;
            Message message = Message.builder().setToken(fcmToken).setNotification(Notification.builder().setTitle(notifTitle).setBody(body).build()).putData("type", "CAMPAIGN").putData("title", notifTitle).putData("body", body).putData("campaignId", campaignId != null ? campaignId : "").build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Push de campanha atualizada enviado: {}", response);
        } catch (Exception e) {
            log.error("Erro ao enviar push de campanha atualizada: {}", e.getMessage());
        }
    }
}