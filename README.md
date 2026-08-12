# Cliente Movil TrueQ
## AUTORES
1. Jesús Alejandro Gutierrez Montufar
1. Miguel Angel Nuñez Gómez
1. Joseph Abrahan Yáñez García
Universidad Tecnológica de Tula - Tepeji
Tecnologías de la Información, Área Desarrollo de Software
Asesora Prof. Odisey Porras
2026

Cliente nativo de Android para la misma API que usa el sitio web
(`sitioWebTrueQ`), con las mismas funcionalidades **excepto el panel de
administrador** (como se pidió).

## Cómo abrirlo

1. Abre la carpeta `TrueQAndroid/` completa en Android Studio ("Open" →
   selecciona la carpeta, no un archivo).
2. **Falta el binario `gradle-wrapper.jar`** (es un `.jar` compilado, no algo
   que se pueda escribir a mano, y este entorno no tiene acceso a
   `gradle.org` para descargarlo). Dejé `gradle-wrapper.properties`
   apuntando a Gradle 8.9, pero al abrir el proyecto Android Studio va a
   avisarte que falta el wrapper — dale a "OK"/"Sync" cuando te ofrezca
   generarlo automáticamente, o corre `gradle wrapper` tú mismo si tienes
   Gradle instalado.
3. Deja que Gradle sincronice. Si Android Studio sugiere actualizar el AGP,
   Kotlin o el Gradle Wrapper a una versión más nueva, acepta la sugerencia —
   se usaron versiones estables pero conservadoras a propósito, para
   maximizar que abran sin fricción en cualquier Android Studio reciente.
4. Corre la app en un emulador (recomendado para empezar) o dispositivo físico.

## Conectar con el backend

Por defecto la app apunta a `http://10.0.2.2:8080/api/` — `10.0.2.2` es como
el **emulador** de Android ve el `localhost` de tu PC. Así que:

- Si usas el **emulador de Android Studio**: solo levanta el backend
  (`dotnet run` o docker-compose) en tu PC en el puerto 8080 y ya debería
  conectar.
- Si usas un **dispositivo físico**: cambia `API_ORIGIN` en
  `app/src/main/java/com/trueq/app/data/remote/RetrofitProvider.kt` por la IP
  de tu PC en la red local (ej. `http://192.168.1.50:8080`), y agrega esa IP
  al `network_security_config.xml` (`app/src/main/res/xml/`) para permitir
  tráfico HTTP sin TLS, igual que ya está hecho para `10.0.2.2`.
- Si despliegas la API con HTTPS en un dominio real, cambia `API_ORIGIN` a esa
  URL y ya no necesitas tocar `network_security_config.xml` ni
  `usesCleartextTraffic` en el `AndroidManifest.xml`.

## Qué incluye (espejo del sitio web, sin el panel de admin)

- **Auth**: login, registro, sesión persistida (DataStore) con validación de
  expiración del JWT — mismo comportamiento que el fix que se hizo en el
  sitio web.
- **Feed principal**: categorías, geolocalización (con fallback a CDMX si no
  hay permiso), tarjetas de producto.
- **Búsqueda y filtros**: panel colapsable (acordeón), categoría, tipo de
  transacción (poblado desde el endpoint, opción "Todos" sin filtrar),
  presupuesto y distancia.
- **Detalle de artículo**: carrusel de fotos (respeta el campo `Orden`),
  calificación del vendedor, botones "Enviar mensaje" y "Proponer oferta".
- **Publicar artículo**: fotos con reordenamiento (flechas ◀▶, la de índice 0
  es la "Principal"), tipo de transacción desde el endpoint, ubicación.
- **Mensajes**: lista de chats + conversación con historial completo,
  actualización automática cada 4 segundos (polling, igual que en el sitio
  web — no hay WebSockets en el backend), ofertas (Trueque / Comprar /
  Trueque + diferencia / Solicitud de donación para artículos publicados
  como donación) y calificación con estrellas tras una oferta aceptada.
- **Notificaciones**: listar, marcar leídas, badge de conteo en la campana.
- **Perfil**: datos reales del backend, trueques realizados, artículos
  activos, reseñas.

## Arquitectura

- **UI**: Jetpack Compose + Material 3, un `Screen.kt` + `ViewModel.kt` por
  pantalla (`ui/screens/<área>/`).
- **Navegación**: Navigation Compose (`navigation/TrueQNavHost.kt`), rutas en
  `navigation/Routes.kt`.
- **Red**: Retrofit + OkHttp + kotlinx.serialization
  (`data/remote/TrueQApi.kt`). `data/remote/ApiUtils.kt` tiene un
  `safeApiCall` que replica cómo el sitio web lee `err.response.data.message`
  para mostrar el error real del backend.
- **DI**: manual, sin Hilt (`di/AppContainer.kt` + `di/ViewModelFactory.kt`),
  a propósito — para no sumar otro punto de falla (procesador de anotaciones
  KSP) en un proyecto que no se pudo compilar aquí.
- **Modelos**: `data/model/*.kt`, un espejo 1:1 de los DTOs de C# del backend
  (mismos nombres de campo en camelCase, ya que ASP.NET Core serializa así
  por defecto).

## Limitaciones conocidas (heredadas del backend, iguales al sitio web)

- No hay endpoint de "chat por Id" — la conversación se busca dentro de la
  lista completa de chats.
- `CrearChat` puede duplicar chats si programas otra lógica de creación fuera
  del flujo ya resuelto de "Enviar mensaje"/"Proponer oferta" (esos dos ya
  reutilizan el chat existente).
- Las notificaciones de propuesta/calificación no tienen el `chatId` directo
  (traen el Id de la propuesta), así que no navegan al chat exacto — mismo
  comportamiento que el sitio web.
