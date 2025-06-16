package com.csoft;

import io.vertx.core.Vertx;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.assertNotEquals;

public class MainVerticleTest {
    private static Vertx vertx;

    @BeforeClass
    public static void setUp() throws Exception {
        vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle()).toCompletionStage().toCompletableFuture().get();
        // Give the server a moment to start
        Thread.sleep(500);
    }

    @AfterClass
    public static void tearDown() {
        if (vertx != null) {
            vertx.close();
        }
    }

    @Test
    public void testIndexNot404() throws Exception {
        URL url = new URL("http://localhost:8080/");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        int code = conn.getResponseCode();
        conn.disconnect();
        assertNotEquals(404, code);
    }
}
