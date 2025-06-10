# csoft

Este es un proyecto base de Java usando [Vert.x](https://vertx.io/) y [Bazel](https://bazel.build/).
Incluye una pequeña interfaz web desarrollada con Vue.js para descargar y
consultar el histórico de sorteos de la Bonoloto.

Incluye la dependencia `netty-resolver-dns-native-macos` para evitar la advertencia
de Netty en macOS sobre la resolución DNS.

## Ejecutar

```
bazel run //:vertx_hello
```

Al ejecutarlo, se inicia un servidor HTTP en el puerto `8080` que sirve la página
web Vue.js disponible en [http://localhost:8080](http://localhost:8080). Desde
la página principal puedes actualizar el histórico de sorteos y consultar los
resultados descargados. Ahora también puedes consultar las estadísticas de pares
e impares desde el enlace **Estadísticas** de la propia interfaz.

## Obtener estadísticas de pares e impares

Puedes calcular cuántos números pares e impares aparecieron en cada sorteo usando el binario `bonoloto_stats`:

```
bazel run //:bonoloto_stats -- path/al/fichero.csv
```

La salida es un CSV con las columnas `FECHA`, `EVEN` e `ODD` indicando la cantidad de números pares e impares en los seis números de cada combinación ganadora.

