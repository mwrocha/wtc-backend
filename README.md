# WTC Connecta — Backend

API REST do projeto WTC Connecta, desenvolvido em Java com Spring Boot e MongoDB Atlas como banco de dados NoSQL.

---

## Tecnologias

| Camada | Tecnologia |
|--------|-----------|
| Linguagem | Java 17 |
| Framework | Spring Boot 3.2 |
| Segurança | Spring Security + JWT (jjwt 0.12) + BCrypt |
| Banco de Dados | MongoDB Atlas (NoSQL) |
| Push Notifications | Firebase Cloud Messaging (FCM) |
| Storage de Arquivos | Cloudflare R2 via MinIO SDK |

---

## Pré-requisitos

- Java 17+
- Maven 3.8+
- Conta ativa no [MongoDB Atlas](https://cloud.mongodb.com)
- Projeto configurado no [Firebase Console](https://console.firebase.google.com)
- Bucket configurado no [Cloudflare R2](https://dash.cloudflare.com)

---

## Configuração

### 1. application.properties

Edite o arquivo `src/main/resources/application.properties` com as suas credenciais:

```properties
# MongoDB
spring.data.mongodb.uri=mongodb+srv://<usuario>:<senha>@<cluster>.mongodb.net/wtcdb

# JWT
jwt.secret=<chave-secreta-minimo-256-bits>
jwt.expiration=86400000

# Firebase
firebase.credentials.path=src/main/resources/firebase-adminsdk.json

# Cloudflare R2 (MinIO SDK)
minio.endpoint=https://<account-id>.r2.cloudflarestorage.com
minio.access-key=<access-key>
minio.secret-key=<secret-key>
minio.bucket=wtc-media
```

### 2. Credenciais do Firebase

O arquivo `firebase-adminsdk.json` **não está incluído no repositório** por questões de segurança.

Para obter o arquivo:
1. Acesse o [Firebase Console](https://console.firebase.google.com)
2. Vá em **Configurações do Projeto → Contas de serviço**
3. Clique em **Gerar nova chave privada**
4. Salve o arquivo como `firebase-adminsdk.json` em `src/main/resources/`

---

## Como Executar

```bash
# Clonar o repositório
git clone https://github.com/mwrocha/wtc-backend.git
cd wtc-backend

# Adicionar firebase-adminsdk.json em src/main/resources/
# (ver seção acima)

# Executar
mvn spring-boot:run
```

O servidor sobe na porta **8080** por padrão.

---

## Conectar o App Android

Configure o `BASE_URL` no app de acordo com o ambiente:

```kotlin
// Emulador Android (mesmo PC)
private const val BASE_URL = "http://10.0.2.2:8080/"

// Dispositivo físico (mesma rede Wi-Fi)
// Descubra o IP: cmd → ipconfig → "Endereço IPv4"
private const val BASE_URL = "http://192.168.X.X:8080/"
```

---

## Autenticação

Todas as rotas — exceto `/api/auth/login` e `/api/auth/register` — requerem o header:

```
Authorization: Bearer <token>
```

O sistema implementa **sessão única**: ao logar em outro dispositivo, o token anterior é invalidado automaticamente.

---

## Endpoints Principais

### Autenticação
| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/auth/login` | Login — retorna JWT |
| POST | `/api/auth/register` | Cadastro de usuário (OPERATOR ou CLIENT) |

### Clientes
| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/clients` | Lista clientes (filtros: name, tag, groupId, divisionId) |
| GET | `/api/clients/{id}` | Busca cliente por ID |
| PUT | `/api/clients/{id}` | Atualiza dados do cliente |
| GET | `/api/clients/{id}/notes` | Lista anotações do cliente |
| POST | `/api/clients/{id}/notes` | Cria anotação |

### Mensagens
| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/messages/direct` | Envia mensagem 1:1 |
| POST | `/api/messages/group` | Envia mensagem para grupo |
| GET | `/api/messages/conversation/{conversationId}` | Histórico da conversa |
| PUT | `/api/messages/{id}` | Edita mensagem (somente dono, até 5 min) |
| DELETE | `/api/messages/{id}` | Exclui mensagem (somente dono, até 5 min) |

### Campanhas
| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/campaigns` | Cria campanha |
| GET | `/api/campaigns` | Lista campanhas |
| PUT | `/api/campaigns/{id}` | Atualiza campanha |
| POST | `/api/campaigns/{id}/dispatch` | Dispara campanha + push FCM |
| DELETE | `/api/campaigns/{id}` | Remove campanha |
| GET | `/api/campaigns-received` | Campanhas recebidas pelo cliente |

### Grupos e Divisões
| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/groups` | Lista grupos (filtros: divisionId, clientId) |
| POST | `/api/groups` | Cria grupo |
| PUT | `/api/groups/{id}` | Atualiza grupo |
| POST | `/api/groups/{id}/members/{clientId}` | Adiciona cliente ao grupo |
| DELETE | `/api/groups/{id}/members/{clientId}` | Remove cliente do grupo |
| GET | `/api/divisions` | Lista divisões |
| POST | `/api/divisions` | Cria divisão |

### Atendimentos
| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/conversations/pending` | Fila de espera |
| GET | `/api/conversations/pending/count` | Quantidade na fila |
| POST | `/api/conversations/{id}/assume` | Operador assume atendimento |
| POST | `/api/conversations/{id}/close` | Operador encerra atendimento |

### Demais
| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/tasks` | Cria tarefa Kanban |
| PATCH | `/api/tasks/{id}/status` | Atualiza status da tarefa |
| POST | `/api/ratings/{sessionId}` | Cliente avalia atendimento |
| GET | `/api/audit` | Trilha de auditoria (operador) |
| POST | `/api/upload/image` | Upload de imagem/PDF para Cloudflare R2 |
| POST | `/api/users/change-email` | Altera e-mail (requer senha) |
| POST | `/api/users/change-password` | Altera senha |

---

## Collections MongoDB

| Collection | Descrição |
|-----------|-----------|
| `users` | Operadores e clientes |
| `messages` | Mensagens de chat e campanhas |
| `campaigns` | Campanhas criadas pelos operadores |
| `conversations` | Controle de fila e status de atendimento |
| `attendance_sessions` | Sessões de atendimento |
| `attendance_ratings` | Avaliações dos atendimentos |
| `groups` | Grupos de clientes |
| `divisions` | Divisões/setores |
| `notes` | Anotações dos operadores sobre clientes |
| `tasks` | Tarefas do Kanban |
| `audit_logs` | Trilha de auditoria |
| `group_change_requests` | Solicitações de troca de grupo |

---

## Estrutura do Projeto

```
src/main/java/br/com/wtc/
├── config/          # SecurityConfig, CorsConfig, FirebaseConfig, MinioConfig
├── security/        # JwtFilter, JwtUtil, CustomUserDetailsService
├── domain/
│   ├── model/       # User, Message, Campaign, Group, Division, Task, ...
│   ├── repository/  # Interfaces MongoRepository
│   └── service/     # Lógica de negócio de cada domínio
├── web/
│   ├── controller/  # Controllers REST
│   └── dto/         # DTOs de request e response
├── infra/           # FirebasePushService, MinioStorageService, SchedulerJob
└── exception/       # GlobalExceptionHandler, BusinessException
```

---

## Deeplinks Suportados

Usados nos botões de ação das campanhas:

| Deeplink | Destino no App |
|----------|---------------|
| `wtcconnecta://chat` | Tela de conversas |
| `wtcconnecta://campaigns` | Campanhas Express |
| `wtcconnecta://profile` | Perfil do usuário |
| `wtcconnecta://kanban` | Painel Kanban |

---

> **Nota:** o arquivo `firebase-adminsdk.json` está listado no `.gitignore` e nunca deve ser commitado no repositório.
