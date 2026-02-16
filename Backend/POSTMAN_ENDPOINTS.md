# SGTE Backend — Guía de Endpoints para Postman

> **Base URL:** `http://localhost:8080`  
> **Autenticación:** JWT Bearer Token  
> **Content-Type:** `application/json` (para todos los bodies JSON)

---

## Tabla de Contenidos

1. [Autenticación (Auth)](#1-autenticación-auth)
2. [Autenticación de Dos Factores (2FA)](#2-autenticación-de-dos-factores-2fa)
3. [Usuarios](#3-usuarios)
4. [Credenciales](#4-credenciales)
5. [Estudiantes](#5-estudiantes)
6. [Roles](#6-roles)
7. [Permisos](#7-permisos)
8. [Facultades](#8-facultades)
9. [Carreras](#9-carreras)
10. [Configuración](#10-configuración)
11. [Workflows](#11-workflows)
12. [Workflow Stages](#12-workflow-stages)
13. [Processing Stages](#13-processing-stages)
14. [Estados](#14-estados)
15. [Calendario Académico](#15-calendario-académico)
16. [Plantillas de Documentos](#16-plantillas-de-documentos)
17. [Razones de Rechazo](#17-razones-de-rechazo)
18. [Trámites (Procedures)](#18-trámites-procedures)
19. [Solicitudes (Applications)](#19-solicitudes-applications)
20. [Reglas de Plazos (Deadline Rules)](#20-reglas-de-plazos-deadline-rules)
21. [Notificaciones](#21-notificaciones)
22. [Tipos de Notificación](#22-tipos-de-notificación)
23. [Historial de Etapas de Solicitud](#23-historial-de-etapas-de-solicitud)
24. [Seguimiento de Etapas (Stage Tracking)](#24-seguimiento-de-etapas-stage-tracking)
25. [Documentos Adjuntos](#25-documentos-adjuntos)
26. [Documentos Generados](#26-documentos-generados)
27. [Firmas Digitales](#27-firmas-digitales)
28. [Requisitos de Trámites](#28-requisitos-de-trámites)
29. [Tokens de Sesión](#29-tokens-de-sesión)
30. [Refresh Tokens](#30-refresh-tokens)

---

## 1. Autenticación (Auth)

### 1.1 Login con contraseña

```
POST /api/v1/auth/token
Content-Type: application/x-www-form-urlencoded
```

> **NO requiere Authorization header**

**Ejemplo exitoso:**

| Parámetro | Valor |
|-----------|-------|
| `grantType` | `password` |
| `username` | `admin@uteq.edu.ec` |
| `password` | `Admin123!@` |
| `withRefreshToken` | `true` |

**Respuesta 200 (sin 2FA):**
```json
{
    "access_token": "eyJhbGciOiJSUzI1NiJ9...",
    "token_type": "Bearer",
    "expires_in": "900",
    "refresh_token": "eyJhbGciOiJSUzI1NiJ9..."
}
```

**Respuesta 200 (con 2FA activo):**
```json
{
    "pre_auth_token": "eyJhbGciOiJSUzI1NiJ9...",
    "requires_2fa": "true",
    "message": "Se requiere verificación 2FA"
}
```

**Ejemplo con error — credenciales inválidas:**

| Parámetro | Valor |
|-----------|-------|
| `grantType` | `password` |
| `username` | `admin@uteq.edu.ec` |
| `password` | `contraseñaIncorrecta` |
| `withRefreshToken` | `false` |

**Respuesta 401:**
```json
{
    "error": "Credenciales inválidas"
}
```

**Ejemplo con error — campos faltantes:**

| Parámetro | Valor |
|-----------|-------|
| `grantType` | `password` |

**Respuesta 400:**
```json
{
    "error": "Correo y contraseña son requeridos"
}
```

**Ejemplo con error — grant type no soportado:**

| Parámetro | Valor |
|-----------|-------|
| `grantType` | `client_credentials` |

**Respuesta 400:**
```json
{
    "error": "Grant type no soportado: client_credentials"
}
```

---

### 1.2 Renovar token con Refresh Token

```
POST /api/v1/auth/token
Content-Type: application/x-www-form-urlencoded
```

**Ejemplo exitoso:**

| Parámetro | Valor |
|-----------|-------|
| `grantType` | `refresh_token` |
| `refreshToken` | `eyJhbGciOiJSUzI1NiJ9...` *(token obtenido del login)* |

**Respuesta 200:**
```json
{
    "access_token": "eyJhbGciOiJSUzI1NiJ9...(nuevo)",
    "token_type": "Bearer",
    "expires_in": "900"
}
```

**Ejemplo con error — token expirado/inválido:**

| Parámetro | Valor |
|-----------|-------|
| `grantType` | `refresh_token` |
| `refreshToken` | `token_invalido_o_expirado` |

**Respuesta 401:**
```json
{
    "error": "Refresh token inválido: ..."
}
```

---

### 1.3 Verificación 2FA durante login

```
POST /api/v1/auth/2fa-verify
Content-Type: application/x-www-form-urlencoded
```

**Ejemplo exitoso con código TOTP:**

| Parámetro | Valor |
|-----------|-------|
| `preAuthToken` | `eyJhbGciOiJSUzI1NiJ9...` *(pre_auth_token del login)* |
| `code` | `123456` |

**Respuesta 200:**
```json
{
    "access_token": "eyJhbGciOiJSUzI1NiJ9...",
    "token_type": "Bearer",
    "expires_in": "900",
    "refresh_token": "eyJhbGciOiJSUzI1NiJ9..."
}
```

**Ejemplo exitoso con código de respaldo:**

| Parámetro | Valor |
|-----------|-------|
| `preAuthToken` | `eyJhbGciOiJSUzI1NiJ9...` |
| `backupCode` | `AB12CD34` |

**Ejemplo con error — código inválido:**

| Parámetro | Valor |
|-----------|-------|
| `preAuthToken` | `eyJhbGciOiJSUzI1NiJ9...` |
| `code` | `000000` |

**Respuesta 401:**
```json
{
    "error": "Código 2FA inválido"
}
```

**Ejemplo con error — token no es pre_auth:**

| Parámetro | Valor |
|-----------|-------|
| `preAuthToken` | `eyJhbG...(un access_token normal)` |
| `code` | `123456` |

**Respuesta 401:**
```json
{
    "error": "Token no es un pre-auth token válido"
}
```

---

## 2. Autenticación de Dos Factores (2FA)

> Todos los endpoints (excepto `/validate` y `/validate-backup`) requieren JWT en el header:  
> `Authorization: Bearer <access_token>`

### 2.1 Configurar 2FA

```
POST /api/v1/2fa/setup
Authorization: Bearer <access_token>
```

**Respuesta 200:**
```json
{
    "secretKey": "JBSWY3DPEHPK3PXP",
    "qrCodeUri": "otpauth://totp/SGTE-UTEQ:admin@uteq.edu.ec?secret=JBSWY3DPEHPK3PXP&issuer=SGTE-UTEQ",
    "backupCodes": [
        "A1B2C3D4",
        "E5F6G7H8",
        "I9J0K1L2",
        "M3N4O5P6",
        "Q7R8S9T0",
        "U1V2W3X4",
        "Y5Z6A7B8",
        "C9D0E1F2"
    ]
}
```

**Ejemplo con error — 2FA ya activo:**

**Respuesta 422:**
```json
{
    "status": 422,
    "error": "Unprocessable Entity",
    "message": "2FA ya está activo. Desactívelo primero para reconfigurarlo."
}
```

---

### 2.2 Verificar y activar 2FA (primera vez)

```
POST /api/v1/2fa/verify
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "code": 123456
}
```

**Respuesta 200:**
```json
{
    "message": "2FA activado exitosamente",
    "enabled": true
}
```

**Body con error — código inválido:**
```json
{
    "code": 000000
}
```

**Respuesta 400:**
```json
{
    "status": 400,
    "error": "Bad Request",
    "message": "Código TOTP inválido. Verifique e intente nuevamente."
}
```

**Body con error — campo nulo:**
```json
{
    "code": null
}
```

**Respuesta 400:**
```json
{
    "status": 400,
    "error": "Bad Request",
    "message": "Errores de validación",
    "errors": {
        "code": "El código TOTP es requerido"
    }
}
```

---

### 2.3 Consultar estado de 2FA

```
GET /api/v1/2fa/status
Authorization: Bearer <access_token>
```

**Respuesta 200 (activo):**
```json
{
    "enabled": true,
    "verifiedAt": "2026-02-15T10:30:00"
}
```

**Respuesta 200 (no configurado):**
```json
{
    "enabled": false,
    "verifiedAt": null
}
```

---

### 2.4 Desactivar 2FA

```
DELETE /api/v1/2fa/disable
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "code": 123456
}
```

**Respuesta 200:**
```json
{
    "message": "2FA desactivado exitosamente",
    "enabled": false
}
```

**Ejemplo con error — 2FA no está activo:**

**Respuesta 422:**
```json
{
    "status": 422,
    "error": "Unprocessable Entity",
    "message": "2FA no está activo."
}
```

---

### 2.5 Regenerar códigos de respaldo

```
POST /api/v1/2fa/backup-codes/regenerate
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "code": 123456
}
```

**Respuesta 200:**
```json
{
    "message": "Códigos de respaldo regenerados exitosamente",
    "backupCodes": [
        "X1Y2Z3A4",
        "B5C6D7E8",
        "F9G0H1I2",
        "J3K4L5M6",
        "N7O8P9Q0",
        "R1S2T3U4",
        "V5W6X7Y8",
        "Z9A0B1C2"
    ]
}
```

---

### 2.6 Validar código TOTP (durante login — público)

```
POST /api/v1/2fa/validate?email=admin@uteq.edu.ec
Content-Type: application/json
```

> **NO requiere Authorization header**

**Body exitoso:**
```json
{
    "code": 123456
}
```

**Respuesta 200:**
```json
{
    "valid": true
}
```

**Body con código incorrecto:**
```json
{
    "code": 999999
}
```

**Respuesta 200:**
```json
{
    "valid": false
}
```

---

### 2.7 Validar código de respaldo (durante login — público)

```
POST /api/v1/2fa/validate-backup?email=admin@uteq.edu.ec
Content-Type: application/json
```

> **NO requiere Authorization header**

**Body exitoso:**
```json
{
    "backupCode": "A1B2C3D4"
}
```

**Respuesta 200:**
```json
{
    "valid": true
}
```

**Body con error — campo vacío:**
```json
{
    "backupCode": ""
}
```

**Respuesta 400:**
```json
{
    "status": 400,
    "error": "Bad Request",
    "message": "Errores de validación",
    "errors": {
        "backupCode": "El código de respaldo es requerido"
    }
}
```

---

## 3. Usuarios

> **Requiere:** `Authorization: Bearer <token>` con rol `ROLE_ADMIN`

### 3.1 Listar todos los usuarios

```
GET /api/v1/users
Authorization: Bearer <token_admin>
```

**Respuesta 200:**
```json
[
    {
        "idUser": 1,
        "names": "Admin",
        "surnames": "Sistema",
        "cardId": "0912345678",
        "institutionalEmail": "admin@uteq.edu.ec",
        "personalMail": "admin@gmail.com",
        "phoneNumber": "0991234567",
        "statement": true,
        "active": true,
        "roles": ["ROLE_ADMIN"],
        "configurationId": 1
    }
]
```

**Ejemplo con error — sin autorización:**
```
GET /api/v1/users
(sin header Authorization)
```

**Respuesta 401:**
```json
{
    "error": "Unauthorized"
}
```

**Ejemplo con error — rol insuficiente:**
```
GET /api/v1/users
Authorization: Bearer <token_estudiante>
```

**Respuesta 403:**
```json
{
    "status": 403,
    "error": "Forbidden",
    "message": "Acceso denegado. No tiene permisos para esta operación."
}
```

---

### 3.2 Obtener usuario por ID

```
GET /api/v1/users/1
Authorization: Bearer <token_admin>
```

**Respuesta 200:**
```json
{
    "idUser": 1,
    "names": "Admin",
    "surnames": "Sistema",
    "cardId": "0912345678",
    "institutionalEmail": "admin@uteq.edu.ec",
    "personalMail": "admin@gmail.com",
    "phoneNumber": "0991234567",
    "statement": true,
    "active": true,
    "roles": ["ROLE_ADMIN"],
    "configurationId": 1
}
```

**Ejemplo con error — ID no existe:**
```
GET /api/v1/users/9999
Authorization: Bearer <token_admin>
```

**Respuesta 404:**
```json
{
    "status": 404,
    "error": "Not Found",
    "message": "Usuario no encontrado con id: 9999"
}
```

---

### 3.3 Crear usuario

```
POST /api/v1/users
Authorization: Bearer <token_admin>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "names": "Juan Carlos",
    "surnames": "Pérez López",
    "cardId": "0923456789",
    "institutionalEmail": "jperez@uteq.edu.ec",
    "personalMail": "juanperez@gmail.com",
    "phoneNumber": "0987654321",
    "configurationsIdConfiguration": 1
}
```

**Respuesta 201:**
```json
{
    "idUser": 2,
    "names": "Juan Carlos",
    "surnames": "Pérez López",
    "cardId": "0923456789",
    "institutionalEmail": "jperez@uteq.edu.ec",
    "personalMail": "juanperez@gmail.com",
    "phoneNumber": "0987654321",
    "statement": true,
    "active": true,
    "roles": [],
    "configurationId": 1
}
```

**Body con error — validaciones fallidas:**
```json
{
    "names": "",
    "surnames": "P",
    "cardId": "123",
    "institutionalEmail": "correo-invalido",
    "phoneNumber": "abc",
    "configurationsIdConfiguration": null
}
```

**Respuesta 400:**
```json
{
    "status": 400,
    "error": "Bad Request",
    "message": "Errores de validación",
    "errors": {
        "names": "no debe estar vacío",
        "surnames": "el tamaño debe estar entre 2 y 255",
        "cardId": "debe coincidir con \"^[0-9]{10}$\"",
        "institutionalEmail": "debe coincidir con \".*@uteq\\.edu\\.ec$\"",
        "phoneNumber": "debe coincidir con \"^[0-9]{10}$\"",
        "configurationsIdConfiguration": "no debe ser nulo"
    }
}
```

**Body con error — email duplicado:**
```json
{
    "names": "María",
    "surnames": "González Torres",
    "cardId": "0934567890",
    "institutionalEmail": "admin@uteq.edu.ec",
    "configurationsIdConfiguration": 1
}
```

**Respuesta 409:**
```json
{
    "status": 409,
    "error": "Conflict",
    "message": "Ya existe un Usuario con institutionalEmail: admin@uteq.edu.ec"
}
```

**Body con error — cédula duplicada:**
```json
{
    "names": "Pedro",
    "surnames": "Rodríguez Castro",
    "cardId": "0912345678",
    "institutionalEmail": "prodriguez@uteq.edu.ec",
    "configurationsIdConfiguration": 1
}
```

**Respuesta 409:**
```json
{
    "status": 409,
    "error": "Conflict",
    "message": "Ya existe un Usuario con cardId: 0912345678"
}
```

---

### 3.4 Actualizar usuario

```
PUT /api/v1/users/2
Authorization: Bearer <token_admin>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "idUser": 2,
    "names": "Juan Carlos Actualizado",
    "surnames": "Pérez López",
    "cardId": "0923456789",
    "institutionalEmail": "jperez@uteq.edu.ec",
    "personalMail": "juancarlos.nuevo@gmail.com",
    "phoneNumber": "0987654321",
    "statement": true,
    "configurationsIdConfiguration": 1,
    "active": true
}
```

**Respuesta 200:**
```json
{
    "idUser": 2,
    "names": "Juan Carlos Actualizado",
    "surnames": "Pérez López",
    "cardId": "0923456789",
    "institutionalEmail": "jperez@uteq.edu.ec",
    "personalMail": "juancarlos.nuevo@gmail.com",
    "phoneNumber": "0987654321",
    "statement": true,
    "active": true,
    "roles": [],
    "configurationId": 1
}
```

---

### 3.5 Eliminar usuario

```
DELETE /api/v1/users/2
Authorization: Bearer <token_admin>
```

**Respuesta 204:** *(Sin cuerpo)*

**Ejemplo con error — ID no existe:**
```
DELETE /api/v1/users/9999
Authorization: Bearer <token_admin>
```

**Respuesta 404:**
```json
{
    "status": 404,
    "error": "Not Found",
    "message": "Usuario no encontrado con id: 9999"
}
```

---

### 3.6 Desactivar usuario

```
PATCH /api/v1/users/2/deactivate
Authorization: Bearer <token_admin>
```

**Respuesta 200:**
```json
{
    "idUser": 2,
    "names": "Juan Carlos",
    "surnames": "Pérez López",
    "active": false,
    "..."
}
```

---

### 3.7 Activar usuario

```
PATCH /api/v1/users/2/activate
Authorization: Bearer <token_admin>
```

**Respuesta 200:**
```json
{
    "idUser": 2,
    "names": "Juan Carlos",
    "surnames": "Pérez López",
    "active": true,
    "..."
}
```

---

## 4. Credenciales

> **Requiere:** `Authorization: Bearer <token>` (autenticado)  
> Algunos endpoints requieren `ROLE_ADMIN`

### 4.1 Crear credenciales

```
POST /api/v1/credentials
Authorization: Bearer <token_admin>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "username": "jperez@uteq.edu.ec",
    "passwordHash": "MiPassword123!@"
}
```

**Respuesta 201:**
```json
{
    "id": 2,
    "dateModification": null,
    "lastLogin": null,
    "failedAttempts": 0,
    "accountLocked": false,
    "passwordExpiryDate": "2026-05-15",
    "userId": 2
}
```

**Body con error — contraseña débil:**
```json
{
    "username": "jperez@uteq.edu.ec",
    "passwordHash": "123"
}
```

**Respuesta 400:**
```json
{
    "status": 400,
    "error": "Bad Request",
    "message": "Errores de validación",
    "errors": {
        "passwordHash": "el tamaño debe estar entre 8 y 100"
    }
}
```

---

### 4.2 Cambiar contraseña

```
POST /api/v1/credentials/2/change-password
Authorization: Bearer <token>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "currentPassword": "MiPassword123!@",
    "newPassword": "NuevoPassword456!@"
}
```

**Respuesta 200:**
```json
{
    "id": 2,
    "dateModification": "2026-02-15T10:30:00",
    "failedAttempts": 0,
    "accountLocked": false,
    "..."
}
```

**Body con error — contraseña actual incorrecta:**
```json
{
    "currentPassword": "contraseñaEquivocada",
    "newPassword": "NuevoPassword456!@"
}
```

**Respuesta 400:**
```json
{
    "status": 400,
    "error": "Bad Request",
    "message": "La contraseña actual es incorrecta"
}
```

---

### 4.3 Reset de contraseña (Admin)

```
POST /api/v1/credentials/2/reset-password
Authorization: Bearer <token_admin>
```

**Respuesta 200:**
```json
{
    "message": "Contraseña reseteada exitosamente",
    "temporaryPassword": "Tmp@2x8kP1"
}
```

---

### 4.4 Verificar si contraseña está expirada

```
GET /api/v1/credentials/2/password-expired
Authorization: Bearer <token>
```

**Respuesta 200:**
```json
{
    "expired": false
}
```

---

### 4.5 Bloquear / Desbloquear cuenta

```
POST /api/v1/credentials/2/lock
Authorization: Bearer <token_admin>
```

**Respuesta 200:** Cuenta bloqueada

```
POST /api/v1/credentials/2/unlock
Authorization: Bearer <token_admin>
```

**Respuesta 200:** Cuenta desbloqueada

---

## 5. Estudiantes

> **Requiere:** `Authorization: Bearer <token>` (autenticado)

### 5.1 Listar estudiantes

```
GET /api/v1/students
Authorization: Bearer <token>
```

**Respuesta 200:**
```json
[
    {
        "idStudent": 1,
        "semester": "5",
        "parallel": "A",
        "status": "activo",
        "userName": "Juan Carlos Pérez López",
        "userEmail": "jperez@uteq.edu.ec",
        "careerName": "Ingeniería en Sistemas",
        "careerCode": "ISC"
    }
]
```

---

### 5.2 Obtener estudiante por ID

```
GET /api/v1/students/1
Authorization: Bearer <token>
```

---

### 5.3 Buscar por carrera

```
GET /api/v1/students/career/1
Authorization: Bearer <token>
```

---

### 5.4 Buscar por semestre y paralelo

```
GET /api/v1/students/semester/5/parallel/A
Authorization: Bearer <token>
```

---

### 5.5 Buscar por usuario

```
GET /api/v1/students/user/2
Authorization: Bearer <token>
```

---

### 5.6 Crear estudiante

```
POST /api/v1/students
Authorization: Bearer <token_admin_o_coordinador>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "semester": "5",
    "parallel": "A",
    "usersIdUser": 2,
    "careersIdCareer": 1,
    "status": "activo"
}
```

**Respuesta 201:**
```json
{
    "idStudent": 1,
    "semester": "5",
    "parallel": "A",
    "status": "activo",
    "userName": "Juan Carlos Pérez López",
    "userEmail": "jperez@uteq.edu.ec",
    "careerName": "Ingeniería en Sistemas",
    "careerCode": "ISC"
}
```

**Body con error — semestre inválido:**
```json
{
    "semester": "15",
    "parallel": "A",
    "usersIdUser": 2,
    "careersIdCareer": 1
}
```

**Respuesta 400:**
```json
{
    "status": 400,
    "error": "Bad Request",
    "message": "Errores de validación",
    "errors": {
        "semester": "debe coincidir con \"^[1-9]|10$\""
    }
}
```

**Body con error — paralelo inválido:**
```json
{
    "semester": "5",
    "parallel": "z",
    "usersIdUser": 2,
    "careersIdCareer": 1
}
```

**Respuesta 400:**
```json
{
    "status": 400,
    "error": "Bad Request",
    "message": "Errores de validación",
    "errors": {
        "parallel": "debe coincidir con \"^[A-Z]$\""
    }
}
```

---

### 5.7 Actualizar estudiante

```
PUT /api/v1/students/1
Authorization: Bearer <token_admin_o_coordinador>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "idStudent": 1,
    "semester": "6",
    "parallel": "B",
    "careersIdCareer": 1,
    "status": "activo"
}
```

---

### 5.8 Cambiar estado del estudiante

```
PATCH /api/v1/students/1/status?status=inactivo
Authorization: Bearer <token_admin_o_coordinador>
```

**Ejemplo con error — estado inválido:**
```
PATCH /api/v1/students/1/status?status=expulsado
```

**Respuesta 400:**
```json
{
    "status": 400,
    "error": "Bad Request",
    "message": "Estado no válido: expulsado. Estados permitidos: activo, inactivo, graduado, retirado, suspendido"
}
```

---

### 5.9 Promover semestre

```
POST /api/v1/students/1/promote
Authorization: Bearer <token_admin_o_coordinador>
```

**Respuesta 200:** Semestre incrementado

**Ejemplo con error — ya en semestre 10:**

**Respuesta 422:**
```json
{
    "status": 422,
    "error": "Unprocessable Entity",
    "message": "El estudiante ya está en el semestre máximo (10)"
}
```

---

### 5.10 Graduar / Retirar / Reactivar

```
POST /api/v1/students/1/graduate
Authorization: Bearer <token_admin_o_coordinador_o_decano>
```

```
POST /api/v1/students/1/withdraw
Authorization: Bearer <token_admin_o_coordinador>
```

```
POST /api/v1/students/1/reactivate
Authorization: Bearer <token_admin_o_coordinador_o_decano>
```

---

### 5.11 Eliminar estudiante

```
DELETE /api/v1/students/1
Authorization: Bearer <token_admin>
```

**Respuesta 204:** *(Sin cuerpo)*

---

## 6. Roles

> **Requiere:** `Authorization: Bearer <token>` con rol `ROLE_ADMIN`

### 6.1 Listar roles

```
GET /api/v1/roles
Authorization: Bearer <token_admin>
```

**Respuesta 200:**
```json
[
    {
        "idRole": 1,
        "roleName": "ROLE_ADMIN",
        "roleDescription": "Administrador del sistema",
        "permissions": [
            {
                "idPermission": 1,
                "code": "MANAGE_USERS",
                "description": "Gestionar usuarios"
            }
        ]
    }
]
```

---

### 6.2 Obtener rol por nombre

```
GET /api/v1/roles/name/ROLE_ADMIN
Authorization: Bearer <token_admin>
```

---

### 6.3 Crear rol

```
POST /api/v1/roles
Authorization: Bearer <token_admin>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "roleName": "ROLE_SECRETARY",
    "roleDescription": "Secretaria de la facultad"
}
```

**Respuesta 201:**
```json
{
    "idRole": 5,
    "roleName": "ROLE_SECRETARY",
    "roleDescription": "Secretaria de la facultad",
    "permissions": []
}
```

**Body con error — nombre sin prefijo ROLE_:**
```json
{
    "roleName": "SECRETARY",
    "roleDescription": "Sin prefijo"
}
```

**Respuesta 400:**
```json
{
    "status": 400,
    "error": "Bad Request",
    "message": "Errores de validación",
    "errors": {
        "roleName": "debe coincidir con \"^ROLE_[A-Z_]+$\""
    }
}
```

**Body con error — rol duplicado:**
```json
{
    "roleName": "ROLE_ADMIN",
    "roleDescription": "Duplicado"
}
```

**Respuesta 409:**
```json
{
    "status": 409,
    "error": "Conflict",
    "message": "Ya existe un Rol con roleName: ROLE_ADMIN"
}
```

---

### 6.4 Asignar permisos a un rol

```
POST /api/v1/roles/1/permissions
Authorization: Bearer <token_admin>
Content-Type: application/json
```

**Body exitoso:**
```json
[1, 2, 3]
```

**Respuesta 200:** Rol con permisos actualizados

---

### 6.5 Remover permisos de un rol

```
DELETE /api/v1/roles/1/permissions
Authorization: Bearer <token_admin>
Content-Type: application/json
```

**Body exitoso:**
```json
[2, 3]
```

---

### 6.6 Asignar rol a usuario

```
POST /api/v1/roles/1/users/2
Authorization: Bearer <token_admin>
```

**Respuesta 200:** Rol asignado

---

### 6.7 Remover rol de usuario

```
DELETE /api/v1/roles/1/users/2
Authorization: Bearer <token_admin>
```

**Respuesta 204:** *(Sin cuerpo)*

---

### 6.8 Eliminar rol

```
DELETE /api/v1/roles/5
Authorization: Bearer <token_admin>
```

**Ejemplo con error — rol protegido:**
```
DELETE /api/v1/roles/1
Authorization: Bearer <token_admin>
```

**Respuesta 422:**
```json
{
    "status": 422,
    "error": "Unprocessable Entity",
    "message": "No se puede eliminar el rol protegido: ROLE_ADMIN"
}
```

---

## 7. Permisos

> **Requiere:** `ROLE_ADMIN`

### 7.1 Listar permisos

```
GET /api/v1/permissions
Authorization: Bearer <token_admin>
```

---

### 7.2 Crear permiso

```
POST /api/v1/permissions
Authorization: Bearer <token_admin>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "code": "MANAGE_REPORTS",
    "description": "Gestionar reportes del sistema"
}
```

**Body con error — código con minúsculas:**
```json
{
    "code": "manage_reports",
    "description": "Inválido"
}
```

**Respuesta 400:**
```json
{
    "status": 400,
    "error": "Bad Request",
    "message": "Errores de validación",
    "errors": {
        "code": "debe coincidir con \"^[A-Z_]+$\""
    }
}
```

---

### 7.3 Actualizar / Eliminar permiso

```
PUT /api/v1/permissions/1
DELETE /api/v1/permissions/1
Authorization: Bearer <token_admin>
```

---

## 8. Facultades

> **Requiere:** `Authorization: Bearer <token>` (autenticado)

### 8.1 Listar facultades

```
GET /api/v1/faculty
Authorization: Bearer <token>
```

---

### 8.2 Crear facultad

```
POST /api/v1/faculty
Authorization: Bearer <token>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "facultyname": "Facultad de Ciencias de la Ingeniería",
    "facultycode": "FCI",
    "deaniduser": 3
}
```

**Body con error — nombre muy corto:**
```json
{
    "facultyname": "FC",
    "facultycode": "F"
}
```

**Respuesta 400:**
```json
{
    "status": 400,
    "error": "Bad Request",
    "message": "Errores de validación",
    "errors": {
        "facultyname": "el tamaño debe estar entre 3 y 150",
        "facultycode": "el tamaño debe estar entre 2 y 20"
    }
}
```

---

### 8.3 Actualizar / Eliminar facultad

```
PUT /api/v1/faculty/1
DELETE /api/v1/faculty/1
Authorization: Bearer <token>
```

---

## 9. Carreras

> **Requiere:** `Authorization: Bearer <token>` (autenticado)

### 9.1 Listar carreras (todas o por facultad)

```
GET /api/v1/careers
GET /api/v1/careers?facultyid=1
Authorization: Bearer <token>
```

---

### 9.2 Crear carrera

```
POST /api/v1/careers
Authorization: Bearer <token>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "careername": "Ingeniería en Sistemas Computacionales",
    "careercode": "ISC",
    "facultiesidfaculty": 1,
    "coordinatoriduser": 4
}
```

**Body con error — campos requeridos vacíos:**
```json
{
    "careername": "",
    "careercode": "",
    "facultiesidfaculty": null
}
```

**Respuesta 400:**
```json
{
    "status": 400,
    "error": "Bad Request",
    "message": "Errores de validación",
    "errors": {
        "careername": "no debe estar vacío",
        "careercode": "no debe estar vacío",
        "facultiesidfaculty": "no debe ser nulo"
    }
}
```

---

### 9.3 Actualizar / Eliminar carrera

```
PUT /api/v1/careers/1
DELETE /api/v1/careers/1
Authorization: Bearer <token>
```

---

## 10. Configuración

> **Requiere:** `ROLE_ADMIN`

### 10.1 Listar configuraciones

```
GET /api/v1/configuration
Authorization: Bearer <token_admin>
```

---

### 10.2 Crear configuración

```
POST /api/v1/configuration
Authorization: Bearer <token_admin>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "profilepicturepath": "/uploads/profiles/default.png",
    "signaturepath": "/uploads/signatures/default.png",
    "enable_sms": false,
    "enable_email": true,
    "enable_whatsapp": false,
    "notificationfrequency": "inmediata"
}
```

**Body con error — frecuencia inválida:**
```json
{
    "notificationfrequency": "cada_hora"
}
```

**Respuesta 400:**
```json
{
    "status": 400,
    "error": "Bad Request",
    "message": "Errores de validación",
    "errors": {
        "notificationfrequency": "debe coincidir con \"^(diaria|semanal|mensual|inmediata)$\""
    }
}
```

---

## 11. Workflows

> **Requiere:** `Authorization: Bearer <token>` (autenticado)

### 11.1 Listar workflows

```
GET /api/v1/work-flows
GET /api/v1/work-flows?onlyActive=true
Authorization: Bearer <token>
```

---

### 11.2 Crear workflow

```
POST /api/v1/work-flows
Authorization: Bearer <token>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "workflowname": "Trámite de Matrícula",
    "workflowdescription": "Proceso para matriculación ordinaria y extraordinaria",
    "active": true
}
```

**Body con error — nombre muy corto:**
```json
{
    "workflowname": "ab",
    "workflowdescription": "Descripción"
}
```

**Respuesta 400:**
```json
{
    "status": 400,
    "error": "Bad Request",
    "message": "Errores de validación",
    "errors": {
        "workflowname": "el tamaño debe estar entre 3 y 100"
    }
}
```

---

### 11.3 Actualizar workflow

```
PUT /api/v1/work-flows/1
Authorization: Bearer <token>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "idworkflow": 1,
    "workflowname": "Trámite de Matrícula Actualizado",
    "workflowdescription": "Proceso actualizado",
    "active": true
}
```

---

### 11.4 Eliminar workflow

```
DELETE /api/v1/work-flows/1
Authorization: Bearer <token>
```

---

## 12. Workflow Stages

> **Requiere:** `Authorization: Bearer <token>` (autenticado)

### 12.1 Listar / Crear / Actualizar / Eliminar

```
GET /api/v1/workflow-stages
GET /api/v1/workflow-stages/1
Authorization: Bearer <token>
```

```
POST /api/v1/workflow-stages
Authorization: Bearer <token>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "workflow": { "idWorkflow": 1 },
    "processingStage": { "idProcessingStage": 1 },
    "sequenceOrder": 1,
    "isOptional": false
}
```

```
PUT /api/v1/workflow-stages/1
DELETE /api/v1/workflow-stages/1
```

---

## 13. Processing Stages

> **Requiere:** `Authorization: Bearer <token>` (autenticado)

### 13.1 Listar etapas

```
GET /api/v1/processing-stages
Authorization: Bearer <token>
```

---

### 13.2 Crear etapa

```
POST /api/v1/processing-stages
Authorization: Bearer <token>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "stagename": "Revisión Documental",
    "stagecode": "REV_DOC",
    "stagedescription": "Revisión de documentos entregados por el estudiante",
    "stageorder": 1,
    "requiresapproval": true,
    "maxdurationdays": 5
}
```

**Body con error — orden negativo:**
```json
{
    "stagename": "Etapa",
    "stagecode": "ET",
    "stageorder": 0
}
```

**Respuesta 400:**
```json
{
    "status": 400,
    "error": "Bad Request",
    "message": "Errores de validación",
    "errors": {
        "stageorder": "debe ser mayor que o igual a 1"
    }
}
```

---

## 14. Estados

> **Requiere:** `Authorization: Bearer <token>` (autenticado)

### 14.1 Listar estados

```
GET /api/v1/states
GET /api/v1/states?category=tramite
Authorization: Bearer <token>
```

---

### 14.2 Crear estado

```
POST /api/v1/states
Authorization: Bearer <token>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "statename": "Pendiente",
    "statedescription": "Solicitud pendiente de revisión",
    "statecategory": "tramite"
}
```

**Body con error — nombre vacío:**
```json
{
    "statename": "",
    "statedescription": "Descripción",
    "statecategory": ""
}
```

**Respuesta 400:**
```json
{
    "status": 400,
    "error": "Bad Request",
    "message": "Errores de validación",
    "errors": {
        "statename": "no debe estar vacío",
        "statecategory": "no debe estar vacío"
    }
}
```

---

## 15. Calendario Académico

> **Requiere:** `Authorization: Bearer <token>` (autenticado)

### 15.1 Listar calendarios

```
GET /api/v1/academic-calendar
GET /api/v1/academic-calendar?onlyActive=true
Authorization: Bearer <token>
```

---

### 15.2 Crear período académico

```
POST /api/v1/academic-calendar
Authorization: Bearer <token>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "calendarname": "Período Académico 2026-1",
    "academicperiod": "2026-1",
    "startdate": "2026-04-01",
    "enddate": "2026-08-31",
    "active": true
}
```

**Body con error — campos requeridos nulos:**
```json
{
    "calendarname": "",
    "academicperiod": "",
    "startdate": null,
    "enddate": null
}
```

**Respuesta 400:**
```json
{
    "status": 400,
    "error": "Bad Request",
    "message": "Errores de validación",
    "errors": {
        "calendarname": "no debe estar vacío",
        "academicperiod": "no debe estar vacío",
        "startdate": "no debe ser nulo",
        "enddate": "no debe ser nulo"
    }
}
```

---

## 16. Plantillas de Documentos

> **Requiere:** `Authorization: Bearer <token>` (autenticado)

### 16.1 Listar plantillas

```
GET /api/v1/document-templates
GET /api/v1/document-templates?onlyActive=true
Authorization: Bearer <token>
```

---

### 16.2 Crear plantilla

```
POST /api/v1/document-templates
Authorization: Bearer <token>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "templatename": "Solicitud de Matrícula",
    "templatecode": "SOL_MAT",
    "templatepath": "/templates/solicitud_matricula.docx",
    "documenttype": "solicitud",
    "version": "1.0",
    "requiressignature": true,
    "active": true
}
```

---

## 17. Razones de Rechazo

> **Requiere:** `Authorization: Bearer <token>` (autenticado)

### 17.1 Listar razones

```
GET /api/v1/reject-reason
GET /api/v1/reject-reason?onlyActive=true
Authorization: Bearer <token>
```

---

### 17.2 Crear razón de rechazo

```
POST /api/v1/reject-reason
Authorization: Bearer <token>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "reasoncode": "DOC_INCOMPLETO",
    "reasondescription": "Documentación incompleta o faltante",
    "category": "documentacion",
    "active": true
}
```

**Body con error — descripción muy corta:**
```json
{
    "reasoncode": "X",
    "reasondescription": "abc",
    "category": ""
}
```

**Respuesta 400:**
```json
{
    "status": 400,
    "error": "Bad Request",
    "message": "Errores de validación",
    "errors": {
        "reasoncode": "el tamaño debe estar entre 2 y 50",
        "reasondescription": "el tamaño debe estar entre 5 y 500",
        "category": "no debe estar vacío"
    }
}
```

---

## 18. Trámites (Procedures)

> **Requiere:** `Authorization: Bearer <token>` (autenticado)  
> POST/PUT/DELETE requieren `ROLE_ADMIN`

### 18.1 Listar trámites activos

```
GET /api/v1/procedures
Authorization: Bearer <token>
```

### 18.2 Listar todos (incluyendo inactivos)

```
GET /api/v1/procedures/all
Authorization: Bearer <token_admin>
```

### 18.3 Buscar por código

```
GET /api/v1/procedures/code/MAT-ORD
Authorization: Bearer <token>
```

### 18.4 Buscar por workflow

```
GET /api/v1/procedures/workflow/1
Authorization: Bearer <token>
```

---

### 18.5 Crear trámite

```
POST /api/v1/procedures
Authorization: Bearer <token_admin>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "procedurename": "Matrícula Ordinaria",
    "description": "Proceso de matrícula para período regular",
    "maxduration": 30,
    "workflowidworkflow": 1
}
```

**Respuesta 201:**
```json
{
    "idProcedure": 1,
    "procedurename": "Matrícula Ordinaria",
    "description": "Proceso de matrícula para período regular",
    "procedureCode": "MAT-001",
    "maxduration": 30,
    "status": "activo",
    "workflowId": 1,
    "workflowName": "Trámite de Matrícula",
    "requires2FA": false
}
```

**Body con error — duración fuera de rango:**
```json
{
    "procedurename": "Trámite",
    "description": "Descripción",
    "maxduration": 0,
    "workflowidworkflow": 1
}
```

**Respuesta 400:**
```json
{
    "status": 400,
    "error": "Bad Request",
    "message": "Errores de validación",
    "errors": {
        "maxduration": "debe ser mayor que o igual a 1"
    }
}
```

---

### 18.6 Activar / Desactivar trámite

```
POST /api/v1/procedures/1/activate
POST /api/v1/procedures/1/deactivate
Authorization: Bearer <token_admin>
```

---

### 18.7 Verificar si requiere 2FA

```
GET /api/v1/procedures/1/requires-2fa
Authorization: Bearer <token>
```

**Respuesta 200:**
```json
{
    "requires2FA": false
}
```

---

## 19. Solicitudes (Applications)

> **Requiere:** `Authorization: Bearer <token>` (autenticado)

### 19.1 Listar solicitudes

```
GET /api/v1/applications
Authorization: Bearer <token>
```

### 19.2 Por usuario

```
GET /api/v1/applications/user/2
Authorization: Bearer <token>
```

### 19.3 Por prioridad

```
GET /api/v1/applications/priority/alta
Authorization: Bearer <token>
```

---

### 19.4 Crear solicitud

```
POST /api/v1/applications
Authorization: Bearer <token>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "applicationCode": "SOL-2026-001",
    "estimatedCompletionDate": "2026-03-15",
    "applicationDetails": "Solicitud de matrícula ordinaria para el período 2026-1",
    "currentStageTrackingId": 1,
    "proceduresIdProcedure": 1,
    "applicantUserId": 2,
    "priority": "normal"
}
```

**Body con error — fecha pasada:**
```json
{
    "applicationCode": "SOL-2026-002",
    "estimatedCompletionDate": "2025-01-01",
    "currentStageTrackingId": 1,
    "proceduresIdProcedure": 1,
    "applicantUserId": 2,
    "priority": "normal"
}
```

**Respuesta 400:**
```json
{
    "status": 400,
    "error": "Bad Request",
    "message": "Errores de validación",
    "errors": {
        "estimatedCompletionDate": "debe ser una fecha en presente o en el futuro"
    }
}
```

**Body con error — prioridad inválida:**
```json
{
    "applicationCode": "SOL-003",
    "estimatedCompletionDate": "2026-05-01",
    "currentStageTrackingId": 1,
    "proceduresIdProcedure": 1,
    "applicantUserId": 2,
    "priority": "critica"
}
```

**Respuesta 400:**
```json
{
    "status": 400,
    "error": "Bad Request",
    "message": "Errores de validación",
    "errors": {
        "priority": "debe coincidir con \"^(baja|normal|alta|urgente)$\""
    }
}
```

---

### 19.5 Resolver solicitud

```
PATCH /api/v1/applications/1/resolve?resolution=Solicitud aprobada correctamente
Authorization: Bearer <token>
```

---

### 19.6 Rechazar solicitud

```
PATCH /api/v1/applications/1/reject?rejectionReasonId=1
Authorization: Bearer <token>
```

---

## 20. Reglas de Plazos (Deadline Rules)

> **Requiere:** `Authorization: Bearer <token>` (autenticado)

### 20.1 Listar reglas

```
GET /api/v1/deadlinerules
GET /api/v1/deadlinerules?onlyActive=true
Authorization: Bearer <token>
```

---

### 20.2 Crear regla

```
POST /api/v1/deadlinerules
Authorization: Bearer <token>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "rulename": "Plazo Matrícula Ordinaria",
    "procedurecategory": "matricula",
    "basedeadlinedays": 15,
    "warningdaysbefore": 3,
    "active": true
}
```

---

### 20.3 Actualizar regla

```
PUT /api/v1/deadlinerules/1
Authorization: Bearer <token>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "iddeadlinerule": 1,
    "rulename": "Plazo Matrícula Actualizado",
    "procedurecategory": "matricula",
    "basedeadlinedays": 20,
    "warningdaysbefore": 5,
    "active": true
}
```

---

## 21. Notificaciones

> **Requiere:** `Authorization: Bearer <token>` (autenticado)

### 21.1 Listar / Crear

```
GET /api/v1/notifications
GET /api/v1/notifications/1
Authorization: Bearer <token>
```

```
POST /api/v1/notifications
Authorization: Bearer <token>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "notificationName": "Solicitud Recibida",
    "message": "Su solicitud SOL-2026-001 ha sido recibida correctamente",
    "notificationType": { "idNotificationType": 1 },
    "application": { "idApplication": 1 },
    "recipientUser": { "idUser": 2 },
    "deliveryStatus": "pending",
    "deliveryChannel": "email"
}
```

---

## 22. Tipos de Notificación

> **Requiere:** `Authorization: Bearer <token>` (autenticado)

```
GET /api/v1/notification-types
GET /api/v1/notification-types/1
Authorization: Bearer <token>
```

```
POST /api/v1/notification-types
Authorization: Bearer <token>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "nameTypeNotification": "Solicitud Aprobada",
    "templateCode": "SOL_APPROVED",
    "priorityLevel": "alta"
}
```

---

## 23. Historial de Etapas de Solicitud

> **Requiere:** `Authorization: Bearer <token>` (autenticado)

```
GET /api/v1/application-stage-history
GET /api/v1/application-stage-history/1
Authorization: Bearer <token>
```

```
POST /api/v1/application-stage-history
Authorization: Bearer <token>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "application": { "idApplication": 1 },
    "stageTracking": { "idStageTracking": 1 },
    "enteredAt": "2026-02-15T08:00:00",
    "processedByUser": { "idUser": 3 },
    "comments": "Documentos recibidos y en revisión"
}
```

---

## 24. Seguimiento de Etapas (Stage Tracking)

> **Requiere:** `Authorization: Bearer <token>` (autenticado)

```
GET /api/v1/stage-tracking
GET /api/v1/stage-tracking/1
Authorization: Bearer <token>
```

```
POST /api/v1/stage-tracking
Authorization: Bearer <token>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "state": { "idState": 1 },
    "processingStage": { "idProcessingStage": 1 },
    "enteredAt": "2026-02-15T08:00:00",
    "assignedToUser": { "idUser": 3 },
    "notes": "Asignado para revisión inicial"
}
```

---

## 25. Documentos Adjuntos

> **Requiere:** `Authorization: Bearer <token>` (autenticado)

```
GET /api/v1/attached-documents
GET /api/v1/attached-documents/1
Authorization: Bearer <token>
```

```
POST /api/v1/attached-documents
Authorization: Bearer <token>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "application": { "idApplication": 1 },
    "requirement": { "idRequirementsOfTheProcedure": 1 },
    "fileName": "cedula_identidad.pdf",
    "filePath": "/uploads/documents/2026/cedula_identidad.pdf",
    "fileSizeBytes": 204800,
    "mimeType": "application/pdf",
    "uploadDate": "2026-02-15T10:00:00",
    "uploadedByUser": { "idUser": 2 }
}
```

---

## 26. Documentos Generados

> **Requiere:** `Authorization: Bearer <token>` (autenticado)

```
GET /api/v1/documents-generated
GET /api/v1/documents-generated/1
Authorization: Bearer <token>
```

```
POST /api/v1/documents-generated
Authorization: Bearer <token>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "application": { "idApplication": 1 },
    "template": { "idTemplate": 1 },
    "documentType": "certificado",
    "documentPath": "/generated/2026/certificado_matricula.pdf",
    "generatedAt": "2026-02-15T11:00:00",
    "generatedByUser": { "idUser": 3 }
}
```

---

## 27. Firmas Digitales

> **Requiere:** `Authorization: Bearer <token>` (autenticado)

```
GET /api/v1/digital-signatures
GET /api/v1/digital-signatures/1
Authorization: Bearer <token>
```

```
POST /api/v1/digital-signatures
Authorization: Bearer <token>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "user": { "idUser": 3 },
    "certificatePath": "/certs/decano_firma.p12",
    "certificateSerial": "ABC123DEF456",
    "issuer": "SGTE-UTEQ CA",
    "validFrom": "2026-01-01",
    "validUntil": "2027-01-01",
    "signatureAlgorithm": "SHA256withRSA",
    "active": true,
    "createdAt": "2026-02-15T10:00:00"
}
```

---

## 28. Requisitos de Trámites

> **Requiere:** `Authorization: Bearer <token>` (autenticado)

```
GET /api/v1/requirements
GET /api/v1/requirements/1
Authorization: Bearer <token>
```

```
POST /api/v1/requirements
Authorization: Bearer <token>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "procedure": { "idProcedure": 1 },
    "requirementName": "Copia de Cédula",
    "requirementDescription": "Copia digitalizada de cédula de identidad",
    "requirementType": "documento",
    "isMandatory": true,
    "displayOrder": 1
}
```

---

## 29. Tokens de Sesión

> **Requiere:** `Authorization: Bearer <token>` (autenticado)

```
GET /api/v1/session-tokens
GET /api/v1/session-tokens/1
Authorization: Bearer <token>
```

```
POST /api/v1/session-tokens
Authorization: Bearer <token>
Content-Type: application/json
```

**Body exitoso:**
```json
{
    "user": { "idUser": 2 },
    "token": "session_token_value_abc123",
    "ipAddress": "192.168.1.100",
    "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
    "createdAt": "2026-02-15T08:00:00",
    "expiresAt": "2026-02-15T20:00:00"
}
```

---

## 30. Refresh Tokens

> **Requiere:** `Authorization: Bearer <token>` (autenticado)  
> **Nota:** ID es de tipo `Long`

```
GET /api/v1/refresh-tokens
GET /api/v1/refresh-tokens/1
Authorization: Bearer <token>
```

---

## Errores Comunes en Todos los Endpoints

### Sin token de autenticación

```
GET /api/v1/users
(sin header Authorization)
```

**Respuesta 401:**
```json
{
    "error": "Unauthorized"
}
```

---

### Token expirado

```
GET /api/v1/users
Authorization: Bearer eyJhbGciOiJSUzI1NiJ9...(token expirado)
```

**Respuesta 401:**
```json
{
    "error": "Jwt expired at ..."
}
```

---

### Rol insuficiente

```
GET /api/v1/users
Authorization: Bearer <token_estudiante>
```

**Respuesta 403:**
```json
{
    "status": 403,
    "error": "Forbidden",
    "message": "Acceso denegado. No tiene permisos para esta operación."
}
```

---

### Recurso no encontrado

```
GET /api/v1/users/99999
Authorization: Bearer <token_admin>
```

**Respuesta 404:**
```json
{
    "status": 404,
    "error": "Not Found",
    "message": "Usuario no encontrado con id: 99999"
}
```

---

### Body JSON inválido

```
POST /api/v1/users
Authorization: Bearer <token_admin>
Content-Type: application/json

{esto no es json}
```

**Respuesta 400:**
```json
{
    "status": 400,
    "error": "Bad Request",
    "message": "JSON malformado o ilegible"
}
```

---

## Resumen de Endpoints

| Módulo | Base Path | Total Endpoints | Auth |
|--------|-----------|----------------|------|
| Auth | `/api/v1/auth` | 2 | Público |
| 2FA | `/api/v1/2fa` | 7 | Mixto |
| Usuarios | `/api/v1/users` | 7 | ADMIN |
| Credenciales | `/api/v1/credentials` | 11 | Mixto |
| Estudiantes | `/api/v1/students` | 15 | Mixto |
| Roles | `/api/v1/roles` | 12 | ADMIN |
| Permisos | `/api/v1/permissions` | 4 | ADMIN |
| Facultades | `/api/v1/faculty` | 4 | Autenticado |
| Carreras | `/api/v1/careers` | 4 | Autenticado |
| Configuración | `/api/v1/configuration` | 4 | ADMIN |
| Workflows | `/api/v1/work-flows` | 4 | Autenticado |
| Workflow Stages | `/api/v1/workflow-stages` | 5 | Autenticado |
| Processing Stages | `/api/v1/processing-stages` | 4 | Autenticado |
| Estados | `/api/v1/states` | 4 | Autenticado |
| Calendario | `/api/v1/academic-calendar` | 4 | Autenticado |
| Plantillas Doc. | `/api/v1/document-templates` | 4 | Autenticado |
| Razones Rechazo | `/api/v1/reject-reason` | 4 | Autenticado |
| Trámites | `/api/v1/procedures` | 13 | Mixto |
| Solicitudes | `/api/v1/applications` | 9 | Autenticado |
| Deadline Rules | `/api/v1/deadlinerules` | 4 | Autenticado |
| Notificaciones | `/api/v1/notifications` | 5 | Autenticado |
| Tipos Notif. | `/api/v1/notification-types` | 5 | Autenticado |
| Historial Etapas | `/api/v1/application-stage-history` | 5 | Autenticado |
| Stage Tracking | `/api/v1/stage-tracking` | 5 | Autenticado |
| Docs. Adjuntos | `/api/v1/attached-documents` | 5 | Autenticado |
| Docs. Generados | `/api/v1/documents-generated` | 5 | Autenticado |
| Firmas Digitales | `/api/v1/digital-signatures` | 5 | Autenticado |
| Requisitos | `/api/v1/requirements` | 5 | Autenticado |
| Session Tokens | `/api/v1/session-tokens` | 5 | Autenticado |
| Refresh Tokens | `/api/v1/refresh-tokens` | 5 | Autenticado |
| **TOTAL** | | **~168** | |
