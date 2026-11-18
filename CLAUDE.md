# DailyHub — Contexto para Claude Code

App personal de Daniel Remón (solo para él). Objetivo: aprender **Kotlin** (backend) y **Swift** (app de iPhone).
También es proyecto de portfolio: el código tiene que estar limpio y ser fácil de entender.

Plan completo (arquitectura, modelos, API, fases): `docs/PLAN.md`. Léelo al empezar cada fase.

## Cómo trabajamos (IMPORTANTE)

Daniel está aprendiendo. Sabe Java básico (DAM1). Kotlin y Swift son nuevos para él.

- **Daniel escribe el código.** No crees ni edites archivos de código salvo que te lo pida explícitamente. Enséñale el código en el chat y él lo escribe.
- Pasos pequeños: un archivo o una clase por paso.
- En cada paso:
  1. Qué vamos a hacer y por qué (2-3 frases).
  2. Ruta exacta del archivo.
  3. Código completo del archivo, o el trozo exacto a añadir indicando dónde va.
  4. Explicación de cada clase y cada método: qué hace, parámetros, qué devuelve y por qué está escrito así.
  5. Conceptos nuevos de Kotlin/Swift comparados con su equivalente en Java.
  6. Cómo comprobar que funciona (ejecutar, curl, test o iPhone).
  7. Mensaje de commit sugerido.
- Puedes leer los archivos del repo para revisar lo que ha escrito Daniel y señalar errores.
- **Pruebas: las ejecutas tú** cuando Daniel lo pida.
  - Backend (Windows): `./gradlew test`, arrancar el servidor en segundo plano y hacer peticiones con curl. Páralo al acabar.
  - iOS (Mac): puedes compilar con `xcodebuild` para detectar errores. La prueba en el iPhone la hace Daniel.
  - Enseña cada comando que ejecutes y explica el resultado, para que Daniel aprenda a hacerlo él.
- Si algo falla: explica la causa antes de dar el arreglo y no edites el código, lo escribe Daniel. Propón una línea para "Errores y lecciones".
- No pases a la siguiente fase si la actual no compila y funciona.
- Responde en español de España, frases cortas.

## Stack

| Parte | Tecnología | Dónde |
|---|---|---|
| Backend | Kotlin, Ktor 3, kotlinx.serialization, kotlinx-datetime, Exposed, SQLite (PostgreSQL en la fase 7) | PC Windows, IntelliJ IDEA, carpeta `backend/` |
| App | Swift, SwiftUI (iOS 17+), URLSession con async/await, HealthKit | Mac, Xcode, carpeta `ios/` |
| IA | Groq (plan gratuito, API compatible con OpenAI), llamada **solo desde el backend** | `backend/` |

## Reglas del proyecto

- Toda la lógica y los datos viven en el backend. La app de iOS muestra datos, recoge lo que escribe Daniel y lee los pasos de Salud.
- Nombres de clases, métodos, variables y commits en inglés. Comentarios en español.
- Capas del backend: `routes` → `service` → `repository` → `db`. Las rutas nunca tocan la base de datos directamente.
- La app sigue MVVM: `View` (SwiftUI) + `ViewModel` (`@Observable`) + `APIClient`.
- Colores solo desde `Theme.swift` (paleta en `docs/PLAN.md`, sección 7). Modo oscuro siempre.
- Los modelos de Swift (`Codable`) reflejan exactamente los DTO de Kotlin. Si cambia uno, cambia el otro y se actualiza el contrato de la API en `docs/PLAN.md`.
- Secretos (`AI_API_KEY`, `APP_TOKEN`) solo en `backend/.env`, que está en `.gitignore`. En el repo va `backend/.env.example` sin valores.
- Se trabaja desde dos ordenadores: `git pull` antes de empezar, commit y `git push` al acabar.

## Comandos

- Backend: `cd backend` y `./gradlew run` (PowerShell: `.\gradlew run`)
- Tests del backend: `./gradlew test`
- Probar la API: `curl http://localhost:8080/health` (en PowerShell: `curl.exe`)
- iOS: abrir el proyecto de `ios/` en Xcode y ejecutar en el iPhone.

## Estado

<!-- Actualizar al acabar cada paso -->
- Fase actual: 0 — Cimientos
- Último paso hecho: —
- Siguiente paso: crear el proyecto Ktor en `backend/`

## Errores y lecciones

<!-- Una línea por error: qué pasó → causa → solución -->
-

## Entorno

| | Windows (PC) | Mac |
|---|---|---|
| Uso | Backend Kotlin en IntelliJ IDEA | App SwiftUI en Xcode y pruebas en el iPhone |
| Ruta del repo | `C:\Users\danie\Desktop\PROYECTO` | `~/Proyectos/app-personal` |

- **Repo**: https://github.com/dremonj/app-personal (público, rama `main`). Git en el Mac autenticado con GitHub CLI; se usa el correo noreply de GitHub.
- **Windows**: la terminal tiene Java 8 por defecto. Usar el JDK 21 de IntelliJ (`C:\Users\danie\.jdks\temurin-21.0.13`).
- **Mac**: MacBook Air 2012 (Intel, 8 GB de RAM, ~121 GB de disco) con macOS Sequoia 15.8.3 vía OpenCore Legacy Patcher (OCLP 2.5.1). Va lento.
  - **No actualizar macOS** sin comprobar antes que OCLP lo admite. Tras cada actualización, revisar los *Root Patches* en OCLP.
  - JDK 21 (Temurin 21.0.13, x64) en `~/Library/Java/JavaVirtualMachines/`. No hay Homebrew.
- **Xcode 26.3** es el máximo para Sequoia (Xcode 27 pide Tahoe). Tiene el componente iOS 26.2. **Sin simulador**: se prueba en el iPhone por cable.
- **iPhone con iOS 27.2.1**. Xcode 26.3 instala y ejecuta apps en él. El primer arranque con depurador tarda mucho.
- **Apple ID gratuito (Personal Team)**: las apps caducan a los 7 días. Para renovar, conectar el iPhone y pulsar ▶ en Xcode.
- **`gradlew` debe ser ejecutable.** Si sale `permission denied: ./gradlew` en el Mac: `git update-index --chmod=+x gradlew` y `chmod +x gradlew`, luego commit y push.
- **Estructura heredada**: en la raíz hay un proyecto Gradle Kotlin Multiplatform (`shared/`, sin archivos `.kt`) de antes de este plan. El plan usa `backend/` (Ktor) e `ios/`. Hay que decidir en la fase 0 cómo adaptarlo.
