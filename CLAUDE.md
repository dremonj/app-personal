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

- El repo todavía es un proyecto **Kotlin JVM + Gradle** (JDK 21) recién creado, con `src/` vacío. **Falta convertirlo a Kotlin Multiplatform** (`shared/` + `iosApp/`).
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
- Kotlin/Native admite Mac Intel como host, e `iosArm64` es un destino Tier 1. La primera compilación en este Mac será lenta.
- Git en el Mac está autenticado con GitHub CLI (`gh auth login`). En este repo se usa el correo noreply de GitHub.

## Flujo de trabajo con dos ordenadores

1. **Al empezar**: `git pull` (en IntelliJ, Ctrl+T).
2. Trabajar: lógica y tests en Windows, pantallas en el Mac.
3. **Al terminar**: `git add .` → `git commit -m "mensaje claro"` → `git push` (en IntelliJ, Ctrl+K y después Ctrl+Shift+K).
