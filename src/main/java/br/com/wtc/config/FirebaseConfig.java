package br.com.wtc.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initFirebase() throws IOException {

        // Carrega o arquivo de credenciais de src/main/resources/
        InputStream serviceAccount = getClass()
                .getClassLoader()
                .getResourceAsStream("firebase-adminsdk.json");

        if (serviceAccount == null) {
            throw new IllegalStateException(
                    "firebase-adminsdk.json não encontrado em src/main/resources/"
            );
        }

        // Evita inicializar duas vezes (ex: hot reload do DevTools)
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
        }
    }
}