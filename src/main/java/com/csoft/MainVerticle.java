package com.csoft;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import com.csoft.BonolotoDataDownloader;
import com.csoft.BonolotoStats;

public class MainVerticle extends AbstractVerticle {
    private static final Logger LOGGER = Logger.getLogger(MainVerticle.class.getName());
    @Override
    public void start() {
        Router router = Router.router(vertx);

        router.get("/api/update").handler(ctx -> {
            try {
                Path out = Path.of("data/history.csv");
                LOGGER.info("Actualizando historico en " + out.toAbsolutePath());
                BonolotoDataDownloader.downloadAndClean(out);
                long size = Files.size(out);
                LOGGER.info("Actualizacion finalizada. Tama\u00f1o del fichero: " + size + " bytes");
                ctx.response().putHeader("Content-Type", "application/json")
                    .end("{\"status\":\"ok\"}");
            } catch (Exception e) {
                ctx.fail(e);
            }
        });

        router.get("/api/history").handler(ctx -> {
            try {
                Path path = Path.of("data/history.csv");
                if (!Files.exists(path)) {
                    ctx.response().setStatusCode(404).end();
                    return;
                }
                long size = Files.size(path);
                LOGGER.info("Sirviendo historico desde " + path.toAbsolutePath() + " (" + size + " bytes)");
                String csv = Files.readString(path);
                ctx.response().putHeader("Content-Type", "text/csv").end(csv);
            } catch (Exception e) {
                ctx.fail(e);
            }
        });

        router.get("/api/stats").handler(ctx -> {
            try {
                Path path = Path.of("data/history.csv");
                if (!Files.exists(path)) {
                    ctx.response().setStatusCode(404).end();
                    return;
                }
                var stats = BonolotoStats.compute(path);
                StringBuilder sb = new StringBuilder();
                sb.append("FECHA,EVEN,ODD\n");
                for (var s : stats) {
                    sb.append(s.date).append(',').append(s.even).append(',').append(s.odd).append('\n');
                }
                ctx.response().putHeader("Content-Type", "text/csv").end(sb.toString());
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
