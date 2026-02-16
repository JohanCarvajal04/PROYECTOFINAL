# Guía de Permisos por Método — SGTE Backend

> **Proyecto:** Sistema de Gestión de Trámites Estudiantiles (SGTE)  
> **Framework:** Spring Boot 3.2.2 + Spring Security + OAuth2 Resource Server  
> **Fecha:** Febrero 2026

---

## Tabla de Contenidos

1. [Arquitectura del Sistema de Permisos](#1-arquitectura-del-sistema-de-permisos)
2. [Cómo Funciona](#2-cómo-funciona)
3. [Nomenclatura de Códigos de Permisos](#3-nomenclatura-de-códigos-de-permisos)
4. [Catálogo Completo de Permisos por Controlador](#4-catálogo-completo-de-permisos-por-controlador)
5. [Matriz de Permisos por Rol](#5-matriz-de-permisos-por-rol)
6. [Cómo Aplicar Permisos Nuevos](#6-cómo-aplicar-permisos-nuevos)
7. [Endpoints Públicos (Sin Permiso)](#7-endpoints-públicos-sin-permiso)
8. [Preguntas Frecuentes](#8-preguntas-frecuentes)

---

## 1. Arquitectura del Sistema de Permisos

### 1.1 Modelo de Datos

```
Users ──── N:M ──── Roles ──── N:M ──── Permissions
                                           │
                                      code (VARCHAR)
                                      description (VARCHAR)
```

- Un **usuario** puede tener múltiples **roles** (ROLE_ADMIN, ROLE_COORDINATOR, etc.)
- Cada **rol** puede tener múltiples **permisos** (CAL_CREAR, SOL_LISTAR, etc.)
- Los permisos se verifican en cada endpoint mediante `@PreAuthorize`

### 1.2 Flujo de Autorización

```
1. Usuario envía request con JWT Bearer Token
        │
2. Spring Security extrae el subject (email) del JWT
        │
3. CustomUserDetailsService carga el usuario por email
        │
4. Para cada ROL del usuario:
   ├── Agrega authority: "ROLE_ADMIN", "ROLE_COORDINATOR", etc.
   └── Para cada PERMISO del rol:
       └── Agrega authority: "CAL_CREAR", "SOL_LISTAR", etc.
        │
5. @PreAuthorize("hasAuthority('XXX')") verifica si el usuario
   tiene la authority correspondiente
        │
6. ✅ Acceso permitido  ó  ❌ 403 Forbidden
```

### 1.3 Clases Involucradas

| Clase | Ubicación | Responsabilidad |
|-------|-----------|-----------------|
| `CustomUserDetailsService` | `Services/Impl/` | Carga usuario + roles + permisos como authorities |
| `SecurityConfig` | `Config/` | Configura Spring Security, habilita `@PreAuthorize` |
| `Roles` (Entity) | `Entity/` | Relación `@ManyToMany(fetch = EAGER)` con `Permissions` |
| `Permissions` (Entity) | `Entity/` | Contiene `code` y `description` |

### 1.4 CustomUserDetailsService — Código Clave

```java
@Override
public UserDetails loadUserByUsername(String email) {
    Users user = usersRepository.findByInstitutionalEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

    List<GrantedAuthority> authorities = new ArrayList<>();

    for (Roles role : user.getRoles()) {
        // Agrega el rol como authority (ej: ROLE_ADMIN)
        authorities.add(new SimpleGrantedAuthority(role.getRoleName()));

        // Agrega cada permiso del rol como authority (ej: CAL_CREAR)
        for (Permissions permission : role.getPermissions()) {
            SimpleGrantedAuthority permAuthority =
                new SimpleGrantedAuthority(permission.getCode());
            if (!authorities.contains(permAuthority)) {
                authorities.add(permAuthority);
            }
        }
    }

    return new User(user.getInstitutionalEmail(),
                    credential.getPasswordHash(), authorities);
}
```

---

## 2. Cómo Funciona

### 2.1 Niveles de Seguridad

El sistema usa **dos niveles** de `@PreAuthorize`:

| Nivel | Dónde | Ejemplo | Propósito |
|-------|-------|---------|-----------|
| **Clase** | `@PreAuthorize` en la clase del controlador | `@PreAuthorize("isAuthenticated()")` | Garantiza que TODOS los endpoints requieren autenticación |
| **Método** | `@PreAuthorize` en cada método | `@PreAuthorize("hasAuthority('CAL_CREAR')")` | Controla acceso granular por operación |

### 2.2 Expresiones SpEL Utilizadas

| Expresión | Significado | Uso |
|-----------|-------------|-----|
| `isAuthenticated()` | Usuario tiene JWT válido | Seguridad base de clase |
| `hasAuthority('XXX')` | Usuario tiene el permiso exacto `XXX` | Seguridad por método |
| `hasRole('ADMIN')` | ~~Obsoleto~~ — migrado a `hasAuthority` | Ya no se usa |
| `hasAnyRole(...)` | ~~Obsoleto~~ — migrado a `hasAuthority` | Ya no se usa |

### 2.3 Ejemplo Práctico

```java
@RestController
@RequestMapping("/api/v1/careers")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")  // ← Nivel clase: requiere JWT
public class CareerController {

    @PostMapping
    @PreAuthorize("hasAuthority('CARRERA_CREAR')")  // ← Nivel método: permiso específico
    public ResponseEntity<?> create(@Valid @RequestBody CCareerRequest request) {
        service.createCareer(request);
        return ResponseEntity.ok("Carrera creada correctamente");
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CARRERA_LISTAR')")  // ← Otro permiso
    public ResponseEntity<?> list(@RequestParam(required = false) Integer facultyId) {
        return ResponseEntity.ok(service.listCareer(facultyId));
    }
}
```

**Resultado:**
- Si un usuario con rol `ROLE_STUDENT` intenta `POST /api/v1/careers`, y `ROLE_STUDENT` **no** tiene el permiso `CARRERA_CREAR`, recibirá `403 Forbidden`.
- Si tiene `CARRERA_LISTAR`, podrá hacer `GET /api/v1/careers` exitosamente.

---

## 3. Nomenclatura de Códigos de Permisos

### 3.1 Patrón

```
{MÓDULO}_{ACCIÓN}
```

### 3.2 Módulos

| Código Módulo | Módulo | Controlador |
|---------------|--------|-------------|
| `CAL` | Calendario Académico | AcademicCalendarController |
| `SOL` | Solicitudes | ApplicationsController |
| `HIST` | Historial de Etapas | ApplicationStageHistoryController |
| `DOCADJ` | Documentos Adjuntos | AttachedDocumentsController |
| `CARRERA` | Carreras | CareerController |
| `CONFIG` | Configuración | ConfigurationController |
| `CRED` | Credenciales | CredentialsController |
| `REGLA` | Reglas de Plazo | DeadlineruleControllers |
| `FIRMA` | Firmas Digitales | DigitalSignaturesController |
| `DOCGEN` | Documentos Generados | DocumentsGeneratedController |
| `PLANTILLA` | Plantillas | DocumentTemplateController |
| `ESTUDIANTE` | Estudiantes | StudentsController |
| `FACULTAD` | Facultades | FacultyController |
| `NOTIF` | Notificaciones | NotificationController |
| `TIPNOTIF` | Tipos de Notificación | NotificationTypeController |
| `PERMISO` | Permisos | PermissionController |
| `TRAMITE` | Trámites | ProceduresController |
| `ETAPA` | Etapas de Procesamiento | ProcessingStageController |
| `RECHAZO` | Razones de Rechazo | RejectReasonController |
| `TOKEN` | Tokens de Refresco | RefreshTokenController |
| `REQUISITO` | Requisitos | RequirementsController |
| `ROL` | Roles | RolesController |
| `SESION` | Sesiones | SessionTokenController |
| `SEGUIMIENTO` | Seguimiento | StageTrackingController |
| `ESTADO` | Estados | StatesController |
| `USUARIO` | Usuarios | UsersController |
| `FLUJO` | Flujos de Trabajo | WorkFlowsController |
| `FLUJOETAPA` | Etapas de Flujo | WorkflowStagesController |
| `AUTH2FA` | Autenticación 2FA | TwoFactorAuthController |

### 3.3 Acciones Comunes

| Acción | Operación HTTP | Descripción |
|--------|---------------|-------------|
| `LISTAR` | GET (lista) | Ver todos los registros |
| `VER` | GET (by id) | Ver un registro específico |
| `CREAR` | POST | Crear nuevo registro |
| `MODIFICAR` | PUT | Actualizar registro existente |
| `ELIMINAR` | DELETE | Eliminar registro |
| `ACTIVAR` | POST | Activar registro inactivo |
| `DESACTIVAR` | PATCH/POST | Desactivar registro activo |

### 3.4 Acciones Especiales

| Código | Descripción |
|--------|-------------|
| `SOL_RESOLVER` | Resolver/aprobar una solicitud |
| `SOL_RECHAZAR` | Rechazar una solicitud |
| `CRED_CAMBIAR_PASS` | Cambiar contraseña |
| `CRED_RESETEAR_PASS` | Resetear contraseña (admin) |
| `CRED_BLOQUEAR` | Bloquear cuenta de usuario |
| `CRED_DESBLOQUEAR` | Desbloquear cuenta de usuario |
| `ESTUDIANTE_PROMOVER` | Promover estudiante al siguiente semestre |
| `ESTUDIANTE_GRADUAR` | Graduar estudiante |
| `ESTUDIANTE_RETIRAR` | Retirar estudiante |
| `ESTUDIANTE_REACTIVAR` | Reactivar estudiante retirado |
| `ROL_ASIGNAR_PERMISO` | Asignar permisos a un rol |
| `ROL_REMOVER_PERMISO` | Remover permisos de un rol |
| `ROL_ASIGNAR_USUARIO` | Asignar rol a usuario |
| `ROL_REMOVER_USUARIO` | Remover rol de usuario |
| `AUTH2FA_CONFIGURAR` | Iniciar configuración 2FA |
| `AUTH2FA_VERIFICAR` | Verificar código TOTP |
| `AUTH2FA_DESACTIVAR` | Desactivar 2FA |
| `AUTH2FA_ESTADO` | Consultar estado 2FA |
| `AUTH2FA_REGENERAR` | Regenerar códigos de respaldo |

---

## 4. Catálogo Completo de Permisos por Controlador

### 4.1 AcademicCalendarController (`/api/v1/academic-calendar`)

| Método HTTP | Endpoint | Permiso | Descripción |
|-------------|----------|---------|-------------|
| POST | `/` | `CAL_CREAR` | Crear calendario |
| PUT | `/{id}` | `CAL_MODIFICAR` | Actualizar calendario |
| DELETE | `/{id}` | `CAL_ELIMINAR` | Eliminar calendario |
| GET | `/` | `CAL_LISTAR` | Listar calendarios |

### 4.2 ApplicationsController (`/api/v1/applications`)

| Método HTTP | Endpoint | Permiso | Descripción |
|-------------|----------|---------|-------------|
| GET | `/legacy` | `SOL_LISTAR` | Listar solicitudes (legacy) |
| GET | `/legacy/{id}` | `SOL_VER` | Ver solicitud (legacy) |
| GET | `/` | `SOL_LISTAR` | Listar solicitudes |
| GET | `/{id}` | `SOL_VER` | Ver solicitud |
| GET | `/code/{code}` | `SOL_VER` | Buscar por código |
| GET | `/user/{userId}` | `SOL_LISTAR` | Listar por usuario |
| POST | `/` | `SOL_CREAR` | Crear solicitud |
| PUT | `/{id}` | `SOL_MODIFICAR` | Modificar solicitud |
| POST | `/{id}/resolve` | `SOL_RESOLVER` | Resolver solicitud |
| POST | `/{id}/reject` | `SOL_RECHAZAR` | Rechazar solicitud |
| DELETE | `/{id}` | `SOL_ELIMINAR` | Eliminar solicitud |

### 4.3 ApplicationStageHistoryController (`/api/v1/application-stage-history`)

| Método HTTP | Endpoint | Permiso | Descripción |
|-------------|----------|---------|-------------|
| GET | `/` | `HIST_LISTAR` | Listar historial |
| GET | `/{id}` | `HIST_VER` | Ver historial |
| POST | `/` | `HIST_CREAR` | Crear historial |
| PUT | `/{id}` | `HIST_MODIFICAR` | Modificar historial |
| DELETE | `/{id}` | `HIST_ELIMINAR` | Eliminar historial |

### 4.4 AttachedDocumentsController (`/api/v1/attached-documents`)

| Método HTTP | Endpoint | Permiso | Descripción |
|-------------|----------|---------|-------------|
| GET | `/` | `DOCADJ_LISTAR` | Listar documentos |
| GET | `/{id}` | `DOCADJ_VER` | Ver documento |
| POST | `/` | `DOCADJ_CREAR` | Crear documento |
| PUT | `/{id}` | `DOCADJ_MODIFICAR` | Modificar documento |
| DELETE | `/{id}` | `DOCADJ_ELIMINAR` | Eliminar documento |

### 4.5 CareerController (`/api/v1/career`)

| Método HTTP | Endpoint | Permiso | Descripción |
|-------------|----------|---------|-------------|
| POST | `/` | `CARRERA_CREAR` | Crear carrera |
| PUT | `/{id}` | `CARRERA_MODIFICAR` | Modificar carrera |
| DELETE | `/{id}` | `CARRERA_ELIMINAR` | Eliminar carrera |
| GET | `/` | `CARRERA_LISTAR` | Listar carreras |

### 4.6 ConfigurationController (`/api/v1/configuration`)

| Método HTTP | Endpoint | Permiso | Descripción |
|-------------|----------|---------|-------------|
| POST | `/` | `CONFIG_CREAR` | Crear configuración |
| PUT | `/{id}` | `CONFIG_MODIFICAR` | Modificar configuración |
| DELETE | `/{id}` | `CONFIG_ELIMINAR` | Eliminar configuración |
| GET | `/` | `CONFIG_LISTAR` | Listar configuraciones |

### 4.7 CredentialsController (`/api/v1/credentials`)

| Método HTTP | Endpoint | Permiso | Descripción |
|-------------|----------|---------|-------------|
| GET | `/` | `CRED_LISTAR` | Listar credenciales |
| GET | `/{id}` | `CRED_VER` | Ver credencial |
| POST | `/` | `CRED_CREAR` | Crear credencial |
| POST | `/{id}/change-password` | `CRED_CAMBIAR_PASS` | Cambiar contraseña |
| POST | `/{id}/reset-password` | `CRED_RESETEAR_PASS` | Resetear contraseña |
| GET | `/{id}/password-expired` | *isAuthenticated* | Verificar expiración |
| POST | `/{id}/lock` | `CRED_BLOQUEAR` | Bloquear cuenta |
| POST | `/{id}/unlock` | `CRED_DESBLOQUEAR` | Desbloquear cuenta |
| DELETE | `/{id}` | `CRED_ELIMINAR` | Eliminar credencial |

### 4.8 DeadlineruleControllers (`/api/v1/deadlinerule`)

| Método HTTP | Endpoint | Permiso | Descripción |
|-------------|----------|---------|-------------|
| POST | `/` | `REGLA_CREAR` | Crear regla |
| PUT | `/{id}` | `REGLA_MODIFICAR` | Modificar regla |
| DELETE | `/{id}` | `REGLA_ELIMINAR` | Eliminar regla |
| GET | `/` | `REGLA_LISTAR` | Listar reglas |

### 4.9 DigitalSignaturesController (`/api/v1/digital-signatures`)

| Método HTTP | Endpoint | Permiso | Descripción |
|-------------|----------|---------|-------------|
| GET | `/` | `FIRMA_LISTAR` | Listar firmas |
| GET | `/{id}` | `FIRMA_VER` | Ver firma |
| POST | `/` | `FIRMA_CREAR` | Crear firma |
| PUT | `/{id}` | `FIRMA_MODIFICAR` | Modificar firma |
| DELETE | `/{id}` | `FIRMA_ELIMINAR` | Eliminar firma |

### 4.10 DocumentsGeneratedController (`/api/v1/documents-generated`)

| Método HTTP | Endpoint | Permiso | Descripción |
|-------------|----------|---------|-------------|
| GET | `/` | `DOCGEN_LISTAR` | Listar documentos |
| GET | `/{id}` | `DOCGEN_VER` | Ver documento |
| POST | `/` | `DOCGEN_CREAR` | Crear documento |
| PUT | `/{id}` | `DOCGEN_MODIFICAR` | Modificar documento |
| DELETE | `/{id}` | `DOCGEN_ELIMINAR` | Eliminar documento |

### 4.11 DocumentTemplateController (`/api/v1/document-templates`)

| Método HTTP | Endpoint | Permiso | Descripción |
|-------------|----------|---------|-------------|
| POST | `/` | `PLANTILLA_CREAR` | Crear plantilla |
| PUT | `/{id}` | `PLANTILLA_MODIFICAR` | Modificar plantilla |
| DELETE | `/{id}` | `PLANTILLA_ELIMINAR` | Eliminar plantilla |
| GET | `/` | `PLANTILLA_LISTAR` | Listar plantillas |

### 4.12 FacultyController (`/api/v1/faculty`)

| Método HTTP | Endpoint | Permiso | Descripción |
|-------------|----------|---------|-------------|
| POST | `/` | `FACULTAD_CREAR` | Crear facultad |
| PUT | `/{id}` | `FACULTAD_MODIFICAR` | Modificar facultad |
| DELETE | `/{id}` | `FACULTAD_ELIMINAR` | Eliminar facultad |
| GET | `/` | `FACULTAD_LISTAR` | Listar facultades |

### 4.13 NotificationController (`/api/v1/notifications`)

| Método HTTP | Endpoint | Permiso | Descripción |
|-------------|----------|---------|-------------|
| GET | `/` | `NOTIF_LISTAR` | Listar notificaciones |
| GET | `/{id}` | `NOTIF_VER` | Ver notificación |
| POST | `/` | `NOTIF_CREAR` | Crear notificación |
| PUT | `/{id}` | `NOTIF_MODIFICAR` | Modificar notificación |
| DELETE | `/{id}` | `NOTIF_ELIMINAR` | Eliminar notificación |

### 4.14 NotificationTypeController (`/api/v1/notification-types`)

| Método HTTP | Endpoint | Permiso | Descripción |
|-------------|----------|---------|-------------|
| GET | `/` | `TIPNOTIF_LISTAR` | Listar tipos |
| GET | `/{id}` | `TIPNOTIF_VER` | Ver tipo |
| POST | `/` | `TIPNOTIF_CREAR` | Crear tipo |
| PUT | `/{id}` | `TIPNOTIF_MODIFICAR` | Modificar tipo |
| DELETE | `/{id}` | `TIPNOTIF_ELIMINAR` | Eliminar tipo |

### 4.15 PermissionController (`/api/v1/permissions`)

| Método HTTP | Endpoint | Permiso | Descripción |
|-------------|----------|---------|-------------|
| POST | `/` | `PERMISO_CREAR` | Crear permiso |
| PUT | `/{id}` | `PERMISO_MODIFICAR` | Modificar permiso |
| DELETE | `/{id}` | `PERMISO_ELIMINAR` | Eliminar permiso |
| GET | `/` | `PERMISO_LISTAR` | Listar permisos |

### 4.16 ProceduresController (`/api/v1/procedures`)

| Método HTTP | Endpoint | Permiso | Descripción |
|-------------|----------|---------|-------------|
| GET | `/legacy` | `TRAMITE_LISTAR` | Listar (legacy) |
| GET | `/legacy/{id}` | `TRAMITE_VER` | Ver (legacy) |
| GET | `/` | `TRAMITE_LISTAR` | Listar trámites |
| GET | `/all` | `TRAMITE_LISTAR` | Listar todos (incluye inactivos) |
| GET | `/{id}` | `TRAMITE_VER` | Ver trámite |
| GET | `/code/{code}` | `TRAMITE_VER` | Buscar por código |
| GET | `/workflow/{workflowId}` | `TRAMITE_LISTAR` | Listar por workflow |
| POST | `/` | `TRAMITE_CREAR` | Crear trámite |
| PUT | `/{id}` | `TRAMITE_MODIFICAR` | Modificar trámite |
| POST | `/{id}/activate` | `TRAMITE_ACTIVAR` | Activar trámite |
| POST | `/{id}/deactivate` | `TRAMITE_DESACTIVAR` | Desactivar trámite |
| GET | `/{id}/requires-2fa` | `TRAMITE_VER` | Verificar si requiere 2FA |
| DELETE | `/{id}` | `TRAMITE_ELIMINAR` | Eliminar trámite |

### 4.17 ProcessingStageController (`/api/v1/processing-stages`)

| Método HTTP | Endpoint | Permiso | Descripción |
|-------------|----------|---------|-------------|
| POST | `/` | `ETAPA_CREAR` | Crear etapa |
| PUT | `/{id}` | `ETAPA_MODIFICAR` | Modificar etapa |
| DELETE | `/{id}` | `ETAPA_ELIMINAR` | Eliminar etapa |
| GET | `/` | `ETAPA_LISTAR` | Listar etapas |

### 4.18 RejectReasonController (`/api/v1/reject-reason`)

| Método HTTP | Endpoint | Permiso | Descripción |
|-------------|----------|---------|-------------|
| POST | `/` | `RECHAZO_CREAR` | Crear razón |
| PUT | `/{id}` | `RECHAZO_MODIFICAR` | Modificar razón |
| DELETE | `/{id}` | `RECHAZO_ELIMINAR` | Eliminar razón |
| GET | `/` | `RECHAZO_LISTAR` | Listar razones |

### 4.19 RefreshTokenController (`/api/v1/refresh-tokens`)

| Método HTTP | Endpoint | Permiso | Descripción |
|-------------|----------|---------|-------------|
| GET | `/` | `TOKEN_LISTAR` | Listar tokens |
| GET | `/{id}` | `TOKEN_VER` | Ver token |
| POST | `/` | `TOKEN_CREAR` | Crear token |
| PUT | `/{id}` | `TOKEN_MODIFICAR` | Modificar token |
| DELETE | `/{id}` | `TOKEN_ELIMINAR` | Eliminar token |

### 4.20 RequirementsController (`/api/v1/requirements`)

| Método HTTP | Endpoint | Permiso | Descripción |
|-------------|----------|---------|-------------|
| GET | `/` | `REQUISITO_LISTAR` | Listar requisitos |
| GET | `/{id}` | `REQUISITO_VER` | Ver requisito |
| POST | `/` | `REQUISITO_CREAR` | Crear requisito |
| PUT | `/{id}` | `REQUISITO_MODIFICAR` | Modificar requisito |
| DELETE | `/{id}` | `REQUISITO_ELIMINAR` | Eliminar requisito |

### 4.21 RolesController (`/api/v1/roles`)

| Método HTTP | Endpoint | Permiso | Descripción |
|-------------|----------|---------|-------------|
| GET | `/legacy` | `ROL_LISTAR` | Listar roles (legacy) |
| GET | `/legacy/{id}` | `ROL_VER` | Ver rol (legacy) |
| GET | `/` | `ROL_LISTAR` | Listar roles |
| GET | `/{id}` | `ROL_VER` | Ver rol |
| GET | `/name/{roleName}` | `ROL_VER` | Buscar por nombre |
| POST | `/` | `ROL_CREAR` | Crear rol |
| PUT | `/{id}` | `ROL_MODIFICAR` | Modificar rol |
| DELETE | `/{id}` | `ROL_ELIMINAR` | Eliminar rol |
| POST | `/{roleId}/permissions` | `ROL_ASIGNAR_PERMISO` | Asignar permisos |
| DELETE | `/{roleId}/permissions` | `ROL_REMOVER_PERMISO` | Remover permisos |
| POST | `/{roleId}/users/{userId}` | `ROL_ASIGNAR_USUARIO` | Asignar rol a usuario |
| DELETE | `/{roleId}/users/{userId}` | `ROL_REMOVER_USUARIO` | Remover rol de usuario |

### 4.22 SessionTokenController (`/api/v1/session-tokens`)

| Método HTTP | Endpoint | Permiso | Descripción |
|-------------|----------|---------|-------------|
| GET | `/` | `SESION_LISTAR` | Listar sesiones |
| GET | `/{id}` | `SESION_VER` | Ver sesión |
| POST | `/` | `SESION_CREAR` | Crear sesión |
| PUT | `/{id}` | `SESION_MODIFICAR` | Modificar sesión |
| DELETE | `/{id}` | `SESION_ELIMINAR` | Eliminar sesión |

### 4.23 StageTrackingController (`/api/v1/stage-tracking`)

| Método HTTP | Endpoint | Permiso | Descripción |
|-------------|----------|---------|-------------|
| GET | `/` | `SEGUIMIENTO_LISTAR` | Listar seguimiento |
| GET | `/{id}` | `SEGUIMIENTO_VER` | Ver seguimiento |
| POST | `/` | `SEGUIMIENTO_CREAR` | Crear seguimiento |
| PUT | `/{id}` | `SEGUIMIENTO_MODIFICAR` | Modificar seguimiento |
| DELETE | `/{id}` | `SEGUIMIENTO_ELIMINAR` | Eliminar seguimiento |

### 4.24 StatesController (`/api/v1/states`)

| Método HTTP | Endpoint | Permiso | Descripción |
|-------------|----------|---------|-------------|
| POST | `/` | `ESTADO_CREAR` | Crear estado |
| PUT | `/{id}` | `ESTADO_MODIFICAR` | Modificar estado |
| DELETE | `/{id}` | `ESTADO_ELIMINAR` | Eliminar estado |
| GET | `/` | `ESTADO_LISTAR` | Listar estados |

### 4.25 StudentsController (`/api/v1/students`)

| Método HTTP | Endpoint | Permiso | Descripción |
|-------------|----------|---------|-------------|
| GET | `/legacy` | `ESTUDIANTE_LISTAR` | Listar (legacy) |
| GET | `/legacy/{id}` | `ESTUDIANTE_VER` | Ver (legacy) |
| GET | `/` | `ESTUDIANTE_LISTAR` | Listar estudiantes |
| GET | `/{id}` | `ESTUDIANTE_VER` | Ver estudiante |
| GET | `/career/{careerId}` | `ESTUDIANTE_LISTAR` | Listar por carrera |
| GET | `/semester/{s}/parallel/{p}` | `ESTUDIANTE_LISTAR` | Listar por semestre |
| GET | `/user/{userId}` | `ESTUDIANTE_VER` | Buscar por usuario |
| POST | `/` | `ESTUDIANTE_CREAR` | Matricular estudiante |
| PUT | `/{id}` | `ESTUDIANTE_MODIFICAR` | Modificar estudiante |
| PATCH | `/{id}/status` | `ESTUDIANTE_MODIFICAR` | Cambiar status |
| POST | `/{id}/promote` | `ESTUDIANTE_PROMOVER` | Promover semestre |
| POST | `/{id}/graduate` | `ESTUDIANTE_GRADUAR` | Graduar |
| POST | `/{id}/withdraw` | `ESTUDIANTE_RETIRAR` | Retirar |
| POST | `/{id}/reactivate` | `ESTUDIANTE_REACTIVAR` | Reactivar |
| DELETE | `/{id}` | `ESTUDIANTE_ELIMINAR` | Eliminar |

### 4.26 UsersController (`/api/v1/users`)

| Método HTTP | Endpoint | Permiso | Descripción |
|-------------|----------|---------|-------------|
| GET | `/` | `USUARIO_LISTAR` | Listar usuarios |
| GET | `/{id}` | `USUARIO_VER` | Ver usuario |
| POST | `/` | `USUARIO_CREAR` | Crear usuario |
| PUT | `/{id}` | `USUARIO_MODIFICAR` | Modificar usuario |
| DELETE | `/{id}` | `USUARIO_ELIMINAR` | Eliminar usuario |
| PATCH | `/{id}/deactivate` | `USUARIO_DESACTIVAR` | Desactivar usuario |
| PATCH | `/{id}/activate` | `USUARIO_ACTIVAR` | Activar usuario |

### 4.27 WorkFlowsController (`/api/v1/work-flows`)

| Método HTTP | Endpoint | Permiso | Descripción |
|-------------|----------|---------|-------------|
| POST | `/` | `FLUJO_CREAR` | Crear flujo |
| PUT | `/{id}` | `FLUJO_MODIFICAR` | Modificar flujo |
| DELETE | `/{id}` | `FLUJO_ELIMINAR` | Eliminar flujo |
| GET | `/` | `FLUJO_LISTAR` | Listar flujos |

### 4.28 WorkflowStagesController (`/api/v1/workflow-stages`)

| Método HTTP | Endpoint | Permiso | Descripción |
|-------------|----------|---------|-------------|
| GET | `/` | `FLUJOETAPA_LISTAR` | Listar etapas |
| GET | `/{id}` | `FLUJOETAPA_VER` | Ver etapa |
| POST | `/` | `FLUJOETAPA_CREAR` | Crear etapa |
| PUT | `/{id}` | `FLUJOETAPA_MODIFICAR` | Modificar etapa |
| DELETE | `/{id}` | `FLUJOETAPA_ELIMINAR` | Eliminar etapa |

### 4.29 TwoFactorAuthController (`/api/v1/2fa`)

| Método HTTP | Endpoint | Permiso | Descripción |
|-------------|----------|---------|-------------|
| POST | `/setup` | `AUTH2FA_CONFIGURAR` | Iniciar configuración 2FA |
| POST | `/verify` | `AUTH2FA_VERIFICAR` | Verificar código TOTP |
| DELETE | `/disable` | `AUTH2FA_DESACTIVAR` | Desactivar 2FA |
| GET | `/status` | `AUTH2FA_ESTADO` | Consultar estado 2FA |
| POST | `/backup-codes/regenerate` | `AUTH2FA_REGENERAR` | Regenerar códigos |
| POST | `/validate` | *Público* | Validar código durante login |
| POST | `/validate-backup` | *Público* | Validar código de respaldo |

---

## 5. Matriz de Permisos por Rol

### 5.1 Resumen de Conteo

| Rol | Total Permisos | Nivel de Acceso |
|-----|---------------|-----------------|
| `ROLE_ADMIN` | **TODOS** (~140) | Acceso completo al sistema |
| `ROLE_COORDINATOR` | ~55 | Gestión académica y trámites |
| `ROLE_DEAN` | ~40 | Supervisión y aprobaciones |
| `ROLE_STUDENT` | ~30 | Acceso a sus propios datos |

### 5.2 Leyenda

| Símbolo | Significado |
|---------|-------------|
| ✅ | Permiso asignado |
| ❌ | Sin acceso |

### 5.3 Matriz Detallada

| Permiso | ADMIN | COORDINATOR | DEAN | STUDENT |
|---------|-------|-------------|------|---------|
| **CALENDARIO** | | | | |
| CAL_CREAR | ✅ | ❌ | ❌ | ❌ |
| CAL_MODIFICAR | ✅ | ❌ | ❌ | ❌ |
| CAL_ELIMINAR | ✅ | ❌ | ❌ | ❌ |
| CAL_LISTAR | ✅ | ✅ | ✅ | ✅ |
| **SOLICITUDES** | | | | |
| SOL_LISTAR | ✅ | ✅ | ✅ | ✅ |
| SOL_VER | ✅ | ✅ | ✅ | ✅ |
| SOL_CREAR | ✅ | ✅ | ❌ | ✅ |
| SOL_MODIFICAR | ✅ | ✅ | ❌ | ❌ |
| SOL_ELIMINAR | ✅ | ❌ | ❌ | ❌ |
| SOL_RESOLVER | ✅ | ✅ | ✅ | ❌ |
| SOL_RECHAZAR | ✅ | ✅ | ✅ | ❌ |
| **ESTUDIANTES** | | | | |
| ESTUDIANTE_LISTAR | ✅ | ✅ | ✅ | ❌ |
| ESTUDIANTE_VER | ✅ | ✅ | ✅ | ✅ |
| ESTUDIANTE_CREAR | ✅ | ✅ | ❌ | ❌ |
| ESTUDIANTE_MODIFICAR | ✅ | ✅ | ❌ | ❌ |
| ESTUDIANTE_PROMOVER | ✅ | ✅ | ❌ | ❌ |
| ESTUDIANTE_GRADUAR | ✅ | ❌ | ✅ | ❌ |
| ESTUDIANTE_RETIRAR | ✅ | ✅ | ❌ | ❌ |
| ESTUDIANTE_REACTIVAR | ✅ | ✅ | ✅ | ❌ |
| ESTUDIANTE_ELIMINAR | ✅ | ❌ | ❌ | ❌ |
| **USUARIOS** | | | | |
| USUARIO_LISTAR | ✅ | ❌ | ❌ | ❌ |
| USUARIO_VER | ✅ | ❌ | ❌ | ❌ |
| USUARIO_CREAR | ✅ | ❌ | ❌ | ❌ |
| USUARIO_MODIFICAR | ✅ | ❌ | ❌ | ❌ |
| USUARIO_ELIMINAR | ✅ | ❌ | ❌ | ❌ |
| USUARIO_ACTIVAR | ✅ | ❌ | ❌ | ❌ |
| USUARIO_DESACTIVAR | ✅ | ❌ | ❌ | ❌ |
| **ROLES** | | | | |
| ROL_LISTAR | ✅ | ❌ | ❌ | ❌ |
| ROL_VER | ✅ | ❌ | ❌ | ❌ |
| ROL_CREAR | ✅ | ❌ | ❌ | ❌ |
| ROL_MODIFICAR | ✅ | ❌ | ❌ | ❌ |
| ROL_ELIMINAR | ✅ | ❌ | ❌ | ❌ |
| ROL_ASIGNAR_PERMISO | ✅ | ❌ | ❌ | ❌ |
| ROL_REMOVER_PERMISO | ✅ | ❌ | ❌ | ❌ |
| ROL_ASIGNAR_USUARIO | ✅ | ❌ | ❌ | ❌ |
| ROL_REMOVER_USUARIO | ✅ | ❌ | ❌ | ❌ |
| **CREDENCIALES** | | | | |
| CRED_LISTAR | ✅ | ❌ | ❌ | ❌ |
| CRED_VER | ✅ | ❌ | ❌ | ❌ |
| CRED_CREAR | ✅ | ❌ | ❌ | ❌ |
| CRED_CAMBIAR_PASS | ✅ | ✅ | ✅ | ✅ |
| CRED_RESETEAR_PASS | ✅ | ❌ | ❌ | ❌ |
| CRED_BLOQUEAR | ✅ | ❌ | ❌ | ❌ |
| CRED_DESBLOQUEAR | ✅ | ❌ | ❌ | ❌ |
| CRED_ELIMINAR | ✅ | ❌ | ❌ | ❌ |
| **2FA** | | | | |
| AUTH2FA_CONFIGURAR | ✅ | ✅ | ✅ | ✅ |
| AUTH2FA_VERIFICAR | ✅ | ✅ | ✅ | ✅ |
| AUTH2FA_DESACTIVAR | ✅ | ✅ | ✅ | ✅ |
| AUTH2FA_ESTADO | ✅ | ✅ | ✅ | ✅ |
| AUTH2FA_REGENERAR | ✅ | ✅ | ✅ | ✅ |

> **Nota:** Los permisos ADMIN-only (CRED_*, ROL_*, USUARIO_*, PERMISO_*, TOKEN_*, SESION_*) son exclusivos del administrador por seguridad.

---

## 6. Cómo Aplicar Permisos Nuevos

### 6.1 Paso a Paso

Para agregar un nuevo permiso al sistema:

#### Paso 1: Definir el código

Seguir la nomenclatura `{MÓDULO}_{ACCIÓN}`:

```
Ejemplo: REPORTE_GENERAR
```

#### Paso 2: Insertar en la base de datos

```sql
INSERT INTO permissions (code, description, createdat)
VALUES ('REPORTE_GENERAR', 'Generar reportes del sistema', NOW());
```

O usar el SP existente:
```sql
CALL spi_permission('REPORTE_GENERAR', 'Generar reportes del sistema');
```

#### Paso 3: Asignar a roles

Via API REST:
```http
POST /api/v1/roles/{roleId}/permissions
Content-Type: application/json
Authorization: Bearer {jwt_admin}

[15]  // ID del permiso recién creado
```

Via SQL directo:
```sql
CALL sp_assign_role_permissions('ROLE_ADMIN', ARRAY['REPORTE_GENERAR']);
CALL sp_assign_role_permissions('ROLE_COORDINATOR', ARRAY['REPORTE_GENERAR']);
```

#### Paso 4: Anotar el método del controlador

```java
@GetMapping("/generate")
@PreAuthorize("hasAuthority('REPORTE_GENERAR')")
public ResponseEntity<?> generateReport() {
    // ...
}
```

#### Paso 5: Compilar y probar

```bash
./mvnw clean compile
```

### 6.2 Verificar Permisos de un Usuario

Consulta SQL para ver todos los permisos efectivos de un usuario:

```sql
SELECT DISTINCT p.code, p.description
FROM users u
JOIN users_roles ur ON u.iduser = ur.users_iduser
JOIN roles r ON ur.roles_idrole = r.idrole
JOIN roles_permissions rp ON r.idrole = rp.roles_idrole
JOIN permissions p ON rp.permissions_idpermission = p.idpermission
WHERE u.institutionalemail = 'admin@uteq.edu.ec'
ORDER BY p.code;
```

### 6.3 Combinar Permisos (OR)

Si un endpoint debe ser accesible con cualquiera de varios permisos:

```java
@PreAuthorize("hasAuthority('SOL_RESOLVER') or hasAuthority('SOL_RECHAZAR')")
public ResponseEntity<?> processApplication() { ... }
```

### 6.4 Combinar Permisos (AND)

Si un endpoint requiere múltiples permisos simultáneamente:

```java
@PreAuthorize("hasAuthority('SOL_CREAR') and hasAuthority('TRAMITE_VER')")
public ResponseEntity<?> createApplicationForProcedure() { ... }
```

---

## 7. Endpoints Públicos (Sin Permiso)

Estos endpoints no requieren autenticación ni permisos:

| Controlador | Endpoint | Método | Propósito |
|-------------|----------|--------|-----------|
| AuthController | `/api/v1/auth` | POST | Login (obtener JWT) |
| AuthController | `/api/v1/auth/logout` | POST | Cerrar sesión |
| TwoFactorAuthController | `/api/v1/2fa/validate` | POST | Validar código 2FA en login |
| TwoFactorAuthController | `/api/v1/2fa/validate-backup` | POST | Validar código de respaldo |

Estos están configurados como `permitAll()` en `SecurityConfig`.

---

## 8. Preguntas Frecuentes

### ¿Qué pasa si un usuario no tiene el permiso requerido?

El servidor responde con `403 Forbidden` y el cuerpo indica `Access Denied`.

### ¿Se pueden asignar permisos directamente a un usuario (sin rol)?

No. El modelo actual es `User → Roles → Permissions`. Los permisos siempre pasan por un rol.

### ¿Los permisos se incluyen en el JWT?

No. Los permisos se cargan dinámicamente en cada request desde la base de datos via `CustomUserDetailsService`. Esto permite cambiar permisos sin invalidar tokens existentes.

### ¿Cómo revocar un permiso de un rol?

Via API REST:
```http
DELETE /api/v1/roles/{roleId}/permissions
Content-Type: application/json
Authorization: Bearer {jwt_admin}

[15]  // IDs de permisos a remover
```

### ¿Qué diferencia hay entre `hasRole()` y `hasAuthority()`?

- `hasRole('ADMIN')` busca authority con prefijo `ROLE_` → equivale a buscar `ROLE_ADMIN`
- `hasAuthority('CAL_CREAR')` busca la authority exacta `CAL_CREAR`
- En este proyecto **solo se usa `hasAuthority()`** para permisos granulares

### ¿Puedo usar `@Secured` en lugar de `@PreAuthorize`?

Sí, pero `@PreAuthorize` es más flexible porque soporta expresiones SpEL complejas. Se recomienda mantener `@PreAuthorize` por consistencia.

---

> **Total de permisos en el sistema: ~140 códigos únicos**  
> Distribuidos en 29 módulos funcionales, cubriendo todos los 168+ endpoints del backend.
