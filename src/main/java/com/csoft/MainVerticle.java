package com.csoft;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import java.nio.file.Files;
import java.nio.file.Path;

import com.csoft.BonolotoDataDownloader;

public class MainVerticle extends AbstractVerticle {
    @Override
    public void start() {
        Router router = Router.router(vertx);

        router.get("/api/update").handler(ctx -> {
            try {
                Path out = Path.of("webroot/history.csv");
                BonolotoDataDownloader.downloadAndClean(out);
                ctx.response().putHeader("Content-Type", "application/json")
                    .end("{\"status\":\"ok\"}");
            } catch (Exception e) {
                ctx.fail(e);
            }
        });

        router.get("/api/history").handler(ctx -> {
            try {
                Path path = Path.of("webroot/history.csv");
                if (!Files.exists(path)) {
                    ctx.response().setStatusCode(404).end();
                    return;
                }
                String csv = Files.readString(path);
                ctx.response().putHeader("Content-Type", "text/csv").end(csv);
            } catch (Exception e) {
                ctx.fail(e);
            }
        });

        router.route().handler(StaticHandler.create("webroot"));

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8080);
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle());
    }
}
