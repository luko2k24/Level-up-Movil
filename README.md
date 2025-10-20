#  LEVEL-UP GAMER — Aplicación Móvil 

## Descripción del Proyecto
**Level-Up Gamer** es una aplicación móvil desarrollada en **Android Studio con Kotlin y Jetpack Compose**, creada como parte de la **Evaluación Parcial 2** de la asignatura **DSY1105 – Desarrollo de Aplicaciones Móviles** 

El objetivo del proyecto es ofrecer una experiencia fluida para los usuarios que desean explorar, comprar y calificar productos gamer desde su dispositivo móvil. La app aplica principios de **usabilidad, diseño visual, persistencia local y validaciones desacopladas**



## Integrantes del equipo
- **Lukas Meza **  
- **Christian Sandoval**

**Sección:** 002D  
**Profesor:** Bryan Vicente Soto Astudillo  


## Funcionalidades implementadas

### Interfaz y navegación
- Diseño visual coherente con la estética gamer (colores oscuros y acentos neón).  
- Navegación fluida entre pantallas mediante `NavController`.  
- Componentes visuales jerárquicos (`Card`, `LazyRow`, `OutlinedTextField`, `Scaffold`).  

### Formularios y validaciones
- Formulario de **registro e inicio de sesión** con validaciones visuales por campo.  
- Retroalimentación clara mediante íconos y mensajes visuales.  
- Validación de campos gestionada en un archivo lógico independiente (`Validacion.kt`).  

###  Lógica desacoplada y gestión de estado
- Arquitectura **MVVM** (Model–ViewModel–View).  
- Lógica centralizada en `ViewModelAutenticacion` y `ReviewViewModel`.  
- Flujo de datos mediante **StateFlow** y **coroutines**.  

###  Persistencia local
- Implementación de **Room Database** para almacenar usuarios, productos y reseñas.  
- Repositorios (`UsuarioRepository`, `ProductoRepository`, `ReseniaRepository`) para comunicación entre capas.  

### Animaciones
- Uso de `AnimatedVisibility`, `slideInHorizontally`, `fadeIn` y `spring()` para transiciones suaves y efectos visuales en formularios y vistas.  

### Recursos nativos (en progreso)
- Preparado para integrar acceso a cámara o galería para imágenes de perfil o productos.  
- Implementación futura de **vibración al confirmar compras**.

### Herramientas colaborativas
- Repositorio alojado en **GitHub** 
- Planificación y seguimiento de tareas en **Trello**   


## Pasos para ejecutar el proyecto

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/tuusuario/LevelUpMobile.git
2. Descargar el zip desde el Github
