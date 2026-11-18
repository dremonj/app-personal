# DailyHub — Plan del proyecto

App personal para iPhone. Backend en Kotlin (PC Windows) y app en Swift (Mac + iPhone).
Nombre: **DailyHub**.

---

## 1. Qué hace la app

| Sección | Qué muestra o permite |
|---|---|
| **Hoy** (pantalla de inicio) | Pasos del día con objetivo, si has cumplido la rutina del gimnasio, lo que tienes hoy (clases, citas, reuniones) y el próximo examen o cita importante con días restantes |
| **Calendario** | Apuntar exámenes, citas, clases (que se repiten cada semana) y reuniones |
| **Entreno** | Apuntar los ejercicios y series que has hecho al acabar en el gimnasio. Mapa del cuerpo (delante y detrás) con los músculos trabajados esta semana |
| **Recetas** | Tus recetas por comida (desayuno, comida, cena, snack). Botón "Sin ideas" que te da una al azar |
| **Coach** | Chat con IA sobre gimnasio y dieta, con tus entrenos como contexto |

---

## 2. Arquitectura

```
iPhone (Swift / SwiftUI)                      PC Windows (Kotlin / Ktor)
┌─────────────────────────┐   HTTP + JSON    ┌──────────────────────────────┐
│ Views  ─▶ ViewModels    │ ───────────────▶ │ routes ─▶ service ─▶ repo    │──▶ SQLite
│ APIClient               │ ◀─────────────── │                              │
│ HealthService (pasos)   │                  │ AiClient ────────────────────┼──▶ API de IA (Groq)
└─────────────────────────┘                  └──────────────────────────────┘
```

### Qué se hace en Kotlin y qué en Swift

| Funcionalidad | Kotlin (backend) | Swift (iPhone) |
|---|---|---|
| Resumen de hoy | Junta pasos, estado del gym y eventos en `GET /api/today` | Pantalla Hoy |
| Calendario | CRUD de eventos. Repite las clases semanales dentro del rango pedido | Vista mes, lista del día, formulario |
| Rutina | Guarda qué días entrenas y calcula si has cumplido | Ajustes de rutina |
| Entrenos | Catálogo de ejercicios, sesiones y series. Pide a la IA los músculos de cada ejercicio nuevo | Formulario para apuntar ejercicios y series |
| Mapa muscular | Calcula las series por músculo de la semana | Dibuja el cuerpo y colorea cada músculo |
| Pasos | Guarda el histórico | Lee los pasos de Salud (HealthKit) y los envía |
| Recetas | CRUD, filtro por comida, receta al azar | Lista, detalle y formulario |
| Coach IA | Guarda la API key, monta el contexto y llama a la IA | Pantalla de chat |

---

## 3. Estructura del repo

Si tu repo ya tiene otra estructura, adapta las rutas.

```
dailyhub/
├── CLAUDE.md
├── README.md
├── .gitignore
├── docs/
│   └── PLAN.md
├── backend/                         # Kotlin + Ktor (Windows, IntelliJ)
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   ├── .env.example
│   └── src/
│       ├── main/kotlin/com/danielremon/dailyhub/
│       │   ├── Application.kt
│       │   ├── plugins/       # Serialization, Database, Routing, StatusPages, Auth
│       │   ├── model/         # DTOs y enums
│       │   ├── db/            # Tablas de Exposed
│       │   ├── repository/    # Acceso a la base de datos
│       │   ├── service/       # Lógica (resumen, rutina, volumen muscular, coach)
│       │   ├── routes/        # Endpoints HTTP
│       │   └── ai/            # Cliente de la API de IA
│       └── test/kotlin/com/danielremon/dailyhub/
└── ios/                             # SwiftUI (Mac, Xcode)
    └── DailyHub/
        ├── DailyHubApp.swift
        ├── Config/            # URL del servidor, token
        ├── Models/            # Structs Codable (copia de los DTO)
        ├── Networking/        # APIClient
        ├── Services/          # HealthService (HealthKit)
        ├── Features/
        │   ├── Today/
        │   ├── Calendar/
        │   ├── Workout/       # Incluye BodyMap
        │   ├── Recipes/
        │   └── Coach/
        └── Shared/            # Componentes reutilizables
```

---

## 4. Modelo de datos

### Enums (iguales en Kotlin y Swift, se envían como texto)

```kotlin
enum class EventType  { CLASS, APPOINTMENT, MEETING, EXAM, OTHER }
enum class Recurrence { NONE, WEEKLY }
enum class MealType   { BREAKFAST, LUNCH, DINNER, SNACK }
enum class MuscleRole { PRIMARY, SECONDARY }
enum class GymStatus  { DONE, PENDING, MISSED, REST_DAY }
enum class Muscle {
    CHEST, FRONT_DELTS, SIDE_DELTS, REAR_DELTS, BICEPS, TRICEPS, FOREARMS,
    ABS, OBLIQUES, TRAPS, LATS, LOWER_BACK, GLUTES, QUADS, HAMSTRINGS, ADDUCTORS, CALVES
}
```

### Tablas

| Tabla | Columnas | Notas |
|---|---|---|
| `events` | id, title, type, date, start_time?, end_time?, important, recurrence, repeat_until?, notes? | `important = true` para exámenes y citas que salen en "próximo importante" |
| `recipes` | id, name, meal_type, ingredients, instructions, prep_minutes?, kcal?, protein_g? | `ingredients`: un ingrediente por línea |
| `exercises` | id, name (único) | Catálogo que crece según apuntas ejercicios nuevos |
| `exercise_muscles` | exercise_id, muscle, role | Relación N-M entre ejercicio y músculo |
| `workout_sessions` | id, date (única), notes? | Una sesión por día |
| `workout_sets` | id, session_id, exercise_id, position, reps, weight_kg | Cada fila es una serie |
| `routine_days` | day_of_week (1 = lunes … 7 = domingo) | Días que te toca entrenar |
| `daily_steps` | date (clave), steps | Lo envía el iPhone |
| `settings` | key, value | Por ejemplo `steps_goal = 10000` |

`?` = puede ser nulo.

---

## 5. Contrato de la API

Fuente de verdad para Kotlin y Swift. Fechas en formato `2026-11-18`, horas en `08:30`.

| Método | Ruta | Para qué |
|---|---|---|
| GET | `/health` | Comprobar que el servidor responde |
| GET | `/api/today` | Resumen del día |
| GET | `/api/events?from=&to=` | Eventos de un rango (con las clases semanales ya repetidas) |
| POST | `/api/events` | Crear evento |
| PUT | `/api/events/{id}` | Editar evento |
| DELETE | `/api/events/{id}` | Borrar evento |
| GET | `/api/recipes?mealType=` | Listar recetas (filtro opcional) |
| GET | `/api/recipes/random?mealType=` | Una receta al azar |
| GET | `/api/recipes/{id}` | Ver receta |
| POST | `/api/recipes` | Crear receta |
| PUT | `/api/recipes/{id}` | Editar receta |
| DELETE | `/api/recipes/{id}` | Borrar receta |
| GET | `/api/exercises?search=` | Buscar en tu catálogo (autocompletar) |
| POST | `/api/exercises/suggest-muscles` | La IA sugiere los músculos de un ejercicio nuevo |
| POST | `/api/exercises` | Crear ejercicio con sus músculos (ya confirmados) |
| GET | `/api/workouts/{date}` | Sesión de un día |
| PUT | `/api/workouts/{date}` | Guardar la sesión del día (crea o reemplaza) |
| GET | `/api/routine` | Días de entreno |
| PUT | `/api/routine` | Cambiar días de entreno |
| GET | `/api/muscles/weekly?date=` | Series por músculo en la semana de esa fecha |
| PUT | `/api/steps/{date}` | Guardar los pasos de un día |
| POST | `/api/coach/chat` | Preguntar al coach |

### Ejemplo: `GET /api/today`

```json
{
  "date": "2026-11-18",
  "steps": 7421,
  "stepsGoal": 10000,
  "gym": { "status": "PENDING", "weekDone": 2, "weekPlanned": 4 },
  "events": [
    { "id": 3, "title": "Programación", "type": "CLASS", "startTime": "08:30", "endTime": "10:20" }
  ],
  "nextImportant": { "id": 9, "title": "Examen de Bases de Datos", "date": "2026-11-23", "daysLeft": 5 }
}
```

### Ejemplo: `POST /api/exercises/suggest-muscles`

Petición:
```json
{ "name": "press inclinado con mancuernas" }
```
Respuesta:
```json
{ "primary": ["CHEST", "FRONT_DELTS"], "secondary": ["TRICEPS"] }
```

### Ejemplo: `PUT /api/workouts/2026-11-18`

```json
{
  "notes": "Buen día",
  "sets": [
    { "exerciseId": 4, "reps": 10, "weightKg": 60.0 },
    { "exerciseId": 4, "reps": 8,  "weightKg": 65.0 },
    { "exerciseId": 7, "reps": 12, "weightKg": 14.0 }
  ]
}
```

---

## 6. Lógica clave del backend

Cada una lleva su test.

- **RecurrenceExpander**: dado un rango `from..to`, un evento `WEEKLY` genera una copia cada 7 días desde su fecha hasta `repeatUntil` o hasta `to`.
- **RoutineComplianceService**: estado de un día.
  - Hay sesión guardada → `DONE`
  - No es día de entreno y no hay sesión → `REST_DAY`
  - Es día de entreno, es hoy y no hay sesión → `PENDING`
  - Es día de entreno, ya pasó y no hay sesión → `MISSED`
  - Además: entrenos hechos / planificados en la semana.
- **MuscleSuggestionService**: recibe el nombre del ejercicio tal como lo escribes. Pide a la IA que responda solo con JSON `{"primary": [...], "secondary": [...]}` usando los valores del enum `Muscle`.
  - Valida la respuesta: descarta cualquier valor que no exista en el enum.
  - Si la llamada falla o no hay API key, devuelve listas vacías y eliges tú a mano.
  - Solo se llama con ejercicios nuevos. Los del catálogo ya tienen sus músculos guardados.
  - `AiClient` es una interfaz: en los tests se usa uno falso y, si algún día cambias de proveedor (por ejemplo, a Claude), solo cambia la implementación.
- **MuscleVolumeService**: semana de lunes a domingo. Cada serie suma 1 a sus músculos principales y 0,5 a los secundarios. Devuelve todos los músculos, también los que tienen 0.
  - Colores en la app: 0 gris · menos de 5 bajo · de 5 a 9 medio · 10 o más alto (umbrales configurables).
- **TodayService**: junta pasos, rutina, eventos del día y el próximo evento importante. Si no hay pasos guardados, `steps` va a `null`.
- **CoachService**: prompt de sistema (entrenador y nutrición, en español, recuerda que no es consejo médico) + contexto con tus entrenos de los últimos 14 días y tu volumen semanal. La app envía el historial del chat y el backend no lo guarda (de momento).

---

## 7. Pantallas de la app

`TabView` con 5 pestañas: **Hoy · Calendario · Entreno · Recetas · Coach**.
Ajustes (días de rutina, objetivo de pasos, URL del servidor) en una hoja que se abre desde Hoy.

### Colores (solo modo oscuro)

| Nombre | Hex | Uso |
|---|---|---|
| Midnight Ink | `#121A2E` | Fondo de la app |
| Ink Surface | `#1C2640` | Tarjetas y listas |
| Azure Blue | `#2F8CFF` | Color principal: botones, pestaña activa, enlaces, entreno hecho |
| Vermilion Orange | `#F2552C` | Lo importante y los avisos: exámenes, entreno sin hacer, volumen muscular alto |
| Texto principal | `#F2F4F8` | Títulos y texto |
| Texto secundario | `#9AA4B8` | Horas, etiquetas, datos secundarios |
| Músculo sin trabajar | `#2C3852` | Mapa muscular |

Reglas:
- La app va siempre en modo oscuro (`.preferredColorScheme(.dark)`).
- Los colores se definen una vez en `Assets.xcassets` (Color Sets) y se usan desde `Theme.swift` (`Color.midnightInk`, `Color.azureBlue`…). Nunca colores sueltos en las vistas.
- Botones rellenos de Azure Blue: texto en Midnight Ink (contraste 5,2:1). En blanco se queda en 3:1.
- El color nunca va solo: siempre con icono o texto (✓ hecho, ✕ sin hacer).
- Mapa muscular: 0 series → gris · bajo → Azure Blue al 40 % · medio → Azure Blue · alto → Vermilion Orange.

---

## 8. Conexión iPhone ↔ PC (desarrollo)

- Ktor escucha en `0.0.0.0:8080` para aceptar conexiones de la red local.
- Firewall de Windows: permitir el puerto 8080 en red privada.
- IP del PC: `ipconfig` (por ejemplo `192.168.1.40`). Conviene reservarla en el router para que no cambie.
- Probar primero desde Safari del iPhone: `http://192.168.1.40:8080/health`.
- iOS pide permiso de red local: añadir `NSLocalNetworkUsageDescription` en Info.plist.
- Si iOS bloquea HTTP (error de ATS): añadir `NSAppTransportSecurity` → `NSAllowsLocalNetworking = YES`.
- Fuera de casa no funciona hasta la fase 7.

---

## 9. Fases

Cada fase acaba con algo que funciona en el iPhone y un commit.

Orden dentro de cada fase: **Kotlin primero** (Windows) → probar con curl → commit y push → **Swift después** (Mac, `git pull`) → probar en el iPhone. No se empieza la siguiente fase hasta que las dos partes funcionan juntas.

### Fase 0 — Cimientos
**Hecho cuando:** el iPhone muestra "Servidor OK" leyendo `GET /health` del PC.
1. Estructura del repo, `.gitignore` (Gradle, IntelliJ, Xcode, `.env`) y README mínimo.
2. Proyecto Ktor en `backend/` con Netty y kotlinx.serialization.
3. `Application.kt` con `GET /health` → `{"status":"ok"}`. Probar con curl.
4. Abrir el puerto en el firewall y probar desde Safari del iPhone.
5. Proyecto SwiftUI en `ios/` con `TabView` y 5 pestañas vacías. Color Sets de la paleta + `Theme.swift` + modo oscuro.
6. `APIClient` mínimo y pantalla que llama a `/health`.

**Aprendes:** Gradle, `fun main`, lambdas, rutas de Ktor · `View`, `TabView`, `async/await`, `URLSession`.

### Fase 1 — Calendario (primer CRUD completo)
**Hecho cuando:** creas una clase semanal y un examen desde el iPhone y aparecen en sus días.
1. Enums `EventType` y `Recurrence` + `EventDto`.
2. Conexión a SQLite con Exposed + tabla `Events`.
3. `EventRepository` (crear, leer, editar, borrar).
4. `RecurrenceExpander` + `EventService` + test.
5. `eventRoutes` + `StatusPages` para errores (400, 404).
6. iOS: modelo `Event` (`Codable`) y métodos en `APIClient`.
7. iOS: `CalendarViewModel` + lista de eventos del día.
8. iOS: formulario crear/editar y borrar deslizando.
9. iOS: selector de día con `DatePicker` estilo `.graphical`.

**Aprendes:** `data class`, null safety, `enum`, funciones de extensión, transacciones, tests · `Codable`, `@Observable`, `NavigationStack`, `Form`, `.sheet`.

### Fase 2 — Recetas (repaso del patrón)
**Hecho cuando:** guardas recetas y "Sin ideas" te da una de la comida elegida.
1. `MealType` + `RecipeDto` + tabla `Recipes`.
2. Repository, service y routes (incluye `/random`).
3. iOS: lista con filtro por comida (`Picker` segmentado), detalle y formulario.
4. iOS: botón "Sin ideas".

**Opcional:** intenta escribir tú cada capa antes de ver el código y luego compara.

### Fase 3 — Entrenos y rutina
**Hecho cuando:** apuntas tu entreno al acabar y el backend sabe si has cumplido.
1. `Muscle`, `MuscleRole` + tablas `Exercises` y `ExerciseMuscles`.
2. Buscar en tu catálogo (autocompletar) + `POST /api/exercises`.
3. Crear cuenta gratuita en la consola de Groq y una API key.
4. `backend/.env` con `AI_BASE_URL=https://api.groq.com/openai/v1`, `AI_API_KEY` y `AI_MODEL` (uno de los modelos gratuitos de Groq). Leerlos al arrancar.
5. Interfaz `AiClient` + implementación `OpenAiCompatibleClient` con el cliente HTTP de Ktor llamando a `/chat/completions`.
6. `MuscleSuggestionService` + `POST /api/exercises/suggest-muscles` + test con un `AiClient` falso.
7. Tablas `WorkoutSessions` y `WorkoutSets` + `GET/PUT /api/workouts/{date}`.
8. Tabla `RoutineDays` + `GET/PUT /api/routine`.
9. `RoutineComplianceService` + test.
10. iOS: "Entreno de hoy": escribes el ejercicio → si es nuevo, aparecen los músculos sugeridos ya marcados → confirmas o corriges → añades series (reps × kg) → guardar.
11. iOS: ajustes de rutina.

**Nota:** el plan gratuito de Groq no pide tarjeta. Tiene límites de peticiones y tokens por día, que para una sola persona sobran. Solo se hace una llamada por cada ejercicio nuevo.

**Aprendes:** relaciones 1-N y N-M, transacciones con varias tablas, validación, interfaces e inyección de dependencias, consumir una API externa · listas dinámicas y formularios anidados.

### Fase 4 — Pantalla Hoy y pasos
**Hecho cuando:** al abrir la app ves tu día de un vistazo.
1. Tablas `DailySteps` y `Settings` + `PUT /api/steps/{date}`.
2. `TodayService` + `GET /api/today` + test.
3. iOS: `HealthService` con HealthKit (pedir permiso y leer los pasos de hoy).
4. iOS: enviar los pasos al backend al abrir la app.
5. iOS: pantalla Hoy (pasos con barra de progreso, estado del gym, eventos del día y próximo importante).

**Los pasos son opcionales.** Salen del propio iPhone. Si HealthKit no funciona con la cuenta gratuita, la pantalla Hoy se muestra sin pasos (`"steps": null`) y seguimos. Alternativa opcional: automatización de Atajos que cada noche lea los pasos de Salud y haga `PUT /api/steps/{fecha}`.

**Aprendes:** servicios que combinan datos · HealthKit, permisos y ciclo de vida de la app.

### Fase 5 — Mapa muscular
**Hecho cuando:** ves en el cuerpo qué has trabajado esta semana.
1. `MuscleVolumeService` + `GET /api/muscles/weekly` + test.
2. Conseguir un SVG del cuerpo (delante y detrás) con un path por músculo y licencia libre (MIT o CC0). Citarlo en el README.
3. Convertir cada path en un `Shape` de SwiftUI asociado a un `Muscle`.
4. `BodyMapView`: selector Delante/Detrás, colores por nivel, leyenda. Al tocar un músculo se ven sus series.

**Aprendes:** agregaciones · `Shape`, `Path`, `ZStack`, gestos.

### Fase 6 — Coach IA
**Hecho cuando:** preguntas "¿qué me falta entrenar esta semana?" y responde con tus datos.
1. Ampliar `AiClient` (fase 3) para enviar conversaciones con varios mensajes.
2. `CoachService` con prompt de sistema y contexto.
3. `POST /api/coach/chat`.
4. iOS: pantalla de chat (burbujas, campo de texto, estado "pensando…").

**Nota:** si una pregunta supera el límite diario gratuito de Groq, la API devuelve un error 429. El backend lo traduce a un mensaje claro en la app ("Límite diario alcanzado, vuelve mañana").

**Aprendes:** JSON anidado, historial de conversación, timeouts y errores de red.

### Fase 7 — Portfolio y despliegue
**Hecho cuando:** la app funciona fuera de casa y el repo se entiende sin explicarlo.
1. Autenticación con token fijo (`Authorization: Bearer`) en todas las rutas `/api`.
2. Pasar de SQLite a PostgreSQL.
3. Dockerfile del backend y despliegue en un servidor (el proveedor se decide en ese momento, los precios cambian).
4. GitHub Actions: compilar y pasar tests en cada push.
5. README: qué es, capturas, arquitectura, cómo ejecutarlo y decisiones técnicas.

---

## 10. Decisiones tomadas

Sirven también para explicarlas en una entrevista.

- **Backend Kotlin separado de la app (no Kotlin Multiplatform):** encaja con el reparto Windows/Mac y es la arquitectura cliente-servidor más habitual en empresa.
- **Ktor y no Spring Boot:** menos "magia" con anotaciones. Ves cada ruta y cada capa. Lo que aprendes (HTTP, capas, JSON, base de datos) se aplica igual en Spring.
- **SQLite al principio:** un solo archivo, sin instalar nada. Con Exposed, pasar a PostgreSQL cambia poco.
- **API key en el backend, no en la app:** no viaja en el iPhone, no acaba en GitHub y el backend puede añadir tus datos como contexto.
- **IA con Groq (plan gratuito) y formato compatible con OpenAI:** coste 0. Muchos proveedores usan ese mismo formato, así que cambiar de proveedor es cambiar `AI_BASE_URL`, `AI_API_KEY` y `AI_MODEL` en el `.env`. Se descartaron Claude (de pago) y el plan gratuito de Gemini (sus condiciones limitan el uso gratuito en la UE y pueden usar tus datos para mejorar sus productos).
- **Los músculos de cada ejercicio los sugiere la IA:** escribes el nombre como quieras. La primera vez la IA propone los músculos y tú confirmas o corriges. Se guarda en tu catálogo y no se vuelve a preguntar. Se descartó una base de datos pública de ejercicios (como wger) porque habría que buscar cada ejercicio con su nombre, no con el tuyo.
- **Pasos del propio iPhone, leídos de Salud (HealthKit):** el reloj Xiaomi no se puede vincular al iPhone, así que se descarta. Si más adelante hay un reloj compatible, sus pasos también entran en Salud y la app no cambia.
- **Funciones opcionales:** si algo no funciona con la cuenta gratuita de Apple (por ejemplo, HealthKit), se deja fuera y la app sigue funcionando sin ello.
- **Código en inglés, comentarios en español.**

---

## 11. Confirmado

- [x] Reloj Xiaomi: descartado. Los pasos salen del iPhone.
- [x] Cuenta de Apple Developer: gratuita. La app se reinstala desde Xcode cada 7 días (los datos están en el backend, no se pierden).
- [x] Nombre de la app: DailyHub.
