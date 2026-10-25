# 🎮 Hache's Quizz

### Aplicación Android de preguntas de verdadero o falso

**Hache's Quizz** es una aplicación desarrollada en **Kotlin y Jetpack Compose** con Android Studio, 

El proyecto ha evolucionado progresivamente, incorporando nuevas funcionalidades sin perder las desarrolladas anteriormente.

La aplicación mantiene una identidad visual propia, con colores púrpura, fucsia y amarillo, y permite alternar entre un tema oscuro y uno claro.

---

## 🛠️ Tecnologías utilizadas

- **Lenguaje:** Kotlin.
- **Entorno de desarrollo:** Android Studio.
- **Interfaz:** Jetpack Compose y Material 3.
- **Audio:** MediaPlayer.
- **Mapas:** Google Maps SDK for Android.
- **Almacenamiento:** archivos de texto locales y SharedPreferences.
- **Navegación:** actividades Android e Intent.
- **Componentes:** AndroidView, MapView, SeekBar y controles multimedia.

---

## 📱 Funcionalidades

### Prácticas 1 y 2: Juego de preguntas

La aplicación permite jugar a un cuestionario de verdadero o falso con las siguientes características:

- Modo clásico y modo infantil.
- Diferentes niveles de dificultad.
- Partidas sin límite de tiempo o con temporizadores de 15 y 8 segundos.
- Estadísticas de respuestas y resultados.
- Posibilidad de añadir preguntas personalizadas.
- Almacenamiento local de las preguntas.
- Cambio entre tema claro y oscuro.

### Práctica 3: Incorporación de audio

Se amplió el cuestionario con funcionalidades multimedia:

- Cinco preguntas con archivos de audio.
- Reproducción y pausa de sonidos.
- Barra de progreso interactiva mediante SeekBar.
- Visualización del tiempo transcurrido y la duración total.
- Reproducción en bucle.
- Efectos sonoros para respuestas correctas e incorrectas.
- Animaciones en determinados elementos del cuestionario.
- Acceso a información complementaria mediante enlaces externos.

Los archivos de audio están incluidos en `app/src/main/res/raw/`, por lo que no es necesaria una conexión a Internet para reproducirlos.

### Práctica 4: Integración de mapas

Se incorporaron nuevas pantallas y funcionalidades relacionadas con Tenerife.

**Identificación de usuarios**

La aplicación dispone de una pantalla de acceso con tres usuarios de prueba:

| Usuario | Contraseña |
|---|---|
| `alumno` | `pgl2026` |
| `profesor` | `cesarmanrique` |
| `hache` | `dam2026` |

Durante las pruebas en dispositivos físicos se detectaron problemas relacionados con la capitalización automática y los espacios introducidos por algunos teclados virtuales.

Por este motivo, se añadieron tres botones de acceso rápido que rellenan automáticamente los campos de usuario y contraseña.

**Estos botones son exclusivamente una ayuda para las pruebas académicas y se eliminarían en una versión destinada a usuarios reales.** La identificación utiliza credenciales definidas en el código y no constituye un sistema de autenticación seguro.

**Pantalla de bienvenida**

- Saludo personalizado según el usuario.
- Información sobre Tenerife y sus lugares de interés.
- Navegación hacia el cuestionario, la galería y los mapas.
- Música ambiental que puede activarse o desactivarse.
- Cierre de sesión.

**Galería de imágenes**

La galería contiene cinco fotografías relacionadas con Tenerife:

1. CIFP César Manrique.
2. Plaza de España de Santa Cruz de Tenerife.
3. Parque García Sanabria.
4. Catedral de San Cristóbal de La Laguna.
5. Parque Nacional del Teide.

Se puede navegar entre las fotografías y consultar sus descripciones.

**Explorador de Google Maps**

Se ha desarrollado la integración de mapas con las siguientes funcionalidades:

- Mapa centrado inicialmente en Santa Cruz de Tenerife.
- Selector de localizaciones de interés.
- Marcadores con información geográfica.
- Animaciones de cámara para desplazarse entre lugares.
- Representación de recorridos mediante `Polyline`.
- Cálculo de distancias entre localizaciones.
- Representación de un área geográfica mediante `Circle`.
- Incorporación de lugares personalizados seleccionando coordenadas sobre el mapa.
- Almacenamiento local de los lugares añadidos.

Las rutas representadas mediante `Polyline` conectan las coordenadas de los lugares; no calculan itinerarios reales por carreteras.

---

## 🗺️ Configuración de Google Maps

**La integración de Google Maps está implementada en el código, pero la visualización de la cartografía requiere una clave API válida y autorizada.**

Por tanto, las funcionalidades dependientes de la visualización del mapa están pendientes de una comprobación completa con una clave autorizada.

### Cómo configurar una clave propia

1. Crear o utilizar un proyecto en Google Cloud.
2. Habilitar **Maps SDK for Android**.
3. Obtener una clave API autorizada para la aplicación.
4. Abrir el archivo `local.properties`, situado en la raíz del proyecto.
5. Añadir la siguiente propiedad:

    ```properties
    MAPS_API_KEY=TU_CLAVE_API
    ```

6. Sincronizar Gradle, compilar y ejecutar la aplicación.

La clave se incorpora al manifiesto mediante `manifestPlaceholders` desde `app/build.gradle.kts`.

**Por seguridad, `local.properties` está excluido del repositorio mediante `.gitignore`.**

Si la clave está restringida a aplicaciones Android, debe estar autorizada para el paquete `com.hache.hachesquizz` y la huella SHA-1 del certificado utilizado para firmar la aplicación.

Más información: https://developers.google.com/maps/documentation/android-sdk/get-api-key

---

## 📂 Estructura del proyecto

Algunos archivos principales:

| Archivo | Función |
|---|---|
| `LoginActivity.kt` | Identificación de usuarios. |
| `BienvenidaActivity.kt` | Bienvenida y acceso a las distintas secciones. |
| `MenuPrincipal.kt` | Menú del cuestionario. |
| `Buzz.kt` | Desarrollo de las partidas. |
| `Pregunta.kt` | Modelo de las preguntas. |
| `RepositorioPreguntas.kt` | Gestión y almacenamiento de preguntas. |
| `ControladorAudio.kt` | Reproducción de audio y efectos sonoros. |
| `PanelAudio.kt` | Controles de reproducción de audio. |
| `MusicaFondo.kt` | Gestión de la música ambiental. |
| `GaleriaActivity.kt` | Galería de fotografías de Tenerife. |
| `MapaActivity.kt` | Explorador geográfico. |
| `MapaUtils.kt` | Gestión del ciclo de vida de MapView. |
| `AgregarLugarActivity.kt` | Creación de nuevas localizaciones. |
| `Lugar.kt` | Modelo de los lugares de interés. |
| `RepositorioLugares.kt` | Almacenamiento y cálculo de distancias. |

Los recursos gráficos se encuentran en `app/src/main/res/drawable/` y los archivos de sonido en `app/src/main/res/raw/`.

---

## ▶️ Cómo ejecutar el proyecto

1. Clonar el repositorio:

    ```bash
    git clone https://github.com/HacheStack/HachesQuizz.git
    ```

2. Abrir el proyecto en Android Studio.
3. Esperar a que finalice la sincronización de Gradle.
4. Seleccionar un emulador o conectar un dispositivo Android.
5. Ejecutar la aplicación.
6. Iniciar sesión con una de las cuentas de prueba indicadas anteriormente.
