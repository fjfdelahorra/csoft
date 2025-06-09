workspace(name = "csoft")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

RULES_JVM_EXTERNAL_TAG = "6.2"
RULES_JVM_EXTERNAL_SHA = "96f3d62aea5cf03f1d9c6945f1d0320d1b1c06aae898777fb6b4931f4538b863"

http_archive(
    name = "rules_jvm_external",
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    sha256 = RULES_JVM_EXTERNAL_SHA,
    urls = [
        "https://github.com/bazelbuild/rules_jvm_external/archive/refs/tags/%s.tar.gz" % RULES_JVM_EXTERNAL_TAG,
    ],
)

load("@rules_jvm_external//:repositories.bzl", "rules_jvm_external_deps")
load("@rules_jvm_external//:setup.bzl", "rules_jvm_external_setup")

rules_jvm_external_deps()
rules_jvm_external_setup()

load("@rules_jvm_external//:defs.bzl", "maven_install")

maven_install(
    name = "maven",
    artifacts = [
        "io.vertx:vertx-core:4.5.1",
    ],
    repositories = [
        "https://repo.maven.apache.org/maven2",
    ],
)
