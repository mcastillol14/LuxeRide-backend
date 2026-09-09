# Documentación Completa de API Endpoints - Sistema LuxeRide

## Configuración

La aplicación ya no lleva credenciales embebidas: se leen desde variables de entorno. Antes de arrancar, define:

| Variable | Descripción | Por defecto |
|---|---|---|
| `DB_URL` | URL JDBC de la base de datos MySQL | `jdbc:mysql://localhost:3306/mcastillol14_taxi` |
| `DB_USERNAME` | Usuario de la base de datos | `alumno` |
| `DB_PASSWORD` | Contraseña de la base de datos | *(obligatoria, sin valor por defecto)* |
| `MAIL_HOST` | Host SMTP | `smtp.gmail.com` |
| `MAIL_PORT` | Puerto SMTP | `587` |
| `MAIL_USERNAME` | Usuario/correo SMTP | *(obligatoria)* |
| `MAIL_PASSWORD` | Contraseña de aplicación SMTP | *(obligatoria)* |
| `JWT_SECRET` | Clave de firma de los JWT (Base64, ≥256 bits) | *(obligatoria)* |

Ejemplo de arranque local:
```bash
export DB_PASSWORD=tu_password
export MAIL_USERNAME=tu_correo@gmail.com
export MAIL_PASSWORD=tu_password_de_aplicacion
export JWT_SECRET=tu_clave_base64
./mvnw spring-boot:run
```

Puedes generar una clave JWT válida con:
```bash
openssl rand -base64 64
```

> ⚠️ Este repositorio tuvo credenciales reales committeadas (contraseña de la base de datos, contraseña de aplicación de Gmail y la clave de firma JWT), tanto en `application.properties` como en `target/` (artefactos de compilación que no debían estar en git). Se han sacado del código, pero **quitarlas del código no invalida credenciales que ya quedaron expuestas en el historial de git público** — rota manualmente: la contraseña de la base de datos MySQL, la contraseña de aplicación de Gmail, y usa una `JWT_SECRET` nueva (esto invalidará las sesiones/tokens ya emitidos, lo cual es deseable).

## Tabla de Contenidos
1. [Autenticación y Autorización](#autenticación-y-autorización)
2. [Endpoints Públicos](#endpoints-públicos)
3. [Endpoints de Usuario (Autenticado)](#endpoints-de-usuario-autenticado)
4. [Endpoints de Taxista](#endpoints-de-taxista)
5. [Endpoints de Administrador](#endpoints-de-administrador)
6. [Códigos de Respuesta HTTP](#códigos-de-respuesta-http)
7. [Modelos de Datos](#modelos-de-datos)
8. [Ejemplos de Uso](#ejemplos-de-uso)

---

## Autenticación y Autorización

### Sistema de Roles
- **ROL_CLIENTE**: Usuario básico del sistema
- **ROL_TAXISTA**: Conductor de taxi (hereda permisos de cliente)
- **ROL_ADMIN**: Administrador del sistema (acceso completo)

### Headers de Autenticación
Para endpoints que requieren autenticación, incluir:
```
Authorization: Bearer {jwt_token}
```

---

## Endpoints Públicos

### 1. Registro de Usuario
```
POST /api/usuarios/registrar
```

**Descripción**: Registra un nuevo usuario en el sistema

**Content-Type**: `application/json`

**Parámetros del Body**:
```json
{
  "nombre": "string",          // OBLIGATORIO - Nombre del usuario
  "apellidos": "string",       // OBLIGATORIO - Apellidos del usuario
  "dni": "string",            // OBLIGATORIO - DNI válido español (8 dígitos + letra)
  "email": "string",          // OBLIGATORIO - Email único válido
  "password": "string"        // OBLIGATORIO - Contraseña
}
```

**Respuesta Exitosa** (200):
```json
"Usuario registrado exitosamente"
```

**Errores Posibles**:
- 400: Datos inválidos o duplicados
- 500: Error interno del servidor

**Ejemplo de Request**:
```json
{
  "nombre": "Juan",
  "apellidos": "Pérez García",
  "dni": "12345678A",
  "email": "juan.perez@email.com",
  "password": "mipassword123"
}
```

---

### 2. Inicio de Sesión
```
POST /api/usuarios/iniciar
```

**Descripción**: Autentica un usuario y devuelve un token JWT

**Content-Type**: `application/json`

**Parámetros del Body**:
```json
{
  "email": "string",      // OBLIGATORIO - Email del usuario
  "password": "string"    // OBLIGATORIO - Contraseña
}
```

**Respuesta Exitosa** (200):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "Login exitoso"
}
```

**Errores Posibles**:
- 401: Credenciales incorrectas
- 403: Cuenta bloqueada
- 500: Error interno del servidor

---

## Endpoints de Usuario (Autenticado)

### 3. Obtener Información Personal
```
GET /api/usuarios/info
```

**Descripción**: Obtiene información del usuario autenticado

**Autorización**: `ROL_CLIENTE`, `ROL_TAXISTA`, `ROL_ADMIN`

**Headers**: `Authorization: Bearer {token}`

**Respuesta Exitosa** (200):
```json
{
  "id": 1,
  "nombre": "Juan",
  "apellidos": "Pérez García",
  "dni": "12345678A",
  "email": "juan.perez@email.com",
  "rol": {
    "id": 1,
    "nombre": "ROL_CLIENTE"
  },
  "accountNonLocked": true
}
```

---

### 4. Listar Coches en Servicio
```
GET /api/usuarios/enServicio
```

**Descripción**: Obtiene lista de coches actualmente en servicio

**Autorización**: `ROL_CLIENTE`, `ROL_TAXISTA`

**Headers**: `Authorization: Bearer {token}`

**Respuesta Exitosa** (200):
```json
[
  {
    "id": 1,
    "modelo": "Prius",
    "marca": "Toyota",
    "matricula": "1234ABC",
    "licencia": {
      "id": 1,
      "numero": "LIC001"
    },
    "usuarios": [
      {
        "id": 2,
        "nombre": "Carlos",
        "apellidos": "López",
        "dni": "87654321B",
        "email": "carlos.lopez@email.com",
        "rol": {
          "id": 2,
          "nombre": "ROL_TAXISTA"
        },
        "accountNonLocked": true
      }
    ],
    "disponible": false,
    "taxistaEnServicio": {
      "id": 2,
      "nombre": "Carlos",
      "apellidos": "López",
      "dni": "87654321B",
      "email": "carlos.lopez@email.com",
      "rol": {
        "id": 2,
        "nombre": "ROL_TAXISTA"
      },
      "accountNonLocked": true
    }
  }
]
```

---

### 5. Listar Todos los Servicios
```
GET /api/usuarios/allServicios
```

**Descripción**: Obtiene lista de todos los servicios disponibles

**Autorización**: `ROL_CLIENTE`, `ROL_TAXISTA`, `ROL_ADMIN`

**Headers**: `Authorization: Bearer {token}`

**Query Parameters**:
- `page` (opcional): Número de página (default: 0)
- `size` (opcional): Tamaño de página (default: 20)
- `sort` (opcional): Campo de ordenación
- `tipo` (opcional): Filtro por tipo de servicio

**Respuesta Exitosa** (200):
```json
{
  "content": [
    {
      "id": 1,
      "tipo": "ESTANDAR",
      "descripcion": "Servicio de taxi estándar",
      "precioPorKm": 1.5
    },
    {
      "id": 2,
      "tipo": "PREMIUM",
      "descripcion": "Servicio premium con vehículo de lujo",
      "precioPorKm": 2.5
    }
  ],
  "pageable": {
    "sort": {
      "sorted": false,
      "unsorted": true
    },
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 2,
  "totalPages": 1,
  "first": true,
  "last": true,
  "numberOfElements": 2
}
```

---

### 6. Registrar Viaje
```
POST /api/usuarios/hacerViaje
```

**Descripción**: Registra un nuevo viaje en el sistema

**Autorización**: `ROL_CLIENTE`

**Headers**: `Authorization: Bearer {token}`

**Content-Type**: `application/json`

**Parámetros del Body**:
```json
{
  "horaInicio": "2024-06-09T10:30:00",    // OBLIGATORIO - Fecha/hora inicio (ISO 8601)
  "horaLlegada": "2024-06-09T11:00:00",   // OBLIGATORIO - Fecha/hora llegada (ISO 8601)
  "origen": "string",                      // OBLIGATORIO - Dirección de origen
  "destino": "string",                     // OBLIGATORIO - Dirección de destino
  "distanciaKm": 15.5,                    // OBLIGATORIO - Distancia en kilómetros
  "precioTotal": 23.25,                   // OBLIGATORIO - Precio total del viaje
  "cliente": {                            // OBLIGATORIO - Información del cliente
    "id": 1
  },
  "taxista": {                            // OBLIGATORIO - Información del taxista
    "id": 2
  },
  "servicio": {                           // OBLIGATORIO - Tipo de servicio
    "id": 1
  },
  "coche": {                              // OBLIGATORIO - Coche utilizado
    "id": 1
  },
  "foto": "base64encodedstring"           // OPCIONAL - Foto del viaje en base64
}
```

**Respuesta Exitosa** (200):
```json
"Viaje registrado exitosamente."
```

**Errores Posibles**:
- 400: Datos inválidos (horas, IDs inexistentes, validaciones fallidas)
- 401: Token inválido
- 403: Sin permisos (no es cliente)
- 500: Error interno del servidor

---

### 7. Generar PDF del Último Viaje
```
GET /api/usuarios/generarPDF/{idCliente}
```

**Descripción**: Genera un PDF con información del último viaje del cliente

**Autorización**: `ROL_CLIENTE`, `ROL_ADMIN`

**Headers**: `Authorization: Bearer {token}`

**Path Parameters**:
- `idCliente` (obligatorio): ID del cliente

**Respuesta Exitosa** (200):
- **Content-Type**: `application/pdf`
- **Body**: Archivo PDF binario

**Headers de Respuesta**:
```
Content-Disposition: attachment; filename=ultimo_viaje_cliente_{idCliente}.pdf
Content-Type: application/pdf
```

**Errores Posibles**:
- 404: Cliente no encontrado o sin viajes
- 401: Token inválido
- 403: Sin permisos
- 500: Error generando PDF

---

## Endpoints de Taxista

### 8. Poner Coche en Servicio
```
PUT /api/taxista/ponerEnServicio/{cocheId}/{taxistaId}
```

**Descripción**: Pone un coche en servicio asignado a un taxista

**Autorización**: `ROL_TAXISTA`, `ROL_ADMIN`

**Headers**: `Authorization: Bearer {token}`

**Path Parameters**:
- `cocheId` (obligatorio): ID del coche a poner en servicio
- `taxistaId` (obligatorio): ID del taxista que operará el coche

**Respuesta Exitosa** (200):
```json
{
  "id": 2,
  "nombre": "Carlos",
  "apellidos": "López",
  "dni": "87654321B",
  "email": "carlos.lopez@email.com",
  "rol": {
    "id": 2,
    "nombre": "ROL_TAXISTA"
  },
  "accountNonLocked": true
}
```

**Errores Posibles**:
- 400: Coche no disponible, taxista no asignado al coche, coche ya en servicio
- 401: Token inválido
- 403: Sin permisos
- 404: Coche o taxista no encontrado
- 500: Error interno del servidor

---

### 9. Liberar Coche del Servicio
```
PUT /api/taxista/liberarCoche/{id}
```

**Descripción**: Libera un coche del servicio, dejándolo disponible

**Autorización**: `ROL_TAXISTA`, `ROL_ADMIN`

**Headers**: `Authorization: Bearer {token}`

**Path Parameters**:
- `id` (obligatorio): ID del coche a liberar

**Respuesta Exitosa** (200):
```json
"Coche liberado y ahora está disponible."
```

**Errores Posibles**:
- 400: Coche no está en servicio
- 401: Token inválido
- 403: Sin permisos
- 404: Coche no encontrado
- 500: Error interno del servidor

---

### 10. Obtener Coches Disponibles del Taxista
```
GET /api/taxista/cochesTaxistas/{taxistaId}
```

**Descripción**: Obtiene lista de coches disponibles asignados a un taxista específico

**Autorización**: `ROL_TAXISTA`, `ROL_ADMIN`

**Headers**: `Authorization: Bearer {token}`

**Path Parameters**:
- `taxistaId` (obligatorio): ID del taxista

**Respuesta Exitosa** (200):
```json
[
  {
    "id": 1,
    "modelo": "Prius",
    "marca": "Toyota",
    "matricula": "1234ABC",
    "licencia": {
      "id": 1,
      "numero": "LIC001"
    },
    "usuarios": [
      {
        "id": 2,
        "nombre": "Carlos",
        "apellidos": "López",
        "dni": "87654321B",
        "email": "carlos.lopez@email.com",
        "rol": {
          "id": 2,
          "nombre": "ROL_TAXISTA"
        },
        "accountNonLocked": true
      }
    ],
    "disponible": true,
    "taxistaEnServicio": null
  }
]
```

**Errores Posibles**:
- 400: Taxista inválido
- 401: Token inválido
- 403: Sin permisos
- 404: Taxista no encontrado
- 500: Error interno del servidor

---

## Endpoints de Administrador

### 11. Listar Todos los Usuarios
```
GET /api/admin/allUsuarios
```

**Descripción**: Obtiene lista paginada de todos los usuarios del sistema

**Autorización**: `ROL_ADMIN`

**Headers**: `Authorization: Bearer {token}`

**Query Parameters**:
- `page` (opcional): Número de página (default: 0)
- `size` (opcional): Tamaño de página (default: 20)
- `sort` (opcional): Campo de ordenación (ej: `nombre,asc`)
- `dni` (opcional): Filtro por DNI

**Ejemplo URL**:
```
GET /api/admin/allUsuarios?page=0&size=10&sort=nombre,asc&dni=12345678A
```

**Respuesta Exitosa** (200):
```json
{
  "content": [
    {
      "id": 1,
      "nombre": "Juan",
      "apellidos": "Pérez García",
      "dni": "12345678A",
      "email": "juan.perez@email.com",
      "rol": {
        "id": 1,
        "nombre": "ROL_CLIENTE"
      },
      "accountNonLocked": true
    }
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false
    },
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true,
  "numberOfElements": 1
}
```

---

### 12. Editar Usuario
```
PUT /api/admin/editarUsuario/{id}
```

**Descripción**: Edita información de un usuario existente

**Autorización**: `ROL_ADMIN`

**Headers**: `Authorization: Bearer {token}`

**Path Parameters**:
- `id` (obligatorio): ID del usuario a editar

**Query Parameters** (todos opcionales):
- `nombre`: Nuevo nombre
- `apellidos`: Nuevos apellidos
- `dni`: Nuevo DNI
- `email`: Nuevo email

**Ejemplo URL**:
```
PUT /api/admin/editarUsuario/1?nombre=Juan Carlos&email=nuevo.email@test.com
```

**Respuesta Exitosa** (200):
```json
"Usuario editado correctamente."
```

**Errores Posibles**:
- 400: Email o DNI ya en uso por otro usuario
- 401: Token inválido
- 403: Sin permisos
- 404: Usuario no encontrado
- 500: Error interno del servidor

---

### 13. Bloquear Cuenta de Usuario
```
PUT /api/admin/bloquearCuenta/{id}
```

**Descripción**: Bloquea la cuenta de un usuario impidiendo su acceso

**Autorización**: `ROL_ADMIN`

**Headers**: `Authorization: Bearer {token}`

**Path Parameters**:
- `id` (obligatorio): ID del usuario a bloquear

**Respuesta Exitosa** (200):
```json
"Cuenta bloqueada correctamente."
```

**Errores Posibles**:
- 401: Token inválido
- 403: Sin permisos
- 404: Usuario no encontrado
- 500: Error interno del servidor

---

### 14. Desbloquear Cuenta de Usuario
```
PUT /api/admin/desbloquearCuenta/{id}
```

**Descripción**: Desbloquea la cuenta de un usuario permitiendo su acceso

**Autorización**: `ROL_ADMIN`

**Headers**: `Authorization: Bearer {token}`

**Path Parameters**:
- `id` (obligatorio): ID del usuario a desbloquear

**Respuesta Exitosa** (200):
```json
"Cuenta desbloqueada correctamente."
```

---

### 15. Asignar Rol de Taxista
```
PUT /api/admin/addTaxista/{id}
```

**Descripción**: Asigna el rol de taxista a un usuario cliente

**Autorización**: `ROL_ADMIN`

**Headers**: `Authorization: Bearer {token}`

**Path Parameters**:
- `id` (obligatorio): ID del usuario

**Respuesta Exitosa** (200):
```json
"El usuario ahora es un taxista."
```

**Errores Posibles**:
- 400: Usuario ya es taxista o admin
- 401: Token inválido
- 403: Sin permisos
- 404: Usuario no encontrado
- 500: Error interno del servidor

---

### 16. Eliminar Rol de Taxista
```
PUT /api/admin/deleteTaxista/{id}
```

**Descripción**: Elimina el rol de taxista, convirtiendo al usuario en cliente

**Autorización**: `ROL_ADMIN`

**Headers**: `Authorization: Bearer {token}`

**Path Parameters**:
- `id` (obligatorio): ID del usuario

**Respuesta Exitosa** (200):
```json
"El usuario ya no es un taxista."
```

---

### 17. Listar Todos los Coches
```
GET /api/admin/allCoches
```

**Descripción**: Obtiene lista paginada de todos los coches del sistema

**Autorización**: `ROL_ADMIN`

**Headers**: `Authorization: Bearer {token}`

**Query Parameters**:
- `page` (opcional): Número de página (default: 0)
- `size` (opcional): Tamaño de página (default: 20)
- `sort` (opcional): Campo de ordenación
- `matricula` (opcional): Filtro por matrícula

**Respuesta Exitosa** (200):
```json
{
  "content": [
    {
      "id": 1,
      "modelo": "Prius",
      "marca": "Toyota",
      "matricula": "1234ABC",
      "licencia": {
        "id": 1,
        "numero": "LIC001"
      },
      "usuarios": [
        {
          "id": 2,
          "nombre": "Carlos",
          "apellidos": "López",
          "dni": "87654321B",
          "email": "carlos.lopez@email.com",
          "rol": {
            "id": 2,
            "nombre": "ROL_TAXISTA"
          },
          "accountNonLocked": true
        }
      ],
      "disponible": true,
      "taxistaEnServicio": null
    }
  ],
  "pageable": {
    "sort": {
      "sorted": false,
      "unsorted": true
    },
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true,
  "numberOfElements": 1
}
```

---

### 18. Agregar Nuevo Coche
```
POST /api/admin/addCoche
```

**Descripción**: Registra un nuevo coche en el sistema

**Autorización**: `ROL_ADMIN`

**Headers**: `Authorization: Bearer {token}`

**Content-Type**: `application/json`

**Parámetros del Body**:
```json
{
  "matricula": "string",      // OBLIGATORIO - Matrícula única
  "marca": "string",          // OBLIGATORIO - Marca del vehículo
  "modelo": "string",         // OBLIGATORIO - Modelo del vehículo
  "disponible": true,         // OPCIONAL - Estado de disponibilidad (default: true)
  "enServicio": false         // OPCIONAL - Estado de servicio (default: false)
}
```

**Respuesta Exitosa** (200):
```json
"Coche registrado exitosamente"
```

**Errores Posibles**:
- 400: Matrícula ya existe, datos inválidos
- 401: Token inválido
- 403: Sin permisos
- 500: Error interno del servidor

---

### 19. Eliminar Coche
```
DELETE /api/admin/eliminarCoche/{id}
```

**Descripción**: Elimina un coche del sistema

**Autorización**: `ROL_ADMIN`

**Headers**: `Authorization: Bearer {token}`

**Path Parameters**:
- `id` (obligatorio): ID del coche a eliminar

**Respuesta Exitosa** (200):
```json
"Coche eliminado exitosamente."
```

**Errores Posibles**:
- 400: Coche en uso (viajes asociados)
- 401: Token inválido
- 403: Sin permisos
- 404: Coche no encontrado
- 500: Error interno del servidor

---

### 20. Asignar Usuario a Coche
```
PUT /api/admin/addUsuarioToCoche/{cocheId}/{usuarioId}
```

**Descripción**: Asigna un taxista a un coche específico

**Autorización**: `ROL_ADMIN`

**Headers**: `Authorization: Bearer {token}`

**Path Parameters**:
- `cocheId` (obligatorio): ID del coche
- `usuarioId` (obligatorio): ID del usuario (debe ser taxista)

**Respuesta Exitosa** (200):
```json
"Usuario asignado al coche exitosamente"
```

**Errores Posibles**:
- 400: Usuario no es taxista, ya está asignado
- 401: Token inválido
- 403: Sin permisos
- 404: Coche o usuario no encontrado
- 500: Error interno del servidor

---

### 21. Desasignar Usuario de Coche
```
DELETE /api/admin/deleteUsuarioToCoche/{cocheId}/{usuarioId}
```

**Descripción**: Elimina la asignación de un taxista de un coche

**Autorización**: `ROL_ADMIN`

**Headers**: `Authorization: Bearer {token}`

**Path Parameters**:
- `cocheId` (obligatorio): ID del coche
- `usuarioId` (obligatorio): ID del usuario

**Respuesta Exitosa** (200):
```json
"Usuario desasignado del coche exitosamente"
```

---

### 22. Asignar Licencia a Coche
```
PUT /api/admin/addLicenciaToCoche/{cocheId}/{licenciaId}
```

**Descripción**: Asigna una licencia a un coche

**Autorización**: `ROL_ADMIN`

**Headers**: `Authorization: Bearer {token}`

**Path Parameters**:
- `cocheId` (obligatorio): ID del coche
- `licenciaId` (obligatorio): ID de la licencia

**Respuesta Exitosa** (200):
```json
"Licencia asignada al coche exitosamente"
```

**Errores Posibles**:
- 400: Licencia ya asignada a otro coche
- 401: Token inválido
- 403: Sin permisos
- 404: Coche o licencia no encontrada
- 500: Error interno del servidor

---

### 23. Listar Todos los Servicios (Admin)
```
GET /api/admin/allServicios
```

**Descripción**: Obtiene lista paginada de todos los servicios (versión administrativa)

**Autorización**: `ROL_ADMIN`

**Headers**: `Authorization: Bearer {token}`

**Query Parameters**:
- `page` (opcional): Número de página (default: 0)
- `size` (opcional): Tamaño de página (default: 20)
- `sort` (opcional): Campo de ordenación
- `tipo` (opcional): Filtro por tipo de servicio

**Respuesta**: Igual que endpoint `/api/usuarios/allServicios` pero con acceso administrativo

---

### 24. Agregar Nuevo Servicio
```
POST /api/admin/addServicio
```

**Descripción**: Crea un nuevo tipo de servicio

**Autorización**: `ROL_ADMIN`

**Headers**: `Authorization: Bearer {token}`

**Content-Type**: `application/json`

**Parámetros del Body**:
```json
{
  "tipo": "string",           // OBLIGATORIO - Tipo de servicio único
  "descripcion": "string",    // OBLIGATORIO - Descripción del servicio
  "precioPorKm": 2.5         // OBLIGATORIO - Precio por kilómetro
}
```

**Ejemplo**:
```json
{
  "tipo": "EJECUTIVO",
  "descripcion": "Servicio ejecutivo con vehículo premium y conductor profesional",
  "precioPorKm": 3.0
}
```

**Respuesta Exitosa** (200):
```json
"Servicio creado exitosamente"
```

**Errores Posibles**:
- 400: Tipo de servicio ya existe, datos inválidos
- 401: Token inválido
- 403: Sin permisos
- 500: Error interno del servidor

---

### 25. Eliminar Servicio
```
DELETE /api/admin/deleteServicio/{id}
```

**Descripción**: Elimina un tipo de servicio del sistema

**Autorización**: `ROL_ADMIN`

**Headers**: `Authorization: Bearer {token}`

**Path Parameters**:
- `id` (obligatorio): ID del servicio a eliminar

**Respuesta Exitosa** (200):
```json
"Servicio eliminado exitosamente"
```

**Errores Posibles**:
- 400: Servicio en uso (viajes asociados)
- 401: Token inválido
- 403: Sin permisos
- 404: Servicio no encontrado
- 500: Error interno del servidor

---

### 26. Listar Todas las Licencias
```
GET /api/admin/allLicencias
```

**Descripción**: Obtiene lista paginada de todas las licencias

**Autorización**: `ROL_ADMIN`

**Headers**: `Authorization: Bearer {token}`

**Query Parameters**:
- `page` (opcional): Número de página (default: 0)
- `size` (opcional): Tamaño de página (default: 20)
- `sort` (opcional): Campo de ordenación

**Respuesta Exitosa** (200):
```json
{
  "content": [
    {
      "id": 1,
      "numero": "LIC001"
    },
    {
      "id": 2,
      "numero": "LIC002"
    }
  ],
  "pageable": {
    "sort": {
      "sorted": false,
      "unsorted": true
    },
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 2,
  "totalPages": 1,
  "first": true,
  "last": true,
  "numberOfElements": 2
}
```

---

### 27. Agregar Nueva Licencia
```
POST /api/admin/addLicencia
```

**Descripción**: Crea una nueva licencia

**Autorización**: `ROL_ADMIN`

**Headers**: `Authorization: Bearer {token}`

**Content-Type**: `application/json`

**Parámetros del Body**:
```json
{
  "numero": "string"      // OBLIGATORIO - Número de licencia único
}
```

**Respuesta Exitosa** (200):
```json
"Licencia registrada exitosamente"
```

**Errores Posibles**:
- 400: Número de licencia ya existe
- 401: Token inválido
- 403: Sin permisos
- 500: Error interno del servidor

---

### 28. Eliminar Licencia
```
DELETE /api/admin/deleteLicencia/{id}
```

**Descripción**: Elimina una licencia del sistema

**Autorización**: `ROL_ADMIN`

**Headers**: `Authorization: Bearer {token}`

**Path Parameters**:
- `id` (obligatorio): ID de la licencia a eliminar

**Respuesta Exitosa** (200):
```json
"Licencia eliminada exitosamente"
```

**Errores Posibles**:
- 400: Licencia asignada a un coche
- 401: Token inválido
- 403: Sin permisos
- 404: Licencia no encontrada
- 500: Error interno del servidor

---

### 29. Editar Licencia
```
PUT /api/admin/editarLicencia/{id}
```

**Descripción**: Edita el número de una licencia existente

**Autorización**: `ROL_ADMIN`

**Headers**: `Authorization: Bearer {token}`

**Path Parameters**:
- `id` (obligatorio): ID de la licencia a editar

**Query Parameters**:
- `numero` (obligatorio): Nuevo número de licencia

**Ejemplo URL**:
```
PUT /api/admin/editarLicencia/1?numero=LIC001-UPDATED
```

**Respuesta Exitosa** (200):
```json
"Licencia editada correctamente"
```

**Errores Posibles**:
- 400: Nuevo número ya existe
- 401: Token inválido
- 403: Sin permisos
- 404: Licencia no encontrada
- 500: Error interno del servidor

---

## Códigos de Respuesta HTTP

| Código | Descripción | Casos de Uso |
|--------|-------------|--------------|
| **200 OK** | Operación exitosa | Consultas exitosas, actualizaciones completadas |
| **400 Bad Request** | Solicitud inválida | Datos faltantes/incorrectos, validaciones fallidas |
| **401 Unauthorized** | No autenticado | Token ausente, inválido o expirado |
| **403 Forbidden** | Sin permisos | Rol insuficiente, cuenta bloqueada |
| **404 Not Found** | Recurso no encontrado | ID inexistente, endpoint no válido |
| **500 Internal Server Error** | Error del servidor | Errores no controlados, problemas de BD |

---

## Modelos de Datos

### UsuarioDTO
```json
{
  "id": "integer",
  "nombre": "string",
  "apellidos": "string", 
  "dni": "string",
  "email": "string",
  "rol": {
    "id": "integer",
    "nombre": "string"
  },
  "accountNonLocked": "boolean"
}
```

### CocheDTO
```json
{
  "id": "integer",
  "modelo": "string",
  "marca": "string",
  "matricula": "string",
  "licencia": {
    "id": "integer",
    "numero": "string"
  },
  "usuarios": ["UsuarioDTO"],
  "disponible": "boolean",
  "taxistaEnServicio": "UsuarioDTO o null"
}
```

### Servicio
```json
{
  "id": "integer",
  "tipo": "string",
  "descripcion": "string",
  "precioPorKm": "decimal"
}
```

### Viaje
```json
{
  "id": "integer",
  "horaInicio": "datetime (ISO 8601)",
  "horaLlegada": "datetime (ISO 8601)",
  "origen": "string",
  "destino": "string",
  "distanciaKm": "decimal",
  "precioTotal": "decimal",
  "cliente": {
    "id": "integer"
  },
  "taxista": {
    "id": "integer"
  },
  "servicio": {
    "id": "integer"
  },
  "coche": {
    "id": "integer"
  },
  "foto": "string (base64) - opcional"
}
```

### AuthResponse
```json
{
  "token": "string",
  "message": "string"
}
```

### LoginRequest
```json
{
  "email": "string",
  "password": "string"
}
```

---

## Ejemplos de Uso

### Ejemplo 1: Flujo Completo de Cliente

1. **Registro**:
```bash
curl -X POST http://localhost:8080/api/usuarios/registrar \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Ana",
    "apellidos": "García López",
    "dni": "11111111A",
    "email": "ana.garcia@email.com",
    "password": "password123"
  }'
```

2. **Login**:
```bash
curl -X POST http://localhost:8080/api/usuarios/iniciar \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ana.garcia@email.com",
    "password": "password123"
  }'
```

3. **Ver coches en servicio**:
```bash
curl -X GET http://localhost:8080/api/usuarios/enServicio \
  -H "Authorization: Bearer {token}"
```

4. **Hacer un viaje**:
```bash
curl -X POST http://localhost:8080/api/usuarios/hacerViaje \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "horaInicio": "2024-06-09T10:30:00",
    "horaLlegada": "2024-06-09T11:00:00",
    "origen": "Calle Mayor 1, Madrid",
    "destino": "Gran Vía 50, Madrid",
    "distanciaKm": 5.2,
    "precioTotal": 7.8,
    "cliente": {"id": 1},
    "taxista": {"id": 2},
    "servicio": {"id": 1},
    "coche": {"id": 1}
  }'
```

### Ejemplo 2: Flujo Administrativo

1. **Crear servicio premium**:
```bash
curl -X POST http://localhost:8080/api/admin/addServicio \
  -H "Authorization: Bearer {admin_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "tipo": "PREMIUM",
    "descripcion": "Servicio premium con vehículo de lujo",
    "precioPorKm": 2.5
  }'
```

2. **Agregar coche**:
```bash
curl -X POST http://localhost:8080/api/admin/addCoche \
  -H "Authorization: Bearer {admin_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "matricula": "5678XYZ",
    "marca": "Mercedes",
    "modelo": "E-Class",
    "disponible": true,
    "enServicio": false
  }'
```

3. **Asignar taxista a coche**:
```bash
curl -X PUT http://localhost:8080/api/admin/addUsuarioToCoche/1/2 \
  -H "Authorization: Bearer {admin_token}"
```

### Ejemplo 3: Flujo de Taxista

1. **Ver coches asignados**:
```bash
curl -X GET http://localhost:8080/api/taxista/cochesTaxistas/2 \
  -H "Authorization: Bearer {taxista_token}"
```

2. **Poner coche en servicio**:
```bash
curl -X PUT http://localhost:8080/api/taxista/ponerEnServicio/1/2 \
  -H "Authorization: Bearer {taxista_token}"
```

3. **Liberar coche**:
```bash
curl -X PUT http://localhost:8080/api/taxista/liberarCoche/1 \
  -H "Authorization: Bearer {taxista_token}"
```

---

## Notas Importantes

### Paginación
Los endpoints que retornan listas utilizan paginación estándar de Spring Boot:
- `page`: Número de página (base 0)
- `size`: Elementos por página
- `sort`: Ordenación (formato: `campo,direccion`)

### Formatos de Fecha
Todas las fechas utilizan formato ISO 8601: `YYYY-MM-DDTHH:mm:ss`

### Validaciones de DNI
El sistema valida DNIs españoles con el formato: 8 dígitos + letra

### Manejo de Archivos
Las fotos de viajes se envían en formato base64 en el campo `foto` del JSON.

### Seguridad
- Los tokens JWT tienen un tiempo de expiración configurable
- Las contraseñas se almacenan encriptadas con BCrypt
- Se requiere HTTPS en producción

Esta documentación cubre todos los endpoints disponibles en el sistema LuxeRide con sus parámetros, respuestas esperadas y ejemplos de uso.
