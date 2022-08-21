package hexlet.code;

import hexlet.code.controllers.UrlsController;
import io.javalin.Javalin;
import io.javalin.plugin.rendering.template.JavalinThymeleaf;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

public final class App {
    public static void main(String[] args) {
        Javalin app = getApp();
        app.start(getPort());
    }

    public static Javalin getApp() {
        Javalin app = Javalin.create(config -> {
            config.enableDevLogging();
            config.enableWebjars();
            JavalinThymeleaf.configure(getTemplateEngine());
        });

        addRoutes(app);
        app.before(ctx -> {
            ctx.attribute("ctx", ctx);
        });
        return app;
    }

    private static void addRoutes(Javalin app) {
        app.get("/", ctx -> ctx.render("index.html"));
        app.get("/urls", UrlsController.listUrls);
        app.post("/urls", UrlsController.createUrl);
        app.get("/urls/{id}", UrlsController.showUrl);
    }

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "8080");
        return Integer.valueOf(port);
    }

    private static TemplateEngine getTemplateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.addDialect(new LayoutDialect());
        templateEngine.addDialect(new Java8TimeDialect());

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/");

        templateEngine.addTemplateResolver(templateResolver);
        return templateEngine;
    }
}
