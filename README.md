# Microblog API
#### Laboratorio AREP - Microservicios

#### 👨🏻‍💻👩🏼‍💻👨🏻‍💻 AUTORES: 
- [Saray Alieth Mendivelso Gonzalez](https://github.com/saraygonm) 
- [Juan Pablo Daza Pereira](https://github.com/JuanPabl07DP)
- [Nicolas Bernal Fuquene](https://github.com/.........)

----------
Este proyecto es una API RESTful para un Microblog donde los usuarios pueden crear, leer y gestionar publicaciones dentro de diferentes streams. Está construido con Spring Boot y tiene integración con JWT para la autenticación y autorización.

---------

## Descripción

La API permite realizar las siguientes acciones:

- Crear y gestionar usuarios.
- Iniciar sesión con autenticación JWT.
- Crear, actualizar y eliminar publicaciones (posts).
- Gestionar streams donde los posts son organizados.
- Seguridad implementada con JWT para proteger las rutas.

------------
## 📍 Comenzando
Estas instrucciones te permitirán obtener una copia del proyecto en funcionamiento en tu máquina local para propósitos de desarrollo y pruebas.

--------------
### Funcionalidades

- **Autenticación JWT**: Los usuarios pueden iniciar sesión con un nombre de usuario y contraseña, y la API genera un token JWT para la autenticación en solicitudes subsecuentes.
- **Control de Acceso**: Rutas protegidas que requieren que el usuario esté autenticado mediante el token JWT.
- **Operaciones CRUD**: Funcionalidades completas para crear, leer, actualizar y eliminar usuarios, publicaciones y streams.

-------------

### 🔧 Prerrequisitos

Debes instalar los siguientes componentes:

- [Java JDK 17 superior](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
- [Maven](https://maven.apache.org/download.cgi)
- [Git](https://git-scm.com/)
- Un navegador Web
- Utiliza el entorno de desarrollo integrado (IDE) de tu preferencia, como por ejemplo:
    - IntelliJ IDEA
    - Eclipse
    - Apache NetBeans

### 🌐 Estructura del Proyecto
La arquitectura del sistema está organizada de la siguiente manera:

- Controladores: Implementan los puntos de acceso de la API para gestionar los usuarios, publicaciones y streams.
- Servicios: Contienen la lógica de negocio para interactuar con la base de datos.
- Repositorios: Interactúan con la base de datos utilizando JPA para las operaciones CRUD.
- Modelo: Define las entidades que son almacenadas en la base de datos.
- Excepciones: Gestionan errores globales y específicos del sistema, como recursos no encontrados o ya existentes.

### ⚙️ Instalación

1. **Clona el repositorio:**
   ```sh
   git clone https://github.com/JuanPabl07DP/AREP_Taller7.git
   ```

ESTO NO SE SI ESTA BBIEN DEDUCELO SEGUN LOS ARCHIVOS QUE TE ENVIO DECIR EN CUAL RAMA UBICARSE Y DEMAS
2. **Entra en la carpeta del proyecto:**
   ```sh
   cd AREP_Taller7
   ```

3. **Compila el proyecto con Maven:**
   ```sh
   mvn clean package
   ```

4. **Inicia el servidor:**
   ```sh
   mvn spring-boot:run
   ```

VERIFICA EL PUERTO SI ES ESE EL QUE SE USO
5. **Accede a la aplicación en el navegador:**
   ```
   http://localhost:8080
   ```

* Una vez iniciado el servidor, podrás visualizar la página web en tu navegador.


<p align="center">
  <img src="/src/main/resources/static/img/1.png" alt="Imagen de la página" width="700px">
</p>


---
## ✅ Ejecutar las pruebas

1. **Pruebas unitarias**: Para ejecutar las pruebas unitarias, asegúrate de tener Maven instalado y ejecuta el siguiente comando en la raíz del proyecto:

   ```bash
   mvn test

<p align="center">
  <img src="/src/main/resources/static/img/test.png" alt="Imagen de la página" width="700px">
</p>


Esto ejecutará todas las pruebas definidas en el directorio src/test/java.

### 1. **AuthControllerTest**
- **Autenticación**: Verifica el inicio de sesión y generación de token JWT.
- **Registro**: Verifica errores con datos duplicados y éxito en registro válido.

### 2. **PostControllerTest**
- **Publicaciones**: Verifica creación, obtención y eliminación de publicaciones.

### 3. **MicroblogApiIntegrationTest**
- **Streams y Publicaciones**: Verifica creación y obtención de streams y publicaciones.
- **Usuario**: Verifica actualización y acceso no autorizado.

### 4. **JwtTokenProviderTest**
- **Tokens JWT**: Verifica la generación, validación y extracción de datos del token.

### 5. **UserServiceTest**
- **Usuarios**: Verifica obtención, creación, actualización y eliminación de usuarios.



2. **Pruebas de integración**: El proyecto incluye pruebas de integración para comprobar que los servicios, controladores y rutas funcionan correctamente en conjunto. Para ejecutar las pruebas de integración:

```bash

mvn verify
```
<p align="center">
  <img src="/src/main/resources/static/img/test2.png" alt="Imagen de la página" width="700px">
</p>


1. **testCreateAndGetStream**
    - **Objetivo**: Verifica que la creación de un stream y su posterior obtención funcionen correctamente.

2. **testCreateAndGetPost**
    - **Objetivo**: Valida la creación de una publicación en un stream específico por un usuario.

3. **testUpdateUser**
    - **Objetivo**: Verifica la actualización de la información de un usuario.

4. **testUnauthorizedAccess**
    - **Objetivo**: Asegura que se manejen correctamente los intentos de acceso sin autenticación.

---


## 🏗️ Arquitectura

### 📌 Diagrama de Clases
### 📌 Diagrama de Secuencia
### 📌 Diagrama de Despliegue
### 📌 Diagrama de Componentes

## 🌐 Frontend

El sistema cuenta con una interfaz web desarrollada en HTML, CSS y JavaScript.

## 🌐 Funcionamiento

- Página principal sin publicaciones.

<p align="center">
  <img src="/src/main/resources/static/img/1.png" alt="Imagen de la página" width="700px">
</p>

- **Formulario de registro**
  
Este formulario se corresponde con el endpoint de POST `/api/auth/signup` que registra a un nuevo usuario, verificando la disponibilidad del nombre de usuario y el correo electrónico antes de crear el nuevo usuario en la base de datos.

<p align="center">
  <img src="/src/main/resources/static/img/createacount.png" alt="Imagen de la página" width="700px">
</p>

- **Formulario de inicio de sesión**
Los usuarios ingresan su nombre de usuario y contraseña. Esta acción corresponde al endpoint POST `/api/auth/signin`, que valida las credenciales y, si son correctas, devuelve un token JWT para la autenticación de futuras solicitudes


<p align="center">
  <img src="/src/main/resources/static/img/2.png" alt="Imagen de la página" width="700px">
</p>


- **Creación de un nuevo stream**
Con nombre y descripción. Este flujo está vinculado al endpoint POST `/api/streams`

<!-- Creación de tabla para alinear las imágenes lado a lado.-->
| <img src="/src/main/resources/static/img/stream.png" alt="Descarga local" width="500px"> | <img src="/src/main/resources/static/img/stream2.png" alt="Importar carpetas" width="500px"> |
|------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------|
| **Creacion Stream**                                                                      | **stream creado**                                                                            |


- **Visualización del stream creado**
Después de crear un stream, se muestra en la interfaz junto con su descripción y fecha de creación

<p align="center">
  <img src="/src/main/resources/static/img/streamjuan.png" alt="Imagen de la página" width="700px">
</p>


- **Formulario para crear un post**
Esto se conecta con el endpoint POST `/api/posts/user/{userId}/stream/{streamId}` del backend para crear una publicación en el stream correspondiente.
<!-- Creación de tabla para alinear las imágenes lado a lado.-->
| <img src="/src/main/resources/static/img/home.png" alt="Descarga local" width="500px"> | <img src="/src/main/resources/static/img/home2.png" alt="Importar carpetas" width="500px"> |
|----------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------|
| **Un post mismo user**                                                                 | **Varios Post dif user**                                                                  |


- **Interfaz con publicaciones en el stream**
  Muestra las publicaciones realizadas en un stream, ordenadas por su fecha de creación.

## 🔒 Autenticación y Seguridad
La seguridad del sistema está gestionada a través de Spring Security y JSON Web Tokens (JWT), lo que garantiza la autenticación y autorización de los usuarios en la API.

### Clases Clave

- **JwtAuthenticationEntryPoint**: Maneja las excepciones de acceso no autorizado. Devuelve un error 401 si el usuario no está autenticado correctamente.
- **JwtTokenProvider**: Genera, valida y extrae la información de los tokens JWT. Los tokens se generan al iniciar sesión y se validan en cada solicitud posterior.
- **JwtAuthenticationFilter**: Filtro de seguridad que intercepta las solicitudes y verifica la validez del token JWT en la cabecera de autorización.
- **UserDetailsServiceImpl**: Implementa `UserDetailsService` de Spring Security para cargar los detalles del usuario basados en su nombre de usuario.
- **SecurityConfig**: Configura las reglas de seguridad, gestión de CORS, manejo de excepciones de autenticación y los filtros de seguridad como `JwtAuthenticationFilter`.


### Flujo de Autenticación

1. **Inicio de sesión**:
    - El usuario envía su nombre de usuario y contraseña.
    - Se genera un JWT con `JwtTokenProvider` y se envía al cliente, quien debe incluirlo en la cabecera de autorización en futuras solicitudes (`Bearer <token>`).

2. **Acceso a recursos protegidos**:
    - El cliente incluye el token JWT en la cabecera de autorización.
    - `JwtAuthenticationFilter` intercepta la solicitud, valida el token y carga el contexto de seguridad correspondiente.

3. **Control de acceso**:
    - Solo los usuarios autenticados pueden acceder a los recursos.
    - Se asignan roles de autorización (por ejemplo, `ROLE_USER`) para controlar el acceso a los recursos.


### Configuración JWT

La configuración de JWT se encuentra en el archivo `application.properties`, con los siguientes parámetros:

- **`app.jwt.secret`**: Clave secreta para firmar los tokens.
- **`app.jwt.expirationMs`**: Tiempo de expiración del token en milisegundos.
- **`app.jwt.tokenPrefix`**: Prefijo para la cabecera de autorización (`Bearer` por defecto).
- **`app.jwt.headerString`**: Nombre de la cabecera HTTP para enviar el token (`Authorization` por defecto).

## Endpoints de Autenticación

- **`POST /api/auth/signin`**: Inicia sesión con nombre de usuario y contraseña. Si son válidos, se retorna un JWT.
- **`POST /api/auth/signup`**: Registra un nuevo usuario con nombre de usuario, correo electrónico y contraseña. Valida que el nombre de usuario y correo electrónico no estén registrados.



-------------------
## 🚀 Despliegue en AWS

-------------------

## 🛠️ Tecnologías Utilizadas

REVISAR SI HACE FALTA ALGO O SI ESTOS ESTAN BIEN
- **Java** - Lenguaje de programación principal
- **Spring Boot** - Framework backend
- **Maven** - Gestor de dependencias
- **Docker** - Contenedorización
- **AWS** - Despliegue en la nube

---
### 📺 Video de Demostración

[![Ver en YouTube](https:)](https:/)




---


