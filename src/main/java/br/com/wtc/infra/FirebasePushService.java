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

    // Notificação de mensagem direta 1:1
    public void sendMessagePush(String fcmToken, String senderName,
                                String body, String conversationId) {
        if (fcmToken == null || fcmToken.isBlank()) return;
        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle("Nova mensagem de " + senderName)
                            .setBody(body)
                            .build())
                    .putData("type", "DIRECT")
                    .putData("conversationId", conversationId)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Push enviado com sucesso: {}", response);
        } catch (Exception e) {
            log.error("Erro ao enviar push: {}", e.getMessage());
        }
    }

    // Notificação de mensagem de grupo
    public void sendGroupPush(String fcmToken, String senderName,
                              String groupName, String body, String groupId) {
        if (fcmToken == null || fcmToken.isBlank()) return;
        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(groupName + " — " + senderName)
                            .setBody(body)
                            .build())
                    .putData("type", "GROUP")
                    .putData("groupId", groupId)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Push de grupo enviado: {}", response);
        } catch (Exception e) {
            log.error("Erro ao enviar push de grupo: {}", e.getMessage());
        }
    }

    // Notificação de campanha
    public void sendCampaignPush(String fcmToken, String title,
                                 String body, String url, String campaignId) {
        if (fcmToken == null || fcmToken.isBlank()) return;
        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putData("type", "CAMPAIGN")
                    .putData("campaignId", campaignId != null ? campaignId : "")
                    .putData("url", url != null ? url : "")
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Push de campanha enviado: {}", response);
        } catch (Exception e) {
            log.error("Erro ao enviar push de campanha: {}", e.getMessage());
        }
    }
}