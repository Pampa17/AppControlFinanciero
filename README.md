# AppControlFinanciero

App de control financiero mensual para estudiantes universitarios: registro de ingresos y
egresos, alertas progresivas de presupuesto y categorización de movimientos, hecha para el
examen práctico de Desarrollo Móvil Multiplataforma (Ingeniería de Software, Universidad de
Santander).

## Funcionalidades

**Reglas de negocio obligatorias**
- El total de egresos del mes nunca puede superar el total de ingresos — el sistema bloquea el
  registro de un egreso que dejaría el saldo en negativo, indicando el monto máximo permitido.
- Alerta visual **Precaución** cuando el saldo disponible llega al 30% del ingreso del mes.
- Alerta visual **Crítico** al 10%, más una notificación local del sistema.
- Ingresos y egresos con descripción, categoría, monto y fecha; validación de campos vacíos y
  montos no positivos.
- Persistencia local entre sesiones (Room + DataStore).
- Egresos editables y eliminables, con confirmación antes de borrar.

**Extras**
- **Categorías dinámicas**: el usuario crea, renombra y elimina sus propias categorías (listas
  separadas para ingresos y gastos); la categoría es obligatoria en ambos formularios. Al
  eliminar una categoría en uso, sus movimientos se reasignan a "Sin categoría" — nunca se
  borran ingresos ni gastos.
- **Vista de Categorías**: gráfico de torta con el dinero por categoría del mes activo.
- **Registro por voz**: botón de micrófono que dicta descripción y monto ("gasté ocho mil pesos
  en transporte") y prellena el formulario para confirmar antes de guardar.
- **Personalización**: tema claro/oscuro, 3 paletas de acento, alias del usuario y
  nombre/emoji del mes activo, todo persistente.

## Stack técnico

- Kotlin 2.2 + Jetpack Compose (Material 3), sin XML de layout.
- Room (persistencia de ingresos/egresos/categorías) + DataStore Preferences (ajustes).
- Navigation Compose para las pantallas y el flujo de Agregar/Editar.
- `android.speech.SpeechRecognizer` nativo para el dictado por voz (sin dependencias externas).

## Cómo correr el proyecto

1. Clona el repo y ábrelo en Android Studio.
2. Antes de compilar, agrega las fuentes del sistema de diseño en `app/src/main/res/font/`:
   - [Space Grotesk](https://fonts.google.com/specimen/Space+Grotesk) → `space_grotesk.ttf`
   - [DM Sans](https://fonts.google.com/specimen/DM+Sans) → `dm_sans.ttf`
   (solo el peso Regular de cada una; los demás pesos no se usan).
3. Sincroniza Gradle y corre en un emulador o dispositivo con Android 7.0 (API 24) o superior.
4. Para el dictado por voz, concede el permiso de micrófono cuando la app lo solicite.

## Autores

- Juan Pablo Santoyo Diaz
- Santiago Sánchez Ribero
