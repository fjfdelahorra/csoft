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
    srcs = ["src/main/java/com/csoft/BonolotoDataDownloader.java"],
    main_class = "com.csoft.BonolotoDataDownloader",
    deps = [
        "@maven//:io_vertx_vertx_core",
    ],
)

java_binary(
    name = "bonoloto_stats",
    srcs = ["src/main/java/com/csoft/BonolotoStats.java"],
    main_class = "com.csoft.BonolotoStats",
)

java_binary(
    name = "bonoloto_evolver",
    srcs = [
        "src/main/java/com/csoft/BonolotoEvolver.java",
        "src/main/java/com/csoft/BonolotoStats.java",
    ],
    main_class = "com.csoft.BonolotoEvolver",
)
