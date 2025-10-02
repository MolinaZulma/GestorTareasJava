# Gestor de Tareas API

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.6-brightgreen)
![Maven](https://img.shields.io/badge/Maven-4.0.0-red)
![Security](https://img.shields.io/badge/Security-JWT%20%26%20Spring-black)
![Database](https://img.shields.io/badge/Database-MySQL%20%7C%20H2-orange)

API RESTful para un sistema de gestión de tareas, construida con Spring Boot. Permite a los usuarios registrarse, iniciar sesión y gestionar sus tareas pendientes. Incluye roles de administrador para la gestión de usuarios.

## ✨ Características

- **Autenticación y Autorización:** Sistema seguro basado en JSON Web Tokens (JWT).
- **Gestión de Usuarios:**
  - Registro de nuevos usuarios.
  - Inicio de sesión.
  - Cambio de contraseña.
- **Gestión de Tareas (CRUD):**
  - Creación, lectura, actualización y eliminación de tareas.
  - Asignación de prioridades y estados.
- **Roles de Usuario:**
  - `USER`: Puede gestionar sus propias tareas.
  - `ADMIN`: Puede gestionar todas las tareas y usuarios del sistema.
- **Documentación de API:** Endpoints documentados y disponibles para pruebas con Swagger UI.

## 🛠️ Stack de Tecnologías

### Backend
- **Java 17:** Lenguaje de programación principal.
- **Spring Boot 3.2.6:** Framework para la creación de la aplicación.
- **Spring Web:** Para construir los endpoints RESTful.
- **Spring Security:** Para manejar la autenticación y autorización.
- **Spring Data JPA:** Para la persistencia de datos y la interacción con la base de datos.
- **JSON Web Tokens (JJWT):** Para la generación y validación de tokens de acceso.
- **MySQL & H2:** Soporte para base de datos relacional (MySQL para producción y H2 para desarrollo/pruebas).
- **Lombok:** Para reducir el código repetitivo en modelos y DTOs.
- **Maven:** Como gestor de dependencias y construcción del proyecto.

### Frontend
- **HTML5, CSS3, JavaScript:** Para la interfaz de usuario básica (registro, login y visualización de tareas).

### Documentación
- **SpringDoc OpenAPI (Swagger):** Para la documentación interactiva de la API.

## 🚀 Cómo Empezar

### Prerrequisitos
- **JDK 17** (Java Development Kit).
- **Maven** instalado y configurado en tu PATH.
- Una instancia de **MySQL** en ejecución (opcional, se puede usar la base de datos en memoria H2).

### Instalación y Configuración
1. Clona el repositorio:
   ```sh
   git clone <URL_DEL_REPOSITORIO>
   ```
2. Navega al directorio del proyecto:
   ```sh
   cd gestortareas
   ```
3. (Opcional) Configura la conexión a tu base de datos MySQL en el archivo `src/main/resources/application.properties`. Si no, el proyecto usará la base de datos H2 por defecto.

### Ejecución
Para iniciar la aplicación, ejecuta el siguiente comando en la raíz del proyecto:
```sh
./mvnw spring-boot:run
```
La API estará disponible en `http://localhost:8080`.

## 📖 Documentación de la API

Una vez que la aplicación está en ejecución, puedes acceder a la documentación interactiva de Swagger UI para ver, probar y entender todos los endpoints disponibles.

- **URL de Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

