java_binary(
    name = "vertx_hello",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**"]),
    main_class = "com.csoft.MainVerticle",
    deps = [
        "@maven//:io_vertx_vertx_core",
        "@maven//:io_vertx_vertx_web",
    ],
)

java_binary(
    name = "bonoloto_downloader",
    srcs = glob(["src/main/java/**/*.java"]),
    main_class = "com.csoft.BonolotoDataDownloader",
    deps = [
        "@maven//:io_vertx_vertx_core",
    ],
)
