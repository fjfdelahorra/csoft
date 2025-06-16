java_library(
    name = "csoft_lib",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**"]),
    deps = [
        "@maven//:io_vertx_vertx_core",
        "@maven//:io_vertx_vertx_web",
    ],
)

java_binary(
    name = "vertx_hello",
    main_class = "com.csoft.MainVerticle",
    runtime_deps = [":csoft_lib"],
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

java_test(
    name = "server_test",
    srcs = ["src/test/java/com/csoft/MainVerticleTest.java"],
    test_class = "com.csoft.MainVerticleTest",
    deps = [
        ":csoft_lib",
        "@maven//:junit_junit",
        "@maven//:io_vertx_vertx_core",
        "@maven//:io_vertx_vertx_web",
    ],
)
