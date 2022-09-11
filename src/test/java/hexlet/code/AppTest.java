package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import io.ebean.DB;
import io.ebean.Transaction;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public final class AppTest {
    private static Javalin app;
    private static String baseUrl;
    private static Transaction transaction;
    private final static int code200 = 200;
    private final int code302 = 302;
    private static MockWebServer server;

    @BeforeAll
    public static void startApp() throws IOException {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;

        server = new MockWebServer();

        MockResponse mockResponse = new MockResponse();
        mockResponse.setResponseCode(code200);
        mockResponse.setBody(Files.readString(new File("src/test/resources/testPage.html").toPath()));

        server.enqueue(mockResponse);
        server.start();
    }

    @AfterAll
    public static void stopApp() throws IOException {
        server.shutdown();
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
        assertThat(response.getStatus()).isEqualTo(code200);
    }

    @Test
    void testAddUrlSuccess() {
        String urlForCheck = "https://frontbackend.com/thymeleaf/working-with-dates-in-thymeleaf";
        HttpResponse<String> response = Unirest
                .post(baseUrl + "/urls")
                .field("url", urlForCheck)
                .asString();
        assertThat(response.getStatus()).isEqualTo(code302);

        Url addedUrl = new QUrl()
                .name.equalTo("https://frontbackend.com/")
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

        assertThat(response.getStatus()).isEqualTo(code302);
        boolean hasWrongUrl = new QUrl()
                .name.equalTo(wrongUrl)
                .exists();
        assertThat(hasWrongUrl).isFalse();

        HttpResponse<String> responseGet = Unirest
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
        assertThat(response2.getStatus()).isEqualTo(code302);

        HttpResponse<String> responseUrls = Unirest
                .get(baseUrl + "/urls")
                .asString();
        String content = responseUrls.getBody();

        assertThat(content).contains("Страница уже существует");
    }

    @Test
    void testUrlCheck() throws MalformedURLException {
        String checkingUrl = server.url("/").toString();

        System.out.println(checkingUrl);
        Unirest.post(baseUrl + "/urls")
                .field("url", checkingUrl)
                .asString();

        Url addedUrl = new QUrl()
                .name.equalTo(Utils.extractDomain(checkingUrl))
                .findOne();
        System.out.println(addedUrl.getName());
        assertThat(addedUrl).isNotNull();

        Unirest.post(baseUrl + "/urls/" + addedUrl.getId() + "/checks").asString();

        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls/" + addedUrl.getId())
                .asString();

        List<UrlCheck> addedUrlCheck = new QUrlCheck()
                .url.equalTo(addedUrl).findList();

        assertThat(addedUrlCheck.get(0).getStatusCode()).isEqualTo(code200);
        assertThat(response.getBody()).contains("200");
        assertThat(addedUrlCheck.get(0).getTitle()).isEqualTo("HTML Page for Testing CSS");
        assertThat(response.getBody()).contains("HTML Page for Testing CSS");
        assertThat(addedUrlCheck.get(0).getH1()).isEqualTo("Testing display of HTML elements");
        assertThat(response.getBody()).contains("Testing display of HTML elements");
        assertThat(addedUrlCheck.get(0).getDescription()).isEqualTo("Free Web tutorials");
        assertThat(response.getBody()).contains("Free Web tutorials");
    }
}
