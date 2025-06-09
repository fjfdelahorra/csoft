# csoft

Este es un proyecto base de Java usando [Vert.x](https://vertx.io/) y [Bazel](https://bazel.build/).

Incluye la dependencia `netty-resolver-dns-native-macos` para evitar la advertencia
de Netty en macOS sobre la resolución DNS.

## Ejecutar

```
bazel run //:vertx_hello
```

Al ejecutarlo, se inicia un servidor HTTP en el puerto `8080` que responde con `Hola Mundo`.
