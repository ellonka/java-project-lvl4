package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.DB;
import io.ebean.Transaction;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

public class AppTest {
    private static Javalin app;
    private static String baseUrl;
    private static Transaction transaction;

    @BeforeAll
    public static void startApp() {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
    }

    @AfterAll
    public static void stopApp() {
        app.stop();
    }

    @BeforeEach
    void beforeEach() {
        transaction = DB.beginTransaction();
    }

    @AfterEach
    void afterEach() {
        transaction.rollback();
    }

    @Test
    void testRoot() {
        HttpResponse<String> response = Unirest.get(baseUrl).asString();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void testAddUrlSuccess() {
        String urlForCheck = "https://frontbackend.com/thymeleaf/working-with-dates-in-thymeleaf";
        HttpResponse<String> response = Unirest
                .post(baseUrl + "/urls")
                .field("url", urlForCheck)
                .asString();
        assertThat(response.getStatus()).isEqualTo(302);

        Url addedUrl = new QUrl()
                .name.equalTo("https://frontbackend.com")
                .findOne();

        assertThat(addedUrl).isNotNull();

        HttpResponse<String> responseUrls = Unirest
                .get(baseUrl + "/urls")
                .asString();
        String content = responseUrls.getBody();

        assertThat(content).contains(addedUrl.getName());
        assertThat(content).contains("Страница успешно добавлена");
    }

    @Test
    void testAddWrongUrl() {
        String wrongUrl = "frontbackend.dfs.sdt";
        HttpResponse<String> response = Unirest
                .post(baseUrl + "/urls")
                .field("url", wrongUrl)
                .asString();

        assertThat(response.getStatus()).isEqualTo(302);
        boolean hasWrongUrl = new QUrl()
                .name.equalTo(wrongUrl)
                .exists();
        assertThat(hasWrongUrl).isFalse();

        HttpResponse<String> responseGet= Unirest
                .get(baseUrl).asString();
        String content = responseGet.getBody();
        assertThat(content).contains("Некорректный URL");
    }

    @Test
    void testAddDuplicateUrl() {
        String urlForCheck = "https://vk.com/habr";
        HttpResponse<String> response1 = Unirest
                .post(baseUrl + "/urls")
                .field("url", urlForCheck)
                .asString();

        HttpResponse<String> response2 = Unirest
                .post(baseUrl + "/urls")
                .field("url", urlForCheck)
                .asString();
        assertThat(response2.getStatus()).isEqualTo(302);

        HttpResponse<String> responseUrls = Unirest
                .get(baseUrl + "/urls")
                .asString();
        String content = responseUrls.getBody();

        assertThat(content).contains("Страница уже существует");
    }
}
