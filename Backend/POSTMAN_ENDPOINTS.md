# Guía de Pruebas en Postman — SGTE Backend

> **Proyecto:** Sistema de Gestión de Trámites Estudiantiles (SGTE)  
> **Base URL:** `http://localhost:8080`  
> **Framework:** Spring Boot 4.0.2 + Spring Security 7 (JWT RSA)  
> **Total Endpoints:** 155 en 31 controladores  
> **Fecha:** Febrero 2026

---

## Tabla de Contenidos

1. [Configuración Inicial de Postman](#1-configuración-inicial-de-postman)
2. [Autenticación (AuthController)](#2-autenticación-authcontroller)
3. [Autenticación de Dos Factores (TwoFactorAuthController)](#3-autenticación-de-dos-factores-twofactorauthcontroller)
4. [Usuarios (UsersController)](#4-usuarios-userscontroller)
5. [Estudiantes (StudentsController)](#5-estudiantes-studentscontroller)
6. [Roles (RolesController)](#6-roles-rolescontroller)
7. [Credenciales (CredentialsController)](#7-credenciales-credentialscontroller)
8. [Solicitudes (ApplicationsController)](#8-solicitudes-applicationscontroller)
9. [Trámites (ProceduresController)](#9-trámites-procedurescontroller)
10. [Documentos Adjuntos (AttachedDocumentsController)](#10-documentos-adjuntos-attacheddocumentscontroller)
11. [Refresh Tokens (RefreshTokenController)](#11-refresh-tokens-refreshtokencontroller)
12. [Email (EmailController)](#12-email-emailcontroller)
13. [Calendario Académico (AcademicCalendarController)](#13-calendario-académico-academiccalendarcontroller)
14. [Carreras (CareerController)](#14-carreras-careercontroller)
15. [Configuración (ConfigurationController)](#15-configuración-configurationcontroller)
16. [Reglas de Plazo (DeadlineruleControllers)](#16-reglas-de-plazo-deadlinerulecontrollers)
17. [Plantillas de Documentos (DocumentTemplateController)](#17-plantillas-de-documentos-documenttemplatecontroller)
18. [Facultades (FacultyController)](#18-facultades-facultycontroller)
19. [Permisos (PermissionController)](#19-permisos-permissioncontroller)
20. [Etapas de Procesamiento (ProcessingStageController)](#20-etapas-de-procesamiento-processingstagecontroller)
21. [Motivos de Rechazo (RejectReasonController)](#21-motivos-de-rechazo-rejectreasoncontroller)
22. [Requisitos (RequirementsController)](#22-requisitos-requirementscontroller)
23. [Estados (StatesController)](#23-estados-statescontroller)
24. [Flujos de Trabajo (WorkFlowsController)](#24-flujos-de-trabajo-workflowscontroller)
25. [Flujo-Etapas (WorkflowStagesController)](#25-flujo-etapas-workflowstagescontroller)
26. [Seguimiento de Etapas (StageTrackingController)](#26-seguimiento-de-etapas-stagetrackingcontroller)
27. [Sesiones (SessionTokenController)](#27-sesiones-sessiontokencontroller)
28. [Tipos de Notificación (NotificationTypeController)](#28-tipos-de-notificación-notificationtypecontroller)
29. [Notificaciones (NotificationController)](#29-notificaciones-notificationcontroller)
30. [Documentos Generados (DocumentsGeneratedController)](#30-documentos-generados-documentsgeneratedcontroller)
31. [Firmas Digitales (DigitalSignaturesController)](#31-firmas-digitales-digitalsignaturescontroller)
32. [Historial de Etapas (ApplicationStageHistoryController)](#32-historial-de-etapas-applicationstagehistorycontroller)
33. [Respuestas de Error](#33-respuestas-de-error)

---

## 1. Configuración Inicial de Postman

### 1.1 Variables de Entorno

Crear un **Environment** en Postman con estas variables:

| Variable | Valor Inicial | Descripción |
|---|---|---|
| `base_url` | `http://localhost:8080` | URL base |
| `access_token` | *(vacío)* | Se llena automáticamente al hacer login |
| `refresh_token` | *(vacío)* | Se llena automáticamente al hacer login |
| `pre_auth_token` | *(vacío)* | Token temporal para flujo 2FA |

### 1.2 Configurar Autorización Automática

En la pestaña **Authorization** de la colección raíz:

- **Type:** Bearer Token  
- **Token:** `{{access_token}}`

Todos los requests que hereden de la colección usarán este token automáticamente.

### 1.3 Script de Auto-Guardado del Token

Agregar este script en la pestaña **Tests** del request de Login (`POST /api/v1/auth/token`):

```javascript
if (pm.response.code === 200) {
    var json = pm.response.json();
    if (json.access_token) {
        pm.environment.set("access_token", json.access_token);
    }
    if (json.refresh_token) {
        pm.environment.set("refresh_token", json.refresh_token);
    }
    if (json.pre_auth_token) {
        pm.environment.set("pre_auth_token", json.pre_auth_token);
    }
}
```

### 1.4 Headers Comunes

| Header | Valor | Se Aplica A |
|---|---|---|
| `Content-Type` | `application/json` | Todos los POST/PUT con body JSON |
| `Content-Type` | `multipart/form-data` | Solo upload de archivos |
| `Authorization` | `Bearer {{access_token}}` | Todos excepto endpoints públicos |

---

## 2. Autenticación (AuthController)

**Base Path:** `/api/v1/auth`  
**Autenticación:** Público (no requiere JWT)

---

### 2.1 Login con Credenciales

```
POST {{base_url}}/api/v1/auth/token
```

**Headers:**
```
Content-Type: application/x-www-form-urlencoded
```

**Body (x-www-form-urlencoded):**

| Key | Value | Requerido |
|---|---|---|
| `grantType` | `password` | Sí |
| `username` | `admin@uteq.edu.ec` | Sí |
| `password` | `Admin123!@` | Sí |
| `withRefreshToken` | `true` | No (default: false) |

**Respuesta 200 OK (sin 2FA):**
```json
{
    "access_token": "eyJhbGciOiJSUzI1NiJ9...",
    "refresh_token": "dGhpcyBpcyBhIHJlZnJlc2g..."
}
```

**Respuesta 200 OK (con 2FA activo):**
```json
{
    "pre_auth_token": "abc123...",
    "requires_2fa": "true"
}
```

---

### 2.2 Renovar Token con Refresh Token

```
POST {{base_url}}/api/v1/auth/token
```

**Body (x-www-form-urlencoded):**

| Key | Value |
|---|---|
| `grantType` | `refresh_token` |
| `refreshToken` | `{{refresh_token}}` |

**Respuesta 200 OK:**
```json
{
    "access_token": "eyJhbGciOiJSUzI1NiJ9..."
}
```

---

### 2.3 Verificar 2FA durante Login

```
POST {{base_url}}/api/v1/auth/2fa-verify
```

**Body (x-www-form-urlencoded):**

| Key | Value |
|---|---|
| `preAuthToken` | `{{pre_auth_token}}` |
| `code` | `123456` |

**Respuesta 200 OK:**
```json
{
    "access_token": "eyJhbGciOiJSUzI1NiJ9...",
    "refresh_token": "dGhpcyBpcyBhIHJlZnJlc2g..."
}
```

---

### 2.4 Logout

```
POST {{base_url}}/api/v1/auth/logout
```

**Headers:**
```
Authorization: Bearer {{access_token}}
```

**Body:** Ninguno

**Respuesta 200 OK:**
```json
{
    "message": "Sesión cerrada exitosamente"
}
```

---

## 3. Autenticación de Dos Factores (TwoFactorAuthController)

**Base Path:** `/api/v1/2fa`  
**Autenticación:** Mixta (algunos públicos, otros requieren JWT)

---

### 3.1 Configurar 2FA (Setup)

```
POST {{base_url}}/api/v1/2fa/setup
```

**Permiso:** `AUTH2FA_CONFIGURAR`  
**Headers:** `Authorization: Bearer {{access_token}}`  
**Body:** Ninguno

**Respuesta 200 OK:**
```json
{
    "secretKey": "JBSWY3DPEHPK3PXP",
    "qrCodeUri": "otpauth://totp/SGTE:admin@uteq.edu.ec?secret=JBSWY3DPEHPK3PXP&issuer=SGTE",
    "backupCodes": ["A1B2C3D4", "E5F6G7H8", "I9J0K1L2", "M3N4O5P6"]
}
```

---

### 3.2 Verificar y Activar 2FA

```
POST {{base_url}}/api/v1/2fa/verify
```

**Permiso:** `AUTH2FA_VERIFICAR`  
**Body (JSON):**
```json
{
    "code": 123456
}
```

---

### 3.3 Desactivar 2FA

```
DELETE {{base_url}}/api/v1/2fa/disable
```

**Permiso:** `AUTH2FA_DESACTIVAR`  
**Body (JSON):**
```json
{
    "code": 123456
}
```

---

### 3.4 Consultar Estado de 2FA

```
GET {{base_url}}/api/v1/2fa/status
```

**Permiso:** `AUTH2FA_ESTADO`

**Respuesta 200 OK:**
```json
{
    "enabled": true,
    "verifiedAt": "2026-01-15T10:30:00"
}
```

---

### 3.5 Regenerar Códigos de Respaldo

```
POST {{base_url}}/api/v1/2fa/backup-codes/regenerate
```

**Permiso:** `AUTH2FA_REGENERAR`  
**Body (JSON):**
```json
{
    "code": 123456
}
```

---

### 3.6 Validar TOTP durante Login (PÚBLICO)

```
POST {{base_url}}/api/v1/2fa/validate?preAuthToken={{pre_auth_token}}
```

**Autenticación:** Ninguna  
**Body (JSON):**
```json
{
    "code": 123456
}
```

---

### 3.7 Validar Código de Respaldo durante Login (PÚBLICO)

```
POST {{base_url}}/api/v1/2fa/validate-backup?preAuthToken={{pre_auth_token}}
```

**Autenticación:** Ninguna  
**Body (JSON):**
```json
{
    "backupCode": "A1B2C3D4"
}
```

---

## 4. Usuarios (UsersController)

**Base Path:** `/api/v1/users`  
**Autenticación:** `isAuthenticated()` (JWT válido requerido)

---

### 4.1 Listar Usuarios

```
GET {{base_url}}/api/v1/users
```

**Permiso:** `USUARIO_LISTAR`

**Respuesta 200 OK:**
```json
[
    {
        "idUser": 1,
        "names": "Carlos",
        "surnames": "Mendoza López",
        "cardId": "0912345678",
        "institutionalEmail": "cmendoza@uteq.edu.ec",
        "personalMail": "carlos@gmail.com",
        "phoneNumber": "0991234567",
        "statement": true,
        "configurationsIdConfiguration": 1,
        "createdAt": "2026-01-10T08:00:00",
        "updatedAt": "2026-02-01T14:30:00",
        "active": true
    }
]
```

---

### 4.2 Obtener Usuario por ID

```
GET {{base_url}}/api/v1/users/1
```

**Permiso:** `USUARIO_VER`

---

### 4.3 Crear Usuario

```
POST {{base_url}}/api/v1/users
```

**Permiso:** `USUARIO_CREAR`  
**Body (JSON):**
```json
{
    "names": "María",
    "surnames": "García Rodríguez",
    "cardId": "0923456789",
    "institutionalEmail": "mgarcia@uteq.edu.ec",
    "personalMail": "maria@gmail.com",
    "phoneNumber": "0992345678",
    "configurationsIdConfiguration": 1
}
```

**Validaciones:**
- `names`: Obligatorio, 2-255 caracteres
- `surnames`: Obligatorio, 2-255 caracteres
- `cardId`: Obligatorio, exactamente 10 dígitos (`^[0-9]{10}$`)
- `institutionalEmail`: Obligatorio, email, debe terminar en `@uteq.edu.ec`
- `personalMail`: Opcional, formato email
- `phoneNumber`: Opcional, exactamente 10 dígitos
- `configurationsIdConfiguration`: Obligatorio

---

### 4.4 Actualizar Usuario

```
PUT {{base_url}}/api/v1/users/1
```

**Permiso:** `USUARIO_MODIFICAR`  
**Body (JSON):**
```json
{
    "idUser": 1,
    "names": "María Elena",
    "surnames": "García Rodríguez",
    "cardId": "0923456789",
    "institutionalEmail": "mgarcia@uteq.edu.ec",
    "personalMail": "mariaelena@gmail.com",
    "phoneNumber": "0992345678",
    "statement": true,
    "configurationsIdConfiguration": 1,
    "active": true
}
```

---

### 4.5 Eliminar Usuario

```
DELETE {{base_url}}/api/v1/users/1
```

**Permiso:** `USUARIO_ELIMINAR`  
**Respuesta:** `204 No Content`

---

### 4.6 Desactivar Usuario (Borrado Lógico)

```
PATCH {{base_url}}/api/v1/users/1/deactivate
```

**Permiso:** `USUARIO_DESACTIVAR`

---

### 4.7 Reactivar Usuario

```
PATCH {{base_url}}/api/v1/users/1/activate
```

**Permiso:** `USUARIO_ACTIVAR`

---

## 5. Estudiantes (StudentsController)

**Base Path:** `/api/v1/students`  
**Autenticación:** `isAuthenticated()`

---

### 5.1 Listar Estudiantes

```
GET {{base_url}}/api/v1/students
```

**Permiso:** `ESTUDIANTE_LISTAR`

**Respuesta 200 OK:**
```json
[
    {
        "idStudent": 1,
        "semester": "5",
        "parallel": "A",
        "userId": 3,
        "userName": "Juan Pérez",
        "userEmail": "jperez@uteq.edu.ec",
        "careerId": 1,
        "careerName": "Ingeniería en Sistemas",
        "enrollmentDate": "2024-04-15",
        "status": "activo"
    }
]
```

---

### 5.2 Obtener Estudiante por ID

```
GET {{base_url}}/api/v1/students/1
```

**Permiso:** `ESTUDIANTE_VER`

---

### 5.3 Listar Estudiantes por Carrera

```
GET {{base_url}}/api/v1/students/career/1
```

**Permiso:** `ESTUDIANTE_LISTAR`

---

### 5.4 Filtrar por Semestre y Paralelo

```
GET {{base_url}}/api/v1/students/semester/5/parallel/A
```

**Permiso:** `ESTUDIANTE_LISTAR`

---

### 5.5 Obtener Estudiante por User ID

```
GET {{base_url}}/api/v1/students/user/3
```

**Permiso:** `ESTUDIANTE_VER`

---

### 5.6 Matricular (Crear) Estudiante

```
POST {{base_url}}/api/v1/students
```

**Permiso:** `ESTUDIANTE_CREAR`  
**Body (JSON):**
```json
{
    "semester": "1",
    "parallel": "A",
    "usersIdUser": 5,
    "careersIdCareer": 1,
    "status": "activo"
}
```

**Validaciones:**
- `semester`: Obligatorio, 1-10 (`^[1-9]|10$`)
- `parallel`: Obligatorio, una letra mayúscula (`^[A-Z]$`)
- `usersIdUser`: Obligatorio
- `careersIdCareer`: Obligatorio
- `status`: Opcional, uno de: `activo`, `inactivo`, `graduado`, `retirado`

---

### 5.7 Actualizar Estudiante

```
PUT {{base_url}}/api/v1/students/1
```

**Permiso:** `ESTUDIANTE_MODIFICAR`  
**Body (JSON):**
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

### 5.8 Cambiar Estado

```
PATCH {{base_url}}/api/v1/students/1/status?status=inactivo
```

**Permiso:** `ESTUDIANTE_MODIFICAR`  
**Params:** `status` = `activo` | `inactivo` | `graduado` | `retirado` | `suspendido`

---

### 5.9 Promover al Siguiente Semestre

```
POST {{base_url}}/api/v1/students/1/promote
```

**Permiso:** `ESTUDIANTE_PROMOVER`  
**Body:** Ninguno

---

### 5.10 Graduar Estudiante

```
POST {{base_url}}/api/v1/students/1/graduate
```

**Permiso:** `ESTUDIANTE_GRADUAR`

---

### 5.11 Retirar Estudiante

```
POST {{base_url}}/api/v1/students/1/withdraw
```

**Permiso:** `ESTUDIANTE_RETIRAR`

---

### 5.12 Reactivar Estudiante

```
POST {{base_url}}/api/v1/students/1/reactivate
```

**Permiso:** `ESTUDIANTE_REACTIVAR`

---

### 5.13 Eliminar Estudiante

```
DELETE {{base_url}}/api/v1/students/1
```

**Permiso:** `ESTUDIANTE_ELIMINAR`  
**Respuesta:** `204 No Content`

---

## 6. Roles (RolesController)

**Base Path:** `/api/v1/roles`  
**Autenticación:** `isAuthenticated()`

---

### 6.1 Listar Roles

```
GET {{base_url}}/api/v1/roles
```

**Permiso:** `ROL_LISTAR`

**Respuesta 200 OK:**
```json
[
    {
        "idRole": 1,
        "roleName": "ROLE_ADMIN",
        "roleDescription": "Administrador del sistema",
        "permissions": [
            {
                "idPermission": 1,
                "code": "USUARIO_LISTAR",
                "description": "Listar usuarios del sistema"
            }
        ]
    }
]
```

---

### 6.2 Obtener Rol por ID

```
GET {{base_url}}/api/v1/roles/1
```

**Permiso:** `ROL_VER`

---

### 6.3 Buscar Rol por Nombre

```
GET {{base_url}}/api/v1/roles/name/ROLE_ADMIN
```

**Permiso:** `ROL_VER`

---

### 6.4 Crear Rol

```
POST {{base_url}}/api/v1/roles
```

**Permiso:** `ROL_CREAR`  
**Body (JSON):**
```json
{
    "roleName": "ROLE_SECRETARY",
    "roleDescription": "Secretaria académica"
}
```

**Validaciones:**
- `roleName`: Obligatorio, 3-50 chars, debe ser `ROLE_XXX` (`^ROLE_[A-Z_]+$`)
- `roleDescription`: Opcional, máx 255 chars

---

### 6.5 Actualizar Rol

```
PUT {{base_url}}/api/v1/roles/5
```

**Permiso:** `ROL_MODIFICAR`  
**Body (JSON):**
```json
{
    "idRole": 5,
    "roleName": "ROLE_SECRETARY",
    "roleDescription": "Secretaria académica actualizada"
}
```

---

### 6.6 Eliminar Rol

```
DELETE {{base_url}}/api/v1/roles/5
```

**Permiso:** `ROL_ELIMINAR`  
**Nota:** Los roles protegidos (`ROLE_ADMIN`, `ROLE_STUDENT`, `ROLE_COORDINATOR`, `ROLE_DEAN`) no pueden eliminarse.

---

### 6.7 Asignar Permisos a un Rol

```
POST {{base_url}}/api/v1/roles/2/permissions
```

**Permiso:** `ROL_ASIGNAR_PERMISO`  
**Body (JSON):**
```json
[1, 2, 3, 5, 8]
```

> El body es un array de IDs de permisos (`Set<Integer>`).

---

### 6.8 Remover Permisos de un Rol

```
DELETE {{base_url}}/api/v1/roles/2/permissions
```

**Permiso:** `ROL_REMOVER_PERMISO`  
**Body (JSON):**
```json
[3, 5]
```

---

### 6.9 Asignar Rol a un Usuario

```
POST {{base_url}}/api/v1/roles/2/users/5
```

**Permiso:** `ROL_ASIGNAR_USUARIO`  
**Body:** Ninguno

---

### 6.10 Remover Rol de un Usuario

```
DELETE {{base_url}}/api/v1/roles/2/users/5
```

**Permiso:** `ROL_REMOVER_USUARIO`

---

## 7. Credenciales (CredentialsController)

**Base Path:** `/api/v1/credentials`  
**Autenticación:** `isAuthenticated()`

---

### 7.1 Listar Credenciales

```
GET {{base_url}}/api/v1/credentials
```

**Permiso:** `CRED_LISTAR`

**Respuesta 200 OK:**
```json
[
    {
        "idCredentials": 1,
        "username": "admin@uteq.edu.ec",
        "lastLogin": "2026-02-15T08:30:00",
        "failedAttempts": 0,
        "accountLocked": false,
        "passwordExpiryDate": "2026-08-15"
    }
]
```

> **Nota:** El hash de la contraseña nunca se incluye en la respuesta.

---

### 7.2 Obtener Credencial por ID

```
GET {{base_url}}/api/v1/credentials/1
```

**Permiso:** `CRED_VER`

---

### 7.3 Crear Credencial

```
POST {{base_url}}/api/v1/credentials
```

**Permiso:** `CRED_CREAR`  
**Body (JSON):**
```json
{
    "username": "jperez@uteq.edu.ec",
    "passwordHash": "MiPassword123!@"
}
```

**Validaciones:**
- `username`: Obligatorio, formato email
- `passwordHash`: Obligatorio, 8-100 chars, debe incluir mayúscula, minúscula, dígito y carácter especial

> El servicio aplica BCrypt automáticamente al guardar.

---

### 7.4 Cambiar Contraseña

```
POST {{base_url}}/api/v1/credentials/1/change-password
```

**Permiso:** `CRED_CAMBIAR_PASS`  
**Body (JSON):**
```json
{
    "currentPassword": "OldPassword123!@",
    "newPassword": "NewPassword456!@"
}
```

---

### 7.5 Resetear Contraseña (Admin)

```
POST {{base_url}}/api/v1/credentials/3/reset-password
```

**Permiso:** `CRED_RESETEAR_PASS`  
**Body:** Ninguno

**Respuesta 200 OK:**
```json
{
    "temporaryPassword": "Tmp$9x2kL!"
}
```

---

### 7.6 Verificar Expiración de Contraseña

```
GET {{base_url}}/api/v1/credentials/1/password-expired
```

**Permiso:** Solo `isAuthenticated()` (sin permiso específico)

**Respuesta 200 OK:**
```json
{
    "expired": false
}
```

---

### 7.7 Bloquear Cuenta

```
POST {{base_url}}/api/v1/credentials/3/lock
```

**Permiso:** `CRED_BLOQUEAR`

---

### 7.8 Desbloquear Cuenta

```
POST {{base_url}}/api/v1/credentials/3/unlock
```

**Permiso:** `CRED_DESBLOQUEAR`

---

### 7.9 Eliminar Credencial

```
DELETE {{base_url}}/api/v1/credentials/3
```

**Permiso:** `CRED_ELIMINAR`  
**Respuesta:** `204 No Content`

---

## 8. Solicitudes (ApplicationsController)

**Base Path:** `/api/v1/applications`  
**Autenticación:** `isAuthenticated()`

---

### 8.1 Listar Solicitudes

```
GET {{base_url}}/api/v1/applications
```

**Permiso:** `SOL_LISTAR`

**Respuesta 200 OK:**
```json
[
    {
        "idApplication": 1,
        "applicationCode": "SOL-2026-001",
        "creationDate": "2026-02-10T09:00:00",
        "estimatedCompletionDate": "2026-03-10",
        "actualCompletionDate": null,
        "applicationDetails": "Solicitud de cambio de carrera",
        "applicationResolution": null,
        "rejectionReasonId": null,
        "currentStageTrackingId": 1,
        "proceduresIdProcedure": 1,
        "applicantUserId": 3,
        "priority": "normal"
    }
]
```

---

### 8.2 Obtener Solicitud por ID

```
GET {{base_url}}/api/v1/applications/1
```

**Permiso:** `SOL_VER`

---

### 8.3 Solicitudes de un Usuario

```
GET {{base_url}}/api/v1/applications/user/3
```

**Permiso:** `SOL_VER`

---

### 8.4 Filtrar por Prioridad

```
GET {{base_url}}/api/v1/applications/priority/alta
```

**Permiso:** `SOL_LISTAR`  
**Valores válidos:** `baja`, `normal`, `alta`, `urgente`

---

### 8.5 Crear Solicitud

```
POST {{base_url}}/api/v1/applications
```

**Permiso:** `SOL_CREAR`  
**Body (JSON):**
```json
{
    "applicationCode": "SOL-2026-002",
    "estimatedCompletionDate": "2026-04-15",
    "applicationDetails": "Solicitud de retiro voluntario del semestre",
    "currentStageTrackingId": 1,
    "proceduresIdProcedure": 2,
    "applicantUserId": 5,
    "priority": "normal"
}
```

**Validaciones:**
- `applicationCode`: Obligatorio, máx 100 chars
- `estimatedCompletionDate`: Obligatorio, fecha futura o presente
- `applicationDetails`: Opcional, máx 5000 chars
- `currentStageTrackingId`: Obligatorio
- `proceduresIdProcedure`: Obligatorio
- `applicantUserId`: Obligatorio
- `priority`: Opcional, uno de: `baja`, `normal`, `alta`, `urgente`

---

### 8.6 Actualizar Solicitud

```
PUT {{base_url}}/api/v1/applications/1
```

**Permiso:** `SOL_MODIFICAR`  
**Body (JSON):**
```json
{
    "idApplication": 1,
    "applicationCode": "SOL-2026-002",
    "estimatedCompletionDate": "2026-04-20",
    "applicationDetails": "Solicitud de retiro voluntario - actualizada",
    "applicationResolution": null,
    "rejectionReasonId": null,
    "currentStageTrackingId": 2,
    "proceduresIdProcedure": 2,
    "applicantUserId": 5,
    "priority": "alta"
}
```

---

### 8.7 Eliminar Solicitud

```
DELETE {{base_url}}/api/v1/applications/1
```

**Permiso:** `SOL_ELIMINAR`  
**Respuesta:** `204 No Content`

---

### 8.8 Resolver Solicitud

```
PATCH {{base_url}}/api/v1/applications/1/resolve?resolution=Aprobado%20por%20el%20consejo
```

**Permiso:** `SOL_RESOLVER`  
**Params:** `resolution` (texto de resolución)

---

### 8.9 Rechazar Solicitud

```
PATCH {{base_url}}/api/v1/applications/1/reject?rejectionReasonId=2
```

**Permiso:** `SOL_RECHAZAR`  
**Params:** `rejectionReasonId` (ID del motivo de rechazo)

---

## 9. Trámites (ProceduresController)

**Base Path:** `/api/v1/procedures`  
**Autenticación:** `isAuthenticated()`

---

### 9.1 Listar Trámites Activos

```
GET {{base_url}}/api/v1/procedures
```

**Permiso:** `TRAMITE_LISTAR`

**Respuesta 200 OK:**
```json
[
    {
        "idProcedure": 1,
        "procedurename": "Cambio de Carrera",
        "description": "Trámite para cambiar de carrera académica",
        "maxduration": 30,
        "workflowidworkflow": 1,
        "status": "activo",
        "createdAt": "2026-01-05T10:00:00"
    }
]
```

---

### 9.2 Listar Todos (Incluyendo Inactivos)

```
GET {{base_url}}/api/v1/procedures/all
```

**Permiso:** `TRAMITE_LISTAR`

---

### 9.3 Obtener Trámite por ID

```
GET {{base_url}}/api/v1/procedures/1
```

**Permiso:** `TRAMITE_VER`

---

### 9.4 Buscar por Código

```
GET {{base_url}}/api/v1/procedures/code/CAM-CARR-001
```

**Permiso:** `TRAMITE_VER`

---

### 9.5 Filtrar por Flujo de Trabajo

```
GET {{base_url}}/api/v1/procedures/workflow/1
```

**Permiso:** `TRAMITE_LISTAR`

---

### 9.6 Crear Trámite

```
POST {{base_url}}/api/v1/procedures
```

**Permiso:** `TRAMITE_CREAR`  
**Body (JSON):**
```json
{
    "procedurename": "Retiro de Semestre",
    "description": "Proceso para retiro voluntario de un semestre",
    "maxduration": 15,
    "workflowidworkflow": 2
}
```

**Validaciones:**
- `procedurename`: Obligatorio, 3-100 chars
- `description`: Opcional, máx 500 chars
- `maxduration`: Obligatorio, 1-365
- `workflowidworkflow`: Obligatorio

---

### 9.7 Actualizar Trámite

```
PUT {{base_url}}/api/v1/procedures/1
```

**Permiso:** `TRAMITE_MODIFICAR`  
**Body (JSON):**
```json
{
    "idProcedure": 1,
    "procedurename": "Cambio de Carrera v2",
    "description": "Trámite actualizado",
    "maxduration": 45,
    "workflowidworkflow": 1,
    "status": "activo"
}
```

---

### 9.8 Activar Trámite

```
POST {{base_url}}/api/v1/procedures/1/activate
```

**Permiso:** `TRAMITE_ACTIVAR`

---

### 9.9 Desactivar Trámite

```
POST {{base_url}}/api/v1/procedures/1/deactivate
```

**Permiso:** `TRAMITE_DESACTIVAR`

---

### 9.10 Verificar si Requiere 2FA

```
GET {{base_url}}/api/v1/procedures/1/requires-2fa
```

**Permiso:** `TRAMITE_VER`

**Respuesta 200 OK:**
```json
{
    "requires2FA": true
}
```

---

### 9.11 Eliminar Trámite

```
DELETE {{base_url}}/api/v1/procedures/1
```

**Permiso:** `TRAMITE_ELIMINAR`  
**Respuesta:** `204 No Content`

---

## 10. Documentos Adjuntos (AttachedDocumentsController)

**Base Path:** `/api/v1/attached-documents`  
**Autenticación:** `isAuthenticated()`  
**Integración:** Google Drive (upload/download)

---

### 10.1 Listar Documentos Adjuntos

```
GET {{base_url}}/api/v1/attached-documents
```

**Permiso:** `DOCADJ_LISTAR`

**Respuesta 200 OK:**
```json
[
    {
        "idAttachedDocument": 1,
        "applicationsIdApplication": 1,
        "requirementId": 1,
        "fileName": "cedula_identidad.pdf",
        "filePath": "drive://1abc2def3ghi",
        "fileSizeBytes": 245760,
        "mimeType": "application/pdf",
        "uploadDate": "2026-02-10T10:30:00",
        "uploadedByUserId": 3
    }
]
```

---

### 10.2 Obtener Metadatos de Documento

```
GET {{base_url}}/api/v1/attached-documents/1
```

**Permiso:** `DOCADJ_VER`

---

### 10.3 Subir Archivo

```
POST {{base_url}}/api/v1/attached-documents/upload
```

**Permiso:** `DOCADJ_CREAR`  
**Content-Type:** `multipart/form-data`

**Body (form-data):**

| Key | Type | Value |
|---|---|---|
| `file` | File | *(seleccionar archivo)* |
| `applicationId` | Text | `1` |
| `requirementId` | Text | `1` |
| `uploadedByUserId` | Text | `3` |

> El archivo se sube a Google Drive y se guarda la referencia en BD.

---

### 10.4 Descargar Archivo

```
GET {{base_url}}/api/v1/attached-documents/1/download
```

**Permiso:** `DOCADJ_VER`  
**Respuesta:** Archivo binario (byte[]) con headers de descarga

> En Postman: clic en "Save Response" → "Save to a file"

---

### 10.5 Actualizar Metadatos

```
PUT {{base_url}}/api/v1/attached-documents/1
```

**Permiso:** `DOCADJ_MODIFICAR`  
**Body (JSON):**
```json
{
    "idAttachedDocument": 1,
    "applicationsIdApplication": 1,
    "requirementId": 2,
    "fileName": "cedula_identidad_v2.pdf",
    "filePath": "drive://1abc2def3ghi",
    "fileSizeBytes": 250000,
    "mimeType": "application/pdf",
    "uploadedByUserId": 3
}
```

---

### 10.6 Eliminar Documento

```
DELETE {{base_url}}/api/v1/attached-documents/1
```

**Permiso:** `DOCADJ_ELIMINAR`  
**Respuesta:** `204 No Content`  
**Nota:** Elimina tanto el registro en BD como el archivo en Google Drive.

---

## 11. Refresh Tokens (RefreshTokenController)

**Base Path:** `/api/v1/refresh-tokens`  
**Autenticación:** `isAuthenticated()`

---

### 11.1 Listar Refresh Tokens

```
GET {{base_url}}/api/v1/refresh-tokens
```

**Permiso:** `TOKEN_LISTAR`

**Respuesta 200 OK:**
```json
[
    {
        "id": 1,
        "userId": 1,
        "expiresAt": "2026-03-16T08:00:00",
        "createdAt": "2026-02-14T08:00:00",
        "revoked": false,
        "deviceInfo": "PostmanRuntime/7.36.0"
    }
]
```

> **Nota:** El valor del token JWT nunca se incluye en la respuesta.

---

### 11.2 Obtener Token por ID

```
GET {{base_url}}/api/v1/refresh-tokens/1
```

**Permiso:** `TOKEN_VER`

---

### 11.3 Revocar Token

```
DELETE {{base_url}}/api/v1/refresh-tokens/1
```

**Permiso:** `TOKEN_ELIMINAR`  
**Respuesta:** `204 No Content`

---

## 12. Email (EmailController)

**Base Path:** `/api/v1/mail`  
**Autenticación:** `isAuthenticated()`

---

### 12.1 Enviar Email de Texto Plano

```
POST {{base_url}}/api/v1/mail/plain
```

**Permiso:** `EMAIL_ENVIAR`  
**Body (JSON):**
```json
{
    "to": "estudiante@uteq.edu.ec",
    "subject": "Notificación de Trámite",
    "body": "Su solicitud SOL-2026-001 ha sido aprobada."
}
```

**Validaciones:**
- `to`: Obligatorio, formato email
- `subject`: Obligatorio
- `body`: Obligatorio

**Respuesta 200 OK:**
```json
{
    "ok": true,
    "message": "Correo enviado exitosamente",
    "timestamp": "2026-02-16T10:00:00"
}
```

---

### 12.2 Enviar Email HTML

```
POST {{base_url}}/api/v1/mail/html
```

**Permiso:** `EMAIL_ENVIAR`  
**Body (JSON):**
```json
{
    "to": "estudiante@uteq.edu.ec",
    "subject": "Bienvenido al SGTE",
    "body": "<h1>Bienvenido</h1><p>Su cuenta ha sido creada exitosamente.</p>"
}
```

---

## 13. Calendario Académico (AcademicCalendarController)

**Base Path:** `/api/v1/academic-calendar`  
**Autenticación:** `isAuthenticated()`  
**Backend:** Stored Procedures

---

### 13.1 Listar Calendarios

```
GET {{base_url}}/api/v1/academic-calendar
```

**Permiso:** `CAL_LISTAR`  
**Params opcionales:** `onlyActive=true` (filtrar solo activos)

**Respuesta 200 OK:**
```json
[
    {
        "idacademiccalendar": 1,
        "calendarname": "Período 2026-1",
        "academicperiod": "2026-1",
        "startdate": "2026-04-01",
        "enddate": "2026-08-31",
        "active": true
    }
]
```

---

### 13.2 Crear Calendario

```
POST {{base_url}}/api/v1/academic-calendar
```

**Permiso:** `CAL_CREAR`  
**Body (JSON):**
```json
{
    "calendarname": "Período 2026-2",
    "academicperiod": "2026-2",
    "startdate": "2026-10-01",
    "enddate": "2027-02-28",
    "active": true
}
```

**Validaciones:**
- `calendarname`: Obligatorio, 3-100 chars
- `academicperiod`: Obligatorio
- `startdate`: Obligatorio
- `enddate`: Obligatorio

---

### 13.3 Actualizar Calendario

```
PUT {{base_url}}/api/v1/academic-calendar/1
```

**Permiso:** `CAL_MODIFICAR`  
**Body (JSON):**
```json
{
    "idacademiccalendar": 1,
    "calendarname": "Período 2026-1 (Actualizado)",
    "academicperiod": "2026-1",
    "startdate": "2026-04-01",
    "enddate": "2026-09-15",
    "active": true
}
```

---

### 13.4 Eliminar Calendario

```
DELETE {{base_url}}/api/v1/academic-calendar/1
```

**Permiso:** `CAL_ELIMINAR`

---

## 14. Carreras (CareerController)

**Base Path:** `/api/v1/careers`  
**Autenticación:** `isAuthenticated()`  
**Backend:** Stored Procedures

---

### 14.1 Listar Carreras

```
GET {{base_url}}/api/v1/careers
```

**Permiso:** `CARRERA_LISTAR`  
**Params opcionales:** `facultyid=1` (filtrar por facultad)

**Respuesta 200 OK:**
```json
[
    {
        "idcareer": 1,
        "careername": "Ingeniería en Sistemas",
        "careercode": "ISI",
        "facultiesidfaculty": 1,
        "facultyname": "Ciencias de la Ingeniería",
        "coordinatoriduser": 2
    }
]
```

---

### 14.2 Crear Carrera

```
POST {{base_url}}/api/v1/careers
```

**Permiso:** `CARRERA_CREAR`  
**Body (JSON):**
```json
{
    "careername": "Ingeniería Industrial",
    "careercode": "IIN",
    "facultiesidfaculty": 1,
    "coordinatoriduser": 4
}
```

**Validaciones:**
- `careername`: Obligatorio, 3-150 chars
- `careercode`: Obligatorio, 2-20 chars
- `facultiesidfaculty`: Obligatorio

---

### 14.3 Actualizar Carrera

```
PUT {{base_url}}/api/v1/careers/1
```

**Permiso:** `CARRERA_MODIFICAR`  
**Body (JSON):**
```json
{
    "idcareer": 1,
    "careername": "Ingeniería en Sistemas Computacionales",
    "careercode": "ISC",
    "facultiesidfaculty": 1,
    "coordinatoriduser": 2
}
```

---

### 14.4 Eliminar Carrera

```
DELETE {{base_url}}/api/v1/careers/1
```

**Permiso:** `CARRERA_ELIMINAR`

---

## 15. Configuración (ConfigurationController)

**Base Path:** `/api/v1/configuration`  
**Autenticación:** `isAuthenticated()`

---

### 15.1 Listar Configuraciones

```
GET {{base_url}}/api/v1/configuration
```

**Permiso:** `CONFIG_LISTAR`

**Respuesta 200 OK:**
```json
[
    {
        "idconfiguration": 1,
        "profilepicturepath": "/storage/profiles/1.jpg",
        "signaturepath": "/storage/signatures/1.png",
        "enable_sms": false,
        "enable_email": true,
        "enable_whatsapp": false,
        "notificationfrequency": "inmediata"
    }
]
```

---

### 15.2 Crear Configuración

```
POST {{base_url}}/api/v1/configuration
```

**Permiso:** `CONFIG_CREAR`  
**Body (JSON):**
```json
{
    "profilepicturepath": "/storage/profiles/default.jpg",
    "signaturepath": null,
    "enable_sms": false,
    "enable_email": true,
    "enable_whatsapp": false,
    "notificationfrequency": "diaria"
}
```

**Validaciones:**
- `notificationfrequency`: `diaria` | `semanal` | `mensual` | `inmediata`

---

### 15.3 Actualizar Configuración

```
PUT {{base_url}}/api/v1/configuration/1
```

**Permiso:** `CONFIG_MODIFICAR`  
**Body (JSON):**
```json
{
    "idconfiguration": 1,
    "profilepicturepath": "/storage/profiles/1_updated.jpg",
    "signaturepath": "/storage/signatures/1.png",
    "enable_sms": true,
    "enable_email": true,
    "enable_whatsapp": true,
    "notificationfrequency": "semanal"
}
```

---

### 15.4 Eliminar Configuración

```
DELETE {{base_url}}/api/v1/configuration/1
```

**Permiso:** `CONFIG_ELIMINAR`

---

## 16. Reglas de Plazo (DeadlineruleControllers)

**Base Path:** `/api/v1/deadlinerules`  
**Autenticación:** `isAuthenticated()`

---

### 16.1 Listar Reglas

```
GET {{base_url}}/api/v1/deadlinerules
```

**Permiso:** `REGLA_LISTAR`  
**Params opcionales:** `onlyActive=true`

**Respuesta 200 OK:**
```json
[
    {
        "iddeadlinerule": 1,
        "rulename": "Plazo estándar",
        "procedurecategory": "general",
        "basedeadlinedays": 30,
        "warningdaysbefore": 5,
        "active": true
    }
]
```

---

### 16.2 Crear Regla

```
POST {{base_url}}/api/v1/deadlinerules
```

**Permiso:** `REGLA_CREAR`  
**Body (JSON):**
```json
{
    "rulename": "Plazo urgente",
    "procedurecategory": "urgente",
    "basedeadlinedays": 5,
    "warningdaysbefore": 1,
    "active": true
}
```

---

### 16.3 Actualizar Regla

```
PUT {{base_url}}/api/v1/deadlinerules/1
```

**Permiso:** `REGLA_MODIFICAR`  
**Body (JSON):**
```json
{
    "iddeadlinerule": 1,
    "rulename": "Plazo estándar actualizado",
    "procedurecategory": "general",
    "basedeadlinedays": 25,
    "warningdaysbefore": 7,
    "active": true
}
```

---

### 16.4 Eliminar Regla

```
DELETE {{base_url}}/api/v1/deadlinerules/1
```

**Permiso:** `REGLA_ELIMINAR`

---

## 17. Plantillas de Documentos (DocumentTemplateController)

**Base Path:** `/api/v1/document-templates`  
**Autenticación:** `isAuthenticated()`

---

### 17.1 Listar Plantillas

```
GET {{base_url}}/api/v1/document-templates
```

**Permiso:** `PLANTILLA_LISTAR`  
**Params opcionales:** `onlyActive=true`

**Respuesta 200 OK:**
```json
[
    {
        "idtemplate": 1,
        "templatename": "Solicitud de Cambio de Carrera",
        "templatecode": "TMPL-CC-001",
        "templatepath": "/templates/cambio_carrera.docx",
        "documenttype": "solicitud",
        "version": "1.0",
        "requiressignature": true,
        "active": true,
        "createdat": "2026-01-05T10:00:00",
        "updatedat": "2026-01-05T10:00:00"
    }
]
```

---

### 17.2 Crear Plantilla

```
POST {{base_url}}/api/v1/document-templates
```

**Permiso:** `PLANTILLA_CREAR`  
**Body (JSON):**
```json
{
    "templatename": "Certificado de Matrícula",
    "templatecode": "TMPL-MAT-001",
    "templatepath": "/templates/certificado_matricula.docx",
    "documenttype": "certificado",
    "version": "1.0",
    "requiressignature": true,
    "active": true
}
```

**Validaciones:**
- `templatename`: Obligatorio, 3-100 chars
- `templatecode`: Obligatorio, 2-50 chars
- `documenttype`: Obligatorio

---

### 17.3 Actualizar Plantilla

```
PUT {{base_url}}/api/v1/document-templates/1
```

**Permiso:** `PLANTILLA_MODIFICAR`  
**Body (JSON):**
```json
{
    "idtemplate": 1,
    "templatename": "Solicitud de Cambio de Carrera v2",
    "templatecode": "TMPL-CC-001",
    "templatepath": "/templates/cambio_carrera_v2.docx",
    "documenttype": "solicitud",
    "version": "2.0",
    "requiressignature": true,
    "active": true
}
```

---

### 17.4 Eliminar Plantilla

```
DELETE {{base_url}}/api/v1/document-templates/1
```

**Permiso:** `PLANTILLA_ELIMINAR`

---

## 18. Facultades (FacultyController)

**Base Path:** `/api/v1/faculty`  
**Autenticación:** `isAuthenticated()`

---

### 18.1 Listar Facultades

```
GET {{base_url}}/api/v1/faculty
```

**Permiso:** `FACULTAD_LISTAR`

**Respuesta 200 OK:**
```json
[
    {
        "idfaculty": 1,
        "facultyname": "Ciencias de la Ingeniería",
        "facultycode": "FCI",
        "deaniduser": 2
    }
]
```

---

### 18.2 Crear Facultad

```
POST {{base_url}}/api/v1/faculty
```

**Permiso:** `FACULTAD_CREAR`  
**Body (JSON):**
```json
{
    "facultyname": "Ciencias Empresariales",
    "facultycode": "FCE",
    "deaniduser": 6
}
```

**Validaciones:**
- `facultyname`: Obligatorio, 3-150 chars
- `facultycode`: Obligatorio, 2-20 chars

---

### 18.3 Actualizar Facultad

```
PUT {{base_url}}/api/v1/faculty/1
```

**Permiso:** `FACULTAD_MODIFICAR`  
**Body (JSON):**
```json
{
    "idfaculty": 1,
    "facultyname": "Ciencias de la Ingeniería y Tecnología",
    "facultycode": "FCIT",
    "deaniduser": 2
}
```

---

### 18.4 Eliminar Facultad

```
DELETE {{base_url}}/api/v1/faculty/1
```

**Permiso:** `FACULTAD_ELIMINAR`

---

## 19. Permisos (PermissionController)

**Base Path:** `/api/v1/permissions`  
**Autenticación:** `isAuthenticated()`

---

### 19.1 Listar Permisos

```
GET {{base_url}}/api/v1/permissions
```

**Permiso:** `PERMISO_LISTAR`

**Respuesta 200 OK:**
```json
[
    {
        "idpermission": 1,
        "code": "USUARIO_LISTAR",
        "description": "Listar usuarios del sistema"
    }
]
```

---

### 19.2 Crear Permiso

```
POST {{base_url}}/api/v1/permissions
```

**Permiso:** `PERMISO_CREAR`  
**Body (JSON):**
```json
{
    "code": "REPORTE_GENERAR",
    "description": "Generar reportes del sistema"
}
```

**Validaciones:**
- `code`: Obligatorio, solo mayúsculas y guiones bajos (`^[A-Z_]+$`), 3-50 chars
- `description`: Opcional, máx 255 chars

---

### 19.3 Actualizar Permiso

```
PUT {{base_url}}/api/v1/permissions/1
```

**Permiso:** `PERMISO_MODIFICAR`  
**Body (JSON):**
```json
{
    "idpermission": 1,
    "code": "USUARIO_LISTAR",
    "description": "Listar todos los usuarios del sistema"
}
```

---

### 19.4 Eliminar Permiso

```
DELETE {{base_url}}/api/v1/permissions/1
```

**Permiso:** `PERMISO_ELIMINAR`

---

## 20. Etapas de Procesamiento (ProcessingStageController)

**Base Path:** `/api/v1/processing-stages`  
**Autenticación:** `isAuthenticated()`

---

### 20.1 Listar Etapas

```
GET {{base_url}}/api/v1/processing-stages
```

**Permiso:** `ETAPA_LISTAR`

**Respuesta 200 OK:**
```json
[
    {
        "idprocessingstage": 1,
        "stagename": "Recepción de Documentos",
        "stagecode": "RECEPCION",
        "stagedescription": "Etapa inicial de recepción de solicitud",
        "stageorder": 1,
        "requiresapproval": false,
        "maxdurationdays": 3
    }
]
```

---

### 20.2 Crear Etapa

```
POST {{base_url}}/api/v1/processing-stages
```

**Permiso:** `ETAPA_CREAR`  
**Body (JSON):**
```json
{
    "stagename": "Revisión Académica",
    "stagecode": "REV_ACAD",
    "stagedescription": "Revisión por parte del coordinador de carrera",
    "stageorder": 2,
    "requiresapproval": true,
    "maxdurationdays": 5
}
```

**Validaciones:**
- `stagename`: Obligatorio, 3-100 chars
- `stagecode`: Obligatorio, 2-50 chars
- `stagedescription`: Opcional, máx 500 chars
- `stageorder`: Obligatorio, mín 1
- `maxdurationdays`: Opcional, mín 1

---

### 20.3 Actualizar Etapa

```
PUT {{base_url}}/api/v1/processing-stages/1
```

**Permiso:** `ETAPA_MODIFICAR`  
**Body (JSON):**
```json
{
    "idprocessingstage": 1,
    "stagename": "Recepción de Documentos v2",
    "stagecode": "RECEPCION",
    "stagedescription": "Etapa inicial actualizada",
    "stageorder": 1,
    "requiresapproval": false,
    "maxdurationdays": 2
}
```

---

### 20.4 Eliminar Etapa

```
DELETE {{base_url}}/api/v1/processing-stages/1
```

**Permiso:** `ETAPA_ELIMINAR`

---

## 21. Motivos de Rechazo (RejectReasonController)

**Base Path:** `/api/v1/reject-reason`  
**Autenticación:** `isAuthenticated()`

---

### 21.1 Listar Motivos

```
GET {{base_url}}/api/v1/reject-reason
```

**Permiso:** `RECHAZO_LISTAR`  
**Params opcionales:** `onlyActive=true`

**Respuesta 200 OK:**
```json
[
    {
        "idrejectionreason": 1,
        "reasoncode": "DOC_INCOMPLETO",
        "reasondescription": "Documentación incompleta o ilegible",
        "category": "documentacion",
        "active": true
    }
]
```

---

### 21.2 Crear Motivo

```
POST {{base_url}}/api/v1/reject-reason
```

**Permiso:** `RECHAZO_CREAR`  
**Body (JSON):**
```json
{
    "reasoncode": "PLAZO_VENCIDO",
    "reasondescription": "El plazo para presentar la solicitud ha vencido",
    "category": "plazo",
    "active": true
}
```

**Validaciones:**
- `reasoncode`: Obligatorio, 2-50 chars
- `reasondescription`: Obligatorio, 5-500 chars
- `category`: Obligatorio

---

### 21.3 Actualizar Motivo

```
PUT {{base_url}}/api/v1/reject-reason/1
```

**Permiso:** `RECHAZO_MODIFICAR`  
**Body (JSON):**
```json
{
    "idrejectionreason": 1,
    "reasoncode": "DOC_INCOMPLETO",
    "reasondescription": "Documentación incompleta, ilegible o no válida",
    "category": "documentacion",
    "active": true
}
```

---

### 21.4 Eliminar Motivo

```
DELETE {{base_url}}/api/v1/reject-reason/1
```

**Permiso:** `RECHAZO_ELIMINAR`

---

## 22. Requisitos (RequirementsController)

**Base Path:** `/api/v1/requirements`  
**Autenticación:** `isAuthenticated()`

---

### 22.1 Listar Requisitos

```
GET {{base_url}}/api/v1/requirements
```

**Permiso:** `REQUISITO_LISTAR`

**Respuesta 200 OK:**
```json
[
    {
        "id": 1,
        "proceduresIdProcedure": 1,
        "requirementName": "Cédula de Identidad",
        "requirementDescription": "Copia a color de cédula de identidad vigente",
        "requirementType": "documento",
        "isMandatory": true,
        "displayOrder": 1
    }
]
```

---

### 22.2 Obtener Requisito por ID

```
GET {{base_url}}/api/v1/requirements/1
```

**Permiso:** `REQUISITO_VER`

---

### 22.3 Crear Requisito

```
POST {{base_url}}/api/v1/requirements
```

**Permiso:** `REQUISITO_CREAR`  
**Body (JSON):**
```json
{
    "proceduresIdProcedure": 1,
    "requirementName": "Record Académico",
    "requirementDescription": "Record académico actualizado del semestre en curso",
    "requirementType": "documento",
    "isMandatory": true,
    "displayOrder": 2
}
```

---

### 22.4 Actualizar Requisito

```
PUT {{base_url}}/api/v1/requirements/1
```

**Permiso:** `REQUISITO_MODIFICAR`  
**Body (JSON):**
```json
{
    "id": 1,
    "proceduresIdProcedure": 1,
    "requirementName": "Cédula de Identidad (Original y Copia)",
    "requirementDescription": "Copia a color de cédula vigente + presentar original",
    "requirementType": "documento",
    "isMandatory": true,
    "displayOrder": 1
}
```

---

### 22.5 Eliminar Requisito

```
DELETE {{base_url}}/api/v1/requirements/1
```

**Permiso:** `REQUISITO_ELIMINAR`  
**Respuesta:** `204 No Content`

---

## 23. Estados (StatesController)

**Base Path:** `/api/v1/states`  
**Autenticación:** `isAuthenticated()`

---

### 23.1 Listar Estados

```
GET {{base_url}}/api/v1/states
```

**Permiso:** `ESTADO_LISTAR`  
**Params opcionales:** `category=solicitud` (filtrar por categoría)

**Respuesta 200 OK:**
```json
[
    {
        "idstate": 1,
        "statename": "Pendiente",
        "statedescription": "Solicitud pendiente de revisión",
        "statecategory": "solicitud"
    }
]
```

---

### 23.2 Crear Estado

```
POST {{base_url}}/api/v1/states
```

**Permiso:** `ESTADO_CREAR`  
**Body (JSON):**
```json
{
    "statename": "En Proceso",
    "statedescription": "La solicitud se encuentra en proceso de revisión",
    "statecategory": "solicitud"
}
```

**Validaciones:**
- `statename`: Obligatorio, 2-50 chars
- `statedescription`: Opcional, máx 255 chars
- `statecategory`: Obligatorio

---

### 23.3 Actualizar Estado

```
PUT {{base_url}}/api/v1/states/1
```

**Permiso:** `ESTADO_MODIFICAR`  
**Body (JSON):**
```json
{
    "idstate": 1,
    "statename": "Pendiente de Revisión",
    "statedescription": "Esperando revisión por parte del coordinador",
    "statecategory": "solicitud"
}
```

---

### 23.4 Eliminar Estado

```
DELETE {{base_url}}/api/v1/states/1
```

**Permiso:** `ESTADO_ELIMINAR`

---

## 24. Flujos de Trabajo (WorkFlowsController)

**Base Path:** `/api/v1/work-flows`  
**Autenticación:** `isAuthenticated()`

---

### 24.1 Listar Flujos

```
GET {{base_url}}/api/v1/work-flows
```

**Permiso:** `FLUJO_LISTAR`  
**Params opcionales:** `onlyActive=true`

**Respuesta 200 OK:**
```json
[
    {
        "idworkflow": 1,
        "workflowname": "Flujo Cambio de Carrera",
        "workflowdescription": "Flujo para procesar cambios de carrera",
        "createdat": "2026-01-05T10:00:00",
        "active": true
    }
]
```

---

### 24.2 Crear Flujo

```
POST {{base_url}}/api/v1/work-flows
```

**Permiso:** `FLUJO_CREAR`  
**Body (JSON):**
```json
{
    "workflowname": "Flujo Retiro de Semestre",
    "workflowdescription": "Proceso de retiro voluntario",
    "active": true
}
```

**Validaciones:**
- `workflowname`: Obligatorio, 3-100 chars
- `workflowdescription`: Opcional, máx 500 chars

---

### 24.3 Actualizar Flujo

```
PUT {{base_url}}/api/v1/work-flows/1
```

**Permiso:** `FLUJO_MODIFICAR`  
**Body (JSON):**
```json
{
    "idworkflow": 1,
    "workflowname": "Flujo Cambio de Carrera v2",
    "workflowdescription": "Flujo actualizado para cambios de carrera",
    "active": true
}
```

---

### 24.4 Eliminar Flujo

```
DELETE {{base_url}}/api/v1/work-flows/1
```

**Permiso:** `FLUJO_ELIMINAR`

---

## 25. Flujo-Etapas (WorkflowStagesController)

**Base Path:** `/api/v1/workflow-stages`  
**Autenticación:** `isAuthenticated()`

---

### 25.1 Listar Flujo-Etapas

```
GET {{base_url}}/api/v1/workflow-stages
```

**Permiso:** `FLUJOETAPA_LISTAR`

**Respuesta 200 OK:**
```json
[
    {
        "idWorkflowStage": 1,
        "workflowIdWorkflow": 1,
        "processingStageIdProcessingStage": 1,
        "sequenceOrder": 1,
        "isOptional": false
    }
]
```

---

### 25.2 Obtener por ID

```
GET {{base_url}}/api/v1/workflow-stages/1
```

**Permiso:** `FLUJOETAPA_VER`

---

### 25.3 Crear Flujo-Etapa

```
POST {{base_url}}/api/v1/workflow-stages
```

**Permiso:** `FLUJOETAPA_CREAR`  
**Body (JSON):**
```json
{
    "workflowIdWorkflow": 1,
    "processingStageIdProcessingStage": 2,
    "sequenceOrder": 2,
    "isOptional": false
}
```

---

### 25.4 Actualizar Flujo-Etapa

```
PUT {{base_url}}/api/v1/workflow-stages/1
```

**Permiso:** `FLUJOETAPA_MODIFICAR`  
**Body (JSON):**
```json
{
    "idWorkflowStage": 1,
    "workflowIdWorkflow": 1,
    "processingStageIdProcessingStage": 1,
    "sequenceOrder": 1,
    "isOptional": true
}
```

---

### 25.5 Eliminar Flujo-Etapa

```
DELETE {{base_url}}/api/v1/workflow-stages/1
```

**Permiso:** `FLUJOETAPA_ELIMINAR`  
**Respuesta:** `204 No Content`

---

## 26. Seguimiento de Etapas (StageTrackingController)

**Base Path:** `/api/v1/stage-tracking`  
**Autenticación:** `isAuthenticated()`

---

### 26.1 Listar Seguimientos

```
GET {{base_url}}/api/v1/stage-tracking
```

**Permiso:** `SEGUIMIENTO_LISTAR`

**Respuesta 200 OK:**
```json
[
    {
        "idStageTracking": 1,
        "stateIdState": 1,
        "processingStageIdProcessingStage": 1,
        "enteredAt": "2026-02-10T09:00:00",
        "completedAt": null,
        "assignedToUserId": 2,
        "notes": "Documentos recibidos"
    }
]
```

---

### 26.2 Obtener por ID

```
GET {{base_url}}/api/v1/stage-tracking/1
```

**Permiso:** `SEGUIMIENTO_VER`

---

### 26.3 Crear Seguimiento

```
POST {{base_url}}/api/v1/stage-tracking
```

**Permiso:** `SEGUIMIENTO_CREAR`  
**Body (JSON):**
```json
{
    "stateIdState": 1,
    "processingStageIdProcessingStage": 2,
    "assignedToUserId": 2,
    "notes": "Asignado para revisión académica"
}
```

---

### 26.4 Actualizar Seguimiento

```
PUT {{base_url}}/api/v1/stage-tracking/1
```

**Permiso:** `SEGUIMIENTO_MODIFICAR`  
**Body (JSON):**
```json
{
    "idStageTracking": 1,
    "stateIdState": 2,
    "processingStageIdProcessingStage": 1,
    "assignedToUserId": 2,
    "notes": "Revisión completada"
}
```

---

### 26.5 Eliminar Seguimiento

```
DELETE {{base_url}}/api/v1/stage-tracking/1
```

**Permiso:** `SEGUIMIENTO_ELIMINAR`  
**Respuesta:** `204 No Content`

---

## 27. Sesiones (SessionTokenController)

**Base Path:** `/api/v1/session-tokens`  
**Autenticación:** `isAuthenticated()`

---

### 27.1 Listar Sesiones

```
GET {{base_url}}/api/v1/session-tokens
```

**Permiso:** `SESION_LISTAR`

**Respuesta 200 OK:**
```json
[
    {
        "idSession": 1,
        "userId": 1,
        "ipAddress": "192.168.1.100",
        "userAgent": "PostmanRuntime/7.36.0",
        "createdAt": "2026-02-16T08:00:00",
        "expiresAt": "2026-02-17T08:00:00",
        "lastActivity": "2026-02-16T10:30:00"
    }
]
```

---

### 27.2 Obtener Sesión por ID

```
GET {{base_url}}/api/v1/session-tokens/1
```

**Permiso:** `SESION_VER`

---

### 27.3 Crear Sesión

```
POST {{base_url}}/api/v1/session-tokens
```

**Permiso:** `SESION_CREAR`  
**Body (JSON):**
```json
{
    "userId": 3,
    "token": "session-abc-123",
    "ipAddress": "192.168.1.105",
    "userAgent": "Mozilla/5.0"
}
```

---

### 27.4 Actualizar Sesión

```
PUT {{base_url}}/api/v1/session-tokens/1
```

**Permiso:** `SESION_MODIFICAR`  
**Body (JSON):**
```json
{
    "idSession": 1,
    "userId": 3,
    "token": "session-abc-123-updated",
    "ipAddress": "192.168.1.105",
    "userAgent": "Mozilla/5.0"
}
```

---

### 27.5 Eliminar Sesión

```
DELETE {{base_url}}/api/v1/session-tokens/1
```

**Permiso:** `SESION_ELIMINAR`  
**Respuesta:** `204 No Content`

---

## 28. Tipos de Notificación (NotificationTypeController)

**Base Path:** `/api/v1/notification-types`  
**Autenticación:** `isAuthenticated()`

---

### 28.1 Listar Tipos

```
GET {{base_url}}/api/v1/notification-types
```

**Permiso:** `TIPNOTIF_LISTAR`

**Respuesta 200 OK:**
```json
[
    {
        "idNotificationType": 1,
        "nameTypeNotification": "Solicitud Aprobada",
        "templateCode": "TMPL_SOL_APROBADA",
        "priorityLevel": "alta"
    }
]
```

---

### 28.2 Obtener Tipo por ID

```
GET {{base_url}}/api/v1/notification-types/1
```

**Permiso:** `TIPNOTIF_VER`

---

### 28.3 Crear Tipo

```
POST {{base_url}}/api/v1/notification-types
```

**Permiso:** `TIPNOTIF_CREAR`  
**Body (JSON):**
```json
{
    "nameTypeNotification": "Solicitud Rechazada",
    "templateCode": "TMPL_SOL_RECHAZADA",
    "priorityLevel": "alta"
}
```

---

### 28.4 Actualizar Tipo

```
PUT {{base_url}}/api/v1/notification-types/1
```

**Permiso:** `TIPNOTIF_MODIFICAR`  
**Body (JSON):**
```json
{
    "idNotificationType": 1,
    "nameTypeNotification": "Solicitud Aprobada Actualizada",
    "templateCode": "TMPL_SOL_APROBADA_V2",
    "priorityLevel": "media"
}
```

---

### 28.5 Eliminar Tipo

```
DELETE {{base_url}}/api/v1/notification-types/1
```

**Permiso:** `TIPNOTIF_ELIMINAR`  
**Respuesta:** `204 No Content`

---

## 29. Notificaciones (NotificationController)

**Base Path:** `/api/v1/notifications`  
**Autenticación:** `isAuthenticated()`

---

### 29.1 Listar Notificaciones

```
GET {{base_url}}/api/v1/notifications
```

**Permiso:** `NOTIF_LISTAR`

**Respuesta 200 OK:**
```json
[
    {
        "idNotification": 1,
        "notificationName": "Trámite aprobado",
        "message": "Su solicitud SOL-2026-001 ha sido aprobada",
        "notificationTypeIdNotificationType": 1,
        "applicationId": 1,
        "recipientUserId": 3,
        "sentAt": "2026-02-15T14:00:00",
        "deliveryStatus": "enviado",
        "deliveryChannel": "email",
        "readAt": null,
        "errorMessage": null,
        "retryCount": 0
    }
]
```

---

### 29.2 Obtener Notificación por ID

```
GET {{base_url}}/api/v1/notifications/1
```

**Permiso:** `NOTIF_VER`

---

### 29.3 Crear Notificación

```
POST {{base_url}}/api/v1/notifications
```

**Permiso:** `NOTIF_CREAR`  
**Body (JSON):**
```json
{
    "notificationName": "Documentos pendientes",
    "message": "Falta adjuntar el record académico a su solicitud",
    "notificationTypeIdNotificationType": 2,
    "applicationId": 1,
    "recipientUserId": 3,
    "deliveryChannel": "email"
}
```

---

### 29.4 Actualizar Notificación

```
PUT {{base_url}}/api/v1/notifications/1
```

**Permiso:** `NOTIF_MODIFICAR`  
**Body (JSON):**
```json
{
    "idNotification": 1,
    "notificationName": "Trámite aprobado",
    "message": "Su solicitud SOL-2026-001 ha sido aprobada oficialmente",
    "notificationTypeIdNotificationType": 1,
    "applicationId": 1,
    "recipientUserId": 3,
    "deliveryStatus": "leido",
    "deliveryChannel": "email",
    "retryCount": 0
}
```

---

### 29.5 Eliminar Notificación

```
DELETE {{base_url}}/api/v1/notifications/1
```

**Permiso:** `NOTIF_ELIMINAR`  
**Respuesta:** `204 No Content`

---

## 30. Documentos Generados (DocumentsGeneratedController)

**Base Path:** `/api/v1/documents-generated`  
**Autenticación:** `isAuthenticated()`

---

### 30.1 Listar Documentos Generados

```
GET {{base_url}}/api/v1/documents-generated
```

**Permiso:** `DOCGEN_LISTAR`

**Respuesta 200 OK:**
```json
[
    {
        "idDocumentGenerated": 1,
        "applicationsIdApplication": 1,
        "templateId": 1,
        "documentType": "solicitud",
        "documentPath": "/generated/SOL-2026-001.pdf",
        "generatedAt": "2026-02-10T11:00:00",
        "generatedByUserId": 2,
        "digitalSignatureId": 1,
        "signatureTimestamp": "2026-02-10T11:30:00"
    }
]
```

---

### 30.2 Obtener por ID

```
GET {{base_url}}/api/v1/documents-generated/1
```

**Permiso:** `DOCGEN_VER`

---

### 30.3 Crear Documento Generado

```
POST {{base_url}}/api/v1/documents-generated
```

**Permiso:** `DOCGEN_CREAR`  
**Body (JSON):**
```json
{
    "applicationsIdApplication": 1,
    "templateId": 1,
    "documentType": "certificado",
    "documentPath": "/generated/CERT-2026-001.pdf",
    "generatedByUserId": 2,
    "digitalSignatureId": null
}
```

---

### 30.4 Actualizar Documento Generado

```
PUT {{base_url}}/api/v1/documents-generated/1
```

**Permiso:** `DOCGEN_MODIFICAR`  
**Body (JSON):**
```json
{
    "idDocumentGenerated": 1,
    "applicationsIdApplication": 1,
    "templateId": 1,
    "documentType": "solicitud",
    "documentPath": "/generated/SOL-2026-001-v2.pdf",
    "generatedByUserId": 2,
    "digitalSignatureId": 1
}
```

---

### 30.5 Eliminar Documento Generado

```
DELETE {{base_url}}/api/v1/documents-generated/1
```

**Permiso:** `DOCGEN_ELIMINAR`  
**Respuesta:** `204 No Content`

---

## 31. Firmas Digitales (DigitalSignaturesController)

**Base Path:** `/api/v1/digital-signatures`  
**Autenticación:** `isAuthenticated()`

---

### 31.1 Listar Firmas

```
GET {{base_url}}/api/v1/digital-signatures
```

**Permiso:** `FIRMA_LISTAR`

**Respuesta 200 OK:**
```json
[
    {
        "idDigitalSignature": 1,
        "userIdUser": 2,
        "certificatePath": "/certs/user2.p12",
        "certificateSerial": "ABC123DEF456",
        "issuer": "SGTE CA",
        "validFrom": "2026-01-01",
        "validUntil": "2027-01-01",
        "signatureAlgorithm": "SHA256withRSA",
        "active": true,
        "createdAt": "2026-01-05T10:00:00"
    }
]
```

---

### 31.2 Obtener Firma por ID

```
GET {{base_url}}/api/v1/digital-signatures/1
```

**Permiso:** `FIRMA_VER`

---

### 31.3 Registrar Firma Digital

```
POST {{base_url}}/api/v1/digital-signatures
```

**Permiso:** `FIRMA_CREAR`  
**Body (JSON):**
```json
{
    "userIdUser": 4,
    "certificatePath": "/certs/user4.p12",
    "certificateSerial": "XYZ789GHI012",
    "issuer": "SGTE CA",
    "validFrom": "2026-02-01",
    "validUntil": "2027-02-01",
    "signatureAlgorithm": "SHA256withRSA",
    "active": true
}
```

---

### 31.4 Actualizar Firma

```
PUT {{base_url}}/api/v1/digital-signatures/1
```

**Permiso:** `FIRMA_MODIFICAR`  
**Body (JSON):**
```json
{
    "idDigitalSignature": 1,
    "userIdUser": 2,
    "certificatePath": "/certs/user2_renewed.p12",
    "certificateSerial": "ABC123DEF456",
    "issuer": "SGTE CA v2",
    "validFrom": "2026-01-01",
    "validUntil": "2028-01-01",
    "signatureAlgorithm": "SHA256withRSA",
    "active": true
}
```

---

### 31.5 Eliminar Firma

```
DELETE {{base_url}}/api/v1/digital-signatures/1
```

**Permiso:** `FIRMA_ELIMINAR`  
**Respuesta:** `204 No Content`

---

## 32. Historial de Etapas (ApplicationStageHistoryController)

**Base Path:** `/api/v1/application-stage-history`  
**Autenticación:** `isAuthenticated()`

---

### 32.1 Listar Historial

```
GET {{base_url}}/api/v1/application-stage-history
```

**Permiso:** `HIST_LISTAR`

**Respuesta 200 OK:**
```json
[
    {
        "idHistory": 1,
        "applicationIdApplication": 1,
        "stageTrackingId": 1,
        "enteredAt": "2026-02-10T09:00:00",
        "exitedAt": "2026-02-12T14:30:00",
        "processedByUserId": 2,
        "comments": "Documentos verificados correctamente"
    }
]
```

---

### 32.2 Obtener por ID

```
GET {{base_url}}/api/v1/application-stage-history/1
```

**Permiso:** `HIST_VER`

---

### 32.3 Crear Registro de Historial

```
POST {{base_url}}/api/v1/application-stage-history
```

**Permiso:** `HIST_CREAR`  
**Body (JSON):**
```json
{
    "applicationIdApplication": 1,
    "stageTrackingId": 2,
    "processedByUserId": 2,
    "comments": "Revisión académica iniciada"
}
```

---

### 32.4 Actualizar Registro

```
PUT {{base_url}}/api/v1/application-stage-history/1
```

**Permiso:** `HIST_MODIFICAR`  
**Body (JSON):**
```json
{
    "idHistory": 1,
    "applicationIdApplication": 1,
    "stageTrackingId": 1,
    "processedByUserId": 2,
    "comments": "Documentos verificados - aprobado para siguiente etapa"
}
```

---

### 32.5 Eliminar Registro

```
DELETE {{base_url}}/api/v1/application-stage-history/1
```

**Permiso:** `HIST_ELIMINAR`  
**Respuesta:** `204 No Content`

---

## 33. Respuestas de Error

Todos los endpoints retornan errores con el formato estándar `ApiErrorResponse`:

### 400 Bad Request (Validación)

```json
{
    "timestamp": "2026-02-16T10:00:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Error de validación",
    "path": "/api/v1/users",
    "errorCode": "VALIDATION_ERROR",
    "fieldErrors": [
        {
            "field": "cardId",
            "message": "must match \"^[0-9]{10}$\"",
            "rejectedValue": "123"
        },
        {
            "field": "institutionalEmail",
            "message": "must end with @uteq.edu.ec",
            "rejectedValue": "user@gmail.com"
        }
    ]
}
```

### 401 Unauthorized (Sin JWT o JWT inválido)

```json
{
    "timestamp": "2026-02-16T10:00:00",
    "status": 401,
    "error": "Unauthorized",
    "message": "Full authentication is required to access this resource",
    "path": "/api/v1/users"
}
```

### 403 Forbidden (Sin permiso)

```json
{
    "timestamp": "2026-02-16T10:00:00",
    "status": 403,
    "error": "Forbidden",
    "message": "Access Denied",
    "path": "/api/v1/users"
}
```

### 404 Not Found

```json
{
    "timestamp": "2026-02-16T10:00:00",
    "status": 404,
    "error": "Not Found",
    "message": "Usuario con ID 999 no encontrado",
    "path": "/api/v1/users/999",
    "errorCode": "RESOURCE_NOT_FOUND"
}
```

### 409 Conflict (Dato duplicado)

```json
{
    "timestamp": "2026-02-16T10:00:00",
    "status": 409,
    "error": "Conflict",
    "message": "Ya existe un usuario con cédula 0912345678",
    "path": "/api/v1/users",
    "errorCode": "DUPLICATE_RESOURCE"
}
```

---

## Resumen de Endpoints por Controlador

| # | Controlador | Base Path | Endpoints | Autenticación |
|---|---|---|---|---|
| 1 | AuthController | `/api/v1/auth` | 3 | Público |
| 2 | TwoFactorAuthController | `/api/v1/2fa` | 7 | Mixta |
| 3 | UsersController | `/api/v1/users` | 7 | JWT |
| 4 | StudentsController | `/api/v1/students` | 13 | JWT |
| 5 | RolesController | `/api/v1/roles` | 10 | JWT |
| 6 | CredentialsController | `/api/v1/credentials` | 9 | JWT |
| 7 | ApplicationsController | `/api/v1/applications` | 9 | JWT |
| 8 | ProceduresController | `/api/v1/procedures` | 11 | JWT |
| 9 | AttachedDocumentsController | `/api/v1/attached-documents` | 6 | JWT |
| 10 | RefreshTokenController | `/api/v1/refresh-tokens` | 3 | JWT |
| 11 | EmailController | `/api/v1/mail` | 2 | JWT |
| 12 | AcademicCalendarController | `/api/v1/academic-calendar` | 4 | JWT |
| 13 | CareerController | `/api/v1/careers` | 4 | JWT |
| 14 | ConfigurationController | `/api/v1/configuration` | 4 | JWT |
| 15 | DeadlineruleControllers | `/api/v1/deadlinerules` | 4 | JWT |
| 16 | DocumentTemplateController | `/api/v1/document-templates` | 4 | JWT |
| 17 | FacultyController | `/api/v1/faculty` | 4 | JWT |
| 18 | PermissionController | `/api/v1/permissions` | 4 | JWT |
| 19 | ProcessingStageController | `/api/v1/processing-stages` | 4 | JWT |
| 20 | RejectReasonController | `/api/v1/reject-reason` | 4 | JWT |
| 21 | RequirementsController | `/api/v1/requirements` | 5 | JWT |
| 22 | StatesController | `/api/v1/states` | 4 | JWT |
| 23 | WorkFlowsController | `/api/v1/work-flows` | 4 | JWT |
| 24 | WorkflowStagesController | `/api/v1/workflow-stages` | 5 | JWT |
| 25 | StageTrackingController | `/api/v1/stage-tracking` | 5 | JWT |
| 26 | SessionTokenController | `/api/v1/session-tokens` | 5 | JWT |
| 27 | NotificationTypeController | `/api/v1/notification-types` | 5 | JWT |
| 28 | NotificationController | `/api/v1/notifications` | 5 | JWT |
| 29 | DocumentsGeneratedController | `/api/v1/documents-generated` | 5 | JWT |
| 30 | DigitalSignaturesController | `/api/v1/digital-signatures` | 5 | JWT |
| 31 | ApplicationStageHistoryController | `/api/v1/application-stage-history` | 5 | JWT |
| | **TOTAL** | | **155** | |

---

## Flujo de Prueba Sugerido en Postman

Para probar el sistema completo de principio a fin:

```
1. POST /auth/token             → Login (obtener JWT)
2. POST /configuration          → Crear configuración global
3. POST /faculty                → Crear facultad
4. POST /careers                → Crear carrera
5. POST /users                  → Crear usuario
6. POST /credentials            → Crear credencial para el usuario
7. POST /roles                  → Crear rol (o usar existente)
8. POST /permissions            → Crear permisos (o usar existentes)
9. POST /roles/{id}/permissions → Asignar permisos al rol
10. POST /roles/{id}/users/{id} → Asignar rol al usuario
11. POST /students              → Matricular estudiante
12. POST /processing-stages     → Crear etapas de procesamiento
13. POST /work-flows            → Crear flujo de trabajo
14. POST /workflow-stages       → Vincular etapas al flujo
15. POST /procedures            → Crear trámite
16. POST /requirements          → Crear requisitos del trámite
17. POST /states                → Crear estados
18. POST /stage-tracking        → Crear seguimiento
19. POST /applications          → Crear solicitud
20. POST /attached-documents/upload → Adjuntar documentos
21. POST /application-stage-history → Registrar avance
22. PATCH /applications/{id}/resolve → Resolver solicitud
23. POST /documents-generated   → Generar documento final
24. POST /digital-signatures    → Firmar documento
25. POST /notifications         → Notificar al estudiante
26. POST /mail/plain            → Enviar email de confirmación
```
