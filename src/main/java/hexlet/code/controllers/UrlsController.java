package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.PagedList;
import io.javalin.http.Handler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class UrlsController {
    public static Handler showUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);
        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();
        ctx.attribute("url", url);
        ctx.render("urls/show.html");
    };

    public static Handler createUrl = ctx -> {
        String input = ctx.formParam("url");
        String domain = "";
        try {
            domain = extractDomain(input);
        } catch (MalformedURLException e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.redirect("/");
            e.printStackTrace();
            return;
        }

        boolean hasUrl = new QUrl()
                .name.equalTo(domain)
                .exists();
        if (hasUrl) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.redirect("/urls");
            return;
        }

        Url url = new Url(domain);
        url.save();
        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.redirect("/urls");
    };

    public static Handler listUrls = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        int rowsPerPage = 10;
        int offset = (page - 1) * rowsPerPage;

        PagedList<Url> pagedUrls = new QUrl()
                .setFirstRow(offset)
                .setMaxRows(rowsPerPage)
                .orderBy()
                .createdAt.desc()
                .findPagedList();

        List<Url> urls = pagedUrls.getList();

        ctx.attribute("urls", urls);
        ctx.attribute("page", page);
        ctx.render("/urls/index.html");
    };

    private static String extractDomain(String input) throws MalformedURLException {
        URL url = new URL(input);
        String[] urlParts = input.split("/");
        String result = urlParts[0] + "//" + urlParts[2];
        return result;
    }
}
