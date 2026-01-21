package br.com.ifrn.ImportReportService;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import java.time.Duration;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {
    protected static String keycloakClientSecret;
    protected static String keycloakUrl;
    protected static final String KEYCLOAK_REALM = "ifrn";
    protected static final String KEYCLOAK_CLIENT = "import-service";
    protected static MinioClient getMinioClient() {
        return MinioClient.builder()
                .endpoint(MINIO_CONTAINER.getS3URL())
                .credentials(MINIO_CONTAINER.getUserName(), MINIO_CONTAINER.getPassword())
                .build();
    }

    @Autowired
    private MockMvc mockMvc;
    // =======================
    // MinIO
    // =======================
    @Container
    protected static final MinIOContainer MINIO_CONTAINER =
            new MinIOContainer(
                    DockerImageName.parse("minio/minio:RELEASE.2023-09-04T19-57-37Z")
            )
                    .withUserName("testuser")
                    .withPassword("testpassword")
                    .withStartupTimeout(Duration.ofMinutes(2))
                    .waitingFor(
                            Wait.forHttp("/minio/health/ready")
                                    .forPort(9000)
                    );

    // =======================
    // RabbitMQ
    // =======================
    @Container
    protected static final RabbitMQContainer RABBITMQ_CONTAINER =
            new RabbitMQContainer(
                    DockerImageName.parse("rabbitmq:3.12-management-alpine")
            )
                    .withStartupTimeout(Duration.ofMinutes(2));

    // =======================
    // Keycloak
    // =======================
    @Container
    protected static final GenericContainer<?> KEYCLOAK_CONTAINER =
            new GenericContainer<>(
                    DockerImageName.parse("quay.io/keycloak/keycloak:25.0")
            )
                    .withEnv("KEYCLOAK_ADMIN", "admin")
                    .withEnv("KEYCLOAK_ADMIN_PASSWORD", "admin")
                    .withCommand("start-dev")
                    .withExposedPorts(8080)
                    .withStartupTimeout(Duration.ofMinutes(3))
                    .waitingFor(
                            Wait.forHttp("/realms/master")
                                    .forPort(8080)
                                    .withStartupTimeout(Duration.ofMinutes(3))
                    );

    // =======================
    // Dynamic Properties
    // =======================
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {

// ---- MinIO (Ajustado para o seu Record) ----
        registry.add("minio.serverUrl", MINIO_CONTAINER::getS3URL);
        registry.add("minio.adminUser", MINIO_CONTAINER::getUserName);
        registry.add("minio.adminPassword", MINIO_CONTAINER::getPassword);
        registry.add("minio.bucketFiles", () -> "test-files");
        registry.add("minio.bucketImages", () -> "test-images");

        // ---- RabbitMQ ----
        registry.add("spring.rabbitmq.host", RABBITMQ_CONTAINER::getHost);
        registry.add("spring.rabbitmq.port", RABBITMQ_CONTAINER::getAmqpPort);
        registry.add("spring.rabbitmq.username", RABBITMQ_CONTAINER::getAdminUsername);
        registry.add("spring.rabbitmq.password", RABBITMQ_CONTAINER::getAdminPassword);

        // ---- Keycloak ----
        String keycloakUrl = String.format(
                "http://%s:%d",
                KEYCLOAK_CONTAINER.getHost(),
                KEYCLOAK_CONTAINER.getMappedPort(8080)
        );

        registry.add(
                "spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloakUrl + "/realms/master"
        );

        registry.add("keycloak.auth-server-url", () -> keycloakUrl);
        registry.add("keycloak.admin-user", () -> "admin");
        registry.add("keycloak.admin-password", () -> "admin");
        registry.add("keycloak.realm", () -> "ifrn");
        registry.add("keycloak.client-secret", () -> keycloakClientSecret != null ? keycloakClientSecret : "temporary-secret");
    }

    // =======================
    // Create MinIO Buckets
    // =======================
    @BeforeAll
    static void createBuckets() throws Exception {

        MinioClient minioClient = MinioClient.builder()
                .endpoint(MINIO_CONTAINER.getS3URL())
                .credentials(
                        MINIO_CONTAINER.getUserName(),
                        MINIO_CONTAINER.getPassword()
                )
                .build();

        createBucketIfNotExists(minioClient, "test-files");
        createBucketIfNotExists(minioClient, "test-images");
    }

    private static void createBucketIfNotExists(
            MinioClient minioClient,
            String bucketName
    ) throws Exception {

        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build()
        );

        if (!exists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
        }
    }
    @BeforeAll
    static void setupKeycloak() throws Exception {
        keycloakUrl = String.format(
                "http://%s:%d",
                KEYCLOAK_CONTAINER.getHost(),
                KEYCLOAK_CONTAINER.getMappedPort(8080)
        );

        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        // 1. Obter token admin
        String body = "username=admin&password=admin&grant_type=password&client_id=admin-cli";
        HttpRequest tokenRequest = HttpRequest.newBuilder()
                .uri(URI.create(keycloakUrl + "/realms/master/protocol/openid-connect/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> tokenResponse = client.send(tokenRequest, HttpResponse.BodyHandlers.ofString());
        JsonNode tokenJson = mapper.readTree(tokenResponse.body());
        String adminToken = tokenJson.get("access_token").asText();

        // 2. Criar realm "ifrn" (se não existir)
        HttpRequest createRealm = HttpRequest.newBuilder()
                .uri(URI.create(keycloakUrl + "/admin/realms"))
                .header("Authorization", "Bearer " + adminToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"realm\":\"" + KEYCLOAK_REALM + "\", \"enabled\":true}"))
                .build();

        client.send(createRealm, HttpResponse.BodyHandlers.discarding());

        // 3. Criar cliente "import-service"
        String clientJson = "{ \"clientId\": \"" + KEYCLOAK_CLIENT + "\", " +
                "\"enabled\": true, " +
                "\"clientAuthenticatorType\": \"client-secret\", " +
                "\"publicClient\": false, " +
                "\"serviceAccountsEnabled\": true }";

        HttpRequest createClient = HttpRequest.newBuilder()
                .uri(URI.create(keycloakUrl + "/admin/realms/" + KEYCLOAK_REALM + "/clients"))
                .header("Authorization", "Bearer " + adminToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(clientJson))
                .build();

        client.send(createClient, HttpResponse.BodyHandlers.discarding());

        // 4. Obter client ID
        HttpRequest getClients = HttpRequest.newBuilder()
                .uri(URI.create(keycloakUrl + "/admin/realms/" + KEYCLOAK_REALM + "/clients?clientId=" + KEYCLOAK_CLIENT))
                .header("Authorization", "Bearer " + adminToken)
                .GET()
                .build();

        HttpResponse<String> clientsResponse = client.send(getClients, HttpResponse.BodyHandlers.ofString());
        JsonNode clientsArray = mapper.readTree(clientsResponse.body());
        String clientId = clientsArray.get(0).get("id").asText();

        // 5. Obter client-secret
        HttpRequest getSecret = HttpRequest.newBuilder()
                .uri(URI.create(keycloakUrl + "/admin/realms/" + KEYCLOAK_REALM + "/clients/" + clientId + "/client-secret"))
                .header("Authorization", "Bearer " + adminToken)
                .GET()
                .build();

        HttpResponse<String> secretResponse = client.send(getSecret, HttpResponse.BodyHandlers.ofString());
        keycloakClientSecret = mapper.readTree(secretResponse.body()).get("value").asText();

        System.out.println("Keycloak client-secret: " + keycloakClientSecret);
    }
}
