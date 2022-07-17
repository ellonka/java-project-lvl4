package hexlet.code;

import io.javalin.Javalin;

public class App {
    public static void main(String[] args) {
        Javalin app = getApp();
        app.start(7070);
    }

    public static Javalin getApp() {
        Javalin app = Javalin.create(config -> {
            config.enableDevLogging();
        });

        addRoutes(app);
        app.before(ctx -> {
            ctx.attribute("ctx", ctx);
        });
        return app;
    }

    private static void addRoutes(Javalin app) {
        app.get("/", ctx -> ctx.result("Hello world"));
    }
}
