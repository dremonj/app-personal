# app-personal

App personal para el iPhone del usuario. La lógica va en **Kotlin** (Kotlin Multiplatform) y la interfaz en **SwiftUI**. La app funciona solo en el iPhone, sin servidor.

## Cómo trabajar con el usuario (IMPORTANTE)

- **El usuario escribe todo el código.** Claude le dice qué escribir, paso a paso y en fragmentos pequeños, y le explica qué hace cada cosa y para qué sirve. No crees ni edites archivos de código de la app salvo que lo pida expresamente. Sí puedes leer el código, revisarlo y explicar errores.
- **Los commits y los push los hace el usuario.** Recuérdale el flujo cuando toque.
- Habla en **español**, con explicaciones sencillas. Está aprendiendo Kotlin/Java y algo de Swift.
- Respuestas breves y directas, sobre todo con dudas rápidas.

## Arquitectura

- **Kotlin (`shared/`)**: modelos de datos, reglas y cálculos, validaciones, guardado de datos en el iPhone y tests. Casi todo el trabajo va aquí. Regla: si no es algo que se ve o se toca, va en Kotlin.
- **Swift (`iosApp/`)**: solo pantallas, navegación, botones y formularios, más el "pegamento" para llamar a Kotlin.
- Al principio se evitan las corrutinas y `Flow` en la API que usa Swift, para que la conexión sea sencilla.
- En `commonMain` se usa solo la librería estándar de Kotlin, nada de `java.*`. Para fechas, `kotlinx-datetime`.

## Estado actual

- **Gradle ya está convertido a Kotlin Multiplatform** (Kotlin 2.4.21, Gradle 9.6, JDK 21). El módulo `shared` tiene los targets `jvm()`, para los tests en Windows, e `iosArm64`, que genera el framework estático `Shared`. Paquete: `com.dremonj.apppersonal`.
- **Falta crear `iosApp/` en Xcode, en el Mac**, y conectarlo con la *Run Script* `./gradlew :shared:embedAndSignAppleFrameworkForXcode`.
- **`shared/` todavía no tiene archivos `.kt`**, así que Gradle responde `NO-SOURCE` y no genera el framework `Shared`. Hasta que exista el primer archivo Kotlin no se puede probar el framework en Xcode.
- En el Mac ya se ha comprobado (2026-11-16) que Gradle arranca con JDK 21, que el proyecto configura bien para `jvm` e `iosArm64` y que el compilador de Kotlin/Native ya está descargado (`~/.konan`).
- Tests en Windows: `./gradlew :shared:jvmTest` (o el botón ▶ de IntelliJ). La terminal de Windows tiene Java 8 por defecto, así que hay que usar el JDK 21 de IntelliJ (`C:\Users\danie\.jdks\temurin-21.0.13`).
- **Falta decidir qué hará la app.** Pregúntalo antes de diseñar clases.

## Entorno

| | Windows (PC) | Mac |
|---|---|---|
| Uso | Lógica Kotlin en IntelliJ IDEA | Pantallas SwiftUI en Xcode y pruebas en el iPhone |
| Ruta del repo | `C:\Users\danie\Desktop\PROYECTO` | `~/Proyectos/app-personal` |

- **Repo**: https://github.com/dremonj/app-personal (público, rama `main`). Es su único repo público, aparte del README del perfil.
- **Mac**: MacBook Air 2012 (Intel, 8 GB de RAM, unos 121 GB de disco) con **macOS Sequoia 15.8.3 instalado con OpenCore Legacy Patcher** (OCLP 2.5.1). Va lento.
  - **No actualizar macOS** sin comprobar antes que OCLP lo admite. Después de cada actualización, revisar en OCLP los *Root Patches*.
- **Xcode 26.3 (Universal)** es el máximo para Sequoia. Xcode 27 pide Tahoe y no es viable. Tiene instalado el componente iOS 26.2, que también hace falta para compilar para el dispositivo. **Sin simulador**: las pruebas se hacen en el iPhone por cable.
- **iPhone con iOS 27.2.1**. Se ha comprobado que Xcode 26.3 instala y ejecuta apps en él. El primer arranque con depurador tarda mucho.
- **Apple ID gratuito (Personal Team)**: las apps caducan a los 7 días. Para renovarlas, conectar el iPhone y pulsar ▶ en Xcode.
- **JDK 21 en el Mac**: Temurin 21.0.13 (x64), instalado en `~/Library/Java/JavaVirtualMachines/`, la carpeta de Java del usuario (no hizo falta `sudo`). `java -version` y `/usr/libexec/java_home` lo encuentran. No hay Homebrew.
- **⚠️ Kotlin/Native va a dejar de admitir los Mac Intel.** Con Kotlin 2.4.21, Gradle avisa: *"The current host platform 'macos_x64' is deprecated and will be removed in a future Kotlin release"*. Hoy funciona, pero **no subas la versión de Kotlin del proyecto sin comprobar antes que sigue admitiendo `macos_x64` como host.** Si se pierde el soporte, este Mac no podrá compilar el framework para el iPhone. `iosArm64` es un destino Tier 1 y la primera compilación en este Mac es lenta.
- **`gradlew` debe ser ejecutable.** Si se crea o se toca desde Windows, puede perder el permiso y en el Mac sale `permission denied: ./gradlew`. Se arregla con `git update-index --chmod=+x gradlew`, que cambia el permiso en el repo, y `chmod +x gradlew`, que lo cambia en el disco. Después, commit y push.
- Git en el Mac está autenticado con GitHub CLI (`gh auth login`). En este repo se usa el correo noreply de GitHub.

## Flujo de trabajo con dos ordenadores

1. **Al empezar**: `git pull` (en IntelliJ, Ctrl+T).
2. Trabajar: lógica y tests en Windows, pantallas en el Mac.
3. **Al terminar**: `git add .` → `git commit -m "mensaje claro"` → `git push` (en IntelliJ, Ctrl+K y después Ctrl+Shift+K).


- Esto es una prueba