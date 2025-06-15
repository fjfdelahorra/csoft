package com.csoft;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.logging.Logger;

import com.csoft.BonolotoDataDownloader;
import com.csoft.BonolotoStats;
import com.csoft.BonolotoEvolver;

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

                int page = Integer.parseInt(ctx.request().getParam("page", "1"));
                int size = Integer.parseInt(ctx.request().getParam("size", "20"));
                if (page < 1) page = 1;
                if (size < 1) size = 20;

                var lines = Files.readAllLines(path);
                String header = lines.isEmpty() ? "" : lines.get(0);
                var data = lines.size() > 1 ? lines.subList(1, lines.size()) : java.util.Collections.<String>emptyList();
                int total = data.size();
                int start = Math.min((page - 1) * size, total);
                int end = Math.min(start + size, total);
                var slice = data.subList(start, end);

                StringBuilder sb = new StringBuilder();
                if (!header.isEmpty()) sb.append(header).append('\n');
                for (String l : slice) sb.append(l).append('\n');

                ctx.response().putHeader("Content-Type", "text/csv")
                    .putHeader("X-Total-Count", Integer.toString(total))
                    .end(sb.toString());
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

                int page = Integer.parseInt(ctx.request().getParam("page", "1"));
                int size = Integer.parseInt(ctx.request().getParam("size", "20"));
                if (page < 1) page = 1;
                if (size < 1) size = 20;

                var stats = BonolotoStats.compute(path);
                int total = stats.size();
                int start = Math.min((page - 1) * size, total);
                int end = Math.min(start + size, total);

                StringBuilder sb = new StringBuilder();
                sb.append("FECHA,N1,N2,N3,N4,N5,N6,COMP,EVEN,ODD,D1,D2,D3,D4,D5,CONSEC\n");
                for (int i = start; i < end; i++) {
                    var s = stats.get(i);
                    sb.append(s.date);
                    for (int n : s.numbers) {
                        sb.append(',').append(n);
                    }
                    sb.append(',').append(s.complement);
                    sb.append(',').append(s.even).append(',').append(s.odd)
                      .append(',').append(s.tens[0]).append(',').append(s.tens[1])
                      .append(',').append(s.tens[2]).append(',').append(s.tens[3])
                      .append(',').append(s.tens[4]).append(',').append(s.consecutive)
                      .append('\n');
                }
                ctx.response().putHeader("Content-Type", "text/csv")
                    .putHeader("X-Total-Count", Integer.toString(total))
                    .end(sb.toString());
            } catch (Exception e) {
                ctx.fail(e);
            }
        });

        router.get("/api/evolve").handler(ctx -> {
            vertx.<BonolotoEvolver.EvolutionResult>executeBlocking(promise -> {
                try {
                    var res = BonolotoEvolver.evolve(Path.of("data/history.csv"));
                    promise.complete(res);
                } catch (Exception e) {
                    promise.fail(e);
                }
            }, ar -> {
                if (ar.succeeded()) {
                    var res = ar.result();
                    var json = new io.vertx.core.json.JsonObject()
                            .put("best", Arrays.stream(res.best).boxed().collect(Collectors.toList()))
                            .put("score", res.score)
                            .put("steps", res.steps);
                    ctx.response().putHeader("Content-Type", "application/json")
                        .end(json.encode());
                } else {
                    ctx.fail(ar.cause());
                }
            });
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
