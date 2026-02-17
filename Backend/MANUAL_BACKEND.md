# SGTE Backend — Manual Técnico Completo

> **Sistema de Gestión de Trámites Estudiantiles (SGTE)**  
> Backend desarrollado con Spring Boot 4.0.2 · Java 21 · PostgreSQL  
> Última actualización: Junio 2025

---

## Tabla de Contenido

1. [Visión General y Arquitectura](#1-visión-general-y-arquitectura)
2. [Archivos Raíz](#2-archivos-raíz)
3. [Configuración (Config)](#3-configuración-config)
4. [Seguridad y Autenticación](#4-seguridad-y-autenticación)
5. [Entidades (Entity)](#5-entidades-entity)
6. [Repositorios (Repository)](#6-repositorios-repository)
7. [Servicios — Interfaces (Services)](#7-servicios--interfaces-services)
8. [Servicios — Implementaciones (Services/Impl)](#8-servicios--implementaciones-servicesimpl)
9. [Controladores (Controllers)](#9-controladores-controllers)
10. [DTOs (Data Transfer Objects)](#10-dtos-data-transfer-objects)
11. [Excepciones y Manejo Global de Errores](#11-excepciones-y-manejo-global-de-errores)
12. [Configuración de application.properties](#12-configuración-de-applicationproperties)
13. [Dependencias Maven (pom.xml)](#13-dependencias-maven-pomxml)
14. [Diagrama de Relaciones entre Entidades](#14-diagrama-de-relaciones-entre-entidades)
15. [Catálogo de Permisos](#15-catálogo-de-permisos)
16. [Resumen Estadístico del Proyecto](#16-resumen-estadístico-del-proyecto)

---

## 1. Visión General y Arquitectura

### 1.1 Propósito del Sistema

SGTE es una plataforma para gestionar trámites estudiantiles universitarios de la UTEQ. Permite a estudiantes crear solicitudes, adjuntar documentos, seguir el progreso de sus trámites y recibir notificaciones. Administradores, coordinadores y decanos gestionan flujos de trabajo, aprueban o rechazan solicitudes y generan documentos oficiales.

### 1.2 Stack Tecnológico

| Componente | Tecnología | Versión | Por qué se eligió |
|---|---|---|---|
| Framework | Spring Boot | 4.0.2 | Ecosistema maduro, autoconfiguración, inyección de dependencias |
| Lenguaje | Java | 21 | LTS, pattern matching, records, virtual threads |
| Base de datos | PostgreSQL | — | Soporte nativo para procedimientos almacenados y funciones |
| Seguridad | Spring Security 7 + OAuth2 Resource Server | — | JWT RS256 stateless, granularidad de permisos |
| Autenticación 2FA | Google Authenticator (TOTP) vía `googleauth` | 1.5.0 | Estándar TOTP RFC 6238, compatible con cualquier app authenticator |
| Validación | Jakarta Bean Validation | — | Validación declarativa con anotaciones (`@NotNull`, `@Size`, etc.) |
| Build | Maven | — | Gestión de dependencias y ciclo de vida estándar |
| Boilerplate | Lombok | 1.18.42 | Elimina getters/setters/builders repetitivos |
| Criptografía | BouncyCastle `bcpkix-jdk18on` | 1.83 | Generación de claves RSA en formato PEM |
| Almacenamiento | Google Drive API v3 | — | Almacenamiento de documentos adjuntos en la nube |
| Email | Spring Mail | — | Envío de correos de notificación y credenciales |
| Driver BD | PostgreSQL JDBC | runtime | Conexión nativa a PostgreSQL |

### 1.3 Arquitectura en Capas

El sistema sigue una arquitectura de capas estricta donde cada capa solo conoce a la inmediatamente inferior:

```
┌─────────────────────────────────────────────────────────────────┐
│                      CAPA DE PRESENTACIÓN                       │
│                       Controladores (31)                        │
│  Reciben HTTP, validan DTOs con @Valid, delegan a servicios     │
│  Seguridad: @PreAuthorize("isAuthenticated()") a nivel clase    │
│  + @PreAuthorize("hasAuthority('XXX')") a nivel método          │
├─────────────────────────────────────────────────────────────────┤
│                     CAPA DE NEGOCIO                             │
│                Servicios — Interfaces (32)                      │
│            Contratos de las operaciones de negocio              │
├─────────────────────────────────────────────────────────────────┤
│                  CAPA DE IMPLEMENTACIÓN                         │
│            Servicios — Implementaciones (32)                    │
│   Lógica de negocio, validaciones, transformación a DTOs        │
│   Todos anotados con @Service @RequiredArgsConstructor          │
│   y @Transactional (soporte transaccional)                      │
├─────────────────────────────────────────────────────────────────┤
│                    CAPA DE DATOS                                │
│                   Repositorios (29)                              │
│       JpaRepository + Queries derivadas / Stored Procs          │
│  11 repos → Procedimientos Almacenados (catálogos)              │
│  18 repos → JPA directo (entidades de negocio)                  │
├─────────────────────────────────────────────────────────────────┤
│                   CAPA DE PERSISTENCIA                          │
│                     Entidades (29)                               │
│              Mapeo ORM a tablas PostgreSQL                       │
│          Lombok @Data/@Builder + JPA @Entity                    │
└─────────────────────────────────────────────────────────────────┘
```

**¿Por qué esta arquitectura?**  
- **Desacoplamiento**: Cambiar la implementación de un servicio no afecta al controlador.
- **Testabilidad**: Cada capa se puede probar de forma aislada con mocks.
- **Mantenibilidad**: Las interfaces de servicio definen contratos claros que facilitan la evolución.

### 1.4 Dos Patrones de Acceso a Datos

El backend implementa **dos patrones** que coexisten según la complejidad de la operación:

| Patrón | Repositorios | Cuándo se usa | Por qué |
|---|---|---|---|
| **Procedimientos Almacenados (SP)** | 11 repos (Calendar, Careers, Config, DeadlineRules, DocumentTemplates, Faculties, Permissions, ProcessingStage, RejectionReasons, States, Workflows) | Catálogos y entidades simples | La lógica de inserción/actualización/borrado vive en PostgreSQL, garantizando consistencia a nivel de BD |
| **JPA Directo** | 18 repos (Applications, Users, Students, Roles, Credentials, etc.) | Entidades con lógica de negocio compleja | La validación y transformación se hace en Java, aprovechando el ORM para relaciones complejas |

### 1.5 Roles del Sistema

| Rol | Descripción |
|---|---|
| `ROLE_ADMIN` | Administrador del sistema, acceso total |
| `ROLE_STUDENT` | Estudiante, puede crear y dar seguimiento a solicitudes |
| `ROLE_COORDINATOR` | Coordinador de carrera, gestiona estudiantes y trámites |
| `ROLE_DEAN` | Decano de facultad, aprueba operaciones académicas |

### 1.6 Sistema de Permisos Granulares

El backend implementa seguridad basada en **permisos individuales** (no solo roles):

1. Cada `Rol` tiene asociado un conjunto de `Permissions` (relación M:N tabla `role_permissions`).
2. `CustomUserDetailsService` carga roles + permisos individuales como `GrantedAuthority`.
3. Los controladores usan `@PreAuthorize("hasAuthority('CODIGO_PERMISO')")` en cada método.
4. Los roles protegidos (`ROLE_ADMIN`, `ROLE_STUDENT`, `ROLE_COORDINATOR`, `ROLE_DEAN`) no pueden eliminarse.

**¿Por qué permisos granulares en lugar de solo roles?**  
Permite asignar accesos específicos sin crear roles nuevos. Por ejemplo, un coordinador podría tener permiso `ESTUDIANTE_PROMOVER` sin necesidad de un rol adicional.

---

## 2. Archivos Raíz

### 2.1 `BackendApplication.java`

Clase principal de Spring Boot que inicia la aplicación.

**Funciones implementadas:**
- `@SpringBootApplication`: Activa la autoconfiguración de Spring Boot (escaneo de componentes, configuración automática de JPA, seguridad, etc.).
- `@EnableConfigurationProperties(RsaKeyConfig.class)`: Registra las propiedades RSA como bean de configuración para que Spring inyecte automáticamente las claves PEM.
- `passwordEncoder()` → `BCryptPasswordEncoder`: Define el algoritmo de hash para contraseñas. BCrypt genera un salt aleatorio en cada hash, lo que impide ataques con tablas rainbow.

**¿Por qué BCrypt?** Es el estándar de la industria para hashing de contraseñas. A diferencia de SHA-256, BCrypt es intencionalmente lento (configurable con factor de costo) y genera salt automáticamente.

### 2.2 `GenerateKeyPair.java`

Utilidad standalone para generar el par de claves RSA de 2048 bits en formato PEM.

**¿Por qué existe?** Los tokens JWT se firman con clave privada RSA y se verifican con clave pública. Este archivo se ejecuta **una sola vez** para crear `public.pem` y `private.pem` en `src/main/resources/certs/`. Usa BouncyCastle para escribir el formato PEM estándar.

**¿Por qué RSA en lugar de HMAC?** RSA (asimétrico) permite que múltiples servicios verifiquen tokens con solo la clave pública, sin conocer la clave privada. HMAC (simétrico) requiere compartir la misma clave secreta con todos los servicios.

---

## 3. Configuración (Config)

### 3.1 `SecurityConfig.java`

Clase central de seguridad que configura toda la cadena de filtros HTTP, CORS, JWT y autenticación.

**Beans y funciones implementadas:**

| Bean/Método | Propósito | Por qué |
|---|---|---|
| `authenticationManager()` | Crea `ProviderManager` con `DaoAuthenticationProvider` | Spring Security 7 requiere pasar `UserDetailsService` en el constructor de `DaoAuthenticationProvider` |
| `filterChain()` | Cadena de filtros HTTP | Define qué rutas son públicas (`/api/v1/auth/**`, `/api/v1/2fa/validate*`) y cuáles requieren JWT |
| `corsConfigurationSource()` | Configuración CORS | Permite que el frontend Angular (localhost:4200) haga peticiones cross-origin |
| `jwtAuthenticationConverter()` | Converter JWT → Authentication | Extrae el claim `scope` del JWT y lo convierte a `GrantedAuthority` para que `hasAuthority()` funcione |
| `jwtDecoder()` | Decodificador con clave pública RSA | Valida la firma de tokens entrantes |
| `jwtEncoder()` | Codificador con par de claves RSA | Genera y firma tokens nuevos |

**Flujo de autenticación:**
1. El request llega con header `Authorization: Bearer <token>`.
2. `JwtDecoder` valida la firma RSA del token.
3. `JwtAuthenticationConverter` extrae los `scope` del payload y los convierte en authorities (`SCOPE_ROLE_ADMIN`, `ROLE_ADMIN`, `USUARIO_LISTAR`, etc.).
4. Spring Security verifica `@PreAuthorize` del controlador contra estas authorities.
5. CSRF está deshabilitado porque JWT es stateless (no usa cookies de sesión).

**¿Por qué sesiones STATELESS?** Cada request es independiente y se autentica por JWT. No se almacena estado de sesión en el servidor, lo que permite escalar horizontalmente.

### 3.2 `RsaKeyConfig.java`

Record de Java que vincula las propiedades `rsa.public-key` y `rsa.private-key` a objetos RSA tipados.

**¿Por qué un record?** Spring Boot automáticamente lee los archivos PEM, parsea el contenido y los inyecta como `RSAPublicKey`/`RSAPrivateKey`. Un record es inmutable y conciso, ideal para configuración.

### 3.3 `GoogleDriveConfig.java`

Configura el cliente de Google Drive API para subir, descargar y eliminar archivos.

**¿Por qué dos modos de carga de credenciales?**  
- **Ruta absoluta**: Para desarrollo local (archivo en disco).
- **Classpath**: Para producción (archivo empaquetado en el JAR).

El bean `Drive` se crea con scope `DRIVE_FILE` (acceso solo a archivos creados por la aplicación, no a toda la cuenta Drive).

### 3.4 `SpResultConverter.java`

Utilidades estáticas para convertir resultados de Stored Procedures PostgreSQL a tipos Java.

**¿Por qué existe?** Los procedimientos almacenados devuelven `Object[]` donde cada valor es un tipo genérico de JDBC. Esta clase proporciona conversión segura con validación de tipos para:
- `toInt()`: Convierte `Number` a `Integer`.
- `toStr()`: Convierte cualquier valor a `String`.
- `toBool()`: Convierte `Boolean` o `Number` (0/1) a `Boolean`.
- `toLocalDateTime()`: Convierte `Timestamp` de JDBC a `LocalDateTime` de Java.
- `toLocalDate()`: Convierte tipos de fecha JDBC a `LocalDate`.

### 3.5 `StringListConverter.java`

Converter JPA que almacena `List<String>` como texto delimitado por comas en la base de datos.

**¿Por qué?** PostgreSQL soporta arrays nativos, pero JPA no los mapea de forma trivial. Este converter almacena listas como `"valor1,valor2,valor3"` y las reconvierte al leer. Se usa en `TwoFactorAuth.backupCodes` para almacenar los 8 códigos de respaldo como texto.

---

## 4. Seguridad y Autenticación

### 4.1 Flujo de Login (AuthController)

El `AuthController` maneja 3 endpoints públicos bajo `/api/v1/auth/`:

#### `POST /api/v1/auth/token`

Endpoint principal de autenticación. Soporta dos grant types:

**Grant type `password`** (login con credenciales):
1. Valida email + contraseña mediante `AuthenticationManager`.
2. Si la autenticación falla, registra intento fallido (`credentialsService.registerFailedAttemptByEmail`). Tras 5 intentos, la cuenta se bloquea automáticamente.
3. Si la autenticación es exitosa, resetea el contador de intentos fallidos.
4. Verifica si el usuario tiene 2FA activado:
   - **Si tiene 2FA**: Emite un `pre_auth_token` JWT con 5 min de expiración y claim `requires_2fa: true`. El frontend debe llamar a `/api/v1/auth/2fa-verify`.
   - **Sin 2FA**: Emite directamente `access_token` (15 min) + opcionalmente `refresh_token` (24h).

**Grant type `refresh_token`** (renovación):
1. Decodifica y valida el refresh token JWT.
2. Busca el token en BD y verifica que no esté revocado.
3. **Rotación**: Revoca el token usado y genera uno nuevo (previene reutilización de tokens robados).
4. Emite nuevo `access_token` + `refresh_token`.

**¿Por qué se implementa la rotación de refresh tokens?** Si un atacante roba un refresh token, solo podrá usarlo una vez. Al usarlo, se revoca automáticamente, y el usuario legítimo detectará la pérdida en su próximo intento de renovación.

#### `POST /api/v1/auth/2fa-verify`

Completa el flujo de login cuando 2FA está activo:
1. Decodifica el `pre_auth_token` y verifica que sea de tipo `pre_auth`.
2. Valida el código TOTP o código de respaldo.
3. Si es válido, emite tokens completos (`access_token` + `refresh_token`).

#### `POST /api/v1/auth/logout`

Revoca todos los refresh tokens activos del usuario autenticado mediante el stored procedure `spu_revoke_all_refresh_tokens`.

### 4.2 Autenticación 2FA (TwoFactorAuthController)

Controlador para gestionar la autenticación de dos factores basada en TOTP (Time-based One-Time Passwords).

**Endpoints protegidos** (requieren JWT válido):

| Endpoint | Método | Permiso | Función |
|---|---|---|---|
| `/api/v1/2fa/setup` | POST | `AUTH2FA_CONFIGURAR` | Genera clave secreta TOTP, URI para QR y 8 códigos de respaldo |
| `/api/v1/2fa/verify` | POST | `AUTH2FA_VERIFICAR` | Primera verificación del código TOTP → activa 2FA |
| `/api/v1/2fa/disable` | DELETE | `AUTH2FA_DESACTIVAR` | Desactiva 2FA (requiere código TOTP válido como confirmación) |
| `/api/v1/2fa/status` | GET | `AUTH2FA_ESTADO` | Consulta si 2FA está activo para el usuario |
| `/api/v1/2fa/backup-codes/regenerate` | POST | `AUTH2FA_REGENERAR` | Regenera códigos de respaldo (requiere TOTP) |

**Endpoints públicos** (durante login con 2FA):

| Endpoint | Método | Función |
|---|---|---|
| `/api/v1/2fa/validate` | POST | Valida código TOTP durante login (requiere `preAuthToken`) |
| `/api/v1/2fa/validate-backup` | POST | Valida código de respaldo durante login (requiere `preAuthToken`) |

**¿Por qué se requiere `preAuthToken` en los endpoints públicos?** Previene enumeración de usuarios. Sin el pre-auth token (que solo se obtiene tras login exitoso), un atacante no puede probar códigos TOTP aleatorios contra emails arbitrarios.

### 4.3 CustomUserDetailsService

Servicio que carga los detalles del usuario para Spring Security.

**Flujo:**
1. Busca al usuario por email institucional.
2. Obtiene sus roles (`ROLE_ADMIN`, `ROLE_STUDENT`, etc.).
3. Para cada rol, carga sus permisos individuales (`USUARIO_LISTAR`, `SOL_CREAR`, etc.).
4. Construye un `UserDetails` con:
   - `username` = email institucional
   - `password` = hash BCrypt
   - `authorities` = roles + permisos
   - `accountLocked` = estado de bloqueo de credenciales
   - `disabled` = estado activo del usuario

**¿Por qué se cargan permisos además de roles?** La anotación `@PreAuthorize("hasAuthority('SOL_CREAR')")` verifica authorities individuales. Sin cargar permisos, solo funcionarían `hasRole('ADMIN')` pero no los permisos granulares.

---

## 5. Entidades (Entity)

Las 29 entidades representan las tablas de la base de datos PostgreSQL. Todas usan Lombok (`@Data`, `@Builder`, `@AllArgsConstructor`, `@NoArgsConstructor`) para reducir boilerplate.

### 5.1 Entidades de Usuarios y Seguridad

#### `Users`
Tabla central del sistema. Representa a cualquier persona registrada (estudiante, coordinador, decano, admin).

| Campo | Tipo | Restricción | Propósito |
|---|---|---|---|
| `idUser` | Integer (PK) | Auto-incremental | Identificador único |
| `names` | String | NOT NULL, 255 chars | Nombres del usuario |
| `surnames` | String | NOT NULL, 255 chars | Apellidos del usuario |
| `cardId` | String | NOT NULL, UNIQUE, 10 chars | Cédula de identidad |
| `institutionalEmail` | String | NOT NULL, UNIQUE | Email institucional (usado como username para login) |
| `personalMail` | String | UNIQUE | Email personal alternativo |
| `phoneNumber` | String | 15 chars | Teléfono de contacto |
| `statement` | Boolean | Default `true` | Estado de declaración del usuario |
| `configuration` | FK → Configurations | NOT NULL | Configuración de perfil (foto, firma, notificaciones) |
| `credentials` | FK → Credentials | OneToOne | Credenciales de acceso (hash de contraseña) |
| `createdAt` | LocalDateTime | NOT NULL | Fecha de creación |
| `active` | Boolean | Default `true` | Borrado lógico |
| `roles` | Set\<Roles\> | M:N EAGER | Roles asignados al usuario |

**¿Por qué roles EAGER?** Se necesitan los roles inmediatamente al autenticar al usuario. Carga lazy causaría `LazyInitializationException` fuera de la transacción.

**¿Por qué `@Getter/@Setter` en lugar de `@Data`?** La entidad Users tiene relaciones bidireccionales (roles, credentials). `@Data` genera `hashCode()` y `toString()` que incluyen estas colecciones, causando recursión infinita. Se usa `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` y `@ToString(exclude = {...})`.

#### `Credentials`
Almacena las credenciales de acceso separadas del usuario.

| Campo | Tipo | Propósito |
|---|---|---|
| `passwordHash` | String | Hash BCrypt de la contraseña |
| `dateModification` | LocalDateTime | Última modificación de contraseña |
| `lastLogin` | LocalDateTime | Último inicio de sesión exitoso |
| `failedAttempts` | Integer (default 0) | Contador de intentos fallidos consecutivos |
| `accountLocked` | Boolean (default false) | Si la cuenta está bloqueada por intentos fallidos |
| `passwordExpiryDate` | LocalDate | Fecha de vencimiento de contraseña (90 días) |

**¿Por qué separar Credentials de Users?** Principio de responsabilidad única. La tabla de usuarios contiene datos de perfil; la de credenciales contiene datos de seguridad. Esto permite:
- Auditar cambios de contraseña sin modificar el usuario.
- Bloquear una cuenta sin afectar datos personales.
- Diferentes políticas de retención para datos de perfil vs. seguridad.

#### `Roles`
Define los roles del sistema con sus permisos asociados.

| Campo | Tipo | Propósito |
|---|---|---|
| `idRole` | Integer (PK) | Identificador único |
| `roleName` | String (UNIQUE) | Nombre del rol (ej: `ROLE_ADMIN`) |
| `roleDescription` | String | Descripción del rol |
| `permissions` | Set\<Permissions\> | M:N EAGER | Permisos asignados |

**¿Por qué permisos EAGER en Roles?** Los permisos de un rol se necesitan al construir `UserDetails` para Spring Security. Carga lazy requeriría una transacción activa que no siempre está disponible.

#### `Permissions`
Permisos individuales que se asignan a roles.

| Campo | Tipo | Propósito |
|---|---|---|
| `code` | String (UNIQUE) | Código del permiso (ej: `USUARIO_LISTAR`) |
| `description` | String | Descripción legible |

#### `TwoFactorAuth`
Configuración de autenticación de dos factores por usuario.

| Campo | Tipo | Propósito |
|---|---|---|
| `credentials` | FK → Credentials (OneToOne, UNIQUE) | Vincula 2FA a las credenciales del usuario |
| `enabled` | Boolean (default false) | Si 2FA está activo |
| `secretKey` | String (TEXT) | Clave secreta TOTP (generada por GoogleAuth) |
| `backupCodes` | List\<String\> (via StringListConverter) | 8 códigos de respaldo de emergencia |
| `verifiedAt` | LocalDateTime | Cuándo se verificó por primera vez |

#### `SessionToken`
Tokens de sesión adicionales (tokens activos del usuario).

| Campo | Tipo | Propósito |
|---|---|---|
| `user` | FK → Users | Propietario del token |
| `token` | String (TEXT, UNIQUE) | Valor del token |
| `ipAddress` | String (45 chars) | IP del cliente (soporta IPv6) |
| `userAgent` | String (TEXT) | User-Agent del navegador |
| `createdAt` | LocalDateTime (NOT NULL) | Cuándo se creó |
| `expiresAt` | LocalDateTime (NOT NULL) | Cuándo expira |
| `lastActivity` | LocalDateTime | Última actividad registrada |

#### `RefreshToken`
Tokens de refresco para renovar el `access_token` sin re-autenticar.

| Campo | Tipo | Propósito |
|---|---|---|
| `token` | String (TEXT, UNIQUE) | Valor JWT del refresh token |
| `user` | FK → Users | Propietario |
| `createdAt` | LocalDateTime | Creación |
| `expiresAt` | LocalDateTime | Expiración (24h) |
| `revoked` | Boolean | Si ha sido revocado (rotación) |
| `deviceInfo` | String | Info del dispositivo |

### 5.2 Entidades Académicas

#### `Faculties`
Facultades de la universidad.

| Campo | Propósito |
|---|---|
| `facultyName` | Nombre de la facultad |
| `facultyCode` | Código abreviado |
| `dean` | FK → Users (LAZY) — Decano asignado |

**¿Por qué LAZY en dean?** No siempre se necesita cargar el decano al listar facultades. LAZY evita joins innecesarios.

#### `Careers`
Carreras dentro de una facultad.

| Campo | Propósito |
|---|---|
| `careerName` | Nombre de la carrera |
| `careerCode` | Código abreviado |
| `faculty` | FK → Faculties (LAZY) — Facultad a la que pertenece |
| `coordinator` | FK → Users (LAZY) — Coordinador asignado |

#### `Students`
Información académica del estudiante (separada de Users).

| Campo | Propósito |
|---|---|
| `semester` | Semestre actual (1-10) |
| `parallel` | Paralelo (A-Z) |
| `user` | FK → Users — Persona que es estudiante |
| `career` | FK → Careers — Carrera de inscripción |
| `enrollmentDate` | Fecha de matrícula |
| `status` | Estado: `activo`, `graduado`, `retirado`, `inactivo` |

**¿Por qué separar Students de Users?** Un usuario puede existir sin ser estudiante (un coordinador, un decano). La tabla Students agrega información académica específica que no aplica a todos los usuarios.

#### `AcademicCalendar`
Períodos académicos con fechas de inicio y fin.

| Campo | Propósito |
|---|---|
| `calendarName` | Nombre del período |
| `startDate` | Fecha de inicio |
| `endDate` | Fecha de fin |
| `academicYear` | Año académico |
| `active` | Si está vigente |

### 5.3 Entidades de Trámites

#### `Procedures` (Trámites)
Define los tipos de trámites disponibles.

| Campo | Propósito |
|---|---|
| `nameProcedure` | Nombre del trámite |
| `procedureCode` | Código único (autogenerado) |
| `workflow` | FK → Workflows (LAZY) — Flujo de trabajo asociado |
| `academicCalendar` | FK → AcademicCalendar (LAZY) — Período académico |
| `deadLineRule` | FK → DeadLinerules (LAZY) — Regla de plazos |
| `requires2fa` | Boolean — Si el trámite requiere 2FA |
| `active` | Boolean — Borrado lógico |

#### `Applications` (Solicitudes)
Instancia concreta de un trámite creado por un estudiante.

| Campo | Propósito |
|---|---|
| `applicationCode` | Código único de la solicitud |
| `creationDate` | Fecha de creación (automática) |
| `estimatedCompletionDate` | Fecha estimada de finalización |
| `actualCompletionDate` | Fecha real de finalización |
| `applicationDetails` | Detalles descriptivos |
| `applicationResolution` | Resolución del trámite |
| `rejectionReason` | FK → RejectionReasons — Motivo de rechazo (si aplica) |
| `currentStageTracking` | FK → StageTracking — Etapa actual del trámite |
| `procedure` | FK → Procedures — Tipo de trámite |
| `applicantUser` | FK → Users — Quién creó la solicitud |
| `priority` | String (default `normal`) — Prioridad: `baja`, `normal`, `alta`, `urgente` |

### 5.4 Entidades de Flujo de Trabajo (Workflow)

El sistema de flujo de trabajo permite definir secuencias de etapas por las que pasa cada trámite:

#### `Workflows`
Definición de un flujo de trabajo.

| Campo | Propósito |
|---|---|
| `workflowName` | Nombre del flujo |
| `workflowDescription` | Descripción |
| `active` | Borrado lógico |

#### `ProcessingStage`
Definición de una etapa de procesamiento (plantilla).

| Campo | Propósito |
|---|---|
| `stageName` | Nombre de la etapa |
| `stageCode` | Código único |
| `stageOrder` | Orden dentro del flujo |
| `requiresApproval` | Si requiere aprobación manual |
| `maxDurationDays` | Días máximos permitidos |

#### `WorkflowStages`
Asocia una etapa de procesamiento a un flujo de trabajo (tabla intermedia).

| Campo | Propósito |
|---|---|
| `workflow` | FK → Workflows (LAZY) — Flujo de trabajo |
| `processingStage` | FK → ProcessingStage (LAZY) — Etapa de procesamiento |
| `sequenceOrder` | Orden secuencial |
| `isOptional` | Si la etapa es opcional |

**¿Por qué LAZY en ambas FK?** `WorkflowStages` es una tabla de relación. Cuando se lista, solo se necesitan los IDs de las relaciones, no los objetos completos.

#### `States`
Catálogo de estados posibles de un trámite.

| Campo | Propósito |
|---|---|
| `stateName` | Nombre del estado |
| `stateCategory` | Categoría (ej: `proceso`, `finalizado`) |

#### `StageTracking`
Seguimiento de una etapa específica de un trámite.

| Campo | Propósito |
|---|---|
| `state` | FK → States — Estado actual de la etapa |
| `processingStage` | FK → ProcessingStage — Qué etapa es |
| `enteredAt` | Cuándo se entró a esta etapa |
| `completedAt` | Cuándo se completó |
| `assignedToUser` | FK → Users — Quién la está procesando |
| `notes` | Notas del responsable |

#### `ApplicationStageHistory`
Historial de todas las etapas por las que pasó una solicitud.

| Campo | Propósito |
|---|---|
| `application` | FK → Applications — Solicitud |
| `stageTracking` | FK → StageTracking — Etapa |
| `enteredAt` | Cuándo entró |
| `exitedAt` | Cuándo salió |
| `processedByUser` | FK → Users — Quién procesó |
| `comments` | Comentarios |

### 5.5 Entidades de Documentos

#### `DocumentTemplates`
Plantillas de documentos oficiales.

| Campo | Propósito |
|---|---|
| `templateCode` | Código único de plantilla |
| `templatePath` | Ruta al archivo de plantilla |
| `documentType` | Tipo de documento |
| `requiresSignature` | Si requiere firma digital |

#### `DocumentsGenerated`
Documentos generados a partir de plantillas.

| Campo | Propósito |
|---|---|
| `application` | FK → Applications — Solicitud asociada |
| `template` | FK → DocumentTemplates — Plantilla usada |
| `documentPath` | Ruta al documento generado |
| `generatedAt` | Cuándo se generó |
| `generatedByUser` | FK → Users — Quién lo generó |
| `digitalSignature` | FK → DigitalSignatures — Firma aplicada |

#### `AttachedDocuments`
Documentos adjuntos subidos a Google Drive.

| Campo | Propósito |
|---|---|
| `application` | FK → Applications — Solicitud asociada |
| `requirement` | FK → RequirementsOfTheProcedure — Requisito que cumple |
| `fileName` | Nombre original del archivo |
| `filePath` | **ID de Google Drive** (no una ruta local) |
| `fileSizeBytes` | Tamaño en bytes |
| `mimeType` | Tipo MIME |
| `uploadDate` | Fecha de subida |
| `uploadedByUser` | FK → Users — Quién lo subió |

#### `DigitalSignatures`
Certificados de firma digital de usuarios.

| Campo | Propósito |
|---|---|
| `user` | FK → Users — Propietario del certificado |
| `certificatePath` | Ruta al certificado |
| `certificateSerial` | Serial del certificado (UNIQUE) |
| `validFrom` / `validUntil` | Período de validez |
| `signatureAlgorithm` | Algoritmo usado |
| `active` | Si está activo |
| `createdAt` | Cuándo se registró |

### 5.6 Entidades de Notificaciones

#### `NotificationType`
Tipos de notificación (catálogo).

| Campo | Propósito |
|---|---|
| `nameTypeNotification` | Nombre del tipo |
| `templateCode` | Código de plantilla para el mensaje |
| `priorityLevel` | Prioridad |

#### `Notification`
Notificaciones enviadas a usuarios.

| Campo | Propósito |
|---|---|
| `notificationName` | Título |
| `message` | Contenido |
| `notificationType` | FK → NotificationType — Tipo |
| `application` | FK → Applications — Solicitud asociada |
| `recipientUser` | FK → Users — Destinatario |
| `sentAt` | Cuándo se envió |
| `deliveryStatus` | Estado: `pending`, `sent`, `failed` |
| `deliveryChannel` | Canal: `email`, `sms`, `push` |
| `retryCount` | Intentos de reenvío |

### 5.7 Entidades Complementarias

| Entidad | Propósito |
|---|---|
| `Configurations` | Configuración de perfil y notificaciones por usuario (foto, firma, canales) |
| `DeadLinerules` | Reglas de plazos para categorías de trámites |
| `RejectionReasons` | Catálogo de motivos de rechazo |
| `RequirementsOfTheProcedure` | Requisitos documentales de cada trámite |

---

## 6. Repositorios (Repository)

Los 29 repositorios extienden `JpaRepository<Entity, ID>` y se dividen en dos categorías:

### 6.1 Repositorios con Stored Procedures (11)

Estos repositorios delegan operaciones CRUD a procedimientos almacenados y funciones de PostgreSQL:

| Repositorio | Tabla | SP Insert | SP Update | SP Delete | Función List |
|---|---|---|---|---|---|
| `IAcademicCalendarRepository` | academiccalendar | `spi_academiccalendar` | `spu_academiccalendar` | `spd_academiccalendar` | `fn_list_academiccalendar` |
| `ICareersRepository` | careers | `spi_career` | `spu_career` | `spd_career` | `fn_list_careers` |
| `IConfigurationsRepository` | configurations | `spi_configuration` | `spu_configuration` | `spd_configuration` | `fn_list_configurations` |
| `IDeadLineRulesRepository` | deadlinerules | `spi_deadlinerule` | `spu_deadlinerule` | `spd_deadlinerule` | `fn_list_deadlinerules` |
| `IDocumentTemplatesRepository` | documenttemplates | `spi_documenttemplate` | `spu_documenttemplate` | `spd_documenttemplate` | `fn_list_documenttemplates` |
| `IFacultiesRepository` | faculties | `spi_faculty` | `spu_faculty` | `spd_faculty` | `fn_list_faculties` |
| `IPermissionsRepository` | permissions | `spi_permission` | `spu_permission` | `spd_permission` | `fn_list_permissions` |
| `IProcessingStageRepository` | processingstage | `spi_processingstage` | `spu_processingstage` | `spd_processingstage` | `fn_list_processingstage` |
| `IRejectionReasonsRepository` | rejectionreasons | `spi_rejectionreason` | `spu_rejectionreason` | `spd_rejectionreason` | `fn_list_rejectionreasons` |
| `IStatesRepository` | states | `spi_state` | `spu_state` | `spd_state` | `fn_list_states` |
| `IWorkflowsRepository` | workflows | `spi_workflow` | `spu_workflow` | `spd_workflow` | `fn_list_workflows` |

**Convención de nombres:** `spi_` = insert, `spu_` = update, `spd_` = delete, `fn_list_` = consulta.

**¿Por qué Stored Procedures para catálogos?** Estas tablas son datos de referencia con operaciones simples. Los SP encapsulan validaciones a nivel de BD (ej: verificar que no exista un código duplicado), garantizando consistencia incluso si se accede directamente a la BD.

### 6.2 Repositorios con JPA Directo (18)

Estos repositorios usan queries derivados de Spring Data JPA:

| Repositorio | Queries personalizados |
|---|---|
| `IApplicationsRepository` | `findByApplicationCode`, `findByApplicantUserIdUser`, `findByPriority`, `existsByApplicationCode` |
| `IUsersRepository` | `findByInstitutionalEmail`, `findByCardId`, `findByActiveTrue`, `findByRolesIdRole`, `existsByInstitutionalEmail`, `existsByCardId` |
| `IStudentsRepository` | `findByUserIdUser`, `findByCareerIdCareer`, `findBySemesterAndParallel`, `findByStatus` |
| `IRolesRepository` | `findByRoleName`, `existsByRoleName` |
| `IProceduresRepository` | `findByProcedureCode`, `findByActiveTrue`, `findByWorkflowIdWorkflow`, `existsByProcedureCode` |
| `IRefreshTokenRepository` | `findByToken`, `findByUserAndRevokedFalse`, `revokeAllByUser` (SP) |
| `ITwoFactorAuthRepository` | `findByCredentials_Id`, `existsByCredentials_IdAndEnabledTrue` |
| Otros 11 repos | Solo CRUD básico (`findAll`, `findById`, `save`, `deleteById`) |

**¿Por qué queries derivados?** Spring Data JPA genera la implementación automáticamente a partir del nombre del método. `findByApplicantUserIdUser(Integer userId)` se traduce a `SELECT * FROM applications WHERE applicant_user_id = ?`.

---

## 7. Servicios — Interfaces (Services)

Las 32 interfaces de servicio definen los contratos de negocio. Se dividen en tres tipos:

### 7.1 Servicios con Lógica de Negocio Avanzada

| Interfaz | Métodos principales | Por qué tiene lógica avanzada |
|---|---|---|
| `IApplicationsService` | `createApplication`, `updateApplication`, `resolveApplication`, `rejectApplication`, `findByApplicantUserId`, `findByPriority` | Validaciones de duplicados, estado de usuario, resolución/rechazo con timestamps automáticos |
| `ICredentialsService` | `changePassword`, `lockAccount`, `unlockAccount`, `registerFailedAttempt`, `resetPassword`, `isPasswordExpired`, `verifyCredentialOwnership` | Política de seguridad: bloqueo tras 5 intentos, expiración cada 90 días, verificación de propiedad |
| `IRolesService` | `createRole`, `assignPermissions`, `removePermissions`, `assignRoleToUser`, `removeRoleFromUser` | Gestión M:N de Roles↔Permisos y Users↔Roles, protección de roles del sistema |
| `IStudentsService` | `enrollStudent`, `promoteToNextSemester`, `graduate`, `withdraw`, `reactivate`, `changeStatus` | Ciclo de vida académico completo con validaciones de transiciones de estado |
| `IProceduresService` | `createProcedure`, `activateProcedure`, `deactivateProcedure`, `requires2FA` | Generación automática de código, activación/desactivación, verificación de 2FA |
| `IUsersService` | `createUser`, `updateUser`, `deactivateUser`, `activateUser` | Validaciones de unicidad (email, cédula), borrado lógico |
| `ITwoFactorAuthService` | `setup2FA`, `verifyAndEnable2FA`, `disable2FA`, `validateCode`, `validateBackupCode`, `regenerateBackupCodes` | Flujo TOTP completo con códigos de respaldo |

### 7.2 Servicios con Stored Procedures

| Interfaz | Operaciones |
|---|---|
| `IAcademicCalendarService` | `createcalendar`, `modifycalendar`, `deletecalendar`, `listarCalendarios` |
| `ICareersService` | `createCareers`, `updateCareers`, `deleteCareers`, `listCareers` |
| `IConfigurationService` | `createConfiguration`, `updateConfiguration`, `deleteConfiguration`, `listConfiguration` |
| `IDeadLineRulesService` | `createDeadlineRule`, `updateDeadlineRule`, `deleteDeadlineRule`, `listDeadlineRules` |
| `IDocumentTemplatesService` | `createDocumentTemplate`, `updateDocumentTemplate`, `deleteDocumentTemplate`, `listDocumentTemplates` |
| `IFacultiesService` | `createFaculty`, `updateFaculty`, `deleteFaculty`, `listFaculties` |
| `IPermissionsService` | `createPermission`, `updatePermission`, `deletePermission`, `listPermissions` |
| `IProcessingStageService` | `createProcessingStage`, `updateProcessingStage`, `deleteProcessingStage`, `listProcessingStages` |
| `IRejectionReasonsService` | `createRejectionReason`, `updateRejectionReason`, `deleteRejectionReason`, `listRejectionReasons` |
| `IStatesService` | `createState`, `updateState`, `deleteState`, `listStates` |
| `IWorkflowsService` | `createWorkflow`, `updateWorkflow`, `deleteWorkflow`, `listWorkflows` |

### 7.3 Servicios CRUD Simples

| Interfaz | Operaciones |
|---|---|
| `IApplicationStageHistoryService` | `findAll`, `findById`, `save`, `deleteById` |
| `IAttachedDocumentsService` | `findAll`, `findById`, `save`, `deleteById` |
| `IDigitalSignaturesService` | `findAll`, `findById`, `save`, `deleteById` |
| `IDocumentsGeneratedService` | `findAll`, `findById`, `save`, `deleteById` |
| `INotificationService` | `findAll`, `findById`, `save`, `deleteById` |
| `INotificationTypeService` | `findAll`, `findById`, `save`, `deleteById` |
| `IRefreshTokenService` | `findAll`, `findById`, `save`, `deleteById` |
| `IRequirementsOfTheProcedureService` | `findAll`, `findById`, `save`, `deleteById` |
| `ISessionTokenService` | `findAll`, `findById`, `save`, `deleteById` |
| `IStageTrackingService` | `findAll`, `findById`, `save`, `deleteById` |
| `IWorkflowStagesService` | `findAll`, `findById`, `save`, `deleteById` |

### 7.4 Servicios Especializados

| Interfaz | Métodos | Propósito |
|---|---|---|
| `IDriveService` | `uploadFile`, `downloadFile`, `deleteFile` | Integración con Google Drive API |
| `IEmailService` | `sendPlainText`, `sendHtml`, `sendCredentials` | Envío de emails vía Spring Mail |

---

## 8. Servicios — Implementaciones (Services/Impl)

### 8.1 Implementaciones con Lógica de Negocio

#### `ApplicationsServiceImpl`

**Funciones clave y por qué existen:**

- `createApplication()`: Valida que el código no exista (previene duplicados), que el usuario esté activo (no permite solicitudes de usuarios desactivados), que existan el procedimiento y stage tracking referenciados. Genera `creationDate` automáticamente con `LocalDateTime.now()`.
- `updateApplication()`: Usa patrón **fetch-then-merge** — busca la entidad existente, modifica solo los campos enviados en el DTO, preserva campos automáticos como `creationDate`.
- `resolveApplication()`: Registra la resolución y marca `actualCompletionDate` con `LocalDateTime.now()`.
- `rejectApplication()`: Vincula un motivo de rechazo catalogado y marca fecha de finalización.
- `validatePriority()`: Restringe prioridad a valores válidos (`baja`, `normal`, `alta`, `urgente`).

**¿Por qué fetch-then-merge en `updateApplication()`?** Al realizar un UPDATE directo con todos los campos del DTO, se pierden valores que el cliente no envió (como `creationDate`). Fetch-then-merge busca primero la entidad en BD, luego solo sobreescribe los campos incluidos en el DTO.

#### `CredentialsServiceImpl`

**Funciones clave:**

- `createCredential()`: Hashea la contraseña con BCrypt y establece fecha de expiración a 90 días.
- `changePassword()`: Verifica la contraseña actual antes de permitir el cambio. Actualiza `dateModification` y reinicia expiración.
- `registerFailedAttempt()` / `registerFailedAttemptByEmail()`: Incrementa `failedAttempts`. Si alcanza 5, bloquea la cuenta automáticamente.
- `registerSuccessfulLogin()`: Resetea `failedAttempts` a 0 y actualiza `lastLogin`.
- `resetPassword()`: Genera una contraseña temporal de 12 caracteres (alfanumérica + especiales).
- `verifyCredentialOwnership()`: Verifica que el usuario autenticado sea dueño de la credencial que intenta modificar.
- `isPasswordExpired()`: Compara `passwordExpiryDate` con la fecha actual.
- `lockAccount()` / `unlockAccount()`: Bloquea/desbloquea manualmente una cuenta.

**¿Por qué bloqueo tras 5 intentos?** Previene ataques de fuerza bruta. Un administrador puede desbloquear manualmente.

**¿Por qué expiración de contraseña cada 90 días?** Cumple con políticas de seguridad institucionales. Obliga al usuario a cambiar su contraseña periódicamente.

#### `StudentsServiceImpl`

**Funciones clave:**

- `enrollStudent()`: Verifica que el usuario no esté ya matriculado (previene inscripción duplicada). Valida que semestre esté entre 1 y 10 y que la carrera exista.
- `promoteToNextSemester()`: Incrementa el semestre de 1 a 10. Lanza `BadRequestException` si ya está en semestre 10.
- `graduate()`: Cambia estado a `graduado`. Solo permite graduar desde estado `activo`.
- `withdraw()`: Cambia estado a `retirado`. Solo desde `activo`.
- `reactivate()`: Devuelve a estado `activo`. Solo desde `retirado` o `inactivo`.
- `changeStatus()`: Validación de transiciones de estado permitidas.

**¿Por qué validaciones de transición de estado?** Un estudiante `graduado` no puede ser `retirado`. Las transiciones permitidas son:
- `activo` → `graduado`, `retirado`, `inactivo`
- `retirado` → `activo`
- `inactivo` → `activo`

#### `RolesServiceImpl`

**Funciones clave:**

- `createRole()`: Valida que el nombre siga el patrón `ROLE_XXX` (convención de Spring Security) y no exista previamente.
- `deleteRole()`: Protege los 4 roles del sistema (`ROLE_ADMIN`, `ROLE_STUDENT`, `ROLE_COORDINATOR`, `ROLE_DEAN`) — no pueden eliminarse.
- `assignPermissions()` / `removePermissions()`: Gestiona la relación M:N Roles↔Permissions.
- `assignRoleToUser()` / `removeRoleFromUser()`: Gestiona la relación M:N Users↔Roles.

**¿Por qué roles protegidos?** Si se elimina `ROLE_ADMIN`, nadie podría gestionar el sistema. Los roles base son esenciales para el funcionamiento mínimo.

#### `ProceduresServiceImpl`

**Funciones clave:**

- `createProcedure()`: Genera automáticamente un código único (`TRAM-XXXX`) si no se proporciona. Verifica que no exista un trámite con el mismo código.
- `updateProcedure()`: Fetch-then-merge para preservar campos automáticos.
- `deleteProcedure()`: Verifica que no existan solicitudes asociadas antes de eliminar (integridad referencial).
- `activateProcedure()` / `deactivateProcedure()`: Borrado lógico — un trámite desactivado no aparece en las búsquedas activas pero se conserva en historial.
- `requires2FA()`: Consulta si un trámite específico requiere autenticación de dos factores.

#### `UsersServiceImpl`

**Funciones clave:**

- `createUser()`: Valida unicidad de `institutionalEmail` y `cardId` (cédula). Lanza `DuplicateResourceException` si ya existen.
- `updateUser()`: Fetch-then-merge — actualiza solo los campos proporcionados, conserva `createdAt`, `active`, `roles`.
- `deactivateUser()` / `activateUser()`: Borrado lógico. Un usuario desactivado no puede iniciar sesión (`disabled = true` en UserDetails).

#### `TwoFactorAuthServiceImpl`

**Funciones clave:**

- `setup2FA()`: Genera clave secreta TOTP usando `GoogleAuthenticator`, crea URI para código QR (formato `otpauth://totp/SGTE:{email}?secret=XXX&issuer=SGTE`) y genera 8 códigos de respaldo de 8 caracteres alfanuméricos.
- `verifyAndEnable2FA()`: Primera verificación del código TOTP. Si es correcto, marca `enabled = true` y registra `verifiedAt = now()`.
- `disable2FA()`: Requiere código TOTP válido como confirmación de identidad. Elimina el registro de 2FA.
- `validateCode()`: Valida un código TOTP (6 dígitos, vigencia ~30 segundos).
- `validateBackupCode()`: Verifica y **consume** un código de respaldo (eliminándolo de la lista). Cuando se agotan, 2FA no tiene respaldo.
- `regenerateBackupCodes()`: Requiere código TOTP válido. Sobreescribe los códigos existentes con 8 nuevos.

### 8.2 Implementaciones con Stored Procedures

Las 11 implementaciones de SP siguen el mismo patrón:

```java
@Service
@RequiredArgsConstructor
@Transactional
public class XxxServiceImpl implements IXxxService {
    
    private final IXxxRepository repository;

    @Override
    public String create(IXxxRequest request) {
        return repository.spInsert(request.getField1(), request.getField2());
    }

    @Override
    public String update(UXxxRequest request) {
        return repository.spUpdate(request.getId(), request.getField1(), ...);
    }

    @Override
    public String delete(DXxxRequest request) {
        return repository.spDelete(request.getId());
    }

    @Override
    public List<Object[]> list() {
        return repository.fnList();
    }
}
```

**¿Por qué retornan `String`?** Los SP de PostgreSQL devuelven un mensaje de resultado (éxito o error) que el servicio propaga al controlador.

**¿Por qué `Object[]` en list?** La función PostgreSQL devuelve un resultado tabular genérico. El controlador usa `SpResultConverter` para convertir a tipos Java.

### 8.3 `DriveServiceImpl`

**Funciones:**

- `uploadFile(MultipartFile file)`: Sube un `MultipartFile` a Google Drive con nombre `UUID_nombreOriginal` (previene colisiones de nombres). Lo almacena en la carpeta configurada por `google.drive.folder.id`. Retorna el `fileId` de Drive que se guarda en `AttachedDocuments.filePath`.
- `downloadFile(String fileId)`: Descarga el contenido de un archivo por su `fileId` como `ByteArrayOutputStream`.
- `deleteFile(String fileId)`: Elimina un archivo de Drive.

**¿Por qué UUID en el nombre?** Dos usuarios podrían subir archivos con el mismo nombre. El UUID como prefijo garantiza unicidad sin afectar el nombre original.

### 8.4 `EmailServiceImpl`

Envía emails mediante `JavaMailSender`:
- `sendPlainText(to, subject, body)`: Email en texto plano.
- `sendHtml(to, subject, htmlBody)`: Email con contenido HTML.
- `sendCredentials(to, username, tempPassword)`: Email formateado con credenciales de acceso temporales.

### 8.5 `CustomUserDetailsService`

Implementa `UserDetailsService` de Spring Security. Carga el usuario por email institucional, incluyendo sus roles y permisos, para construir el objeto `UserDetails`:

```java
@Override
public UserDetails loadUserByUsername(String email) {
    Users user = usersRepository.findByInstitutionalEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("..."));
    
    List<GrantedAuthority> authorities = new ArrayList<>();
    for (Roles role : user.getRoles()) {
        authorities.add(new SimpleGrantedAuthority(role.getRoleName()));
        for (Permissions permission : role.getPermissions()) {
            authorities.add(new SimpleGrantedAuthority(permission.getCode()));
        }
    }
    
    return User.builder()
        .username(user.getInstitutionalEmail())
        .password(user.getCredentials().getPasswordHash())
        .authorities(authorities)
        .accountLocked(user.getCredentials().getAccountLocked())
        .disabled(!user.getActive())
        .build();
}
```

---

## 9. Controladores (Controllers)

Los 31 controladores se dividen en categorías según su patrón de implementación.

### 9.1 Controladores de Autenticación (públicos)

| Controlador | Base Path | Descripción |
|---|---|---|
| `AuthController` | `/api/v1/auth` | Login (password + 2FA), refresh token, logout |
| `TwoFactorAuthController` | `/api/v1/2fa` | Setup, verificación, desactivación de 2FA |

Ver [Sección 4](#4-seguridad-y-autenticación) para detalles completos del flujo.

#### Endpoints de `AuthController`

| Método | Endpoint | Función | Seguridad |
|---|---|---|---|
| POST | `/token` | Autenticación (password/refresh_token grant types) | Público |
| POST | `/2fa-verify` | Verificación de 2FA tras pre-auth | Público |
| POST | `/logout` | Revocar refresh tokens del usuario | Autenticado |

#### Endpoints de `TwoFactorAuthController`

| Método | Endpoint | Función | Seguridad |
|---|---|---|---|
| POST | `/setup` | Configurar 2FA | `AUTH2FA_CONFIGURAR` |
| POST | `/verify` | Verificar y activar 2FA | `AUTH2FA_VERIFICAR` |
| DELETE | `/disable` | Desactivar 2FA | `AUTH2FA_DESACTIVAR` |
| GET | `/status` | Estado de 2FA | `AUTH2FA_ESTADO` |
| POST | `/backup-codes/regenerate` | Regenerar códigos | `AUTH2FA_REGENERAR` |
| POST | `/validate` | Validar TOTP (login) | Público (con pre-auth) |
| POST | `/validate-backup` | Validar backup (login) | Público (con pre-auth) |

### 9.2 Controladores con Lógica de Negocio Completa

Estos controladores delegan toda la lógica al servicio y solo se encargan de:
1. Recibir la petición HTTP.
2. Validar el DTO con `@Valid`.
3. Delegar al servicio.
4. Retornar la respuesta DTO.

#### `ApplicationsController` (`/api/v1/applications`)

| Método | Endpoint | Función | Permiso |
|---|---|---|---|
| GET | `/` | Listar todas las solicitudes | `SOL_LISTAR` |
| GET | `/{id}` | Obtener solicitud por ID | `SOL_VER` |
| GET | `/user/{userId}` | Solicitudes de un usuario | `SOL_LISTAR` |
| GET | `/priority/{priority}` | Solicitudes por prioridad | `SOL_LISTAR` |
| POST | `/` | Crear solicitud | `SOL_CREAR` |
| PUT | `/{id}` | Actualizar solicitud | `SOL_MODIFICAR` |
| DELETE | `/{id}` | Eliminar solicitud | `SOL_ELIMINAR` |
| PATCH | `/{id}/resolve` | Resolver solicitud | `SOL_RESOLVER` |
| PATCH | `/{id}/reject` | Rechazar solicitud | `SOL_RECHAZAR` |

#### `UsersController` (`/api/v1/users`)

| Método | Endpoint | Función | Permiso |
|---|---|---|---|
| GET | `/` | Listar usuarios | `USUARIO_LISTAR` |
| GET | `/{id}` | Obtener usuario | `USUARIO_VER` |
| POST | `/` | Crear usuario | `USUARIO_CREAR` |
| PUT | `/{id}` | Actualizar usuario | `USUARIO_MODIFICAR` |
| DELETE | `/{id}` | Eliminar usuario | `USUARIO_ELIMINAR` |
| PATCH | `/{id}/deactivate` | Desactivar (borrado lógico) | `USUARIO_DESACTIVAR` |
| PATCH | `/{id}/activate` | Reactivar | `USUARIO_ACTIVAR` |

#### `StudentsController` (`/api/v1/students`)

| Método | Endpoint | Función | Permiso |
|---|---|---|---|
| GET | `/` | Listar estudiantes | `ESTUDIANTE_LISTAR` |
| GET | `/{id}` | Obtener estudiante | `ESTUDIANTE_VER` |
| GET | `/career/{careerId}` | Estudiantes por carrera | `ESTUDIANTE_LISTAR` |
| GET | `/user/{userId}` | Estudiante por usuario | `ESTUDIANTE_VER` |
| GET | `/semester-parallel` | Por semestre y paralelo | `ESTUDIANTE_LISTAR` |
| POST | `/` | Matricular | `ESTUDIANTE_CREAR` |
| PUT | `/{id}` | Actualizar | `ESTUDIANTE_MODIFICAR` |
| DELETE | `/{id}` | Eliminar | `ESTUDIANTE_ELIMINAR` |
| PATCH | `/{id}/promote` | Promover semestre | `ESTUDIANTE_PROMOVER` |
| PATCH | `/{id}/graduate` | Graduar | `ESTUDIANTE_GRADUAR` |
| PATCH | `/{id}/withdraw` | Retirar | `ESTUDIANTE_RETIRAR` |
| PATCH | `/{id}/reactivate` | Reactivar | `ESTUDIANTE_REACTIVAR` |
| PATCH | `/{id}/status` | Cambiar estado | `ESTUDIANTE_MODIFICAR` |

#### `RolesController` (`/api/v1/roles`)

| Método | Endpoint | Función | Permiso |
|---|---|---|---|
| GET | `/` | Listar roles | `ROL_LISTAR` |
| GET | `/{id}` | Obtener rol | `ROL_VER` |
| GET | `/name/{name}` | Buscar por nombre | `ROL_VER` |
| POST | `/` | Crear rol | `ROL_CREAR` |
| PUT | `/{id}` | Actualizar | `ROL_MODIFICAR` |
| DELETE | `/{id}` | Eliminar | `ROL_ELIMINAR` |
| POST | `/{id}/permissions` | Asignar permisos | `ROL_ASIGNAR_PERMISO` |
| DELETE | `/{id}/permissions` | Remover permisos | `ROL_REMOVER_PERMISO` |
| POST | `/{roleId}/users/{userId}` | Asignar rol a usuario | `ROL_ASIGNAR_USUARIO` |
| DELETE | `/{roleId}/users/{userId}` | Remover rol de usuario | `ROL_REMOVER_USUARIO` |

#### `ProceduresController` (`/api/v1/procedures`)

| Método | Endpoint | Función | Permiso |
|---|---|---|---|
| GET | `/` | Listar activos | `TRAMITE_LISTAR` |
| GET | `/all` | Listar todos (incl. inactivos) | `TRAMITE_LISTAR` |
| GET | `/{id}` | Obtener trámite | `TRAMITE_VER` |
| GET | `/code/{code}` | Buscar por código | `TRAMITE_VER` |
| GET | `/workflow/{workflowId}` | Por flujo de trabajo | `TRAMITE_LISTAR` |
| POST | `/` | Crear trámite | `TRAMITE_CREAR` |
| PUT | `/{id}` | Actualizar | `TRAMITE_MODIFICAR` |
| DELETE | `/{id}` | Eliminar | `TRAMITE_ELIMINAR` |
| PATCH | `/{id}/activate` | Activar | `TRAMITE_ACTIVAR` |
| PATCH | `/{id}/deactivate` | Desactivar | `TRAMITE_DESACTIVAR` |
| GET | `/{id}/requires-2fa` | Verificar si requiere 2FA | `TRAMITE_VER` |

#### `CredentialsController` (`/api/v1/credentials`)

| Método | Endpoint | Función | Permiso |
|---|---|---|---|
| GET | `/` | Listar credenciales | `CRED_LISTAR` |
| GET | `/{id}` | Obtener credencial | `CRED_VER` |
| POST | `/` | Crear credencial | `CRED_CREAR` |
| DELETE | `/{id}` | Eliminar credencial | `CRED_ELIMINAR` |
| PATCH | `/{id}/change-password` | Cambiar contraseña | `CRED_CAMBIAR_PASS` |
| PATCH | `/{id}/reset-password` | Resetear contraseña | `CRED_RESETEAR_PASS` |
| GET | `/{id}/password-expired` | ¿Contraseña expirada? | `CRED_VER` |
| PATCH | `/{id}/lock` | Bloquear cuenta | `CRED_BLOQUEAR` |
| PATCH | `/{id}/unlock` | Desbloquear cuenta | `CRED_DESBLOQUEAR` |

### 9.3 Controladores con Stored Procedures

Estos controladores trabajan con servicios que llaman a procedimientos almacenados. Retornan mensajes de texto como respuesta:

| Controlador | Base Path | Permisos |
|---|---|---|
| `AcademicCalendarController` | `/api/v1/academic-calendar` | `CAL_CREAR`, `CAL_MODIFICAR`, `CAL_ELIMINAR`, `CAL_LISTAR` |
| `CareerController` | `/api/v1/careers` | `CARRERA_CREAR`, `CARRERA_MODIFICAR`, `CARRERA_ELIMINAR`, `CARRERA_LISTAR` |
| `ConfigurationController` | `/api/v1/configuration` | `CONFIG_CREAR`, `CONFIG_MODIFICAR`, `CONFIG_ELIMINAR`, `CONFIG_LISTAR` |
| `DeadlineruleControllers` | `/api/v1/deadlinerules` | `REGLA_CREAR`, `REGLA_MODIFICAR`, `REGLA_ELIMINAR`, `REGLA_LISTAR` |
| `DocumentTemplateController` | `/api/v1/document-templates` | `PLANTILLA_CREAR`, `PLANTILLA_MODIFICAR`, `PLANTILLA_ELIMINAR`, `PLANTILLA_LISTAR` |
| `FacultyController` | `/api/v1/faculties` | `FACULTAD_CREAR`, `FACULTAD_MODIFICAR`, `FACULTAD_ELIMINAR`, `FACULTAD_LISTAR` |
| `PermissionController` | `/api/v1/permissions` | `PERMISO_CREAR`, `PERMISO_MODIFICAR`, `PERMISO_ELIMINAR`, `PERMISO_LISTAR` |
| `ProcessingStageController` | `/api/v1/processing-stages` | `ETAPA_CREAR`, `ETAPA_MODIFICAR`, `ETAPA_ELIMINAR`, `ETAPA_LISTAR` |
| `RejectReasonController` | `/api/v1/rejection-reasons` | `RECHAZO_CREAR`, `RECHAZO_MODIFICAR`, `RECHAZO_ELIMINAR`, `RECHAZO_LISTAR` |
| `StatesController` | `/api/v1/states` | `ESTADO_CREAR`, `ESTADO_MODIFICAR`, `ESTADO_ELIMINAR`, `ESTADO_LISTAR` |
| `WorkFlowsController` | `/api/v1/workflows` | `FLUJO_CREAR`, `FLUJO_MODIFICAR`, `FLUJO_ELIMINAR`, `FLUJO_LISTAR` |

Cada uno sigue el patrón:

| Método | Endpoint | Función |
|---|---|---|
| POST | `/` | Crear (llama a `spi_xxx`) |
| PUT | `/` | Actualizar (llama a `spu_xxx`) |
| DELETE | `/` | Eliminar (llama a `spd_xxx`) |
| GET | `/` | Listar todos (llama a `fn_list_xxx`) |

### 9.4 Controladores CRUD con DTOs (patrón fetch-then-merge)

Estos controladores convierten entre entidades JPA y DTOs de respuesta mediante un método privado `toResponse()`. Sus métodos `update()` implementan el patrón **fetch-then-merge**:

1. Buscan la entidad existente en BD con `findById()`.
2. Si no existe → `ResourceNotFoundException` (HTTP 404).
3. Aplican solo los campos del DTO sobre la entidad existente.
4. Guardan la entidad modificada (preservando timestamps y campos no enviados).

| Controlador | Base Path | Entidad | Permisos |
|---|---|---|---|
| `WorkflowStagesController` | `/api/v1/workflow-stages` | WorkflowStages | `FLUJOETAPA_*` |
| `StageTrackingController` | `/api/v1/stage-tracking` | StageTracking | `SEGUIMIENTO_*` |
| `SessionTokenController` | `/api/v1/session-tokens` | SessionToken | `SESION_*` |
| `NotificationController` | `/api/v1/notifications` | Notification | `NOTIF_*` |
| `DocumentsGeneratedController` | `/api/v1/documents-generated` | DocumentsGenerated | `DOCGEN_*` |
| `DigitalSignaturesController` | `/api/v1/digital-signatures` | DigitalSignatures | `FIRMA_*` |
| `ApplicationStageHistoryController` | `/api/v1/application-stage-history` | ApplicationStageHistory | `HIST_*` |
| `RequirementsController` | `/api/v1/requirements` | RequirementsOfTheProcedure | `REQUISITO_*` |
| `NotificationTypeController` | `/api/v1/notification-types` | NotificationType | `TIPNOTIF_*` |

Cada uno expone:

| Método | Endpoint | Función |
|---|---|---|
| GET | `/` | Listar todos |
| GET | `/{id}` | Obtener por ID |
| POST | `/` | Crear (con `@Valid`) |
| PUT | `/{id}` | Actualizar (fetch-then-merge, con `@Valid`) |
| DELETE | `/{id}` | Eliminar |

### 9.5 Controlador con Google Drive

#### `AttachedDocumentsController` (`/api/v1/attached-documents`)

Controlador especial que integra operaciones de base de datos con Google Drive:

| Método | Endpoint | Función | Permiso |
|---|---|---|---|
| GET | `/` | Listar documentos adjuntos | `DOCADJ_LISTAR` |
| GET | `/{id}` | Obtener metadatos de documento | `DOCADJ_VER` |
| POST | `/upload` | Subir archivo a Drive + registrar en BD | `DOCADJ_CREAR` |
| GET | `/{id}/download` | Descargar archivo desde Drive | `DOCADJ_VER` |
| PUT | `/{id}` | Actualizar metadatos (fetch-then-merge) | `DOCADJ_MODIFICAR` |
| DELETE | `/{id}` | Eliminar de Drive + eliminar registro de BD | `DOCADJ_ELIMINAR` |

**¿Por qué el upload es multipart y no JSON?** Los archivos binarios no pueden representarse eficientemente en JSON. Multipart permite enviar el archivo junto con los parámetros de relación (`applicationId`, `requirementId`, `uploadedByUserId`).

**¿Por qué se elimina de Drive y BD conjuntamente?** Si solo se eliminara el registro en BD, el archivo quedaría huérfano en Drive consumiendo espacio. La eliminación coordinada garantiza consistencia.

### 9.6 Controladores Auxiliares

#### `RefreshTokenController` (`/api/v1/refresh-tokens`)

| Método | Endpoint | Función | Permiso |
|---|---|---|---|
| GET | `/` | Listar refresh tokens | `TOKEN_LISTAR` |
| GET | `/{id}` | Obtener token por ID | `TOKEN_VER` |
| DELETE | `/{id}` | Revocar token | `TOKEN_ELIMINAR` |

**Importante:** El `RefreshTokenResponse` **nunca expone el valor del token JWT**. Solo muestra el ID, estado de revocación, fechas y dispositivo. Esto previene fuga de tokens si un admin lista los refresh tokens activos.

#### `EmailController` (`/api/v1/mail`)

| Método | Endpoint | Función | Permiso |
|---|---|---|---|
| POST | `/send` | Enviar email de texto plano | `EMAIL_ENVIAR` |
| POST | `/send-html` | Enviar email HTML | `EMAIL_ENVIAR` |

---

## 10. DTOs (Data Transfer Objects)

El proyecto usa ~91 DTOs organizados con una convención de nombres estricta:

### 10.1 Convención de Nombres

| Prefijo | Significado | Ejemplo |
|---|---|---|
| `C` | **Create** Request | `CUserRequest` |
| `U` | **Update** Request | `UUserRequest` |
| `I` | **Insert** Request (SP) | `IAcademicCalendarRequest` |
| `D` | **Delete** Request (SP) | `DAcademicCalendarRequest` |
| — (sin prefijo) | **Response** | `UserResponse`, `ApplicationResponse` |

### 10.2 DTOs de Request (Create/Update)

Los DTOs de request contienen anotaciones de validación Jakarta:

```java
public class CUserRequest {
    @NotNull(message = "El nombre es obligatorio")
    @Size(min = 2, max = 255, message = "El nombre debe tener entre 2 y 255 caracteres")
    private String names;

    @NotNull(message = "El apellido es obligatorio")
    @Size(min = 2, max = 255)
    private String surnames;

    @NotNull(message = "La cédula es obligatoria")
    @Size(min = 10, max = 10, message = "La cédula debe tener 10 dígitos")
    private String cardId;
    
    // ... más campos con validaciones
}
```

**¿Por qué separar Create y Update DTOs?** 
- **Create** puede requerir campos que se autogeneran (como `createdAt`) y no deben enviarse.
- **Update** puede hacer opcionales campos que en Create son obligatorios.
- Permite validaciones diferentes por operación.

### 10.3 DTOs de Response

Los response DTOs **nunca exponen**:
- Hash de contraseña (`passwordHash`).
- Valor de tokens de sesión ni refresh tokens.
- Claves secretas 2FA.
- Entidades JPA completas (previene serialización recursiva).

Solo retornan IDs de llaves foráneas en lugar de objetos anidados:

```java
public class ApplicationResponse {
    private Integer id;
    private String applicationCode;
    private LocalDateTime creationDate;
    private String priority;
    private Integer procedureId;       // Solo el ID, no el objeto completo
    private Integer applicantUserId;   // Solo el ID
    // ...
}
```

### 10.4 DTOs Especiales

| DTO | Tipo | Propósito |
|---|---|---|
| `ApiErrorResponse` | Clase | Respuesta estándar de errores con `fieldErrors` |
| `EmailRequest` | Record | Request para envío de emails (`to`, `subject`, `body`) |
| `EmailResponse` | Record | Confirmación de envío |
| `TwoFactorSetupResponse` | Clase | Contiene `secretKey`, `qrCodeUri` y `backupCodes` |
| `TwoFactorVerifyRequest` | Clase | Código TOTP para verificación |
| `TwoFactorBackupRequest` | Clase | Código de respaldo |
| `TwoFactorAuthResponse` | Clase | Estado de 2FA del usuario |

### 10.5 `ApiErrorResponse`

DTO central para respuestas de error consistentes en toda la API:

```json
{
  "timestamp": "2025-06-16T18:30:00",
  "status": 400,
  "error": "Error de Validación",
  "message": "La solicitud contiene campos inválidos",
  "path": "/api/v1/users",
  "fieldErrors": [
    {
      "field": "names",
      "message": "El nombre es obligatorio",
      "rejectedValue": null
    }
  ]
}
```

**¿Por qué un DTO de error estándar?** El frontend puede manejar cualquier error con la misma estructura. `@JsonInclude(NON_NULL)` omite campos nulos para mantener respuestas limpias.

**Métodos factory:**
- `ApiErrorResponse.of(status, error, message, path)`: Crea respuesta de error sin field errors.
- `ApiErrorResponse.ofValidation(status, error, message, path, fieldErrors)`: Crea respuesta con errores de validación por campo.

---

## 11. Excepciones y Manejo Global de Errores

### 11.1 Excepciones Personalizadas

| Excepción | HTTP Status | Cuándo se lanza | Ejemplo |
|---|---|---|---|
| `ResourceNotFoundException` | 404 | Recurso no existe | `Usuario con ID 999 no encontrado` |
| `DuplicateResourceException` | 409 | Clave duplicada | `Email ya registrado` |
| `BadRequestException` | 400 | Datos inválidos o lógica incumplida | `Prioridad no válida` |
| `BusinessException` | 422 | Regla de negocio violada | `STUDENT_ALREADY_GRADUATED` |
| `UnauthorizedException` | 401 | Acceso no autorizado | `Token no válido` |

### 11.2 `BusinessException`

Excepción especial que incluye un `errorCode` string además del mensaje. Útil para que el frontend muestre mensajes localizados basándose en el código:

```java
throw new BusinessException("STUDENT_ALREADY_GRADUATED", 
    "El estudiante ya está graduado y no puede ser promovido");
```

### 11.3 `GlobalExceptionHandler`

`@RestControllerAdvice` que intercepta **todas** las excepciones y las convierte en respuestas `ApiErrorResponse`:

| Handler | Excepciones | Status | Detalle |
|---|---|---|---|
| `handleValidationException` | `MethodArgumentNotValidException` | 400 | Lista de `fieldErrors` con campo, mensaje y valor rechazado |
| `handleMissingParams` | `MissingServletRequestParameterException` | 400 | Parámetro faltante |
| `handleTypeMismatch` | `MethodArgumentTypeMismatchException` | 400 | Tipo incorrecto |
| `handleResourceNotFound` | `ResourceNotFoundException` | 404 | Recurso no encontrado |
| `handleDuplicateResource` | `DuplicateResourceException` | 409 | Conflicto de duplicados |
| `handleBadRequest` | `BadRequestException` | 400 | Request inválido |
| `handleBusinessException` | `BusinessException` | 422 | Regla de negocio + errorCode |
| `handleUnauthorized` | `UnauthorizedException` | 401 | No autorizado |
| `handleAccessDenied` | `AccessDeniedException` | 403 | Prohibido (sin permisos) |
| `handleAuthenticationException` | `AuthenticationException` + `BadCredentialsException` | 401 | Credenciales inválidas |
| `handleGenericException` | `Exception` (fallback) | 500 | Error inesperado (se loguea) |

**¿Por qué un handler global?** Sin él, Spring devolvería stack traces Java al frontend, exponiendo detalles internos del sistema. El handler global garantiza:
- Respuestas consistentes y legibles.
- Seguridad (no se exponen internals).
- Logging automático de errores 500.

---

## 12. Configuración de `application.properties`

```properties
# === Aplicación ===
spring.application.name=Backend
server.port=8080

# === Base de Datos ===
spring.datasource.url=jdbc:postgresql://localhost:5432/SGTE_V1
spring.datasource.username=postgres
spring.datasource.password=12345
spring.datasource.driver-class-name=org.postgresql.Driver

# === JPA/Hibernate ===
spring.jpa.hibernate.ddl-auto=update    # Auto-crea/actualiza tablas (dev only)
spring.jpa.show-sql=true                # Muestra queries SQL en consola
spring.jpa.properties.hibernate.format_sql=true  # Formatea SQL para legibilidad

# === Logging ===
logging.level.org.hibernate.SQL=DEBUG           # Log de queries
logging.level.org.hibernate.orm.jdbc.bind=TRACE # Log de parámetros bind
logging.level.org.hibernate.type.descriptor.sql=TRACE

# === JWT (claves RSA) ===
rsa.public-key=classpath:certs/public.pem
rsa.private-key=classpath:certs/private.pem

# === CORS ===
cors.allowed-origins=http://localhost:4200   # Frontend Angular

# === Google Drive ===
google.drive.credentials.path=${GOOGLE_DRIVE_CREDENTIALS_PATH:certs/google-drive-credentials.json}
google.drive.folder.id=${GOOGLE_DRIVE_FOLDER_ID:15dnYnPD7vlENY6yzdZGpVXPEXs92-7Rw}
```

**Notas importantes:**
- `ddl-auto=update` es conveniente en desarrollo pero **peligroso en producción** (puede alterar tablas existentes). En producción usar `validate` o `none` con migraciones Flyway/Liquibase.
- Las credenciales de Google Drive se pueden pasar como variables de entorno (`GOOGLE_DRIVE_CREDENTIALS_PATH`), con fallback a classpath.
- Los logs de Hibernate en `TRACE` muestran todos los parámetros SQL — deshabilitar en producción por seguridad y rendimiento.

---

## 13. Dependencias Maven (pom.xml)

### 13.1 Configuración del Proyecto

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.2</version>
</parent>

<properties>
    <java.version>21</java.version>
</properties>
```

### 13.2 Dependencias de Producción

| Dependencia | Group | Propósito |
|---|---|---|
| `spring-boot-starter-webmvc` | Spring | Controladores REST, MVC, Jackson JSON |
| `spring-boot-starter-data-jpa` | Spring | JPA/Hibernate, repositories, transacciones |
| `spring-boot-starter-security` | Spring | Cadena de filtros de seguridad |
| `spring-boot-starter-oauth2-resource-server` | Spring | Soporte JWT (decodificación, validación RS256) |
| `spring-boot-starter-validation` | Spring | Bean Validation (`@NotNull`, `@Size`, `@Valid`, etc.) |
| `spring-boot-starter-mail` | Spring | Envío de emails SMTP vía `JavaMailSender` |
| `postgresql` | PostgreSQL | Driver JDBC para conectar a la BD (scope: runtime) |
| `lombok` 1.18.42 | Lombok | `@Data`, `@Builder`, `@RequiredArgsConstructor` |
| `bcpkix-jdk18on` 1.83 | BouncyCastle | Generación de claves RSA en formato PEM |
| `googleauth` 1.5.0 | WLabs | TOTP para 2FA (RFC 6238) |
| `google-api-services-drive` v3 | Google | API de Google Drive v3 |
| `google-auth-library-oauth2-http` | Google | Autenticación OAuth2 para Google APIs |

### 13.2 Dependencias de Desarrollo/Test

| Dependencia | Scope | Propósito |
|---|---|---|
| `spring-boot-devtools` | runtime (optional) | Reinicio automático en desarrollo |
| `spring-boot-starter-test` | test | JUnit 5, Mockito, AssertJ |
| `spring-boot-starter-webmvc-test` | test | `@WebMvcTest`, MockMvc |
| `spring-security-test` | test | `@WithMockUser`, SecurityMockMvcRequestPostProcessors |

---

## 14. Diagrama de Relaciones entre Entidades

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  Faculties   │ 1:N │   Careers    │ 1:N │   Students   │
│ (dean→Users) ├────→│(coord→Users) ├────→│ (user→Users) │
└──────────────┘     └──────────────┘     └──────────────┘

┌──────────────┐ M:N ┌────────────┐ M:N ┌──────────────┐
│    Users     ├────→│ user_roles ├────→│    Roles     │
│              │     └────────────┘     │              │
│ config→      │                        │ permissions  │
│ Configurations     ┌────────────────┐ │  ↓           │
│              │     │role_permissions├─┤  Set<Perms>  │
│ credentials→ │     └────────────────┘ └──────────────┘
│ Credentials  │
└──────┬───────┘     ┌──────────────┐
       │ 1:1         │ TwoFactorAuth│
       └────────────→│(cred→Creds)  │
                     └──────────────┘

┌──────────────┐ 1:N ┌──────────────┐
│  Workflows   ├────→│WorkflowStages│
└──────┬───────┘     │ (proc_stage→ │
       │             │ProcessingStage)
       │             └──────────────┘
       │
       ↓
┌──────────────┐ 1:N ┌──────────────┐ 1:N ┌──────────────┐
│  Procedures  ├────→│ Applications ├────→│ AttachedDocs │
│(calendar,    │     │(user, stage, │     │(req, drive)  │
│ deadlineRule)│     │ rejection)   │     └──────────────┘
└──────────────┘     │              │
                     │              │ 1:N ┌──────────────┐
                     │              ├────→│ DocsGenerated│
                     │              │     │(template,    │
                     │              │     │ signature)   │
                     │              │     └──────────────┘
                     │              │
                     │              │ 1:N ┌──────────────┐
                     │              ├────→│ AppStageHist │
                     │              │     │(stageTrack,  │
                     │              │     │ processedBy) │
                     └──────────────┘     └──────────────┘

┌──────────────┐     ┌──────────────┐
│    States    │←────│StageTracking │
└──────────────┘     │(procStage,   │
                     │ assignedUser)│
                     └──────────────┘

┌──────────────┐     ┌──────────────┐
│Notif.Type    │←────│ Notification │
└──────────────┘     │(app, user)   │
                     └──────────────┘

┌──────────────┐     ┌──────────────┐
│RefreshToken  │     │ SessionToken │
│(user, token) │     │(user, token) │
└──────────────┘     └──────────────┘

┌──────────────┐     ┌────────────────────┐
│ DigitalSig   │     │ RequirementsOfProc  │
│(user, cert)  │     │(procedure, desc)   │
└──────────────┘     └────────────────────┘
```

---

## 15. Catálogo de Permisos

### 15.1 Permisos por Módulo

| Módulo | Permisos |
|---|---|
| **Usuarios** | `USUARIO_LISTAR`, `USUARIO_VER`, `USUARIO_CREAR`, `USUARIO_MODIFICAR`, `USUARIO_ELIMINAR`, `USUARIO_ACTIVAR`, `USUARIO_DESACTIVAR` |
| **Estudiantes** | `ESTUDIANTE_LISTAR`, `ESTUDIANTE_VER`, `ESTUDIANTE_CREAR`, `ESTUDIANTE_MODIFICAR`, `ESTUDIANTE_ELIMINAR`, `ESTUDIANTE_PROMOVER`, `ESTUDIANTE_GRADUAR`, `ESTUDIANTE_RETIRAR`, `ESTUDIANTE_REACTIVAR` |
| **Roles** | `ROL_LISTAR`, `ROL_VER`, `ROL_CREAR`, `ROL_MODIFICAR`, `ROL_ELIMINAR`, `ROL_ASIGNAR_PERMISO`, `ROL_REMOVER_PERMISO`, `ROL_ASIGNAR_USUARIO`, `ROL_REMOVER_USUARIO` |
| **Credenciales** | `CRED_LISTAR`, `CRED_VER`, `CRED_CREAR`, `CRED_CAMBIAR_PASS`, `CRED_RESETEAR_PASS`, `CRED_BLOQUEAR`, `CRED_DESBLOQUEAR`, `CRED_ELIMINAR` |
| **2FA** | `AUTH2FA_CONFIGURAR`, `AUTH2FA_VERIFICAR`, `AUTH2FA_DESACTIVAR`, `AUTH2FA_ESTADO`, `AUTH2FA_REGENERAR` |
| **Solicitudes** | `SOL_LISTAR`, `SOL_VER`, `SOL_CREAR`, `SOL_MODIFICAR`, `SOL_ELIMINAR`, `SOL_RESOLVER`, `SOL_RECHAZAR` |
| **Trámites** | `TRAMITE_LISTAR`, `TRAMITE_VER`, `TRAMITE_CREAR`, `TRAMITE_MODIFICAR`, `TRAMITE_ACTIVAR`, `TRAMITE_DESACTIVAR`, `TRAMITE_ELIMINAR` |
| **Calendarios** | `CAL_CREAR`, `CAL_MODIFICAR`, `CAL_ELIMINAR`, `CAL_LISTAR` |
| **Carreras** | `CARRERA_CREAR`, `CARRERA_MODIFICAR`, `CARRERA_ELIMINAR`, `CARRERA_LISTAR` |
| **Facultades** | `FACULTAD_CREAR`, `FACULTAD_MODIFICAR`, `FACULTAD_ELIMINAR`, `FACULTAD_LISTAR` |
| **Configuración** | `CONFIG_CREAR`, `CONFIG_MODIFICAR`, `CONFIG_ELIMINAR`, `CONFIG_LISTAR` |
| **Permisos** | `PERMISO_CREAR`, `PERMISO_MODIFICAR`, `PERMISO_ELIMINAR`, `PERMISO_LISTAR` |
| **Estados** | `ESTADO_CREAR`, `ESTADO_MODIFICAR`, `ESTADO_ELIMINAR`, `ESTADO_LISTAR` |
| **Etapas** | `ETAPA_CREAR`, `ETAPA_MODIFICAR`, `ETAPA_ELIMINAR`, `ETAPA_LISTAR` |
| **Reglas de Plazo** | `REGLA_CREAR`, `REGLA_MODIFICAR`, `REGLA_ELIMINAR`, `REGLA_LISTAR` |
| **Flujos de Trabajo** | `FLUJO_CREAR`, `FLUJO_MODIFICAR`, `FLUJO_ELIMINAR`, `FLUJO_LISTAR` |
| **Flujo-Etapas** | `FLUJOETAPA_LISTAR`, `FLUJOETAPA_VER`, `FLUJOETAPA_CREAR`, `FLUJOETAPA_MODIFICAR`, `FLUJOETAPA_ELIMINAR` |
| **Seguimiento** | `SEGUIMIENTO_LISTAR`, `SEGUIMIENTO_VER`, `SEGUIMIENTO_CREAR`, `SEGUIMIENTO_MODIFICAR`, `SEGUIMIENTO_ELIMINAR` |
| **Sesiones** | `SESION_LISTAR`, `SESION_VER`, `SESION_CREAR`, `SESION_MODIFICAR`, `SESION_ELIMINAR` |
| **Tokens** | `TOKEN_LISTAR`, `TOKEN_VER`, `TOKEN_ELIMINAR` |
| **Tipos Notificación** | `TIPNOTIF_LISTAR`, `TIPNOTIF_VER`, `TIPNOTIF_CREAR`, `TIPNOTIF_MODIFICAR`, `TIPNOTIF_ELIMINAR` |
| **Notificaciones** | `NOTIF_LISTAR`, `NOTIF_VER`, `NOTIF_CREAR`, `NOTIF_MODIFICAR`, `NOTIF_ELIMINAR` |
| **Docs Generados** | `DOCGEN_LISTAR`, `DOCGEN_VER`, `DOCGEN_CREAR`, `DOCGEN_MODIFICAR`, `DOCGEN_ELIMINAR` |
| **Firmas Digitales** | `FIRMA_LISTAR`, `FIRMA_VER`, `FIRMA_CREAR`, `FIRMA_MODIFICAR`, `FIRMA_ELIMINAR` |
| **Docs Adjuntos** | `DOCADJ_LISTAR`, `DOCADJ_VER`, `DOCADJ_CREAR`, `DOCADJ_MODIFICAR`, `DOCADJ_ELIMINAR` |
| **Historial Etapas** | `HIST_LISTAR`, `HIST_VER`, `HIST_CREAR`, `HIST_MODIFICAR`, `HIST_ELIMINAR` |
| **Requisitos** | `REQUISITO_LISTAR`, `REQUISITO_VER`, `REQUISITO_CREAR`, `REQUISITO_MODIFICAR`, `REQUISITO_ELIMINAR` |
| **Plantillas Doc** | `PLANTILLA_CREAR`, `PLANTILLA_MODIFICAR`, `PLANTILLA_ELIMINAR`, `PLANTILLA_LISTAR` |
| **Razones Rechazo** | `RECHAZO_CREAR`, `RECHAZO_MODIFICAR`, `RECHAZO_ELIMINAR`, `RECHAZO_LISTAR` |
| **Email** | `EMAIL_ENVIAR` |

---

## 16. Resumen Estadístico del Proyecto

| Métrica | Cantidad |
|---|---|
| **Entidades JPA** | 29 |
| **Repositorios** | 29 (11 con SP + 18 JPA directo) |
| **Interfaces de Servicio** | 32 |
| **Implementaciones de Servicio** | 32 (incluyendo CustomUserDetailsService) |
| **Controladores** | 31 |
| **DTOs** | ~91 (Create + Update + Response + Especiales) |
| **Excepciones personalizadas** | 5 |
| **Archivos de configuración** | 5 |
| **Stored Procedures referenciados** | 33 (11×3 SPIs/SPUs/SPDs) |
| **Funciones PostgreSQL referenciadas** | 11 |
| **Códigos de permisos** | ~140+ |
| **Endpoints REST** | ~150+ |
| **Versión Spring Boot** | 4.0.2 |
| **Versión Java** | 21 |

### Stack de Seguridad Completo

| Aspecto | Implementación |
|---|---|
| Autenticación | JWT RS256 (access_token 15min + refresh_token 24h) |
| Autorización | Permisos granulares (`@PreAuthorize("hasAuthority('...')")`) |
| 2FA | TOTP (Google Authenticator) + 8 códigos de respaldo de 8 chars |
| Contraseñas | BCrypt hash + expiración 90 días + bloqueo tras 5 intentos |
| Tokens | Rotación de refresh tokens + revocación masiva en logout |
| CORS | Configurado para frontend Angular (localhost:4200) |
| CSRF | Deshabilitado (JWT stateless, no usa cookies de sesión) |
| Sesiones | STATELESS (sin estado en servidor) |
| Claves | RSA 2048-bit PEM (pública + privada) |
