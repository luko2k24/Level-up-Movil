# 🎮 LEVEL-UP GAMER — Aplicación Móvil Android

## 📌 Nombre de la aplicación
**Level-Up Gamer**

---

## 👥 Integrantes del equipo
- **Lukas Martín Meza Cofré**  
- **Christian Sandoval**

**Asignatura:** DSY1105 – Desarrollo de Aplicaciones Móviles  
**Sección:** 002D  
**Profesor:** Bryan Vicente Soto Astudillo  

---

## 📝 Descripción del proyecto
**Level-Up Gamer** es una aplicación móvil desarrollada para **Android**, utilizando **Kotlin** y **Jetpack Compose** en **Android Studio**, como parte de la **Evaluación Parcial 2** de la asignatura DSY1105.

La aplicación permite a los usuarios **explorar productos gamer, registrarse, iniciar sesión y dejar reseñas**, aplicando principios de **arquitectura MVVM, validaciones desacopladas, persistencia local y diseño centrado en el usuario**.

---

## ⚙️ Funcionalidades implementadas

### 📱 Interfaz y navegación
- Interfaz visual con estética gamer (colores oscuros y acentos neón).
- Navegación entre pantallas usando **NavController**.
- Uso de componentes de Jetpack Compose como:
  - `Scaffold`
  - `Card`
  - `LazyRow`
  - `OutlinedTextField`

### 🧾 Formularios y validaciones
- Registro e inicio de sesión de usuarios.
- Validaciones por campo con retroalimentación visual.
- Lógica de validación desacoplada en archivo independiente (`Validacion.kt`).

### 🧠 Arquitectura y lógica
- Implementación de arquitectura **MVVM**.
- Uso de `ViewModelAutenticacion` y `ReviewViewModel`.
- Manejo de estado con **StateFlow** y **Kotlin Coroutines**.

### 💾 Persistencia local
- Base de datos local implementada con **Room**.
- Entidades para usuarios, productos y reseñas.
- Repositorios:
  - `UsuarioRepository`
  - `ProductoRepository`
  - `ReseniaRepository`

### 🎞️ Animaciones
- Animaciones con:
  - `AnimatedVisibility`
  - `slideInHorizontally`
  - `fadeIn`
  - `spring()`
- Transiciones suaves entre vistas y formularios.

### 📲 Recursos nativos (en progreso)
- Estructura preparada para uso de cámara o galería.
- Planificación de vibración al confirmar compras.

---

## 🌐 Endpoints utilizados

### 🔹 Endpoints propios
- Autenticación de usuarios (persistencia local con Room).
- Gestión de productos y reseñas.

### 🔹 Endpoints externos
- No se utilizan endpoints externos actualmente.

---

## ▶️ Instrucciones para ejecutar el proyecto

### Opción 1: Clonar repositorio
```bash
git clone https://github.com/tuusuario/LevelUpMobile.git
