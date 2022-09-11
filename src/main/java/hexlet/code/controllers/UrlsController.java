package hexlet.code.controllers;

import hexlet.code.Utils;
import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import java.net.MalformedURLException;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public final class UrlsController {
    private static final int ROWS_PER_PAGE = 10;
    private static final int CODE_200 = 200;
    private static final int CODE_404 = 404;

    public static Handler checkUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        HttpResponse<String> response = Unirest.get(url.getName()).asString();
        int status = response.getStatus();
        if (status == CODE_200) {
            Document document = Jsoup.parse(response.getBody());
            String h1 = document.selectFirst("h1") != null
                    ? document.selectFirst("h1").text()
                    : "";
            String title = document.title();
            String description = document.selectFirst("meta[name=description]") != null
                    ? document.selectFirst("meta[name=description]").attr("content")
                    : "";

            UrlCheck urlCheck = new UrlCheck(status, title, h1, description, url);
            urlCheck.save();
            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flash-type", "success");
        } else if (status == CODE_404) {
            UrlCheck urlCheck = new UrlCheck(404, "","", "", url);
            urlCheck.save();
            ctx.sessionAttribute("flash", "404, Not Found");
            ctx.sessionAttribute("flash-type", "info");
        } else {
            ctx.sessionAttribute("flash", response.getBody());
            ctx.sessionAttribute("flash-type", "danger");
        }
        ctx.redirect("/urls/" + id);
    };
    public static Handler showUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);
        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();
        List<UrlCheck> urlChecks = new QUrlCheck()
                .url.equalTo(url).findList();

        ctx.attribute("checks", urlChecks);
        ctx.attribute("url", url);
        ctx.render("urls/show.html");
    };

    public static Handler createUrl = ctx -> {
        String input = ctx.formParam("url");
        String domain = "";
        try {
            domain = Utils.extractDomain(input);
        } catch (MalformedURLException e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            e.printStackTrace();
            return;
        }

        boolean hasUrl = new QUrl()
                .name.equalTo(domain)
                .exists();
        if (hasUrl) {
            ctx.redirect("/urls");
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "info");
            return;
        }

        Url url = new Url(domain);
        url.save();
        ctx.redirect("/urls");
        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.sessionAttribute("flash-type", "success");
    };

    public static Handler listUrls = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        int offset = (page - 1) * ROWS_PER_PAGE;

        PagedList<Url> pagedUrls = new QUrl()
                .setFirstRow(offset)
                .setMaxRows(ROWS_PER_PAGE)
                .orderBy()
                .createdAt.desc()
                .findPagedList();

        List<Url> urls = pagedUrls.getList();

        ctx.attribute("urls", urls);
        ctx.attribute("page", page);
        ctx.render("/urls/index.html");
    };
}
