# Guía de Permisos por Método — SGTE Backend

> **Proyecto:** Sistema de Gestión de Trámites Estudiantiles (SGTE)  
> **Framework:** Spring Boot 4.0.2 + Spring Security 7 + OAuth2 Resource Server  
> **Fecha:** Febrero 2026

---

## Tabla de Contenidos

1. [Arquitectura del Sistema de Permisos](#1-arquitectura-del-sistema-de-permisos)
2. [Cómo Funciona](#2-cómo-funciona)
3. [Nomenclatura de Códigos de Permisos](#3-nomenclatura-de-códigos-de-permisos)
4. [Catálogo Completo de Permisos por Controlador](#4-catálogo-completo-de-permisos-por-controlador)
5. [Matriz de Permisos por Rol Sugerida](#5-matriz-de-permisos-por-rol-sugerida)
6. [Endpoints Públicos (Sin Permiso)](#6-endpoints-públicos-sin-permiso)
7. [Cómo Aplicar Permisos Nuevos](#7-cómo-aplicar-permisos-nuevos)
8. [SQL de Inserción de Permisos](#8-sql-de-inserción-de-permisos)
9. [Preguntas Frecuentes](#9-preguntas-frecuentes)

---

## 1. Arquitectura del Sistema de Permisos

### 1.1 Modelo de Datos

```
Users ──── N:M ──── Roles ──── N:M ──── Permissions
 (user_roles)                     (role_permissions)
                                           │
                                      code (VARCHAR, UNIQUE)
                                      description (VARCHAR)
```

- Un **usuario** puede tener múltiples **roles** (`ROLE_ADMIN`, `ROLE_STUDENT`, `ROLE_COORDINATOR`, `ROLE_DEAN`).
- Cada **rol** puede tener múltiples **permisos** (`CAL_CREAR`, `SOL_LISTAR`, etc.).
- Un mismo permiso puede estar asignado a varios roles.
- Los permisos son **granulares**: controlan acceso a nivel de método individual.

### 1.2 Tablas Involucradas

| Tabla | Propósito |
|---|---|
| `users` | Usuarios del sistema |
| `roles` | Roles disponibles (ROLE_ADMIN, etc.) |
| `permissions` | Catálogo de permisos individuales |
| `user_roles` | Relación N:M entre usuarios y roles |
| `role_permissions` | Relación N:M entre roles y permisos |

### 1.3 Roles Protegidos del Sistema

Estos 4 roles **no pueden eliminarse** (protegidos en `RolesServiceImpl`):

| Rol | Descripción |
|---|---|
| `ROLE_ADMIN` | Administrador del sistema — acceso total |
| `ROLE_STUDENT` | Estudiante — crea y da seguimiento a solicitudes |
| `ROLE_COORDINATOR` | Coordinador de carrera — gestiona estudiantes y trámites |
| `ROLE_DEAN` | Decano de facultad — aprueba operaciones académicas |

---

## 2. Cómo Funciona

### 2.1 Flujo de Autorización Completo

```
1. Usuario envía request con header: Authorization: Bearer <JWT>
        │
2. JwtDecoder valida la firma RSA del token
        │
3. JwtAuthenticationConverter extrae el claim "scope" del JWT
   y genera GrantedAuthority para cada valor:
   ├── "SCOPE_ROLE_ADMIN"  → authority: SCOPE_ROLE_ADMIN
   ├── "ROLE_ADMIN"        → authority: ROLE_ADMIN
   ├── "USUARIO_LISTAR"    → authority: USUARIO_LISTAR
   └── "SOL_CREAR"         → authority: SOL_CREAR
        │
4. Spring Security evalúa @PreAuthorize del controlador:
   ├── Nivel CLASE:  @PreAuthorize("isAuthenticated()")
   │                 → ¿Tiene JWT válido? Si no → 401
   │
   └── Nivel MÉTODO: @PreAuthorize("hasAuthority('SOL_CREAR')")
                     → ¿Tiene el authority SOL_CREAR? Si no → 403
        │
5. Si pasa ambas validaciones → ejecuta el método del controlador
```

### 2.2 ¿Dónde se Construyen las Authorities?

En `CustomUserDetailsService.loadUserByUsername()`:

```java
// Para cada rol del usuario
for (Roles role : user.getRoles()) {
    // Agrega el nombre del rol como authority
    authorities.add(new SimpleGrantedAuthority(role.getRoleName()));
    
    // Agrega cada permiso del rol como authority individual
    for (Permissions permission : role.getPermissions()) {
        authorities.add(new SimpleGrantedAuthority(permission.getCode()));
    }
}
```

### 2.3 ¿Dónde se Verifica el Permiso?

En cada controlador, a dos niveles:

```java
@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("isAuthenticated()")         // ← Nivel clase: requiere JWT válido
public class UsersController {

    @GetMapping
    @PreAuthorize("hasAuthority('USUARIO_LISTAR')")  // ← Nivel método: requiere permiso específico
    public ResponseEntity<List<UserResponse>> findAll() { ... }
}
```

**Ambas anotaciones se evalúan**:
1. Primero se verifica `isAuthenticated()` (clase).
2. Luego se verifica `hasAuthority('...')` (método).
3. Si cualquiera falla → HTTP 403 Forbidden.

---

## 3. Nomenclatura de Códigos de Permisos

### 3.1 Convención de Nombres

```
{MÓDULO}_{ACCIÓN}
```

| Parte | Descripción | Ejemplos |
|---|---|---|
| **MÓDULO** | Entidad o concepto (en español, abreviado) | `USUARIO`, `SOL`, `TRAMITE`, `CAL`, `CRED` |
| **ACCIÓN** | Operación que autoriza | `LISTAR`, `VER`, `CREAR`, `MODIFICAR`, `ELIMINAR` |

### 3.2 Acciones Estándar

| Acción | Método HTTP | Descripción |
|---|---|---|
| `LISTAR` | GET (colección) | Listar todos los registros |
| `VER` | GET (individual) | Ver un registro por ID |
| `CREAR` | POST | Crear un nuevo registro |
| `MODIFICAR` | PUT / PATCH | Modificar un registro existente |
| `ELIMINAR` | DELETE | Eliminar un registro |

### 3.3 Acciones Especiales

| Acción | Módulo | Descripción |
|---|---|---|
| `ACTIVAR` / `DESACTIVAR` | USUARIO, TRAMITE | Borrado lógico (soft delete) |
| `PROMOVER` | ESTUDIANTE | Promover al siguiente semestre |
| `GRADUAR` | ESTUDIANTE | Cambiar estado a "graduado" |
| `RETIRAR` | ESTUDIANTE | Cambiar estado a "retirado" |
| `REACTIVAR` | ESTUDIANTE | Devolver a estado "activo" |
| `CAMBIAR_PASS` | CRED | Cambiar contraseña propia |
| `RESETEAR_PASS` | CRED | Resetear contraseña ajena (admin) |
| `BLOQUEAR` / `DESBLOQUEAR` | CRED | Bloquear/desbloquear cuenta |
| `RESOLVER` / `RECHAZAR` | SOL | Resolver/rechazar solicitud |
| `ASIGNAR_PERMISO` / `REMOVER_PERMISO` | ROL | Gestionar permisos de un rol |
| `ASIGNAR_USUARIO` / `REMOVER_USUARIO` | ROL | Asignar/remover rol de usuario |
| `CONFIGURAR` / `VERIFICAR` / `DESACTIVAR` | AUTH2FA | Flujo de autenticación 2FA |
| `ESTADO` / `REGENERAR` | AUTH2FA | Consultar estado / regenerar backups |
| `ENVIAR` | EMAIL | Enviar correo electrónico |

---

## 4. Catálogo Completo de Permisos por Controlador

### 4.1 `AuthController` — `/api/v1/auth`

> **Sin seguridad por método** — Endpoints públicos (no requiere `@PreAuthorize`)  
> Solo logout requiere autenticación (nivel clase de la ruta)

| Método HTTP | Endpoint | Permiso | Descripción |
|---|---|---|---|
| POST | `/token` | **PÚBLICO** | Login (grant_type=password o refresh_token) |
| POST | `/2fa-verify` | **PÚBLICO** | Verificar código 2FA durante login |
| POST | `/logout` | **Autenticado** (JWT válido) | Revocar todos los refresh tokens |

---

### 4.2 `TwoFactorAuthController` — `/api/v1/2fa`

> **Seguridad mixta** — Algunos endpoints son protegidos (con JWT), otros son públicos (durante login)

| Método HTTP | Endpoint | Permiso | Descripción |
|---|---|---|---|
| POST | `/setup` | `AUTH2FA_CONFIGURAR` | Generar clave TOTP + códigos de respaldo |
| POST | `/verify` | `AUTH2FA_VERIFICAR` | Verificar TOTP por primera vez (activa 2FA) |
| DELETE | `/disable` | `AUTH2FA_DESACTIVAR` | Desactivar 2FA (requiere código TOTP) |
| GET | `/status` | `AUTH2FA_ESTADO` | Consultar si 2FA está activo |
| POST | `/backup-codes/regenerate` | `AUTH2FA_REGENERAR` | Regenerar códigos de respaldo |
| POST | `/validate` | **PÚBLICO** | Validar código TOTP durante login |
| POST | `/validate-backup` | **PÚBLICO** | Validar código de respaldo durante login |

---

### 4.3 `UsersController` — `/api/v1/users`

> **Clase:** `@PreAuthorize("isAuthenticated()")`

| Método HTTP | Endpoint | Permiso | Descripción |
|---|---|---|---|
| GET | `/` | `USUARIO_LISTAR` | Listar todos los usuarios |
| GET | `/{id}` | `USUARIO_VER` | Obtener usuario por ID |
| POST | `/` | `USUARIO_CREAR` | Crear usuario |
| PUT | `/{id}` | `USUARIO_MODIFICAR` | Actualizar usuario |
| DELETE | `/{id}` | `USUARIO_ELIMINAR` | Eliminar usuario |
| PATCH | `/{id}/deactivate` | `USUARIO_DESACTIVAR` | Desactivar usuario (borrado lógico) |
| PATCH | `/{id}/activate` | `USUARIO_ACTIVAR` | Reactivar usuario |

**Permisos del módulo:** `USUARIO_LISTAR`, `USUARIO_VER`, `USUARIO_CREAR`, `USUARIO_MODIFICAR`, `USUARIO_ELIMINAR`, `USUARIO_DESACTIVAR`, `USUARIO_ACTIVAR`

---

### 4.4 `StudentsController` — `/api/v1/students`

> **Clase:** `@PreAuthorize("isAuthenticated()")`

| Método HTTP | Endpoint | Permiso | Descripción |
|---|---|---|---|
| GET | `/` | `ESTUDIANTE_LISTAR` | Listar todos los estudiantes |
| GET | `/{id}` | `ESTUDIANTE_VER` | Obtener estudiante por ID |
| GET | `/career/{careerId}` | `ESTUDIANTE_LISTAR` | Listar estudiantes por carrera |
| GET | `/semester-parallel` | `ESTUDIANTE_LISTAR` | Filtrar por semestre y paralelo |
| GET | `/user/{userId}` | `ESTUDIANTE_VER` | Obtener estudiante por ID de usuario |
| POST | `/` | `ESTUDIANTE_CREAR` | Matricular estudiante |
| PUT | `/{id}` | `ESTUDIANTE_MODIFICAR` | Actualizar datos del estudiante |
| PATCH | `/{id}/status` | `ESTUDIANTE_MODIFICAR` | Cambiar estado del estudiante |
| PATCH | `/{id}/promote` | `ESTUDIANTE_PROMOVER` | Promover al siguiente semestre |
| PATCH | `/{id}/graduate` | `ESTUDIANTE_GRADUAR` | Graduar estudiante |
| PATCH | `/{id}/withdraw` | `ESTUDIANTE_RETIRAR` | Retirar estudiante |
| PATCH | `/{id}/reactivate` | `ESTUDIANTE_REACTIVAR` | Reactivar estudiante |
| DELETE | `/{id}` | `ESTUDIANTE_ELIMINAR` | Eliminar estudiante |

**Permisos del módulo:** `ESTUDIANTE_LISTAR`, `ESTUDIANTE_VER`, `ESTUDIANTE_CREAR`, `ESTUDIANTE_MODIFICAR`, `ESTUDIANTE_ELIMINAR`, `ESTUDIANTE_PROMOVER`, `ESTUDIANTE_GRADUAR`, `ESTUDIANTE_RETIRAR`, `ESTUDIANTE_REACTIVAR`

---

### 4.5 `RolesController` — `/api/v1/roles`

> **Clase:** `@PreAuthorize("isAuthenticated()")`

| Método HTTP | Endpoint | Permiso | Descripción |
|---|---|---|---|
| GET | `/` | `ROL_LISTAR` | Listar todos los roles |
| GET | `/{id}` | `ROL_VER` | Obtener rol por ID |
| GET | `/name/{name}` | `ROL_VER` | Buscar rol por nombre |
| POST | `/` | `ROL_CREAR` | Crear rol (nombre debe ser ROLE_XXX) |
| PUT | `/{id}` | `ROL_MODIFICAR` | Actualizar rol |
| DELETE | `/{id}` | `ROL_ELIMINAR` | Eliminar rol (protegidos no se eliminan) |
| POST | `/{id}/permissions` | `ROL_ASIGNAR_PERMISO` | Asignar permisos a un rol |
| DELETE | `/{id}/permissions` | `ROL_REMOVER_PERMISO` | Remover permisos de un rol |
| POST | `/{roleId}/users/{userId}` | `ROL_ASIGNAR_USUARIO` | Asignar rol a un usuario |
| DELETE | `/{roleId}/users/{userId}` | `ROL_REMOVER_USUARIO` | Remover rol de un usuario |

**Permisos del módulo:** `ROL_LISTAR`, `ROL_VER`, `ROL_CREAR`, `ROL_MODIFICAR`, `ROL_ELIMINAR`, `ROL_ASIGNAR_PERMISO`, `ROL_REMOVER_PERMISO`, `ROL_ASIGNAR_USUARIO`, `ROL_REMOVER_USUARIO`

---

### 4.6 `CredentialsController` — `/api/v1/credentials`

> **Clase:** `@PreAuthorize("isAuthenticated()")`

| Método HTTP | Endpoint | Permiso | Descripción |
|---|---|---|---|
| GET | `/` | `CRED_LISTAR` | Listar credenciales (sin hash) |
| GET | `/{id}` | `CRED_VER` | Obtener credencial por ID |
| POST | `/` | `CRED_CREAR` | Crear credencial (hashea contraseña) |
| POST | `/{id}/change-password` | `CRED_CAMBIAR_PASS` | Cambiar contraseña (verifica propiedad) |
| POST | `/{id}/reset-password` | `CRED_RESETEAR_PASS` | Resetear contraseña (genera temporal) |
| GET | `/{id}/password-expired` | *(solo autenticado)* | Verificar si la contraseña expiró |
| POST | `/{id}/lock` | `CRED_BLOQUEAR` | Bloquear cuenta |
| POST | `/{id}/unlock` | `CRED_DESBLOQUEAR` | Desbloquear cuenta |
| DELETE | `/{id}` | `CRED_ELIMINAR` | Eliminar credencial |

**Nota:** `/{id}/password-expired` no tiene `@PreAuthorize` a nivel método, solo requiere `isAuthenticated()` de la clase.

**Permisos del módulo:** `CRED_LISTAR`, `CRED_VER`, `CRED_CREAR`, `CRED_CAMBIAR_PASS`, `CRED_RESETEAR_PASS`, `CRED_BLOQUEAR`, `CRED_DESBLOQUEAR`, `CRED_ELIMINAR`

---

### 4.7 `ApplicationsController` — `/api/v1/applications`

> **Clase:** `@PreAuthorize("isAuthenticated()")`

| Método HTTP | Endpoint | Permiso | Descripción |
|---|---|---|---|
| GET | `/` | `SOL_LISTAR` | Listar todas las solicitudes |
| GET | `/{id}` | `SOL_VER` | Obtener solicitud por ID |
| GET | `/user/{userId}` | `SOL_VER` | Solicitudes de un usuario |
| GET | `/priority/{priority}` | `SOL_LISTAR` | Filtrar por prioridad |
| POST | `/` | `SOL_CREAR` | Crear solicitud |
| PUT | `/{id}` | `SOL_MODIFICAR` | Actualizar solicitud |
| DELETE | `/{id}` | `SOL_ELIMINAR` | Eliminar solicitud |
| PATCH | `/{id}/resolve` | `SOL_RESOLVER` | Resolver solicitud |
| PATCH | `/{id}/reject` | `SOL_RECHAZAR` | Rechazar solicitud |

**Permisos del módulo:** `SOL_LISTAR`, `SOL_VER`, `SOL_CREAR`, `SOL_MODIFICAR`, `SOL_ELIMINAR`, `SOL_RESOLVER`, `SOL_RECHAZAR`

---

### 4.8 `ProceduresController` — `/api/v1/procedures`

> **Clase:** `@PreAuthorize("isAuthenticated()")`

| Método HTTP | Endpoint | Permiso | Descripción |
|---|---|---|---|
| GET | `/` | `TRAMITE_LISTAR` | Listar trámites activos |
| GET | `/all` | `TRAMITE_LISTAR` | Listar todos (incluyendo inactivos) |
| GET | `/{id}` | `TRAMITE_VER` | Obtener trámite por ID |
| GET | `/code/{code}` | `TRAMITE_VER` | Buscar por código |
| GET | `/workflow/{workflowId}` | `TRAMITE_LISTAR` | Filtrar por flujo de trabajo |
| POST | `/` | `TRAMITE_CREAR` | Crear trámite |
| PUT | `/{id}` | `TRAMITE_MODIFICAR` | Actualizar trámite |
| PATCH | `/{id}/activate` | `TRAMITE_ACTIVAR` | Activar trámite |
| PATCH | `/{id}/deactivate` | `TRAMITE_DESACTIVAR` | Desactivar trámite |
| GET | `/{id}/requires-2fa` | `TRAMITE_VER` | Verificar si requiere 2FA |
| DELETE | `/{id}` | `TRAMITE_ELIMINAR` | Eliminar trámite |

**Permisos del módulo:** `TRAMITE_LISTAR`, `TRAMITE_VER`, `TRAMITE_CREAR`, `TRAMITE_MODIFICAR`, `TRAMITE_ACTIVAR`, `TRAMITE_DESACTIVAR`, `TRAMITE_ELIMINAR`

---

### 4.9 `AttachedDocumentsController` — `/api/v1/attached-documents`

> **Clase:** `@PreAuthorize("isAuthenticated()")`

| Método HTTP | Endpoint | Permiso | Descripción |
|---|---|---|---|
| GET | `/` | `DOCADJ_LISTAR` | Listar documentos adjuntos |
| GET | `/{id}` | `DOCADJ_VER` | Obtener metadatos de documento |
| POST | `/upload` | `DOCADJ_CREAR` | Subir archivo a Google Drive + BD |
| GET | `/{id}/download` | `DOCADJ_VER` | Descargar archivo de Google Drive |
| PUT | `/{id}` | `DOCADJ_MODIFICAR` | Actualizar metadatos |
| DELETE | `/{id}` | `DOCADJ_ELIMINAR` | Eliminar de Drive + BD |

**Permisos del módulo:** `DOCADJ_LISTAR`, `DOCADJ_VER`, `DOCADJ_CREAR`, `DOCADJ_MODIFICAR`, `DOCADJ_ELIMINAR`

---

### 4.10 `RefreshTokenController` — `/api/v1/refresh-tokens`

> **Clase:** `@PreAuthorize("isAuthenticated()")`

| Método HTTP | Endpoint | Permiso | Descripción |
|---|---|---|---|
| GET | `/` | `TOKEN_LISTAR` | Listar refresh tokens (sin valor JWT) |
| GET | `/{id}` | `TOKEN_VER` | Obtener token por ID (sin valor JWT) |
| DELETE | `/{id}` | `TOKEN_ELIMINAR` | Revocar un refresh token |

**Permisos del módulo:** `TOKEN_LISTAR`, `TOKEN_VER`, `TOKEN_ELIMINAR`

---

### 4.11 `EmailController` — `/api/v1/mail`

> **Clase:** `@PreAuthorize("isAuthenticated()")`

| Método HTTP | Endpoint | Permiso | Descripción |
|---|---|---|---|
| POST | `/send` | `EMAIL_ENVIAR` | Enviar email de texto plano |
| POST | `/send-html` | `EMAIL_ENVIAR` | Enviar email HTML |

**Permisos del módulo:** `EMAIL_ENVIAR`

---

### 4.12 Controladores con Stored Procedures (Catálogos)

Todos estos controladores siguen el mismo patrón CRUD con 4 permisos cada uno.

#### `AcademicCalendarController` — `/api/v1/academic-calendar`

| Método HTTP | Endpoint | Permiso | Descripción |
|---|---|---|---|
| POST | `/` | `CAL_CREAR` | Crear período académico |
| PUT | `/{id}` | `CAL_MODIFICAR` | Actualizar período |
| DELETE | `/{id}` | `CAL_ELIMINAR` | Eliminar período |
| GET | `/` | `CAL_LISTAR` | Listar períodos |

#### `CareerController` — `/api/v1/careers`

| Método HTTP | Endpoint | Permiso | Descripción |
|---|---|---|---|
| POST | `/` | `CARRERA_CREAR` | Crear carrera |
| PUT | `/{id}` | `CARRERA_MODIFICAR` | Actualizar carrera |
| DELETE | `/{id}` | `CARRERA_ELIMINAR` | Eliminar carrera |
| GET | `/` | `CARRERA_LISTAR` | Listar carreras |

#### `ConfigurationController` — `/api/v1/configuration`

| Método HTTP | Endpoint | Permiso | Descripción |
|---|---|---|---|
| POST | `/` | `CONFIG_CREAR` | Crear configuración |
| PUT | `/{id}` | `CONFIG_MODIFICAR` | Actualizar configuración |
| DELETE | `/{id}` | `CONFIG_ELIMINAR` | Eliminar configuración |
| GET | `/` | `CONFIG_LISTAR` | Listar configuraciones |

#### `DeadlineruleControllers` — `/api/v1/deadlinerules`

| Método HTTP | Endpoint | Permiso | Descripción |
|---|---|---|---|
| POST | `/` | `REGLA_CREAR` | Crear regla de plazo |
| PUT | `/{id}` | `REGLA_MODIFICAR` | Actualizar regla |
| DELETE | `/{id}` | `REGLA_ELIMINAR` | Eliminar regla |
| GET | `/` | `REGLA_LISTAR` | Listar reglas |

#### `DocumentTemplateController` — `/api/v1/document-templates`

| Método HTTP | Endpoint | Permiso | Descripción |
|---|---|---|---|
| POST | `/` | `PLANTILLA_CREAR` | Crear plantilla |
| PUT | `/{id}` | `PLANTILLA_MODIFICAR` | Actualizar plantilla |
| DELETE | `/{id}` | `PLANTILLA_ELIMINAR` | Eliminar plantilla |
| GET | `/` | `PLANTILLA_LISTAR` | Listar plantillas |

#### `FacultyController` — `/api/v1/faculties`

| Método HTTP | Endpoint | Permiso | Descripción |
|---|---|---|---|
| POST | `/` | `FACULTAD_CREAR` | Crear facultad |
| PUT | `/{id}` | `FACULTAD_MODIFICAR` | Actualizar facultad |
| DELETE | `/{id}` | `FACULTAD_ELIMINAR` | Eliminar facultad |
| GET | `/` | `FACULTAD_LISTAR` | Listar facultades |

#### `PermissionController` — `/api/v1/permissions`

| Método HTTP | Endpoint | Permiso | Descripción |
|---|---|---|---|
| POST | `/` | `PERMISO_CREAR` | Crear permiso |
| PUT | `/{id}` | `PERMISO_MODIFICAR` | Actualizar permiso |
| DELETE | `/{id}` | `PERMISO_ELIMINAR` | Eliminar permiso |
| GET | `/` | `PERMISO_LISTAR` | Listar permisos |

#### `ProcessingStageController` — `/api/v1/processing-stages`

| Método HTTP | Endpoint | Permiso | Descripción |
|---|---|---|---|
| POST | `/` | `ETAPA_CREAR` | Crear etapa de procesamiento |
| PUT | `/{id}` | `ETAPA_MODIFICAR` | Actualizar etapa |
| DELETE | `/{id}` | `ETAPA_ELIMINAR` | Eliminar etapa |
| GET | `/` | `ETAPA_LISTAR` | Listar etapas |

#### `RejectReasonController` — `/api/v1/rejection-reasons`

| Método HTTP | Endpoint | Permiso | Descripción |
|---|---|---|---|
| POST | `/` | `RECHAZO_CREAR` | Crear motivo de rechazo |
| PUT | `/{id}` | `RECHAZO_MODIFICAR` | Actualizar motivo |
| DELETE | `/{id}` | `RECHAZO_ELIMINAR` | Eliminar motivo |
| GET | `/` | `RECHAZO_LISTAR` | Listar motivos |

#### `StatesController` — `/api/v1/states`

| Método HTTP | Endpoint | Permiso | Descripción |
|---|---|---|---|
| POST | `/` | `ESTADO_CREAR` | Crear estado |
| PUT | `/{id}` | `ESTADO_MODIFICAR` | Actualizar estado |
| DELETE | `/{id}` | `ESTADO_ELIMINAR` | Eliminar estado |
| GET | `/` | `ESTADO_LISTAR` | Listar estados |

#### `WorkFlowsController` — `/api/v1/workflows`

| Método HTTP | Endpoint | Permiso | Descripción |
|---|---|---|---|
| POST | `/` | `FLUJO_CREAR` | Crear flujo de trabajo |
| PUT | `/{id}` | `FLUJO_MODIFICAR` | Actualizar flujo |
| DELETE | `/{id}` | `FLUJO_ELIMINAR` | Eliminar flujo |
| GET | `/` | `FLUJO_LISTAR` | Listar flujos |

---

### 4.13 Controladores CRUD con DTOs

Todos estos controladores tienen 5 endpoints estándar con el mismo patrón.

#### `WorkflowStagesController` — `/api/v1/workflow-stages`

| Método HTTP | Endpoint | Permiso | Descripción |
|---|---|---|---|
| GET | `/` | `FLUJOETAPA_LISTAR` | Listar flujo-etapas |
| GET | `/{id}` | `FLUJOETAPA_VER` | Obtener por ID |
| POST | `/` | `FLUJOETAPA_CREAR` | Crear relación flujo-etapa |
| PUT | `/{id}` | `FLUJOETAPA_MODIFICAR` | Actualizar relación |
| DELETE | `/{id}` | `FLUJOETAPA_ELIMINAR` | Eliminar relación |

#### `StageTrackingController` — `/api/v1/stage-tracking`

| Método HTTP | Endpoint | Permiso | Descripción |
|---|---|---|---|
| GET | `/` | `SEGUIMIENTO_LISTAR` | Listar seguimientos |
| GET | `/{id}` | `SEGUIMIENTO_VER` | Obtener por ID |
| POST | `/` | `SEGUIMIENTO_CREAR` | Crear seguimiento |
| PUT | `/{id}` | `SEGUIMIENTO_MODIFICAR` | Actualizar seguimiento |
| DELETE | `/{id}` | `SEGUIMIENTO_ELIMINAR` | Eliminar seguimiento |

#### `SessionTokenController` — `/api/v1/session-tokens`

| Método HTTP | Endpoint | Permiso | Descripción |
|---|---|---|---|
| GET | `/` | `SESION_LISTAR` | Listar sesiones |
| GET | `/{id}` | `SESION_VER` | Obtener por ID |
| POST | `/` | `SESION_CREAR` | Crear sesión |
| PUT | `/{id}` | `SESION_MODIFICAR` | Actualizar sesión |
| DELETE | `/{id}` | `SESION_ELIMINAR` | Eliminar sesión |

#### `NotificationTypeController` — `/api/v1/notification-types`

| Método HTTP | Endpoint | Permiso | Descripción |
|---|---|---|---|
| GET | `/` | `TIPNOTIF_LISTAR` | Listar tipos de notificación |
| GET | `/{id}` | `TIPNOTIF_VER` | Obtener por ID |
| POST | `/` | `TIPNOTIF_CREAR` | Crear tipo de notificación |
| PUT | `/{id}` | `TIPNOTIF_MODIFICAR` | Actualizar tipo |
| DELETE | `/{id}` | `TIPNOTIF_ELIMINAR` | Eliminar tipo |

#### `NotificationController` — `/api/v1/notifications`

| Método HTTP | Endpoint | Permiso | Descripción |
|---|---|---|---|
| GET | `/` | `NOTIF_LISTAR` | Listar notificaciones |
| GET | `/{id}` | `NOTIF_VER` | Obtener por ID |
| POST | `/` | `NOTIF_CREAR` | Crear notificación |
| PUT | `/{id}` | `NOTIF_MODIFICAR` | Actualizar notificación |
| DELETE | `/{id}` | `NOTIF_ELIMINAR` | Eliminar notificación |

#### `DocumentsGeneratedController` — `/api/v1/documents-generated`

| Método HTTP | Endpoint | Permiso | Descripción |
|---|---|---|---|
| GET | `/` | `DOCGEN_LISTAR` | Listar documentos generados |
| GET | `/{id}` | `DOCGEN_VER` | Obtener por ID |
| POST | `/` | `DOCGEN_CREAR` | Crear registro de documento |
| PUT | `/{id}` | `DOCGEN_MODIFICAR` | Actualizar registro |
| DELETE | `/{id}` | `DOCGEN_ELIMINAR` | Eliminar registro |

#### `DigitalSignaturesController` — `/api/v1/digital-signatures`

| Método HTTP | Endpoint | Permiso | Descripción |
|---|---|---|---|
| GET | `/` | `FIRMA_LISTAR` | Listar firmas digitales |
| GET | `/{id}` | `FIRMA_VER` | Obtener por ID |
| POST | `/` | `FIRMA_CREAR` | Registrar firma digital |
| PUT | `/{id}` | `FIRMA_MODIFICAR` | Actualizar firma |
| DELETE | `/{id}` | `FIRMA_ELIMINAR` | Eliminar firma |

#### `ApplicationStageHistoryController` — `/api/v1/application-stage-history`

| Método HTTP | Endpoint | Permiso | Descripción |
|---|---|---|---|
| GET | `/` | `HIST_LISTAR` | Listar historial de etapas |
| GET | `/{id}` | `HIST_VER` | Obtener por ID |
| POST | `/` | `HIST_CREAR` | Crear registro de historial |
| PUT | `/{id}` | `HIST_MODIFICAR` | Actualizar registro |
| DELETE | `/{id}` | `HIST_ELIMINAR` | Eliminar registro |

#### `RequirementsController` — `/api/v1/requirements`

| Método HTTP | Endpoint | Permiso | Descripción |
|---|---|---|---|
| GET | `/` | `REQUISITO_LISTAR` | Listar requisitos |
| GET | `/{id}` | `REQUISITO_VER` | Obtener por ID |
| POST | `/` | `REQUISITO_CREAR` | Crear requisito |
| PUT | `/{id}` | `REQUISITO_MODIFICAR` | Actualizar requisito |
| DELETE | `/{id}` | `REQUISITO_ELIMINAR` | Eliminar requisito |

---

## 5. Matriz de Permisos por Rol Sugerida

### 5.1 Resumen de Permisos Totales

| Módulo | Cantidad | Códigos |
|---|---|---|
| Usuarios | 7 | `USUARIO_LISTAR`, `USUARIO_VER`, `USUARIO_CREAR`, `USUARIO_MODIFICAR`, `USUARIO_ELIMINAR`, `USUARIO_ACTIVAR`, `USUARIO_DESACTIVAR` |
| Estudiantes | 9 | `ESTUDIANTE_LISTAR`, `ESTUDIANTE_VER`, `ESTUDIANTE_CREAR`, `ESTUDIANTE_MODIFICAR`, `ESTUDIANTE_ELIMINAR`, `ESTUDIANTE_PROMOVER`, `ESTUDIANTE_GRADUAR`, `ESTUDIANTE_RETIRAR`, `ESTUDIANTE_REACTIVAR` |
| Roles | 9 | `ROL_LISTAR`, `ROL_VER`, `ROL_CREAR`, `ROL_MODIFICAR`, `ROL_ELIMINAR`, `ROL_ASIGNAR_PERMISO`, `ROL_REMOVER_PERMISO`, `ROL_ASIGNAR_USUARIO`, `ROL_REMOVER_USUARIO` |
| Credenciales | 8 | `CRED_LISTAR`, `CRED_VER`, `CRED_CREAR`, `CRED_CAMBIAR_PASS`, `CRED_RESETEAR_PASS`, `CRED_BLOQUEAR`, `CRED_DESBLOQUEAR`, `CRED_ELIMINAR` |
| 2FA | 5 | `AUTH2FA_CONFIGURAR`, `AUTH2FA_VERIFICAR`, `AUTH2FA_DESACTIVAR`, `AUTH2FA_ESTADO`, `AUTH2FA_REGENERAR` |
| Solicitudes | 7 | `SOL_LISTAR`, `SOL_VER`, `SOL_CREAR`, `SOL_MODIFICAR`, `SOL_ELIMINAR`, `SOL_RESOLVER`, `SOL_RECHAZAR` |
| Trámites | 7 | `TRAMITE_LISTAR`, `TRAMITE_VER`, `TRAMITE_CREAR`, `TRAMITE_MODIFICAR`, `TRAMITE_ACTIVAR`, `TRAMITE_DESACTIVAR`, `TRAMITE_ELIMINAR` |
| Calendarios | 4 | `CAL_CREAR`, `CAL_MODIFICAR`, `CAL_ELIMINAR`, `CAL_LISTAR` |
| Carreras | 4 | `CARRERA_CREAR`, `CARRERA_MODIFICAR`, `CARRERA_ELIMINAR`, `CARRERA_LISTAR` |
| Configuración | 4 | `CONFIG_CREAR`, `CONFIG_MODIFICAR`, `CONFIG_ELIMINAR`, `CONFIG_LISTAR` |
| Reglas de Plazo | 4 | `REGLA_CREAR`, `REGLA_MODIFICAR`, `REGLA_ELIMINAR`, `REGLA_LISTAR` |
| Plantillas Doc | 4 | `PLANTILLA_CREAR`, `PLANTILLA_MODIFICAR`, `PLANTILLA_ELIMINAR`, `PLANTILLA_LISTAR` |
| Facultades | 4 | `FACULTAD_CREAR`, `FACULTAD_MODIFICAR`, `FACULTAD_ELIMINAR`, `FACULTAD_LISTAR` |
| Permisos | 4 | `PERMISO_CREAR`, `PERMISO_MODIFICAR`, `PERMISO_ELIMINAR`, `PERMISO_LISTAR` |
| Etapas | 4 | `ETAPA_CREAR`, `ETAPA_MODIFICAR`, `ETAPA_ELIMINAR`, `ETAPA_LISTAR` |
| Motivos Rechazo | 4 | `RECHAZO_CREAR`, `RECHAZO_MODIFICAR`, `RECHAZO_ELIMINAR`, `RECHAZO_LISTAR` |
| Estados | 4 | `ESTADO_CREAR`, `ESTADO_MODIFICAR`, `ESTADO_ELIMINAR`, `ESTADO_LISTAR` |
| Flujos Trabajo | 4 | `FLUJO_CREAR`, `FLUJO_MODIFICAR`, `FLUJO_ELIMINAR`, `FLUJO_LISTAR` |
| Flujo-Etapas | 5 | `FLUJOETAPA_LISTAR`, `FLUJOETAPA_VER`, `FLUJOETAPA_CREAR`, `FLUJOETAPA_MODIFICAR`, `FLUJOETAPA_ELIMINAR` |
| Seguimiento | 5 | `SEGUIMIENTO_LISTAR`, `SEGUIMIENTO_VER`, `SEGUIMIENTO_CREAR`, `SEGUIMIENTO_MODIFICAR`, `SEGUIMIENTO_ELIMINAR` |
| Sesiones | 5 | `SESION_LISTAR`, `SESION_VER`, `SESION_CREAR`, `SESION_MODIFICAR`, `SESION_ELIMINAR` |
| Tokens | 3 | `TOKEN_LISTAR`, `TOKEN_VER`, `TOKEN_ELIMINAR` |
| Tipos Notif. | 5 | `TIPNOTIF_LISTAR`, `TIPNOTIF_VER`, `TIPNOTIF_CREAR`, `TIPNOTIF_MODIFICAR`, `TIPNOTIF_ELIMINAR` |
| Notificaciones | 5 | `NOTIF_LISTAR`, `NOTIF_VER`, `NOTIF_CREAR`, `NOTIF_MODIFICAR`, `NOTIF_ELIMINAR` |
| Docs Generados | 5 | `DOCGEN_LISTAR`, `DOCGEN_VER`, `DOCGEN_CREAR`, `DOCGEN_MODIFICAR`, `DOCGEN_ELIMINAR` |
| Firmas Digital | 5 | `FIRMA_LISTAR`, `FIRMA_VER`, `FIRMA_CREAR`, `FIRMA_MODIFICAR`, `FIRMA_ELIMINAR` |
| Docs Adjuntos | 5 | `DOCADJ_LISTAR`, `DOCADJ_VER`, `DOCADJ_CREAR`, `DOCADJ_MODIFICAR`, `DOCADJ_ELIMINAR` |
| Historial | 5 | `HIST_LISTAR`, `HIST_VER`, `HIST_CREAR`, `HIST_MODIFICAR`, `HIST_ELIMINAR` |
| Requisitos | 5 | `REQUISITO_LISTAR`, `REQUISITO_VER`, `REQUISITO_CREAR`, `REQUISITO_MODIFICAR`, `REQUISITO_ELIMINAR` |
| Email | 1 | `EMAIL_ENVIAR` |
| **TOTAL** | **153** | — |

### 5.2 Distribución Sugerida por Rol

> **Nota:** Esta es una distribución recomendada. La asignación real depende de las políticas institucionales.

#### ROLE_ADMIN — Todos los permisos (153)

El administrador tiene acceso completo a todos los módulos.

#### ROLE_STUDENT

| Módulo | Permisos |
|---|---|
| Solicitudes | `SOL_LISTAR`, `SOL_VER`, `SOL_CREAR` |
| Docs Adjuntos | `DOCADJ_LISTAR`, `DOCADJ_VER`, `DOCADJ_CREAR` |
| Notificaciones | `NOTIF_LISTAR`, `NOTIF_VER` |
| 2FA | `AUTH2FA_CONFIGURAR`, `AUTH2FA_VERIFICAR`, `AUTH2FA_DESACTIVAR`, `AUTH2FA_ESTADO`, `AUTH2FA_REGENERAR` |
| Credenciales | `CRED_CAMBIAR_PASS` |
| Seguimiento | `SEGUIMIENTO_LISTAR`, `SEGUIMIENTO_VER` |
| Historial | `HIST_LISTAR`, `HIST_VER` |
| Requisitos | `REQUISITO_LISTAR`, `REQUISITO_VER` |
| Trámites | `TRAMITE_LISTAR`, `TRAMITE_VER` |
| **Total** | **~20 permisos** |

#### ROLE_COORDINATOR

| Módulo | Permisos |
|---|---|
| Usuarios | `USUARIO_LISTAR`, `USUARIO_VER` |
| Estudiantes | `ESTUDIANTE_LISTAR`, `ESTUDIANTE_VER`, `ESTUDIANTE_CREAR`, `ESTUDIANTE_MODIFICAR`, `ESTUDIANTE_PROMOVER`, `ESTUDIANTE_GRADUAR`, `ESTUDIANTE_RETIRAR`, `ESTUDIANTE_REACTIVAR` |
| Solicitudes | `SOL_LISTAR`, `SOL_VER`, `SOL_CREAR`, `SOL_MODIFICAR`, `SOL_RESOLVER`, `SOL_RECHAZAR` |
| Trámites | `TRAMITE_LISTAR`, `TRAMITE_VER` |
| Docs Adjuntos | `DOCADJ_LISTAR`, `DOCADJ_VER`, `DOCADJ_CREAR`, `DOCADJ_MODIFICAR` |
| Docs Generados | `DOCGEN_LISTAR`, `DOCGEN_VER`, `DOCGEN_CREAR` |
| Notificaciones | `NOTIF_LISTAR`, `NOTIF_VER`, `NOTIF_CREAR` |
| Email | `EMAIL_ENVIAR` |
| Seguimiento | `SEGUIMIENTO_LISTAR`, `SEGUIMIENTO_VER`, `SEGUIMIENTO_CREAR`, `SEGUIMIENTO_MODIFICAR` |
| Historial | `HIST_LISTAR`, `HIST_VER`, `HIST_CREAR` |
| Requisitos | `REQUISITO_LISTAR`, `REQUISITO_VER` |
| 2FA | `AUTH2FA_CONFIGURAR`, `AUTH2FA_VERIFICAR`, `AUTH2FA_DESACTIVAR`, `AUTH2FA_ESTADO`, `AUTH2FA_REGENERAR` |
| Credenciales | `CRED_CAMBIAR_PASS`, `CRED_VER` |
| **Total** | **~45 permisos** |

#### ROLE_DEAN

Incluye los mismos permisos que `ROLE_COORDINATOR`, más:

| Módulo | Permisos adicionales |
|---|---|
| Trámites | `TRAMITE_CREAR`, `TRAMITE_MODIFICAR`, `TRAMITE_ACTIVAR`, `TRAMITE_DESACTIVAR` |
| Firmas Digital | `FIRMA_LISTAR`, `FIRMA_VER`, `FIRMA_CREAR` |
| Credenciales | `CRED_RESETEAR_PASS` |
| Carreras | `CARRERA_LISTAR` |
| Facultades | `FACULTAD_LISTAR` |
| **Total** | **~55 permisos** |

---

## 6. Endpoints Públicos (Sin Permiso)

Estos endpoints están exentos de autenticación, configurados en `SecurityConfig.filterChain()`:

```java
.requestMatchers("/api/v1/auth/**").permitAll()
.requestMatchers("/api/v1/2fa/validate*").permitAll()
```

| Endpoint | Método | Descripción |
|---|---|---|
| `POST /api/v1/auth/token` | POST | Login con credenciales / renovar con refresh token |
| `POST /api/v1/auth/2fa-verify` | POST | Verificar código 2FA tras login |
| `POST /api/v1/auth/logout` | POST | Cerrar sesión (la ruta es pública pero requiere JWT) |
| `POST /api/v1/2fa/validate` | POST | Validar código TOTP durante login |
| `POST /api/v1/2fa/validate-backup` | POST | Validar código de respaldo durante login |

**¿Por qué son públicos?**
- `/auth/token`: Es el endpoint de login — no puedes tener JWT antes de autenticarte.
- `/auth/2fa-verify`: Completa el login cuando 2FA está activo — el usuario aún no tiene JWT completo.
- `/2fa/validate` y `/2fa/validate-backup`: Se usan durante el flujo de login 2FA con `preAuthToken`.

---

## 7. Cómo Aplicar Permisos Nuevos

### 7.1 Paso a Paso

Para agregar un nuevo permiso al sistema:

**Paso 1:** Insertar el permiso en la tabla `permissions`:
```sql
INSERT INTO permissions (code, description) 
VALUES ('MODULO_ACCION', 'Descripción del permiso');
```

**Paso 2:** Asignar el permiso a uno o más roles:
```sql
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id_role, p.id_permission 
FROM roles r, permissions p 
WHERE r.role_name = 'ROLE_ADMIN' AND p.code = 'MODULO_ACCION';
```

**Paso 3:** Agregar `@PreAuthorize` al método del controlador:
```java
@GetMapping("/nueva-ruta")
@PreAuthorize("hasAuthority('MODULO_ACCION')")
public ResponseEntity<?> nuevoMetodo() { ... }
```

**Paso 4:** Si es un controlador nuevo, agregar `@PreAuthorize("isAuthenticated()")` a nivel de clase:
```java
@RestController
@RequestMapping("/api/v1/nuevo-modulo")
@PreAuthorize("isAuthenticated()")
public class NuevoController { ... }
```

### 7.2 Verificación

Después de agregar un permiso:

1. **Login como admin** → Obtener JWT.
2. **Llamar al endpoint** con el JWT → Debe retornar 200.
3. **Login como usuario sin permiso** → Llamar al endpoint → Debe retornar 403 Forbidden.
4. **Sin JWT** → Llamar al endpoint → Debe retornar 401 Unauthorized.

### 7.3 Dónde se Evalúa el Permiso

```
SecurityConfig.filterChain()
    └── JwtDecoder: valida firma RSA
    └── JwtAuthenticationConverter: extrae authorities del claim "scope"
         └── jwtGrantedAuthoritiesConverter():
             ├── JwtGrantedAuthoritiesConverter → SCOPE_xxx
             └── También agrega authority sin prefijo SCOPE_ → xxx
                  └── @PreAuthorize("hasAuthority('xxx')") ← aquí se evalúa
```

---

## 8. SQL de Inserción de Permisos

### 8.1 Inserción de Todos los Permisos

```sql
-- ═══════════════════════════════════════════════════════════
-- MÓDULO: USUARIOS
-- ═══════════════════════════════════════════════════════════
INSERT INTO permissions (code, description) VALUES ('USUARIO_LISTAR', 'Listar usuarios del sistema');
INSERT INTO permissions (code, description) VALUES ('USUARIO_VER', 'Ver detalle de un usuario');
INSERT INTO permissions (code, description) VALUES ('USUARIO_CREAR', 'Crear un nuevo usuario');
INSERT INTO permissions (code, description) VALUES ('USUARIO_MODIFICAR', 'Modificar datos de un usuario');
INSERT INTO permissions (code, description) VALUES ('USUARIO_ELIMINAR', 'Eliminar un usuario');
INSERT INTO permissions (code, description) VALUES ('USUARIO_ACTIVAR', 'Reactivar un usuario desactivado');
INSERT INTO permissions (code, description) VALUES ('USUARIO_DESACTIVAR', 'Desactivar un usuario (borrado lógico)');

-- ═══════════════════════════════════════════════════════════
-- MÓDULO: ESTUDIANTES
-- ═══════════════════════════════════════════════════════════
INSERT INTO permissions (code, description) VALUES ('ESTUDIANTE_LISTAR', 'Listar estudiantes');
INSERT INTO permissions (code, description) VALUES ('ESTUDIANTE_VER', 'Ver detalle de un estudiante');
INSERT INTO permissions (code, description) VALUES ('ESTUDIANTE_CREAR', 'Matricular un nuevo estudiante');
INSERT INTO permissions (code, description) VALUES ('ESTUDIANTE_MODIFICAR', 'Modificar datos de un estudiante');
INSERT INTO permissions (code, description) VALUES ('ESTUDIANTE_ELIMINAR', 'Eliminar un estudiante');
INSERT INTO permissions (code, description) VALUES ('ESTUDIANTE_PROMOVER', 'Promover estudiante al siguiente semestre');
INSERT INTO permissions (code, description) VALUES ('ESTUDIANTE_GRADUAR', 'Cambiar estado de estudiante a graduado');
INSERT INTO permissions (code, description) VALUES ('ESTUDIANTE_RETIRAR', 'Cambiar estado de estudiante a retirado');
INSERT INTO permissions (code, description) VALUES ('ESTUDIANTE_REACTIVAR', 'Reactivar un estudiante retirado/inactivo');

-- ═══════════════════════════════════════════════════════════
-- MÓDULO: ROLES
-- ═══════════════════════════════════════════════════════════
INSERT INTO permissions (code, description) VALUES ('ROL_LISTAR', 'Listar roles del sistema');
INSERT INTO permissions (code, description) VALUES ('ROL_VER', 'Ver detalle de un rol');
INSERT INTO permissions (code, description) VALUES ('ROL_CREAR', 'Crear un nuevo rol');
INSERT INTO permissions (code, description) VALUES ('ROL_MODIFICAR', 'Modificar un rol existente');
INSERT INTO permissions (code, description) VALUES ('ROL_ELIMINAR', 'Eliminar un rol (excepto protegidos)');
INSERT INTO permissions (code, description) VALUES ('ROL_ASIGNAR_PERMISO', 'Asignar permisos a un rol');
INSERT INTO permissions (code, description) VALUES ('ROL_REMOVER_PERMISO', 'Remover permisos de un rol');
INSERT INTO permissions (code, description) VALUES ('ROL_ASIGNAR_USUARIO', 'Asignar un rol a un usuario');
INSERT INTO permissions (code, description) VALUES ('ROL_REMOVER_USUARIO', 'Remover un rol de un usuario');

-- ═══════════════════════════════════════════════════════════
-- MÓDULO: CREDENCIALES
-- ═══════════════════════════════════════════════════════════
INSERT INTO permissions (code, description) VALUES ('CRED_LISTAR', 'Listar credenciales');
INSERT INTO permissions (code, description) VALUES ('CRED_VER', 'Ver detalle de credencial');
INSERT INTO permissions (code, description) VALUES ('CRED_CREAR', 'Crear credencial');
INSERT INTO permissions (code, description) VALUES ('CRED_CAMBIAR_PASS', 'Cambiar contraseña propia');
INSERT INTO permissions (code, description) VALUES ('CRED_RESETEAR_PASS', 'Resetear contraseña de otro usuario');
INSERT INTO permissions (code, description) VALUES ('CRED_BLOQUEAR', 'Bloquear una cuenta');
INSERT INTO permissions (code, description) VALUES ('CRED_DESBLOQUEAR', 'Desbloquear una cuenta');
INSERT INTO permissions (code, description) VALUES ('CRED_ELIMINAR', 'Eliminar una credencial');

-- ═══════════════════════════════════════════════════════════
-- MÓDULO: AUTENTICACIÓN 2FA
-- ═══════════════════════════════════════════════════════════
INSERT INTO permissions (code, description) VALUES ('AUTH2FA_CONFIGURAR', 'Configurar 2FA (generar clave TOTP)');
INSERT INTO permissions (code, description) VALUES ('AUTH2FA_VERIFICAR', 'Verificar y activar 2FA');
INSERT INTO permissions (code, description) VALUES ('AUTH2FA_DESACTIVAR', 'Desactivar 2FA');
INSERT INTO permissions (code, description) VALUES ('AUTH2FA_ESTADO', 'Consultar estado de 2FA');
INSERT INTO permissions (code, description) VALUES ('AUTH2FA_REGENERAR', 'Regenerar códigos de respaldo');

-- ═══════════════════════════════════════════════════════════
-- MÓDULO: SOLICITUDES
-- ═══════════════════════════════════════════════════════════
INSERT INTO permissions (code, description) VALUES ('SOL_LISTAR', 'Listar solicitudes');
INSERT INTO permissions (code, description) VALUES ('SOL_VER', 'Ver detalle de una solicitud');
INSERT INTO permissions (code, description) VALUES ('SOL_CREAR', 'Crear una solicitud');
INSERT INTO permissions (code, description) VALUES ('SOL_MODIFICAR', 'Modificar una solicitud');
INSERT INTO permissions (code, description) VALUES ('SOL_ELIMINAR', 'Eliminar una solicitud');
INSERT INTO permissions (code, description) VALUES ('SOL_RESOLVER', 'Resolver una solicitud');
INSERT INTO permissions (code, description) VALUES ('SOL_RECHAZAR', 'Rechazar una solicitud');

-- ═══════════════════════════════════════════════════════════
-- MÓDULO: TRÁMITES
-- ═══════════════════════════════════════════════════════════
INSERT INTO permissions (code, description) VALUES ('TRAMITE_LISTAR', 'Listar trámites');
INSERT INTO permissions (code, description) VALUES ('TRAMITE_VER', 'Ver detalle de un trámite');
INSERT INTO permissions (code, description) VALUES ('TRAMITE_CREAR', 'Crear un trámite');
INSERT INTO permissions (code, description) VALUES ('TRAMITE_MODIFICAR', 'Modificar un trámite');
INSERT INTO permissions (code, description) VALUES ('TRAMITE_ACTIVAR', 'Activar un trámite');
INSERT INTO permissions (code, description) VALUES ('TRAMITE_DESACTIVAR', 'Desactivar un trámite');
INSERT INTO permissions (code, description) VALUES ('TRAMITE_ELIMINAR', 'Eliminar un trámite');

-- ═══════════════════════════════════════════════════════════
-- MÓDULO: CALENDARIOS ACADÉMICOS
-- ═══════════════════════════════════════════════════════════
INSERT INTO permissions (code, description) VALUES ('CAL_CREAR', 'Crear período académico');
INSERT INTO permissions (code, description) VALUES ('CAL_MODIFICAR', 'Modificar período académico');
INSERT INTO permissions (code, description) VALUES ('CAL_ELIMINAR', 'Eliminar período académico');
INSERT INTO permissions (code, description) VALUES ('CAL_LISTAR', 'Listar períodos académicos');

-- ═══════════════════════════════════════════════════════════
-- MÓDULO: CARRERAS
-- ═══════════════════════════════════════════════════════════
INSERT INTO permissions (code, description) VALUES ('CARRERA_CREAR', 'Crear una carrera');
INSERT INTO permissions (code, description) VALUES ('CARRERA_MODIFICAR', 'Modificar una carrera');
INSERT INTO permissions (code, description) VALUES ('CARRERA_ELIMINAR', 'Eliminar una carrera');
INSERT INTO permissions (code, description) VALUES ('CARRERA_LISTAR', 'Listar carreras');

-- ═══════════════════════════════════════════════════════════
-- MÓDULO: CONFIGURACIÓN
-- ═══════════════════════════════════════════════════════════
INSERT INTO permissions (code, description) VALUES ('CONFIG_CREAR', 'Crear configuración');
INSERT INTO permissions (code, description) VALUES ('CONFIG_MODIFICAR', 'Modificar configuración');
INSERT INTO permissions (code, description) VALUES ('CONFIG_ELIMINAR', 'Eliminar configuración');
INSERT INTO permissions (code, description) VALUES ('CONFIG_LISTAR', 'Listar configuraciones');

-- ═══════════════════════════════════════════════════════════
-- MÓDULO: REGLAS DE PLAZO
-- ═══════════════════════════════════════════════════════════
INSERT INTO permissions (code, description) VALUES ('REGLA_CREAR', 'Crear regla de plazo');
INSERT INTO permissions (code, description) VALUES ('REGLA_MODIFICAR', 'Modificar regla de plazo');
INSERT INTO permissions (code, description) VALUES ('REGLA_ELIMINAR', 'Eliminar regla de plazo');
INSERT INTO permissions (code, description) VALUES ('REGLA_LISTAR', 'Listar reglas de plazo');

-- ═══════════════════════════════════════════════════════════
-- MÓDULO: PLANTILLAS DE DOCUMENTOS
-- ═══════════════════════════════════════════════════════════
INSERT INTO permissions (code, description) VALUES ('PLANTILLA_CREAR', 'Crear plantilla de documento');
INSERT INTO permissions (code, description) VALUES ('PLANTILLA_MODIFICAR', 'Modificar plantilla de documento');
INSERT INTO permissions (code, description) VALUES ('PLANTILLA_ELIMINAR', 'Eliminar plantilla de documento');
INSERT INTO permissions (code, description) VALUES ('PLANTILLA_LISTAR', 'Listar plantillas de documentos');

-- ═══════════════════════════════════════════════════════════
-- MÓDULO: FACULTADES
-- ═══════════════════════════════════════════════════════════
INSERT INTO permissions (code, description) VALUES ('FACULTAD_CREAR', 'Crear una facultad');
INSERT INTO permissions (code, description) VALUES ('FACULTAD_MODIFICAR', 'Modificar una facultad');
INSERT INTO permissions (code, description) VALUES ('FACULTAD_ELIMINAR', 'Eliminar una facultad');
INSERT INTO permissions (code, description) VALUES ('FACULTAD_LISTAR', 'Listar facultades');

-- ═══════════════════════════════════════════════════════════
-- MÓDULO: PERMISOS
-- ═══════════════════════════════════════════════════════════
INSERT INTO permissions (code, description) VALUES ('PERMISO_CREAR', 'Crear un permiso');
INSERT INTO permissions (code, description) VALUES ('PERMISO_MODIFICAR', 'Modificar un permiso');
INSERT INTO permissions (code, description) VALUES ('PERMISO_ELIMINAR', 'Eliminar un permiso');
INSERT INTO permissions (code, description) VALUES ('PERMISO_LISTAR', 'Listar permisos');

-- ═══════════════════════════════════════════════════════════
-- MÓDULO: ETAPAS DE PROCESAMIENTO
-- ═══════════════════════════════════════════════════════════
INSERT INTO permissions (code, description) VALUES ('ETAPA_CREAR', 'Crear etapa de procesamiento');
INSERT INTO permissions (code, description) VALUES ('ETAPA_MODIFICAR', 'Modificar etapa de procesamiento');
INSERT INTO permissions (code, description) VALUES ('ETAPA_ELIMINAR', 'Eliminar etapa de procesamiento');
INSERT INTO permissions (code, description) VALUES ('ETAPA_LISTAR', 'Listar etapas de procesamiento');

-- ═══════════════════════════════════════════════════════════
-- MÓDULO: MOTIVOS DE RECHAZO
-- ═══════════════════════════════════════════════════════════
INSERT INTO permissions (code, description) VALUES ('RECHAZO_CREAR', 'Crear motivo de rechazo');
INSERT INTO permissions (code, description) VALUES ('RECHAZO_MODIFICAR', 'Modificar motivo de rechazo');
INSERT INTO permissions (code, description) VALUES ('RECHAZO_ELIMINAR', 'Eliminar motivo de rechazo');
INSERT INTO permissions (code, description) VALUES ('RECHAZO_LISTAR', 'Listar motivos de rechazo');

-- ═══════════════════════════════════════════════════════════
-- MÓDULO: ESTADOS
-- ═══════════════════════════════════════════════════════════
INSERT INTO permissions (code, description) VALUES ('ESTADO_CREAR', 'Crear un estado');
INSERT INTO permissions (code, description) VALUES ('ESTADO_MODIFICAR', 'Modificar un estado');
INSERT INTO permissions (code, description) VALUES ('ESTADO_ELIMINAR', 'Eliminar un estado');
INSERT INTO permissions (code, description) VALUES ('ESTADO_LISTAR', 'Listar estados');

-- ═══════════════════════════════════════════════════════════
-- MÓDULO: FLUJOS DE TRABAJO
-- ═══════════════════════════════════════════════════════════
INSERT INTO permissions (code, description) VALUES ('FLUJO_CREAR', 'Crear flujo de trabajo');
INSERT INTO permissions (code, description) VALUES ('FLUJO_MODIFICAR', 'Modificar flujo de trabajo');
INSERT INTO permissions (code, description) VALUES ('FLUJO_ELIMINAR', 'Eliminar flujo de trabajo');
INSERT INTO permissions (code, description) VALUES ('FLUJO_LISTAR', 'Listar flujos de trabajo');

-- ═══════════════════════════════════════════════════════════
-- MÓDULO: FLUJO-ETAPAS
-- ═══════════════════════════════════════════════════════════
INSERT INTO permissions (code, description) VALUES ('FLUJOETAPA_LISTAR', 'Listar relaciones flujo-etapa');
INSERT INTO permissions (code, description) VALUES ('FLUJOETAPA_VER', 'Ver detalle de flujo-etapa');
INSERT INTO permissions (code, description) VALUES ('FLUJOETAPA_CREAR', 'Crear relación flujo-etapa');
INSERT INTO permissions (code, description) VALUES ('FLUJOETAPA_MODIFICAR', 'Modificar relación flujo-etapa');
INSERT INTO permissions (code, description) VALUES ('FLUJOETAPA_ELIMINAR', 'Eliminar relación flujo-etapa');

-- ═══════════════════════════════════════════════════════════
-- MÓDULO: SEGUIMIENTO DE ETAPAS
-- ═══════════════════════════════════════════════════════════
INSERT INTO permissions (code, description) VALUES ('SEGUIMIENTO_LISTAR', 'Listar seguimientos de etapas');
INSERT INTO permissions (code, description) VALUES ('SEGUIMIENTO_VER', 'Ver detalle de seguimiento');
INSERT INTO permissions (code, description) VALUES ('SEGUIMIENTO_CREAR', 'Crear seguimiento');
INSERT INTO permissions (code, description) VALUES ('SEGUIMIENTO_MODIFICAR', 'Modificar seguimiento');
INSERT INTO permissions (code, description) VALUES ('SEGUIMIENTO_ELIMINAR', 'Eliminar seguimiento');

-- ═══════════════════════════════════════════════════════════
-- MÓDULO: SESIONES
-- ═══════════════════════════════════════════════════════════
INSERT INTO permissions (code, description) VALUES ('SESION_LISTAR', 'Listar sesiones activas');
INSERT INTO permissions (code, description) VALUES ('SESION_VER', 'Ver detalle de sesión');
INSERT INTO permissions (code, description) VALUES ('SESION_CREAR', 'Crear sesión');
INSERT INTO permissions (code, description) VALUES ('SESION_MODIFICAR', 'Modificar sesión');
INSERT INTO permissions (code, description) VALUES ('SESION_ELIMINAR', 'Eliminar sesión');

-- ═══════════════════════════════════════════════════════════
-- MÓDULO: REFRESH TOKENS
-- ═══════════════════════════════════════════════════════════
INSERT INTO permissions (code, description) VALUES ('TOKEN_LISTAR', 'Listar refresh tokens');
INSERT INTO permissions (code, description) VALUES ('TOKEN_VER', 'Ver detalle de refresh token');
INSERT INTO permissions (code, description) VALUES ('TOKEN_ELIMINAR', 'Revocar un refresh token');

-- ═══════════════════════════════════════════════════════════
-- MÓDULO: TIPOS DE NOTIFICACIÓN
-- ═══════════════════════════════════════════════════════════
INSERT INTO permissions (code, description) VALUES ('TIPNOTIF_LISTAR', 'Listar tipos de notificación');
INSERT INTO permissions (code, description) VALUES ('TIPNOTIF_VER', 'Ver detalle de tipo de notificación');
INSERT INTO permissions (code, description) VALUES ('TIPNOTIF_CREAR', 'Crear tipo de notificación');
INSERT INTO permissions (code, description) VALUES ('TIPNOTIF_MODIFICAR', 'Modificar tipo de notificación');
INSERT INTO permissions (code, description) VALUES ('TIPNOTIF_ELIMINAR', 'Eliminar tipo de notificación');

-- ═══════════════════════════════════════════════════════════
-- MÓDULO: NOTIFICACIONES
-- ═══════════════════════════════════════════════════════════
INSERT INTO permissions (code, description) VALUES ('NOTIF_LISTAR', 'Listar notificaciones');
INSERT INTO permissions (code, description) VALUES ('NOTIF_VER', 'Ver detalle de notificación');
INSERT INTO permissions (code, description) VALUES ('NOTIF_CREAR', 'Crear una notificación');
INSERT INTO permissions (code, description) VALUES ('NOTIF_MODIFICAR', 'Modificar una notificación');
INSERT INTO permissions (code, description) VALUES ('NOTIF_ELIMINAR', 'Eliminar una notificación');

-- ═══════════════════════════════════════════════════════════
-- MÓDULO: DOCUMENTOS GENERADOS
-- ═══════════════════════════════════════════════════════════
INSERT INTO permissions (code, description) VALUES ('DOCGEN_LISTAR', 'Listar documentos generados');
INSERT INTO permissions (code, description) VALUES ('DOCGEN_VER', 'Ver detalle de documento generado');
INSERT INTO permissions (code, description) VALUES ('DOCGEN_CREAR', 'Crear registro de documento generado');
INSERT INTO permissions (code, description) VALUES ('DOCGEN_MODIFICAR', 'Modificar registro de documento generado');
INSERT INTO permissions (code, description) VALUES ('DOCGEN_ELIMINAR', 'Eliminar registro de documento generado');

-- ═══════════════════════════════════════════════════════════
-- MÓDULO: FIRMAS DIGITALES
-- ═══════════════════════════════════════════════════════════
INSERT INTO permissions (code, description) VALUES ('FIRMA_LISTAR', 'Listar firmas digitales');
INSERT INTO permissions (code, description) VALUES ('FIRMA_VER', 'Ver detalle de firma digital');
INSERT INTO permissions (code, description) VALUES ('FIRMA_CREAR', 'Registrar firma digital');
INSERT INTO permissions (code, description) VALUES ('FIRMA_MODIFICAR', 'Modificar firma digital');
INSERT INTO permissions (code, description) VALUES ('FIRMA_ELIMINAR', 'Eliminar firma digital');

-- ═══════════════════════════════════════════════════════════
-- MÓDULO: DOCUMENTOS ADJUNTOS
-- ═══════════════════════════════════════════════════════════
INSERT INTO permissions (code, description) VALUES ('DOCADJ_LISTAR', 'Listar documentos adjuntos');
INSERT INTO permissions (code, description) VALUES ('DOCADJ_VER', 'Ver detalle de documento adjunto');
INSERT INTO permissions (code, description) VALUES ('DOCADJ_CREAR', 'Subir documento adjunto');
INSERT INTO permissions (code, description) VALUES ('DOCADJ_MODIFICAR', 'Modificar metadatos de documento adjunto');
INSERT INTO permissions (code, description) VALUES ('DOCADJ_ELIMINAR', 'Eliminar documento adjunto');

-- ═══════════════════════════════════════════════════════════
-- MÓDULO: HISTORIAL DE ETAPAS
-- ═══════════════════════════════════════════════════════════
INSERT INTO permissions (code, description) VALUES ('HIST_LISTAR', 'Listar historial de etapas');
INSERT INTO permissions (code, description) VALUES ('HIST_VER', 'Ver detalle de historial');
INSERT INTO permissions (code, description) VALUES ('HIST_CREAR', 'Crear registro de historial');
INSERT INTO permissions (code, description) VALUES ('HIST_MODIFICAR', 'Modificar registro de historial');
INSERT INTO permissions (code, description) VALUES ('HIST_ELIMINAR', 'Eliminar registro de historial');

-- ═══════════════════════════════════════════════════════════
-- MÓDULO: REQUISITOS
-- ═══════════════════════════════════════════════════════════
INSERT INTO permissions (code, description) VALUES ('REQUISITO_LISTAR', 'Listar requisitos de trámites');
INSERT INTO permissions (code, description) VALUES ('REQUISITO_VER', 'Ver detalle de requisito');
INSERT INTO permissions (code, description) VALUES ('REQUISITO_CREAR', 'Crear un requisito');
INSERT INTO permissions (code, description) VALUES ('REQUISITO_MODIFICAR', 'Modificar un requisito');
INSERT INTO permissions (code, description) VALUES ('REQUISITO_ELIMINAR', 'Eliminar un requisito');

-- ═══════════════════════════════════════════════════════════
-- MÓDULO: EMAIL
-- ═══════════════════════════════════════════════════════════
INSERT INTO permissions (code, description) VALUES ('EMAIL_ENVIAR', 'Enviar correos electrónicos');
```

### 8.2 Asignar Todos los Permisos al Rol ROLE_ADMIN

```sql
-- Asignar TODOS los permisos a ROLE_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id_role, p.id_permission
FROM roles r
CROSS JOIN permissions p
WHERE r.role_name = 'ROLE_ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp 
    WHERE rp.role_id = r.id_role AND rp.permission_id = p.id_permission
);
```

---

## 9. Preguntas Frecuentes

### ¿Qué pasa si un endpoint no tiene `@PreAuthorize` a nivel método?

Solo se evalúa la anotación a nivel de clase (`isAuthenticated()`). Cualquier usuario autenticado con JWT válido podrá acceder. Ejemplo: `GET /{id}/password-expired` en `CredentialsController`.

### ¿Un usuario puede tener permisos sin roles?

No. Los permisos se asignan a **roles**, no directamente a usuarios. El flujo es: `Usuario → Roles → Permisos`.

### ¿Qué pasa si un permiso existe en BD pero no hay `@PreAuthorize` que lo use?

No tiene efecto. El permiso se carga como `GrantedAuthority` en el JWT pero ningún endpoint lo valida.

### ¿Qué pasa si un `@PreAuthorize` referencia un permiso que no existe en BD?

El endpoint siempre retornará **403 Forbidden**, ya que ningún usuario tendrá ese authority en su JWT.

### ¿Puedo usar `hasRole()` en lugar de `hasAuthority()`?

Sí, pero solo para los nombres de roles. `hasRole('ADMIN')` busca la authority `ROLE_ADMIN` (agrega el prefijo `ROLE_` automáticamente). Para permisos granulares como `SOL_CREAR`, debes usar `hasAuthority('SOL_CREAR')`.

### ¿Cómo verifico qué permisos tiene un usuario en su JWT?

Decodifica el JWT (puedes usar jwt.io) y busca el claim `scope`. Contendrá los roles y permisos separados por espacio:

```
"scope": "ROLE_ADMIN USUARIO_LISTAR USUARIO_VER SOL_CREAR SOL_LISTAR ..."
```

### ¿Puedo combinar permisos con `and` / `or` en `@PreAuthorize`?

Sí:
```java
@PreAuthorize("hasAuthority('SOL_CREAR') and hasAuthority('SOL_LISTAR')")  // ambos
@PreAuthorize("hasAuthority('SOL_CREAR') or hasAuthority('SOL_MODIFICAR')") // cualquiera
@PreAuthorize("hasRole('ADMIN') or hasAuthority('SOL_CREAR')")              // mixto
```

### ¿Cuántos permisos tiene el sistema actualmente?

**153 permisos** distribuidos en 29 módulos. Ver la [Sección 5.1](#51-resumen-de-permisos-totales) para el desglose completo.
