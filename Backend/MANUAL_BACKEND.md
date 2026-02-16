# SGTE Backend — Manual Técnico Completo

> **Sistema de Gestión de Trámites Estudiantiles (SGTE)**  
> Backend desarrollado con Spring Boot 3.2.2 · Java 21 · PostgreSQL

---

## Tabla de Contenido

1. [Visión General y Arquitectura](#1-visión-general-y-arquitectura)
2. [Archivos Raíz](#2-archivos-raíz)
3. [Configuración (Config)](#3-configuración-config)
4. [Entidades (Entity)](#4-entidades-entity)
5. [Repositorios (Repository)](#5-repositorios-repository)
6. [Servicios — Interfaces (Services)](#6-servicios--interfaces-services)
7. [Servicios — Implementaciones (Services/Impl)](#7-servicios--implementaciones-servicesimpl)
8. [Controladores (Controllers)](#8-controladores-controllers)
9. [DTOs (Data Transfer Objects)](#9-dtos-data-transfer-objects)
10. [Excepciones (Exceptions)](#10-excepciones-exceptions)
11. [Diagrama de Relaciones entre Entidades](#11-diagrama-de-relaciones-entre-entidades)

---

## 1. Visión General y Arquitectura

### Stack Tecnológico

| Componente | Tecnología |
|---|---|
| Framework | Spring Boot 3.2.2 |
| Lenguaje | Java 21 |
| Base de datos | PostgreSQL |
| Seguridad | Spring Security + OAuth2 Resource Server (JWT RS256) |
| Autenticación 2FA | Google Authenticator (TOTP) vía `googleauth:1.5.0` |
| Validación | Jakarta Bean Validation (`spring-boot-starter-validation`) |
| Build | Maven |
| Reducción de boilerplate | Lombok |

### Arquitectura en Capas

```
┌─────────────────────────────────────────────────────────┐
│                    Controladores (30)                     │
│         Reciben HTTP, validan DTOs, delegan a servicios  │
├─────────────────────────────────────────────────────────┤
│                 Servicios — Interfaces (29)               │
│              Contratos de las operaciones de negocio      │
├─────────────────────────────────────────────────────────┤
│              Servicios — Implementaciones (29+1)          │
│        Lógica de negocio, validaciones, transformación    │
├─────────────────────────────────────────────────────────┤
│                    Repositorios (29)                      │
│     JpaRepository + Queries derivadas / Stored Procs     │
├─────────────────────────────────────────────────────────┤
│                     Entidades (29)                        │
│              Mapeo ORM a tablas PostgreSQL                │
└─────────────────────────────────────────────────────────┘
```

### Roles del Sistema

| Rol | Descripción |
|---|---|
| `ROLE_ADMIN` | Administrador del sistema, acceso total |
| `ROLE_STUDENT` | Estudiante, puede crear y dar seguimiento a solicitudes |
| `ROLE_COORDINATOR` | Coordinador de carrera, gestiona estudiantes y trámites |
| `ROLE_DEAN` | Decano de facultad, aprueba operaciones académicas |

### Patrones de Acceso a Datos

El backend implementa **dos patrones** de acceso a datos que coexisten:

- **Patrón A — Stored Procedures (SP):** Entidades de catálogo (calendarios, carreras, facultades, etc.) delegan todas las operaciones CRUD a procedimientos almacenados y funciones de PostgreSQL. Los resultados `Object[]` se mapean manualmente a DTOs.
- **Patrón B — JPA directo:** Entidades de negocio principales (usuarios, estudiantes, solicitudes, procedimientos, roles, credenciales) utilizan `JpaRepository` con queries derivadas, validación rica y lógica de dominio.

---

## 2. Archivos Raíz

### `BackendApplication.java`

Punto de entrada principal de la aplicación Spring Boot.

- Anotación: `@SpringBootApplication`
- Habilita la lectura de claves RSA mediante `@EnableConfigurationProperties(RsaKeyConfig.class)`
- Define el bean `PasswordEncoder` usando `BCryptPasswordEncoder` para el hashing seguro de contraseñas

### `GenerateKeyPair.java`

Utilidad standalone (no es un bean de Spring) para generar un par de claves RSA de 2048 bits.

- Escribe `public.pem` y `private.pem` en `src/main/resources/certs/`
- Usa BouncyCastle `PemWriter` para formato PEM
- Se ejecuta una sola vez durante la configuración inicial del proyecto
- No se incluye en el flujo normal de la aplicación

---

## 3. Configuración (Config)

### `RsaKeyConfig.java`

Record de Java que vincula las propiedades `rsa.public-key` y `rsa.private-key` del `application.properties` a objetos `RSAPublicKey` / `RSAPrivateKey`. Spring auto-parsea los archivos PEM a objetos RSA tipados.

```java
@ConfigurationProperties(prefix = "rsa")
public record RsaKeyConfig(RSAPublicKey publicKey, RSAPrivateKey privateKey) {}
```

### `SecurityConfig.java`

Configuración central de seguridad de la aplicación.

- **Anotaciones:** `@EnableWebSecurity`, `@EnableMethodSecurity` (habilita `@PreAuthorize` a nivel método)
- **AuthenticationManager:** `DaoAuthenticationProvider` con `CustomUserDetailsService` + BCrypt
- **SecurityFilterChain:**
  - CORS habilitado (orígenes configurables vía propiedad `cors.allowed-origins`)
  - CSRF deshabilitado (API stateless basada en JWT)
  - Endpoints públicos: `/api/v1/auth/**`, `/api/v1/2fa/validate`, `/api/v1/2fa/validate-backup`
  - Todos los demás endpoints requieren autenticación
  - OAuth2 Resource Server con JWT
  - `SessionCreationPolicy.STATELESS`
- **JWT Converter personalizado:** Convierte el claim `scope` del JWT en autoridades duales (`SCOPE_ROLE_X` + `ROLE_X`), permitiendo que tanto `hasRole('ADMIN')` como `hasAuthority('SCOPE_ROLE_ADMIN')` funcionen correctamente
- **JwtDecoder:** `NimbusJwtDecoder` con clave pública RSA
- **JwtEncoder:** `NimbusJwtEncoder` con par de claves RSA

### `SpResultConverter.java`

Clase de utilidad estática para convertir resultados `Object[]` de Stored Procedures a valores Java tipados.

| Método | Descripción |
|---|---|
| `toInt(Object)` | Convierte a `Integer` |
| `toStr(Object)` | Convierte a `String` |
| `toBool(Object)` | Convierte a `Boolean` |
| `toLocalDateTime(Object)` | Convierte a `LocalDateTime` |
| `toLocalDate(Object)` | Convierte a `LocalDate` |

Utilizada por las implementaciones de servicios basados en Stored Procedures para evitar duplicación de código de conversión.

### `StringListConverter.java`

JPA `AttributeConverter<List<String>, String>` que almacena una `List<String>` como texto delimitado por comas en la base de datos. Utilizado por la entidad `TwoFactorAuth` para persistir los códigos de respaldo de 2FA.

---

## 4. Entidades (Entity)

Todas las entidades son clases JPA anotadas con `@Entity` y `@Table`. Utilizan Lombok para reducir boilerplate.

### 4.1 `AcademicCalendar`

**Tabla:** `academiccalendar` · **Lombok:** `@Data`, `@Builder`

Representa un período académico con fechas de inicio y fin.

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idAcademicCalendar | Integer | `idacademiccalendar` | PK, auto-increment |
| calendarName | String | `calendarname` | NOT NULL, length=255 |
| academicPeriod | String | `academicperiod` | NOT NULL, length=100 |
| startDate | LocalDate | `startdate` | NOT NULL |
| endDate | LocalDate | `enddate` | NOT NULL |
| active | Boolean | `active` | NOT NULL, default=`true` |

**Relaciones:** Ninguna

---

### 4.2 `Applications`

**Tabla:** `applications` · **Lombok:** `@Data`, `@Builder`

Representa una solicitud o trámite estudiantil.

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| id | Integer | `idapplication` | PK, auto-increment |
| applicationCode | String | `applicationcode` | NOT NULL, UNIQUE, length=100 |
| creationDate | LocalDateTime | `creationdate` | NOT NULL |
| estimatedCompletionDate | LocalDate | `estimatedcompletiondate` | NOT NULL |
| actualCompletionDate | LocalDateTime | `actualcompletiondate` | nullable |
| applicationDetails | String | `applicationdetails` | TEXT |
| applicationResolution | String | `applicationresolution` | TEXT |
| priority | String | `priority` | NOT NULL, length=20, default=`"normal"` |

**Relaciones:**

| Tipo | Campo | Entidad destino | FK |
|---|---|---|---|
| @ManyToOne | rejectionReason | RejectionReasons | `rejectionreasonid` (nullable) |
| @ManyToOne | currentStageTracking | StageTracking | `currentstagetrackingid` |
| @ManyToOne | procedure | Procedures | `proceduresidprocedure` |
| @ManyToOne | applicantUser | Users | `applicantuserid` |

---

### 4.3 `ApplicationStageHistory`

**Tabla:** `applicationstagehistory` · **Lombok:** `@Data`, `@Builder`

Historial de las etapas por las que pasa una solicitud.

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| id | Integer | `idhistory` | PK, auto-increment |
| enteredAt | LocalDateTime | `enteredat` | NOT NULL |
| exitedAt | LocalDateTime | `exitedat` | nullable |
| comments | String | `comments` | TEXT |

**Relaciones:**

| Tipo | Campo | Entidad destino | FK |
|---|---|---|---|
| @ManyToOne | application | Applications | `applicationidapplication` |
| @ManyToOne | stageTracking | StageTracking | `stagetrackingid` |
| @ManyToOne | processedByUser | Users | `processedbyuserid` (nullable) |

---

### 4.4 `AttachedDocuments`

**Tabla:** `attacheddocuments` · **Lombok:** `@Data`, `@Builder`

Documentos adjuntos subidos por usuarios como requisitos de una solicitud.

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| id | Integer | `idattacheddocument` | PK, auto-increment |
| fileName | String | `filename` | NOT NULL, length=255 |
| filePath | String | `filepath` | NOT NULL, length=500 |
| fileSizeBytes | Long | `filesizebytes` | NOT NULL |
| mimeType | String | `mimetype` | NOT NULL, length=100 |
| uploadDate | LocalDateTime | `uploaddate` | NOT NULL |

**Relaciones:**

| Tipo | Campo | Entidad destino | FK |
|---|---|---|---|
| @ManyToOne | application | Applications | `applicationsidapplication` |
| @ManyToOne | requirement | RequirementsOfTheProcedure | `requirementid` (nullable) |
| @ManyToOne | uploadedByUser | Users | `uploadedbyuserid` |

---

### 4.5 `Careers`

**Tabla:** `careers` · **Lombok:** `@Data`, `@Builder`

Carreras universitarias ofrecidas por las facultades.

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idCareer | Integer | `idcareer` | PK, auto-increment |
| careerName | String | `careername` | NOT NULL, length=255 |
| careerCode | String | `careercode` | length=50 |

**Relaciones:**

| Tipo | Campo | Entidad destino | FK | Extra |
|---|---|---|---|---|
| @ManyToOne | faculty | Faculties | `facultiesidfaculty` | LAZY, @JsonIgnore |
| @ManyToOne | coordinator | Users | `coordinatoriduser` (nullable) | LAZY, @JsonIgnore |

---

### 4.6 `Configurations`

**Tabla:** `configurations` · **Lombok:** `@Data`, `@Builder`

Configuración de preferencias de usuario (notificaciones, foto de perfil, firma).

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| id | Integer | `idconfiguration` | PK, auto-increment |
| profilePicturePath | String | `profilepicturepath` | length=500 |
| signaturePath | String | `signaturepath` | length=500 |
| enableSms | Boolean | `enable_sms` | NOT NULL, default=`false` |
| enableEmail | Boolean | `enable_email` | NOT NULL, default=`true` |
| enableWhatsapp | Boolean | `enable_whatsapp` | NOT NULL, default=`false` |
| notificationFrequency | String | `notificationfrequency` | NOT NULL, length=50, default=`"real_time"` |

**Relaciones:** Ninguna

---

### 4.7 `Credentials`

**Tabla:** `credentials` · **Lombok:** `@Data`, `@Builder`

Credenciales de acceso de un usuario (contraseña hasheada, intentos fallidos, bloqueo).

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| id | Integer | `idcredentials` | PK, auto-increment |
| passwordHash | String | `passwordhash` | NOT NULL |
| dateModification | LocalDateTime | `datemodification` | nullable |
| lastLogin | LocalDateTime | `lastlogin` | nullable |
| failedAttempts | Integer | `failedattempts` | NOT NULL, default=`0` |
| accountLocked | Boolean | `accountlocked` | NOT NULL, default=`false` |
| passwordExpiryDate | LocalDate | `passwordexpirydate` | nullable |

**Relaciones:** Ninguna (referenciada por Users vía @OneToOne y por TwoFactorAuth)

---

### 4.8 `DeadLinerules`

**Tabla:** `deadlinerules` · **Lombok:** `@Data`, `@Builder`

Reglas de plazo que definen los tiempos máximos de resolución según categoría de trámite.

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idDeadLineRule | Integer | `iddeadlinerule` | PK, auto-increment |
| ruleName | String | `rulename` | NOT NULL, length=255 |
| procedureCategory | String | `procedurecategory` | NOT NULL, length=100 |
| baseDeadlineDays | Integer | `basedeadlinedays` | NOT NULL |
| warningDaysBefore | Integer | `warningdaysbefore` | NOT NULL, default=`3` |
| active | Boolean | `active` | NOT NULL, default=`true` |

**Relaciones:** Ninguna

---

### 4.9 `DigitalSignatures`

**Tabla:** `digitalsignatures` · **Lombok:** `@Data`, `@Builder`

Firmas digitales vinculadas a certificados de usuario.

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| id | Integer | `iddigitalsignature` | PK, auto-increment |
| certificatePath | String | `certificatepath` | NOT NULL, length=500 |
| certificateSerial | String | `certificateserial` | NOT NULL, UNIQUE, length=255 |
| issuer | String | `issuer` | NOT NULL, length=255 |
| validFrom | LocalDate | `validfrom` | NOT NULL |
| validUntil | LocalDate | `validuntil` | NOT NULL |
| signatureAlgorithm | String | `signaturealgorithm` | NOT NULL, length=100 |
| active | Boolean | `active` | NOT NULL, default=`true` |
| createdAt | LocalDateTime | `createdat` | NOT NULL |

**Relaciones:**

| Tipo | Campo | Entidad destino | FK |
|---|---|---|---|
| @ManyToOne | user | Users | `useriduser` |

---

### 4.10 `DocumentsGenerated`

**Tabla:** `documentsgenerated` · **Lombok:** `@Data`, `@Builder`

Documentos generados a partir de plantillas como resultado de un trámite.

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| id | Integer | `iddocumentgenerated` | PK, auto-increment |
| documentType | String | `documenttype` | NOT NULL, length=255 |
| documentPath | String | `documentpath` | NOT NULL, length=500 |
| generatedAt | LocalDateTime | `generatedat` | NOT NULL |
| signatureTimestamp | LocalDateTime | `signaturetimestamp` | nullable |

**Relaciones:**

| Tipo | Campo | Entidad destino | FK |
|---|---|---|---|
| @ManyToOne | application | Applications | `applicationsidapplication` |
| @ManyToOne | template | DocumentTemplates | `templateid` (nullable) |
| @ManyToOne | generatedByUser | Users | `generatedbyuserid` |
| @ManyToOne | digitalSignature | DigitalSignatures | `digitalsignatureid` (nullable) |

---

### 4.11 `DocumentTemplates`

**Tabla:** `documenttemplates` · **Lombok:** `@Data`, `@Builder`

Plantillas de documentos que pueden usarse para generar documentos oficiales.

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idTemplate | Integer | `idtemplate` | PK, auto-increment |
| templateName | String | `templatename` | NOT NULL, length=255 |
| templateCode | String | `templatecode` | NOT NULL, UNIQUE, length=50 |
| templatePath | String | `templatepath` | NOT NULL, length=500 |
| documentType | String | `documenttype` | NOT NULL, length=100 |
| version | String | `version` | NOT NULL, length=20 |
| requiresSignature | Boolean | `requiressignature` | NOT NULL, default=`false` |
| active | Boolean | `active` | NOT NULL, default=`true` |
| createdAt | LocalDateTime | `createdat` | NOT NULL |
| updatedAt | LocalDateTime | `updatedat` | nullable |

**Relaciones:** Ninguna

---

### 4.12 `Faculties`

**Tabla:** `faculties` · **Lombok:** `@Data`, `@Builder`

Facultades de la universidad.

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idFaculty | Integer | `idfaculty` | PK, auto-increment |
| facultyName | String | `facultyname` | NOT NULL, length=255 |
| facultyCode | String | `facultycode` | length=50 |

**Relaciones:**

| Tipo | Campo | Entidad destino | FK | Extra |
|---|---|---|---|---|
| @ManyToOne | dean | Users | `deaniduser` (nullable) | LAZY, @JsonIgnore |

---

### 4.13 `Notification`

**Tabla:** `notification` · **Lombok:** `@Data`, `@Builder`

Notificaciones enviadas a usuarios del sistema.

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| id | Integer | `idnotification` | PK, auto-increment |
| notificationName | String | `notificationname` | NOT NULL, length=255 |
| message | String | `message` | TEXT |
| sentAt | LocalDateTime | `sentat` | nullable |
| deliveryStatus | String | `deliverystatus` | NOT NULL, length=50, default=`"pending"` |
| deliveryChannel | String | `deliverychannel` | length=50 |
| readAt | LocalDateTime | `readat` | nullable |
| errorMessage | String | `errormessage` | TEXT |
| retryCount | Integer | `retrycount` | NOT NULL, default=`0` |

**Relaciones:**

| Tipo | Campo | Entidad destino | FK |
|---|---|---|---|
| @ManyToOne | notificationType | NotificationType | `notificationtypeidnotificationtype` |
| @ManyToOne | application | Applications | `applicationid` (nullable) |
| @ManyToOne | recipientUser | Users | `recipientuserid` |

---

### 4.14 `NotificationType`

**Tabla:** `notificationtype` · **Lombok:** `@Data`, `@Builder`

Tipos/categorías de notificaciones.

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idNotificationType | Integer | `idnotificationtype` | PK, auto-increment |
| nameTypeNotification | String | `nametypenotification` | NOT NULL, length=255 |
| templateCode | String | `templatecode` | length=50 |
| priorityLevel | String | `prioritylevel` | length=20 |

**Relaciones:** Ninguna

---

### 4.15 `Permissions`

**Tabla:** `permissions` · **Lombok:** `@Data`, `@Builder`

Permisos granulares asignables a roles.

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idPermission | Integer | `idpermission` | PK, auto-increment |
| code | String | `code` | NOT NULL, UNIQUE, length=100 |
| description | String | `description` | nullable |

**Relaciones:** Ninguna (referenciada por Roles vía ManyToMany)

---

### 4.16 `Procedures`

**Tabla:** `procedures` · **Lombok:** `@Data`, `@Builder`

Tipos de trámites que los estudiantes pueden solicitar.

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idProcedure | Integer | `idprocedure` | PK, auto-increment |
| nameProcedure | String | `nameprocedure` | NOT NULL, length=255 |
| procedureCode | String | `procedurecode` | NOT NULL, UNIQUE, length=50 |
| description | String | `description` | nullable |
| estimatedDurationDays | Integer | `estimateddurationdays` | nullable |
| requires2fa | Boolean | `requires2fa` | NOT NULL, default=`false` |
| active | Boolean | `active` | NOT NULL, default=`true` |
| createdAt | LocalDateTime | `createdat` | NOT NULL |

**Relaciones:**

| Tipo | Campo | Entidad destino | FK | Extra |
|---|---|---|---|---|
| @ManyToOne | workflow | Workflows | `workflowsidworkflow` | LAZY, @JsonIgnore |
| @ManyToOne | academicCalendar | AcademicCalendar | `academiccalendaridacademiccalendar` (nullable) | LAZY, @JsonIgnore |
| @ManyToOne | deadLineRule | DeadLinerules | `deadlineruleid` (nullable) | LAZY, @JsonIgnore |

---

### 4.17 `ProcessingStage`

**Tabla:** `processingstage` · **Lombok:** `@Data`, `@Builder`

Etapas de procesamiento que conforman un flujo de trabajo.

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idProcessingStage | Integer | `idprocessingstage` | PK, auto-increment |
| stageName | String | `stagename` | NOT NULL, length=255 |
| stageCode | String | `stagecode` | NOT NULL, UNIQUE, length=50 |
| stageDescription | String | `stagedescription` | nullable |
| stageOrder | Integer | `stageorder` | NOT NULL |
| requiresApproval | Boolean | `requiresapproval` | NOT NULL, default=`false` |
| maxDurationDays | Integer | `maxdurationdays` | nullable |

**Relaciones:** Ninguna

---

### 4.18 `RefreshToken`

**Tabla:** `refresh_tokens` · **Lombok:** `@Data`, `@Builder`

Tokens de refresco para renovar los JWT de acceso sin re-autenticación.

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| id | Long | `id` | PK, auto-increment |
| createdAt | LocalDateTime | `created_at` | NOT NULL |
| deviceInfo | String | `device_info` | length=255 |
| expiresAt | LocalDateTime | `expires_at` | NOT NULL |
| revoked | Boolean | `revoked` | NOT NULL |
| token | String | `token` | NOT NULL, UNIQUE, TEXT |

**Relaciones:**

| Tipo | Campo | Entidad destino | FK |
|---|---|---|---|
| @ManyToOne | user | Users | `user_id` |

---

### 4.19 `RejectionReasons`

**Tabla:** `rejectionreasons` · **Lombok:** `@Data`, `@Builder`

Razones predefinidas para rechazar solicitudes.

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idRejectionReason | Integer | `idrejectionreason` | PK, auto-increment |
| reasonCode | String | `reasoncode` | NOT NULL, UNIQUE, length=50 |
| reasonDescription | String | `reasondescription` | NOT NULL |
| category | String | `category` | NOT NULL, length=100 |
| active | Boolean | `active` | NOT NULL, default=`true` |

**Relaciones:** Ninguna

---

### 4.20 `RequirementsOfTheProcedure`

**Tabla:** `requirementsoftheprocedure` · **Lombok:** `@Data`, `@Builder`

Requisitos que un trámite exige al estudiante (documentos, formularios, etc.).

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| id | Integer | `idrequirementsoftheprocedure` | PK, auto-increment |
| requirementName | String | `requirementname` | NOT NULL, length=255 |
| requirementDescription | String | `requirementdescription` | TEXT |
| requirementType | String | `requirementtype` | NOT NULL, length=50 |
| isMandatory | Boolean | `ismandatory` | NOT NULL, default=`true` |
| displayOrder | Integer | `displayorder` | nullable |

**Relaciones:**

| Tipo | Campo | Entidad destino | FK |
|---|---|---|---|
| @ManyToOne | procedure | Procedures | `proceduresidprocedure` |

---

### 4.21 `Roles`

**Tabla:** `roles` · **Lombok:** `@Getter`, `@Setter`, `@EqualsAndHashCode(onlyExplicitlyIncluded=true)`

Roles del sistema. Usa `@Getter/@Setter` en lugar de `@Data` para evitar StackOverflow en colecciones bidireccionales.

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idRole | Integer | `idrole` | PK, auto-increment, @EqualsAndHashCode.Include |
| roleName | String | `rolename` | NOT NULL, UNIQUE, length=100 |
| roleDescription | String | `roledescription` | TEXT |

**Relaciones:**

| Tipo | Campo | Entidad destino | Join Table | Fetch |
|---|---|---|---|---|
| @ManyToMany | permissions | Permissions | `role_permissions` (joinCol=`idrole`, inverseCol=`idpermission`) | LAZY |

---

### 4.22 `SessionToken`

**Tabla:** `sessiontokens` · **Lombok:** `@Data`, `@Builder`

Tokens de sesión con información de dispositivo para auditoría.

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| id | Integer | `idsession` | PK, auto-increment |
| token | String | `token` | NOT NULL, UNIQUE, TEXT |
| ipAddress | String | `ipaddress` | length=45 |
| userAgent | String | `useragent` | TEXT |
| createdAt | LocalDateTime | `createdat` | NOT NULL |
| expiresAt | LocalDateTime | `expiresat` | NOT NULL |
| lastActivity | LocalDateTime | `lastactivity` | nullable |

**Relaciones:**

| Tipo | Campo | Entidad destino | FK |
|---|---|---|---|
| @ManyToOne | user | Users | `userid` |

---

### 4.23 `StageTracking`

**Tabla:** `stagetracking` · **Lombok:** `@Data`, `@Builder`

Seguimiento del progreso de una solicitud a través de las etapas de procesamiento.

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| id | Integer | `idstagetracking` | PK, auto-increment |
| enteredAt | LocalDateTime | `enteredat` | NOT NULL |
| completedAt | LocalDateTime | `completedat` | nullable |
| notes | String | `notes` | TEXT |

**Relaciones:**

| Tipo | Campo | Entidad destino | FK |
|---|---|---|---|
| @ManyToOne | state | States | `stateidstate` |
| @ManyToOne | processingStage | ProcessingStage | `processingstageidprocessingstage` |
| @ManyToOne | assignedToUser | Users | `assignedtouserid` (nullable) |

---

### 4.24 `States`

**Tabla:** `states` · **Lombok:** `@Data`, `@Builder`

Estados posibles de un trámite (ej. pendiente, en proceso, aprobado, rechazado).

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idState | Integer | `idstate` | PK, auto-increment |
| stateName | String | `statename` | NOT NULL, UNIQUE, length=100 |
| stateDescription | String | `statedescription` | nullable |
| stateCategory | String | `statecategory` | NOT NULL, length=50 |

**Relaciones:** Ninguna

---

### 4.25 `Students`

**Tabla:** `students` · **Lombok:** `@Data`, `@Builder`

Información académica del estudiante (semestre, paralelo, carrera, estado).

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| id | Integer | `idstudent` | PK, auto-increment |
| semester | String | `semester` | NOT NULL, length=255 |
| parallel | String | `parallel` | NOT NULL, length=1 |
| enrollmentDate | LocalDate | `enrollmentdate` | nullable |
| status | String | `status` | NOT NULL, length=50, default=`"activo"` |

**Relaciones:**

| Tipo | Campo | Entidad destino | FK |
|---|---|---|---|
| @ManyToOne | user | Users | `usersiduser` |
| @ManyToOne | career | Careers | `careersidcareer` |

---

### 4.26 `TwoFactorAuth`

**Tabla:** `twofactorauth` · **Lombok:** `@Data`, `@Builder`

Configuración de autenticación de dos factores (TOTP) vinculada a credenciales.

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| id | Integer | `id2fa` | PK, auto-increment |
| enabled | Boolean | `enabled` | NOT NULL, default=`false` |
| secretKey | String | `secretkey` | TEXT |
| backupCodes | List\<String\> | `backupcodes` | TEXT, usa `StringListConverter` |
| verifiedAt | LocalDateTime | `verifiedat` | nullable |

**Relaciones:**

| Tipo | Campo | Entidad destino | FK |
|---|---|---|---|
| @OneToOne | credentials | Credentials | `credentialsidcredentials` (UNIQUE) |

---

### 4.27 `Users`

**Tabla:** `users` · **Lombok:** `@Getter`, `@Setter`, `@EqualsAndHashCode(onlyExplicitlyIncluded=true)`

Usuarios del sistema. Usa `@Getter/@Setter` en lugar de `@Data` para evitar recursión infinita con colecciones.

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idUser | Integer | `iduser` | PK, auto-increment, @EqualsAndHashCode.Include |
| names | String | `names` | NOT NULL, length=255 |
| surnames | String | `surnames` | NOT NULL, length=255 |
| cardId | String | `cardid` | NOT NULL, UNIQUE, length=10 |
| institutionalEmail | String | `institutionalemail` | NOT NULL, UNIQUE, length=255 |
| personalMail | String | `personalmail` | UNIQUE, length=255 |
| phoneNumber | String | `phonenumber` | length=15 |
| statement | Boolean | `statement` | NOT NULL, default=`true` |
| createdAt | LocalDateTime | `createdat` | NOT NULL |
| updatedAt | LocalDateTime | `updatedat` | nullable |
| active | Boolean | `active` | NOT NULL, default=`true` |

**Relaciones:**

| Tipo | Campo | Entidad destino | FK / Join Table | Fetch |
|---|---|---|---|---|
| @ManyToOne | configuration | Configurations | `configurationsidconfiguration` | default |
| @OneToOne | credentials | Credentials | `credentialsidcredentials` (nullable) | default |
| @ManyToMany | roles | Roles | `user_roles` (joinCol=`iduser`, inverseCol=`idrole`) | EAGER |

---

### 4.28 `Workflows`

**Tabla:** `workflows` · **Lombok:** `@Data`, `@Builder`

Flujos de trabajo que definen la secuencia de etapas para resolver un trámite.

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idWorkflow | Integer | `idworkflow` | PK, auto-increment |
| workflowName | String | `workflowname` | NOT NULL, length=255 |
| workflowDescription | String | `workflowdescription` | nullable |
| createdAt | LocalDateTime | `createdat` | NOT NULL |
| active | Boolean | `active` | NOT NULL, default=`true` |

**Relaciones:** Ninguna

---

### 4.29 `WorkflowStages`

**Tabla:** `workflowstages` · **Lombok:** `@Data`, `@Builder`

Tabla de vinculación entre flujos de trabajo y etapas de procesamiento con orden y opcionalidad.

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idWorkflowStage | Integer | `idworkflowstage` | PK, auto-increment |
| sequenceOrder | Integer | `sequenceorder` | NOT NULL |
| isOptional | Boolean | `isoptional` | NOT NULL, default=`false` |

**Relaciones:**

| Tipo | Campo | Entidad destino | FK | Extra |
|---|---|---|---|---|
| @ManyToOne | workflow | Workflows | `workflowidworkflow` | LAZY, @JsonIgnore |
| @ManyToOne | processingStage | ProcessingStage | `processingstageidprocessingstage` | LAZY, @JsonIgnore |

---

### Tablas de Unión (ManyToMany)

| Tabla de unión | Entidad propietaria | Entidad inversa | Columna propietaria | Columna inversa |
|---|---|---|---|---|
| `role_permissions` | Roles | Permissions | `idrole` | `idpermission` |
| `user_roles` | Users | Roles | `iduser` | `idrole` |

---

## 5. Repositorios (Repository)

Todos los repositorios extienden `JpaRepository<Entity, ID>` y se encuentran en el paquete `com.app.uteq.Repository`.

### 5.1 Repositorios basados en Stored Procedures

Estos repositorios utilizan `@Query(nativeQuery=true)` para invocar procedimientos almacenados y funciones de PostgreSQL.

| Repositorio | Entidad | SPs (CALL) | Funciones (SELECT) |
|---|---|---|---|
| `IAcademicCalendarRepository` | AcademicCalendar | `spi_academiccalendar`, `spu_academiccalendar`, `spd_academiccalendar` | `fn_list_academiccalendar(onlyActive)` |
| `ICareersRepository` | Careers | `spi_career`, `spu_career`, `spd_career` | `fn_list_careers(facultyid)` |
| `IConfigurationsRepository` | Configurations | `spi_configuration`, `spu_configuration`, `spd_configuration` | `fn_list_configurations()` |
| `IDeadLineRulesRepository` | DeadLinerules | `spi_deadlinerule`, `spu_deadlinerule`, `spd_deadlinerule` | `fn_list_deadlinerules(onlyActive)` |
| `IDocumentTemplatesRepository` | DocumentTemplates | `spi_documenttemplate`, `spu_documenttemplate`, `spd_documenttemplate` | `fn_list_documenttemplates(onlyActive)` |
| `IFacultiesRepository` | Faculties | `spi_faculty`, `spu_faculty`, `spd_faculty` | `fn_list_faculties()` |
| `IPermissionsRepository` | Permissions | `spi_permission`, `spu_permission`, `spd_permission` | `fn_list_permissions()` |
| `IProcessingStageRepository` | ProcessingStage | `spi_processingstage`, `spu_processingstage`, `spd_processingstage` | `fn_list_processingstage()` |
| `IRejectionReasonsRepository` | RejectionReasons | `spi_rejectionreason`, `spu_rejectionreason`, `spd_rejectionreason` | `fn_list_rejectionreasons(onlyActive)` |
| `IStatesRepository` | States | `spi_state`, `spu_state`, `spd_state` | `fn_list_states(category)` |
| `IWorkflowsRepository` | Workflows | `spi_workflow`, `spu_workflow`, `spd_workflow` | `fn_list_workflows(onlyActive)` |

### 5.2 Repositorios con queries JPA derivadas

| Repositorio | Entidad | Métodos personalizados |
|---|---|---|
| `IUsersRepository` | Users | `findByInstitutionalEmail`, `findByCardId`, `findByPersonalMail`, `findByActiveTrue`, `findByActiveFalse`, `findByRolesIdRole`, `existsByInstitutionalEmail`, `existsByCardId` |
| `IApplicationsRepository` | Applications | `findByApplicationCode`, `findByApplicantUserIdUser`, `findByPriority`, `findByProcedureIdProcedure`, `existsByApplicationCode` |
| `IProceduresRepository` | Procedures | `findByProcedureCode`, `findByActiveTrue`, `findByActiveFalse`, `findByWorkflowIdWorkflow`, `existsByProcedureCode`, `existsByNameProcedure` |
| `IStudentsRepository` | Students | `findByUserIdUser`, `findByCareerIdCareer`, `findBySemesterAndParallel`, `findByStatus`, `existsByUserIdUser` |
| `IRolesRepository` | Roles | `findByRoleName`, `existsByRoleName` |
| `IRefreshTokenRepository` | RefreshToken | `findByToken`, `findByUserAndRevokedFalse` |
| `ITwoFactorAuthRepository` | TwoFactorAuth | `findByCredentials_Id`, `existsByCredentials_IdAndEnabledTrue` |

### 5.3 Repositorios CRUD básicos

Solo exponen los métodos heredados de `JpaRepository` (sin queries adicionales):

`IApplicationStageHistoryRepository`, `IAttachedDocumentsRepository`, `ICredentialsRepository`, `IDigitalSignaturesRepository`, `IDocumentsGeneratedRepository`, `INotificationRepository`, `INotificationTypeRepository`, `IRequirementsOfTheProcedureRepository`, `ISessionTokenRepository`, `IStageTrackingRepository`, `IWorkflowStagesRepository`

---

## 6. Servicios — Interfaces (Services)

Todas las interfaces definen el contrato de operaciones de negocio. Se encuentran en `com.app.uteq.Services`.

### 6.1 `IAcademicCalendarService`

Gestión de calendarios académicos.

```java
List<AcademicCalendarResponse> listarCalendarios(Boolean onlyActive)
void createcalendar(String calendarname, String academicperiod, LocalDate startdate, LocalDate enddate, Boolean active)
void modifycalendar(UAcademicCalendarRequest request)
void deletecalendar(Integer idacademiccalendar)
```

### 6.2 `IApplicationsService`

Gestión completa de solicitudes/trámites estudiantiles.

```java
// Operaciones legadas
List<Applications> findAll()
Optional<Applications> findById(Integer id)
Applications save(Applications applications)
void deleteById(Integer id)

// Operaciones basadas en DTOs
List<ApplicationResponse> findAllApplications()
ApplicationResponse findApplicationById(Integer id)
List<ApplicationResponse> findByApplicantUserId(Integer userId)
List<ApplicationResponse> findByPriority(String priority)
ApplicationResponse createApplication(CApplicationRequest request)
ApplicationResponse updateApplication(Integer id, UApplicationRequest request)
void deleteApplication(Integer id)
ApplicationResponse resolveApplication(Integer id, String resolution)
ApplicationResponse rejectApplication(Integer id, Integer rejectionReasonId)
```

### 6.3 `IApplicationStageHistoryService`

CRUD básico del historial de etapas de solicitudes.

```java
List<ApplicationStageHistory> findAll()
Optional<ApplicationStageHistory> findById(Integer id)
ApplicationStageHistory save(ApplicationStageHistory entity)
void deleteById(Integer id)
```

### 6.4 `IAttachedDocumentsService`

CRUD básico de documentos adjuntos.

```java
List<AttachedDocuments> findAll()
Optional<AttachedDocuments> findById(Integer id)
AttachedDocuments save(AttachedDocuments entity)
void deleteById(Integer id)
```

### 6.5 `ICareersService`

Gestión de carreras vía Stored Procedures.

```java
void createCareers(CCareersRequest request)
void updateCareers(UCareersRequest request)
void deleteCareers(Integer idcareer)
List<CareersResponse> listCareers(Integer facultyid)
```

### 6.6 `IConfigurationService`

Gestión de configuraciones de usuario vía Stored Procedures.

```java
void createConfiguration(CConfigurationRequest request)
void updateConfiguration(UConfigurationRequest request)
void deleteConfiguration(Integer idconfiguration)
List<ConfigurationResponse> listConfiguration()
```

### 6.7 `ICredentialsService`

Gestión de credenciales con lógica de seguridad de contraseñas.

```java
// Operaciones legadas
List<Credentials> findAll()
Optional<Credentials> findById(Integer id)
Credentials save(Credentials credentials)
void deleteById(Integer id)

// Operaciones de negocio
List<CredentialResponse> findAllCredentials()
CredentialResponse findCredentialById(Integer id)
CredentialResponse createCredential(CCredentialRequest request)
CredentialResponse changePassword(Integer id, String currentPassword, String newPassword)
CredentialResponse lockAccount(Integer id)
CredentialResponse unlockAccount(Integer id)
boolean registerFailedAttempt(Integer id)
void registerSuccessfulLogin(Integer id)
String resetPassword(Integer id)
boolean isPasswordExpired(Integer id)
void verifyCredentialOwnership(Integer credentialId, String authenticatedEmail)
```

### 6.8 `IDeadLineRulesService`

Gestión de reglas de plazo vía Stored Procedures.

```java
void createDeadlinerule(CDeadlineRuleRequest request)
void updateDeadlinerule(UDeadlineRuleRequest request)
void deleteDeadlinerule(Integer iddeadlinerule)
List<DeadlineRuleResponse> listDeadlinerule(Boolean onlyActive)
```

### 6.9 `IDigitalSignaturesService`

CRUD básico de firmas digitales.

```java
List<DigitalSignatures> findAll()
Optional<DigitalSignatures> findById(Integer id)
DigitalSignatures save(DigitalSignatures entity)
void deleteById(Integer id)
```

### 6.10 `IDocumentsGeneratedService`

CRUD básico de documentos generados.

```java
List<DocumentsGenerated> findAll()
Optional<DocumentsGenerated> findById(Integer id)
DocumentsGenerated save(DocumentsGenerated entity)
void deleteById(Integer id)
```

### 6.11 `IDocumentTemplatesService`

Gestión de plantillas de documentos vía Stored Procedures.

```java
void createDocumenttemplate(CDocumentTemplateRequest request)
void updateDocumenttemplate(UDocumentTemplateRequest request)
void deleteDocumenttemplate(Integer idtemplate)
List<DocumentTemplateResponse> listDocumenttemplate(Boolean onlyActive)
```

### 6.12 `IFacultiesService`

Gestión de facultades vía Stored Procedures.

```java
void createFaculty(CFacultyRequest request)
void updateFaculty(UFacultyRequest request)
void deleteFaculty(Integer idfaculty)
List<FacultyResponse> listFaculty()
```

### 6.13 `INotificationService`

CRUD básico de notificaciones.

```java
List<Notification> findAll()
Optional<Notification> findById(Integer id)
Notification save(Notification notification)
void deleteById(Integer id)
```

### 6.14 `INotificationTypeService`

CRUD básico de tipos de notificación.

```java
List<NotificationType> findAll()
Optional<NotificationType> findById(Integer id)
NotificationType save(NotificationType entity)
void deleteById(Integer id)
```

### 6.15 `IPermissionsService`

Gestión de permisos vía Stored Procedures.

```java
void createPermission(CPermissionRequest request)
void updatePermission(UPermissionRequest request)
void deletePermission(Integer idpermission)
List<PermissionResponse> listPermission()
```

### 6.16 `IProceduresService`

Gestión completa de trámites con lógica de negocio avanzada.

```java
// Operaciones legadas
List<Procedures> findAll()
Optional<Procedures> findById(Integer id)
Procedures save(Procedures procedure)
void deleteById(Integer id)

// Operaciones de negocio
List<ProcedureResponse> findAllProcedures()
List<ProcedureResponse> findAllIncludingInactive()
ProcedureResponse findProcedureById(Integer id)
Optional<ProcedureResponse> findByProcedureCode(String procedureCode)
List<ProcedureResponse> findByWorkflow(Integer workflowId)
ProcedureResponse createProcedure(CProcedureRequest request)
ProcedureResponse updateProcedure(Integer id, UProcedureRequest request)
ProcedureResponse activateProcedure(Integer id)
ProcedureResponse deactivateProcedure(Integer id)
void deleteProcedure(Integer id)
boolean requires2FA(Integer id)
```

### 6.17 `IProcessingStageService`

Gestión de etapas de procesamiento vía Stored Procedures.

```java
void createProcessingstage(CProcessingStageRequest request)
void updateProcessingstage(UProcessingStageRequest request)
void deleteProcessingstage(Integer idprocessingstage)
List<ProcessingStageResponse> listProcessingstage()
```

### 6.18 `IRefreshTokenService`

CRUD básico de tokens de refresco.

```java
List<RefreshToken> findAll()
Optional<RefreshToken> findById(Long id)
RefreshToken save(RefreshToken refreshToken)
void deleteById(Long id)
```

### 6.19 `IRejectionReasonsService`

Gestión de razones de rechazo vía Stored Procedures.

```java
void createRejectreason(CRejectionReasonRequest request)
void updateRejectreason(URejectionReasonRequest request)
void deleteRejectreason(Integer idrejectionreason)
List<RejectionReasonResponse> listRejectreason(Boolean onlyActive)
```

### 6.20 `IRequirementsOfTheProcedureService`

CRUD básico de requisitos de trámites.

```java
List<RequirementsOfTheProcedure> findAll()
Optional<RequirementsOfTheProcedure> findById(Integer id)
RequirementsOfTheProcedure save(RequirementsOfTheProcedure entity)
void deleteById(Integer id)
```

### 6.21 `IRolesService`

Gestión completa de roles con asignación de permisos y usuarios.

```java
// Operaciones legadas
List<Roles> findAll()
Optional<Roles> findById(Integer id)
Roles save(Roles role)
void deleteById(Integer id)

// Operaciones de negocio
List<RoleResponse> findAllRoles()
RoleResponse findRoleById(Integer id)
Optional<RoleResponse> findByRoleName(String roleName)
RoleResponse createRole(CRoleRequest request)
RoleResponse updateRole(Integer id, URoleRequest request)
void deleteRole(Integer id)
RoleResponse assignPermissions(Integer roleId, Set<Integer> permissionIds)
RoleResponse removePermissions(Integer roleId, Set<Integer> permissionIds)
void assignRoleToUser(Integer roleId, Integer userId)
void removeRoleFromUser(Integer roleId, Integer userId)
```

### 6.22 `ISessionTokenService`

CRUD básico de tokens de sesión.

```java
List<SessionToken> findAll()
Optional<SessionToken> findById(Integer id)
SessionToken save(SessionToken sessionToken)
void deleteById(Integer id)
```

### 6.23 `IStageTrackingService`

CRUD básico de seguimiento de etapas.

```java
List<StageTracking> findAll()
Optional<StageTracking> findById(Integer id)
StageTracking save(StageTracking stageTracking)
void deleteById(Integer id)
```

### 6.24 `IStatesService`

Gestión de estados vía Stored Procedures.

```java
void createStates(CStateRequest request)
void updateStates(UStateRequest request)
void deleteStates(Integer idstate)
List<StateResponse> listStates(String category)
```

### 6.25 `IStudentsService`

Gestión completa del ciclo de vida estudiantil.

```java
// Operaciones legadas
List<Students> findAll()
Optional<Students> findById(Integer id)
Students save(Students students)
void deleteById(Integer id)

// Operaciones de negocio
List<StudentResponse> findAllStudents()
StudentResponse findStudentById(Integer id)
List<StudentResponse> findByCareer(Integer careerId)
List<StudentResponse> findBySemesterAndParallel(String semester, String parallel)
Optional<StudentResponse> findByUserId(Integer userId)
StudentResponse enrollStudent(CStudentRequest request)
StudentResponse updateStudent(Integer id, UStudentRequest request)
StudentResponse changeStatus(Integer id, String newStatus)
StudentResponse promoteToNextSemester(Integer id)
StudentResponse graduate(Integer id)
StudentResponse withdraw(Integer id)
StudentResponse reactivate(Integer id)
```

### 6.26 `ITwoFactorAuthService`

Operaciones de autenticación de dos factores (TOTP).

```java
// CRUD base
List<TwoFactorAuth> findAll()
Optional<TwoFactorAuth> findById(Integer id)
TwoFactorAuth save(TwoFactorAuth entity)
void deleteById(Integer id)

// Operaciones 2FA
TwoFactorSetupResponse setup2FA(String email)
boolean verifyAndEnable2FA(String email, int code)
void disable2FA(String email, int code)
boolean validateCode(String email, int code)
boolean validateBackupCode(String email, String backupCode)
boolean is2FAEnabled(String email)
TwoFactorAuthResponse getStatus(String email)
List<String> regenerateBackupCodes(String email, int code)
```

### 6.27 `IUsersService`

Gestión completa de usuarios del sistema.

```java
// Operaciones legadas
List<Users> findAll()
Optional<Users> findById(Integer id)
Users save(Users user)
void deleteById(Integer id)

// Operaciones de negocio
List<UserResponse> findAllUsers()
UserResponse findUserById(Integer id)
UserResponse createUser(CUserRequest request)
UserResponse updateUser(Integer id, UUserRequest request)
void deleteUser(Integer id)
UserResponse deactivateUser(Integer id)
UserResponse activateUser(Integer id)
Optional<UserResponse> findByInstitutionalEmail(String email)
```

### 6.28 `IWorkflowsService`

Gestión de flujos de trabajo vía Stored Procedures.

```java
void createWorkflow(CWorkflowRequest request)
void updateWorkflow(UWorkflowRequest request)
void deleteWorkflow(Integer idworkflow)
List<WorkflowResponse> listWorkflow(Boolean onlyActive)
```

### 6.29 `IWorkflowStagesService`

CRUD básico de etapas de flujo de trabajo.

```java
List<WorkflowStages> findAll()
Optional<WorkflowStages> findById(Integer id)
WorkflowStages save(WorkflowStages workflowStage)
void deleteById(Integer id)
```

---

## 7. Servicios — Implementaciones (Services/Impl)

### 7.1 Patrón A: Implementaciones basadas en Stored Procedures

Estas clases delegan todo el CRUD a procedimientos almacenados de PostgreSQL. Los resultados `Object[]` se convierten manualmente a DTOs de respuesta.

| Implementación | Lógica adicional |
|---|---|
| `AcademicCalendarServiceImpl` | Valida que `calendarname` y `academicperiod` no estén vacíos antes de llamar al SP |
| `CareersServiceImpl` | Delegación pura al SP, sin validación backend adicional |
| `ConfigurationServiceImpl` | Delegación pura al SP |
| `DeadLineRulesServiceImpl` | Delegación pura al SP |
| `DocumentTemplatesServiceImpl` | Delegación pura al SP |
| `FacultiesServiceImpl` | Delegación pura al SP |
| `PermissionsServiceImpl` | Delegación pura al SP |
| `ProcessingStageServiceImpl` | Delegación pura al SP |
| `RejectionReasonsServiceImpl` | Delegación pura al SP |
| `StatesServiceImpl` | Delegación pura al SP |
| `WorkflowsServiceImpl` | Delegación pura al SP |

### 7.2 Patrón B: Implementaciones JPA con lógica de negocio

#### `ApplicationsServiceImpl`

Gestión de solicitudes estudiantiles con validación y flujos de estado.

- Valida que el código de solicitud sea único
- Verifica que el usuario solicitante esté activo
- Valida valores de prioridad: `baja`, `normal`, `alta`, `urgente`
- Marca fecha de creación automáticamente
- **Resolver:** Registra la resolución con timestamp de finalización
- **Rechazar:** Asocia una razón de rechazo y registra timestamp
- Usa `@Transactional` en operaciones de escritura

#### `CredentialsServiceImpl`

Gestión de seguridad de contraseñas y cuentas.

- **Hashing:** BCrypt para todas las contraseñas
- **Validación de fortaleza:** Mínimo 8 caracteres, requiere mayúscula, minúscula, dígito y carácter especial
- **Bloqueo automático:** Después de 5 intentos fallidos, la cuenta se bloquea automáticamente
- **Expiración:** Contraseñas expiran a los 90 días
- **Reset:** Genera contraseña temporal de 12 caracteres (alfanumérica + especiales)
- **Verificación de propiedad:** `verifyCredentialOwnership()` verifica que el usuario autenticado sea el dueño de las credenciales (inyecta `IUsersRepository`)
- Nunca expone `passwordHash` en DTOs de respuesta

#### `ProceduresServiceImpl`

Gestión de tipos de trámites.

- **Código automático:** Genera códigos con formato `PREFIX-UUID8` (primeras 3 letras del nombre + 8 caracteres UUID)
- Valida que el workflow asociado exista
- Duración mínima: 1 día
- **Protección de eliminación:** No permite eliminar trámites que tengan solicitudes asociadas (lanza `BusinessException`)
- Activar/desactivar trámites (soft toggle)
- Consulta de requisito de 2FA por trámite

#### `RolesServiceImpl`

Gestión de roles con protección de roles del sistema.

- **Roles protegidos:** `ROLE_ADMIN`, `ROLE_STUDENT`, `ROLE_COORDINATOR`, `ROLE_DEAN` no pueden ser eliminados ni renombrados
- **Validación de formato:** Los nombres de rol deben seguir el patrón `ROLE_[A-Z_]+`
- Previene eliminar roles asignados a usuarios
- Garantiza que los usuarios mantengan al menos 1 rol
- Asignación y remoción de permisos a roles
- Asignación y remoción de roles a usuarios

#### `StudentsServiceImpl`

Ciclo de vida completo del estudiante.

- **Ciclo:** Matricular → Promover (semestre 1-10) → Graduar/Retirar/Reactivar
- Valida semestre (1-10), paralelo (letra A-Z)
- **Transiciones de estado válidas:**
  - `activo` → se puede retirar, suspender, graduar
  - `inactivo` → se puede reactivar
  - `retirado` → se puede reactivar
  - `graduado` → no se puede cambiar (estado final)
  - `suspendido` → se puede reactivar
- **Promoción:** Incrementa semestre (1→2, ..., 9→10), no puede promover más allá de 10

#### `UsersServiceImpl`

Gestión de usuarios del sistema.

- Valida unicidad de email institucional y cédula
- **Eliminación suave:** `deleteUser()` establece `active=false` en lugar de borrar
- Activar/desactivar usuarios
- Marca timestamp `updatedAt` en cada actualización

#### `TwoFactorAuthServiceImpl`

Implementación completa de autenticación TOTP con Google Authenticator.

- **Setup:** Genera secreto TOTP vía `GoogleAuthenticator`, construye URI para QR (`otpauth://totp/SGTE-UTEQ:email`), genera 8 códigos de respaldo alfanuméricos de 8 caracteres
- **Verificación:** Primera verificación del código TOTP habilita 2FA y registra `verifiedAt`
- **Validación:** Valida códigos TOTP durante el flujo de login
- **Backup codes:** Códigos de un solo uso que se consumen al validarse; se pueden regenerar (requiere código TOTP válido)
- **Deshabilitación:** Requiere código TOTP válido para desactivar 2FA

### 7.3 Patrón C: Wrappers CRUD simples

Delegación pura a JpaRepository sin validación ni transformación:

`ApplicationStageHistoryServiceImpl`, `AttachedDocumentsServiceImpl`, `DigitalSignaturesServiceImpl`, `DocumentsGeneratedServiceImpl`, `NotificationServiceImpl`, `NotificationTypeServiceImpl`, `RefreshTokenServiceImpl`, `RequirementsOfTheProcedureServiceImpl`, `SessionTokenServiceImpl`, `StageTrackingServiceImpl`, `WorkflowStagesServiceImpl`

### 7.4 Servicio Especial

#### `CustomUserDetailsService`

Implementa la interfaz `UserDetailsService` de Spring Security.

- Carga usuarios por `institutionalEmail`
- Mapea los `Roles` del usuario a `SimpleGrantedAuthority`
- Gestion de flags `accountLocked` y `active`
- Anotado con `@Transactional(readOnly = true)` para optimización de lectura

---

## 8. Controladores (Controllers)

Todos los controladores son clases `@RestController` que reciben solicitudes HTTP y delegan a los servicios. Usan DTOs para entrada (`@RequestBody @Valid`) y salida.

### 8.1 `AuthController`

**Ruta base:** `/api/v1/auth` · **Seguridad:** Público (sin autenticación)

Maneja la autenticación principal y la emisión de tokens JWT.

| Método | Ruta | Parámetros | Descripción |
|---|---|---|---|
| POST | `/token` | `grantType`, `username?`, `password?`, `withRefreshToken`, `refreshToken?` | Emite tokens JWT. `grantType=password`: autentica y retorna access_token (15min) y opcionalmente refresh_token (24h). Si 2FA está habilitado, retorna `pre_auth_token` (5min). `grantType=refresh_token`: renueva access_token |
| POST | `/2fa-verify` | `preAuthToken`, `code?`, `backupCode?` | Completa login 2FA. Valida código TOTP o código de respaldo contra el pre_auth_token, retorna tokens completos |

### 8.2 `TwoFactorAuthController`

**Ruta base:** `/api/v1/2fa` · **Seguridad:** Mixta

Gestión de autenticación de dos factores.

| Método | Ruta | Seguridad | Descripción |
|---|---|---|---|
| POST | `/setup` | Autenticado | Genera secreto TOTP, URI para QR, códigos de respaldo |
| POST | `/verify` | Autenticado | Primera verificación TOTP → activa 2FA |
| DELETE | `/disable` | Autenticado | Desactiva 2FA (requiere TOTP válido) |
| GET | `/status` | Autenticado | Retorna estado de 2FA |
| POST | `/backup-codes/regenerate` | Autenticado | Regenera códigos de respaldo (requiere TOTP) |
| POST | `/validate` | Público | Valida código TOTP durante flujo de login |
| POST | `/validate-backup` | Público | Valida código de respaldo durante login |

### 8.3 `AcademicCalendarController`

**Ruta base:** `/api/v1/academic-calendar` · **Seguridad:** `isAuthenticated()`

| Método | Ruta | Parámetros | Descripción |
|---|---|---|---|
| POST | `/` | `IAcademicCalendarRequest` body | Crear calendario |
| PUT | `/{idacademiccalendar}` | Path + `UAcademicCalendarRequest` body | Actualizar calendario |
| DELETE | `/{id}` | Path | Eliminar calendario |
| GET | `/` | `onlyActive?` query param | Listar calendarios |

### 8.4 `ApplicationsController`

**Ruta base:** `/api/v1/applications` · **Seguridad:** `isAuthenticated()`

| Método | Ruta | Parámetros | Descripción |
|---|---|---|---|
| GET | `/` | — | Listar todas las solicitudes |
| GET | `/{id}` | Path | Obtener solicitud por ID |
| GET | `/user/{userId}` | Path | Solicitudes por usuario |
| GET | `/priority/{priority}` | Path | Solicitudes por prioridad |
| POST | `/` | `CApplicationRequest` body | Crear solicitud (201) |
| PUT | `/{id}` | Path + `UApplicationRequest` body | Actualizar solicitud |
| DELETE | `/{id}` | Path | Eliminar solicitud (204) |
| PATCH | `/{id}/resolve` | Path + `resolution` query | Resolver solicitud |
| PATCH | `/{id}/reject` | Path + `rejectionReasonId` query | Rechazar solicitud |

### 8.5 `ApplicationStageHistoryController`

**Ruta base:** `/api/v1/application-stage-history` · **Seguridad:** `isAuthenticated()`

CRUD estándar: GET `/`, GET `/{id}`, POST `/`, PUT `/{id}`, DELETE `/{id}`

### 8.6 `AttachedDocumentsController`

**Ruta base:** `/api/v1/attached-documents` · **Seguridad:** `isAuthenticated()`

CRUD estándar: GET `/`, GET `/{id}`, POST `/`, PUT `/{id}`, DELETE `/{id}`

### 8.7 `CareerController`

**Ruta base:** `/api/v1/careers` · **Seguridad:** `isAuthenticated()`

| Método | Ruta | Parámetros | Descripción |
|---|---|---|---|
| POST | `/` | `CCareersRequest` body | Crear carrera (SP) |
| PUT | `/{idcareer}` | Path + `UCareersRequest` body | Actualizar carrera (SP) |
| DELETE | `/{idcareer}` | Path | Eliminar carrera (SP) |
| GET | `/` | `facultyid?` query param | Listar carreras (SP), filtro opcional por facultad |

### 8.8 `ConfigurationController`

**Ruta base:** `/api/v1/configuration` · **Seguridad:** `hasAuthority('SCOPE_ROLE_ADMIN')`

| Método | Ruta | Parámetros | Descripción |
|---|---|---|---|
| POST | `/` | `CConfigurationRequest` body | Crear configuración (SP) |
| PUT | `/{id}` | Path + `UConfigurationRequest` body | Actualizar configuración (SP) |
| DELETE | `/{idconfiguration}` | Path | Eliminar configuración (SP) |
| GET | `/` | — | Listar configuraciones (SP) |

### 8.9 `CredentialsController`

**Ruta base:** `/api/v1/credentials` · **Seguridad:** `isAuthenticated()` (clase); la mayoría de endpoints requiere `ROLE_ADMIN`

| Método | Ruta | Seguridad | Descripción |
|---|---|---|---|
| GET | `/` | ADMIN | Listar todas las credenciales |
| GET | `/{id}` | ADMIN | Obtener credencial por ID |
| POST | `/` | ADMIN | Crear credencial (201) |
| POST | `/{id}/change-password` | Autenticado (verificación de propiedad) | Cambiar contraseña |
| POST | `/{id}/reset-password` | ADMIN | Resetear a contraseña temporal |
| GET | `/{id}/password-expired` | Autenticado | Verificar expiración de contraseña |
| POST | `/{id}/lock` | ADMIN | Bloquear cuenta |
| POST | `/{id}/unlock` | ADMIN | Desbloquear cuenta |
| DELETE | `/{id}` | ADMIN | Eliminar credencial (204) |

### 8.10 `DeadlineruleControllers`

**Ruta base:** `/api/v1/deadlinerules` · **Seguridad:** `isAuthenticated()`

| Método | Ruta | Parámetros | Descripción |
|---|---|---|---|
| POST | `/` | `CDeadlineRuleRequest` body | Crear regla (SP) |
| PUT | `/{id}` | Path + `UDeadlineRuleRequest` body | Actualizar regla (SP) |
| DELETE | `/{id}` | Path | Eliminar regla (SP) |
| GET | `/` | `onlyActive?` query param | Listar reglas (SP) |

### 8.11 `DigitalSignaturesController`

**Ruta base:** `/api/v1/digital-signatures` · **Seguridad:** `isAuthenticated()`

CRUD estándar: GET `/`, GET `/{id}`, POST `/`, PUT `/{id}`, DELETE `/{id}`

### 8.12 `DocumentsGeneratedController`

**Ruta base:** `/api/v1/documents-generated` · **Seguridad:** `isAuthenticated()`

CRUD estándar: GET `/`, GET `/{id}`, POST `/`, PUT `/{id}`, DELETE `/{id}`

### 8.13 `DocumentTemplateController`

**Ruta base:** `/api/v1/document-templates` · **Seguridad:** `isAuthenticated()`

| Método | Ruta | Parámetros | Descripción |
|---|---|---|---|
| POST | `/` | `CDocumentTemplateRequest` body | Crear plantilla (SP) |
| PUT | `/{id}` | Path + `UDocumentTemplateRequest` body | Actualizar plantilla (SP) |
| DELETE | `/{idtemplate}` | Path | Eliminar plantilla (SP) |
| GET | `/` | `onlyActive?` query param | Listar plantillas (SP) |

### 8.14 `FacultyController`

**Ruta base:** `/api/v1/faculty` · **Seguridad:** `isAuthenticated()`

| Método | Ruta | Parámetros | Descripción |
|---|---|---|---|
| POST | `/` | `CFacultyRequest` body | Crear facultad (SP) |
| PUT | `/{id}` | Path + `UFacultyRequest` body | Actualizar facultad (SP) |
| DELETE | `/{idfaculty}` | Path | Eliminar facultad (SP) |
| GET | `/` | — | Listar facultades (SP) |

### 8.15 `NotificationController`

**Ruta base:** `/api/v1/notifications` · **Seguridad:** `isAuthenticated()`

CRUD estándar: GET `/`, GET `/{id}`, POST `/`, PUT `/{id}`, DELETE `/{id}`

### 8.16 `NotificationTypeController`

**Ruta base:** `/api/v1/notification-types` · **Seguridad:** `isAuthenticated()`

CRUD estándar: GET `/`, GET `/{id}`, POST `/`, PUT `/{id}`, DELETE `/{id}`

### 8.17 `PermissionController`

**Ruta base:** `/api/v1/permissions` · **Seguridad:** `hasAuthority('SCOPE_ROLE_ADMIN')`

| Método | Ruta | Parámetros | Descripción |
|---|---|---|---|
| POST | `/` | `CPermissionRequest` body | Crear permiso (SP) |
| PUT | `/{id}` | Path + `UPermissionRequest` body | Actualizar permiso (SP) |
| DELETE | `/{idpermission}` | Path | Eliminar permiso (SP) |
| GET | `/` | — | Listar permisos (SP) |

### 8.18 `ProceduresController`

**Ruta base:** `/api/v1/procedures` · **Seguridad:** `isAuthenticated()` (clase); escritura requiere `ROLE_ADMIN`

| Método | Ruta | Seguridad | Descripción |
|---|---|---|---|
| GET | `/legacy` | Autenticado | Legacy: listar entidades crudas |
| GET | `/legacy/{id}` | Autenticado | Legacy: obtener entidad cruda |
| GET | `/` | Autenticado | Listar trámites activos (DTOs) |
| GET | `/all` | ADMIN | Listar todos incluyendo inactivos |
| GET | `/{id}` | Autenticado | Obtener trámite por ID |
| GET | `/code/{code}` | Autenticado | Obtener por código |
| GET | `/workflow/{workflowId}` | Autenticado | Obtener por workflow |
| POST | `/` | ADMIN | Crear trámite (201) |
| PUT | `/{id}` | ADMIN | Actualizar trámite |
| POST | `/{id}/activate` | ADMIN | Activar trámite |
| POST | `/{id}/deactivate` | ADMIN | Desactivar trámite |
| GET | `/{id}/requires-2fa` | Autenticado | Verificar si requiere 2FA |
| DELETE | `/{id}` | ADMIN | Eliminar trámite (204) |

### 8.19 `ProcessingStageController`

**Ruta base:** `/api/v1/processing-stages` · **Seguridad:** `isAuthenticated()`

| Método | Ruta | Parámetros | Descripción |
|---|---|---|---|
| POST | `/` | `CProcessingStageRequest` body | Crear etapa (SP) |
| PUT | `/{id}` | Path + `UProcessingStageRequest` body | Actualizar etapa (SP) |
| DELETE | `/{idprocessingstage}` | Path | Eliminar etapa (SP) |
| GET | `/` | — | Listar etapas (SP) |

### 8.20 `RefreshTokenController`

**Ruta base:** `/api/v1/refresh-tokens` · **Seguridad:** `isAuthenticated()`

CRUD estándar: GET `/`, GET `/{id}`, POST `/`, PUT `/{id}`, DELETE `/{id}`

### 8.21 `RejectReasonController`

**Ruta base:** `/api/v1/reject-reason` · **Seguridad:** `isAuthenticated()`

| Método | Ruta | Parámetros | Descripción |
|---|---|---|---|
| POST | `/` | `CRejectionReasonRequest` body | Crear razón (SP) |
| PUT | `/{id}` | Path + `URejectionReasonRequest` body | Actualizar razón (SP) |
| DELETE | `/{idrejectionreason}` | Path | Eliminación lógica (SP, active=false) |
| GET | `/` | `onlyActive?` query param | Listar razones (SP) |

### 8.22 `RequirementsController`

**Ruta base:** `/api/v1/requirements` · **Seguridad:** `isAuthenticated()`

CRUD estándar: GET `/`, GET `/{id}`, POST `/`, PUT `/{id}`, DELETE `/{id}`

### 8.23 `RolesController`

**Ruta base:** `/api/v1/roles` · **Seguridad:** `hasAuthority('SCOPE_ROLE_ADMIN')`

| Método | Ruta | Parámetros | Descripción |
|---|---|---|---|
| GET | `/legacy` | — | Legacy: listar entidades crudas |
| GET | `/legacy/{id}` | Path | Legacy: obtener entidad cruda |
| GET | `/` | — | Listar todos los roles (DTOs) |
| GET | `/{id}` | Path | Obtener rol por ID |
| GET | `/name/{roleName}` | Path | Obtener rol por nombre |
| POST | `/` | `CRoleRequest` body | Crear rol (201) |
| PUT | `/{id}` | Path + `URoleRequest` body | Actualizar rol |
| DELETE | `/{id}` | Path | Eliminar rol (204) |
| POST | `/{roleId}/permissions` | Path + `Set<Integer>` body | Asignar permisos al rol |
| DELETE | `/{roleId}/permissions` | Path + `Set<Integer>` body | Remover permisos del rol |
| POST | `/{roleId}/users/{userId}` | Path | Asignar rol a usuario |
| DELETE | `/{roleId}/users/{userId}` | Path | Remover rol de usuario (204) |

### 8.24 `SessionTokenController`

**Ruta base:** `/api/v1/session-tokens` · **Seguridad:** `isAuthenticated()`

CRUD estándar: GET `/`, GET `/{id}`, POST `/`, PUT `/{id}`, DELETE `/{id}`

### 8.25 `StageTrackingController`

**Ruta base:** `/api/v1/stage-tracking` · **Seguridad:** `isAuthenticated()`

CRUD estándar: GET `/`, GET `/{id}`, POST `/`, PUT `/{id}`, DELETE `/{id}`

### 8.26 `StatesController`

**Ruta base:** `/api/v1/states` · **Seguridad:** `isAuthenticated()`

| Método | Ruta | Parámetros | Descripción |
|---|---|---|---|
| POST | `/` | `CStateRequest` body | Crear estado (SP) |
| PUT | `/{id}` | Path + `UStateRequest` body | Actualizar estado (SP) |
| DELETE | `/{idstate}` | Path | Eliminar estado (SP) |
| GET | `/` | `category?` query param | Listar estados (SP), filtro opcional por categoría |

### 8.27 `StudentsController`

**Ruta base:** `/api/v1/students` · **Seguridad:** `isAuthenticated()` (clase); escritura con restricciones de rol

| Método | Ruta | Seguridad | Descripción |
|---|---|---|---|
| GET | `/legacy` | Autenticado | Legacy: listar entidades crudas |
| GET | `/legacy/{id}` | Autenticado | Legacy: obtener entidad cruda |
| GET | `/` | Autenticado | Listar estudiantes activos (DTOs) |
| GET | `/{id}` | Autenticado | Obtener estudiante por ID |
| GET | `/career/{careerId}` | Autenticado | Estudiantes por carrera |
| GET | `/semester/{semester}/parallel/{parallel}` | Autenticado | Por semestre y paralelo |
| GET | `/user/{userId}` | Autenticado | Estudiante por usuario |
| POST | `/` | ADMIN, COORDINATOR | Matricular estudiante (201) |
| PUT | `/{id}` | ADMIN, COORDINATOR | Actualizar estudiante |
| PATCH | `/{id}/status` | ADMIN, COORDINATOR | Cambiar estado |
| POST | `/{id}/promote` | ADMIN, COORDINATOR | Promover al siguiente semestre |
| POST | `/{id}/graduate` | ADMIN, COORDINATOR, DEAN | Graduar estudiante |
| POST | `/{id}/withdraw` | ADMIN, COORDINATOR | Retirar estudiante |
| POST | `/{id}/reactivate` | ADMIN, COORDINATOR, DEAN | Reactivar estudiante |
| DELETE | `/{id}` | ADMIN | Eliminar estudiante (204) |

### 8.28 `UsersController`

**Ruta base:** `/api/v1/users` · **Seguridad:** `hasAuthority('SCOPE_ROLE_ADMIN')`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/` | Listar usuarios activos |
| GET | `/{id}` | Obtener usuario por ID |
| POST | `/` | Crear usuario (201) |
| PUT | `/{id}` | Actualizar usuario |
| DELETE | `/{id}` | Eliminación suave (204) |
| PATCH | `/{id}/deactivate` | Desactivar usuario |
| PATCH | `/{id}/activate` | Activar usuario |

### 8.29 `WorkFlowsController`

**Ruta base:** `/api/v1/work-flows` · **Seguridad:** `isAuthenticated()`

| Método | Ruta | Parámetros | Descripción |
|---|---|---|---|
| POST | `/` | `CWorkflowRequest` body | Crear flujo de trabajo (SP) |
| PUT | `/{id}` | Path + `UWorkflowRequest` body | Actualizar flujo (SP) |
| DELETE | `/{idworkflow}` | Path | Eliminación lógica (SP, active=false) |
| GET | `/` | `onlyActive?` query param | Listar flujos (SP) |

### 8.30 `WorkflowStagesController`

**Ruta base:** `/api/v1/workflow-stages` · **Seguridad:** `isAuthenticated()`

CRUD estándar: GET `/`, GET `/{id}`, POST `/`, PUT `/{id}`, DELETE `/{id}`

---

## 9. DTOs (Data Transfer Objects)

Total de **91 DTOs** organizados por dominio. Convención de nombres:

| Prefijo/Sufijo | Propósito |
|---|---|
| `C*Request` / `I*Request` | DTO de creación/inserción |
| `U*Request` | DTO de actualización |
| `D*Request` | DTO de eliminación |
| `*Response` | DTO de respuesta |

Todos son clases Java anotadas con Lombok `@Data`.

### 9.1 Calendario Académico

| DTO | Propósito | Campos clave con validación |
|---|---|---|
| `IAcademicCalendarRequest` | Crear calendario | `calendarname` (@NotBlank, @Size 3-100), `academicperiod` (@NotBlank), `startdate` (@NotNull), `enddate` (@NotNull), `active` |
| `UAcademicCalendarRequest` | Actualizar | `idacademiccalendar`, `calendarname`, `academicperiod`, `startdate`, `enddate`, `active` |
| `DAcademicCalendarRequest` | Eliminar | `idacademiccalendar` |
| `AcademicCalendarResponse` | Respuesta | Todos los campos del calendario |

### 9.2 Solicitudes

| DTO | Propósito | Campos clave con validación |
|---|---|---|
| `CApplicationRequest` | Crear solicitud | `applicationCode` (@NotBlank, @Size max 100), `estimatedCompletionDate` (@NotNull, @FutureOrPresent), `priority` (@Pattern: baja\|normal\|alta\|urgente), `proceduresIdProcedure` (@NotNull), `applicantUserId` (@NotNull) |
| `UApplicationRequest` | Actualizar | Todos los campos con las mismas validaciones |
| `ApplicationResponse` | Respuesta | IDs de relaciones + fechas + resolución |

### 9.3 Historial de Etapas

| DTO | Propósito | Campos |
|---|---|---|
| `CApplicationStageHistoryRequest` | Crear entrada de historial | `applicationIdApplication`, `stageTrackingId`, `processedByUserId`, `comments` |
| `UApplicationStageHistoryRequest` | Actualizar | Todos + `idHistory` |
| `ApplicationStageHistoryResponse` | Respuesta | Todos + `enteredAt`, `exitedAt` |

### 9.4 Documentos Adjuntos

| DTO | Propósito | Campos |
|---|---|---|
| `CAttachedDocumentRequest` | Subir documento | `applicationsIdApplication`, `requirementId`, `fileName`, `filePath`, `fileSizeBytes`, `mimeType`, `uploadedByUserId` |
| `UAttachedDocumentRequest` | Actualizar | Todos + `idAttachedDocument` |
| `AttachedDocumentResponse` | Respuesta | Todos + `uploadDate` |

### 9.5 Carreras

| DTO | Propósito | Campos clave con validación |
|---|---|---|
| `CCareersRequest` | Crear carrera | `careername` (@NotBlank, @Size 3-150), `careercode` (@NotBlank, @Size 2-20), `facultiesidfaculty` (@NotNull) |
| `UCareersRequest` | Actualizar | `idcareer`, `careername`, `careercode`, `facultiesidfaculty`, `coordinatoriduser` |
| `CareersResponse` | Respuesta | Incluye `facultyname` del JOIN |

### 9.6 Configuraciones

| DTO | Propósito | Campos clave con validación |
|---|---|---|
| `CConfigurationRequest` | Crear config | `notificationfrequency` (@Pattern: diaria\|semanal\|mensual\|inmediata) |
| `UConfigurationRequest` | Actualizar | `idconfiguration` + todos los campos |
| `ConfigurationResponse` | Respuesta | Todos los campos de configuración |

### 9.7 Credenciales

| DTO | Propósito | Campos clave con validación |
|---|---|---|
| `CCredentialRequest` | Crear credencial | `username` (@NotBlank, @Email), `passwordHash` (@NotBlank, @Size 8-100, @Pattern requiere complejidad) |
| `UCredentialRequest` | Actualizar | `idCredentials`, `username`, `passwordHash` |
| `CredentialResponse` | Respuesta | `idCredentials`, `username`, `lastLogin`, `failedAttempts`, `accountLocked`, `passwordExpiryDate` — **nunca expone passwordHash** |

### 9.8 Reglas de Plazo

| DTO | Propósito | Campos |
|---|---|---|
| `CDeadlineRuleRequest` | Crear regla | `rulename`, `procedurecategory`, `basedeadlinedays`, `warningdaysbefore`, `active` |
| `UDeadlineRuleRequest` | Actualizar | `iddeadlinerule` + todos |
| `DeadlineRuleResponse` | Respuesta | Todos los campos |

### 9.9 Firmas Digitales

| DTO | Propósito | Campos |
|---|---|---|
| `CDigitalSignatureRequest` | Crear firma | `userIdUser`, `certificatePath`, `certificateSerial`, `issuer`, `validFrom`, `validUntil`, `signatureAlgorithm`, `active` |
| `UDigitalSignatureRequest` | Actualizar | `idDigitalSignature` + todos |
| `DigitalSignatureResponse` | Respuesta | Todos + `createdAt` |

### 9.10 Documentos Generados

| DTO | Propósito | Campos |
|---|---|---|
| `CDocumentGeneratedRequest` | Crear documento | `applicationsIdApplication`, `templateId`, `documentType`, `documentPath`, `generatedByUserId`, `digitalSignatureId` |
| `UDocumentGeneratedRequest` | Actualizar | `idDocumentGenerated` + todos |
| `DocumentGeneratedResponse` | Respuesta | Todos + `generatedAt`, `signatureTimestamp` |

### 9.11 Plantillas de Documentos

| DTO | Propósito | Campos clave con validación |
|---|---|---|
| `CDocumentTemplateRequest` | Crear plantilla | `templatename` (@NotBlank, @Size 3-100), `templatecode` (@NotBlank, @Size 2-50), `documenttype` (@NotBlank) |
| `UDocumentTemplateRequest` | Actualizar | `idtemplate` + todos sin validación |
| `DocumentTemplateResponse` | Respuesta | Todos + `createdat`, `updatedat` |

### 9.12 Facultades

| DTO | Propósito | Campos clave con validación |
|---|---|---|
| `CFacultyRequest` | Crear facultad | `facultyname` (@NotBlank, @Size 3-150), `facultycode` (@NotBlank, @Size 2-20) |
| `UFacultyRequest` | Actualizar | `idfaculty` + todos |
| `FacultyResponse` | Respuesta | `idfaculty`, `facultyname`, `facultycode`, `deaniduser` |

### 9.13 Notificaciones

| DTO | Propósito | Campos |
|---|---|---|
| `CNotificationRequest` | Crear notificación | `notificationName`, `message`, `notificationTypeIdNotificationType`, `applicationId`, `recipientUserId`, `deliveryChannel` |
| `UNotificationRequest` | Actualizar | Todos + `deliveryStatus`, `retryCount` |
| `NotificationResponse` | Respuesta | Todos + `sentAt`, `readAt`, `errorMessage` |

### 9.14 Tipos de Notificación

| DTO | Propósito | Campos |
|---|---|---|
| `CNotificationTypeRequest` | Crear tipo | `typename`, `typedescription` |
| `UNotificationTypeRequest` | Actualizar | `idNotificationType` + todos |
| `NotificationTypeResponse` | Respuesta | Todos |

### 9.15 Permisos

| DTO | Propósito | Campos clave con validación |
|---|---|---|
| `CPermissionRequest` | Crear permiso | `code` (@NotBlank, @Pattern `^[A-Z_]+$`, @Size 3-50), `description` (@Size max 255) |
| `UPermissionRequest` | Actualizar | `idpermission` + todos |
| `PermissionResponse` | Respuesta | Todos |

### 9.16 Procedimientos/Trámites

| DTO | Propósito | Campos clave con validación |
|---|---|---|
| `CProcedureRequest` | Crear trámite | `procedurename` (@NotBlank, @Size 3-100), `description` (@Size max 500), `maxduration` (@NotNull, @Min 1, @Max 365), `workflowidworkflow` (@NotNull) |
| `UProcedureRequest` | Actualizar | `idProcedure` + todos |
| `ProcedureResponse` | Respuesta | Todos + `createdAt` |

### 9.17 Etapas de Procesamiento

| DTO | Propósito | Campos clave con validación |
|---|---|---|
| `CProcessingStageRequest` | Crear etapa | `stagename` (@NotBlank, @Size 3-100), `stagecode` (@NotBlank, @Size 2-50), `stageorder` (@NotNull, @Min 1) |
| `UProcessingStageRequest` | Actualizar | `idprocessingstage` + todos |
| `ProcessingStageResponse` | Respuesta | Todos |

### 9.18 Refresh Tokens

| DTO | Propósito | Campos |
|---|---|---|
| `CRefreshTokenRequest` | Crear token | `userId`, `token`, `expiresAt` |
| `URefreshTokenRequest` | Actualizar | `id` + todos + `revoked` |
| `RefreshTokenResponse` | Respuesta | Todos + `createdAt` |

### 9.19 Razones de Rechazo

| DTO | Propósito | Campos clave con validación |
|---|---|---|
| `CRejectionReasonRequest` | Crear razón | `reasoncode` (@NotBlank, @Size 2-50), `reasondescription` (@NotBlank, @Size 5-500), `category` (@NotBlank) |
| `URejectionReasonRequest` | Actualizar | `idrejectionreason` + todos |
| `RejectionReasonResponse` | Respuesta | Todos |

### 9.20 Requisitos del Trámite

| DTO | Propósito | Campos |
|---|---|---|
| `CRequirementRequest` | Crear requisito | `proceduresIdProcedure`, `requirementName`, `requirementDescription`, `requirementType`, `isMandatory`, `displayOrder` |
| `URequirementRequest` | Actualizar | `id` + todos |
| `RequirementResponse` | Respuesta | Todos |

### 9.21 Roles

| DTO | Propósito | Campos clave con validación |
|---|---|---|
| `CRoleRequest` | Crear rol | `roleName` (@NotBlank, @Size 3-50, @Pattern `^ROLE_[A-Z_]+$`), `roleDescription` (@Size max 255) |
| `URoleRequest` | Actualizar | `idRole` + todos |
| `RoleResponse` | Respuesta | Incluye lista anidada de `PermissionInfo` (`idPermission`, `code`, `description`) |

### 9.22 Session Tokens

| DTO | Propósito | Campos |
|---|---|---|
| `CSessionTokenRequest` | Crear sesión | `userId`, `token`, `ipAddress`, `userAgent` |
| `USessionTokenRequest` | Actualizar | `idSession` + todos |
| `SessionTokenResponse` | Respuesta | Todos + `createdAt`, `expiresAt`, `lastActivity` |

### 9.23 Seguimiento de Etapas

| DTO | Propósito | Campos |
|---|---|---|
| `CStageTrackingRequest` | Crear seguimiento | `stateIdState`, `processingStageIdProcessingStage`, `assignedToUserId`, `notes` |
| `UStageTrackingRequest` | Actualizar | `idStageTracking` + todos |
| `StageTrackingResponse` | Respuesta | Todos + `enteredAt`, `completedAt` |

### 9.24 Estados

| DTO | Propósito | Campos clave con validación |
|---|---|---|
| `CStateRequest` | Crear estado | `statename` (@NotBlank, @Size 2-50), `statecategory` (@NotBlank) |
| `UStateRequest` | Actualizar | `idstate` (@NotNull), `statename` (@Size 2-50) |
| `StateResponse` | Respuesta | Todos |

### 9.25 Estudiantes

| DTO | Propósito | Campos clave con validación |
|---|---|---|
| `CStudentRequest` | Matricular estudiante | `semester` (@NotBlank, @Pattern 1-10), `parallel` (@NotBlank, @Pattern A-Z), `usersIdUser` (@NotNull), `careersIdCareer` (@NotNull), `status` (@Pattern activo\|inactivo\|graduado\|retirado) |
| `UStudentRequest` | Actualizar | `idStudent` (@NotNull) + validaciones actualizadas + `suspendido` como estado adicional |
| `StudentResponse` | Respuesta | Incluye `userName`, `userEmail`, `careerName` desnormalizados |

### 9.26 Usuarios

| DTO | Propósito | Campos clave con validación |
|---|---|---|
| `CUserRequest` | Crear usuario | `names` (@NotBlank, @Size 2-255), `surnames` (@NotBlank, @Size 2-255), `cardId` (@NotBlank, @Pattern `^[0-9]{10}$`), `institutionalEmail` (@NotBlank, @Email, @Pattern `.*@uteq.edu.ec$`), `phoneNumber` (@Pattern `^[0-9]{10}$`), `configurationsIdConfiguration` (@NotNull) |
| `UUserRequest` | Actualizar | `idUser` (@NotNull) + todas las validaciones |
| `UserResponse` | Respuesta | Todos + `createdAt`, `updatedAt`, `active` |

### 9.27 Flujos de Trabajo

| DTO | Propósito | Campos clave con validación |
|---|---|---|
| `CWorkflowRequest` | Crear flujo | `workflowname` (@NotBlank, @Size 3-100), `workflowdescription` (@Size max 500) |
| `UWorkflowRequest` | Actualizar | `idworkflow` (@NotNull) + todos |
| `WorkflowResponse` | Respuesta | Todos + `createdat` |

### 9.28 Etapas de Flujo

| DTO | Propósito | Campos |
|---|---|---|
| `CWorkflowStageRequest` | Crear link workflow-etapa | `workflowIdWorkflow`, `processingStageIdProcessingStage`, `sequenceOrder`, `isOptional` |
| `UWorkflowStageRequest` | Actualizar | `idWorkflowStage` + todos |
| `WorkflowStageResponse` | Respuesta | Todos |

### 9.29 Autenticación 2FA

| DTO | Propósito | Campos |
|---|---|---|
| `TwoFactorVerifyRequest` | Verificar código TOTP | `code` (@NotNull, Integer) |
| `TwoFactorBackupRequest` | Usar código de respaldo | `backupCode` (@NotBlank, String) |
| `TwoFactorSetupResponse` | Respuesta de setup 2FA | `secretKey`, `qrCodeUri`, `backupCodes` (List) |
| `TwoFactorAuthResponse` | Estado de 2FA | `enabled`, `verifiedAt` |

### 9.30 Respuesta de Error

| DTO | Propósito | Campos |
|---|---|---|
| `ApiErrorResponse` | Respuesta de error estandarizada | `timestamp`, `status`, `error`, `message`, `path`, `errorCode`, `fieldErrors` (List\<FieldError\>), `details` (Map). Clase interna `FieldError`: `field`, `message`, `rejectedValue`. Usa `@JsonInclude(NON_NULL)`. Métodos fábrica estáticos `of()` y `withFieldErrors()` |

---

## 10. Excepciones (Exceptions)

### `GlobalExceptionHandler`

`@RestControllerAdvice` que intercepta todas las excepciones lanzadas por controladores y retorna respuestas consistentes en formato `ApiErrorResponse`.

| Excepción manejada | Código HTTP | Descripción |
|---|---|---|
| `MethodArgumentNotValidException` | 400 | Errores de validación de bean con detalle por campo |
| `MissingServletRequestParameterException` | 400 | Parámetro de request faltante |
| `MethodArgumentTypeMismatchException` | 400 | Tipo de argumento incorrecto |
| `ResourceNotFoundException` | 404 | Recurso no encontrado |
| `DuplicateResourceException` | 409 | Recurso duplicado |
| `BadRequestException` | 400 | Request inválido (genérico) |
| `BusinessException` | 422 | Violación de regla de negocio (incluye `errorCode`) |
| `UnauthorizedException` | 401 | Acceso no autorizado |
| `AccessDeniedException` | 403 | Acceso denegado (sin permisos) |
| `AuthenticationException` / `BadCredentialsException` | 401 | Credenciales inválidas |
| `Exception` (genérica) | 500 | Error interno (fallback, se registra en logs) |

### Excepciones personalizadas

| Excepción | HTTP | Campos adicionales | Uso |
|---|---|---|---|
| `BadRequestException` | 400 | `message` | Datos de solicitud inválidos |
| `BusinessException` | 422 | `message`, `errorCode` | Violación de reglas de negocio con código específico |
| `DuplicateResourceException` | 409 | `resourceName`, `fieldName`, `fieldValue` | Intento de crear recurso duplicado |
| `ResourceNotFoundException` | 404 | `resourceName`, `fieldName`, `fieldValue` | Recurso no encontrado por ID o campo |
| `UnauthorizedException` | 401 | `message` | Acceso sin autenticación válida |

---

## 11. Diagrama de Relaciones entre Entidades

```
                    ┌──────────────┐
                    │  Faculties   │
                    │  (facultad)  │◄──── dean: Users
                    └──────┬───────┘
                           │ 1
                           │
                           ▼ *
                    ┌──────────────┐
                    │   Careers    │
                    │  (carrera)   │◄──── coordinator: Users
                    └──────┬───────┘
                           │ 1
                           │
                           ▼ *
                    ┌──────────────┐        ┌──────────────┐
                    │   Students   │───────►│    Users     │
                    │ (estudiante) │        │  (usuario)   │
                    └──────────────┘        └──────┬───────┘
                                                   │
                        ┌──────────────────────────┼──────────────────────┐
                        │                          │                      │
                        ▼                          ▼                      ▼
                ┌───────────────┐         ┌───────────────┐     ┌─────────────────┐
                │Configurations │         │  Credentials  │     │     Roles       │
                │ (config user) │         │ (contraseñas) │     │   (roles)       │
                └───────────────┘         └───────┬───────┘     └────────┬────────┘
                                                  │ 1                    │
                                                  │                      │ * ◄──► *
                                                  ▼ 1                    ▼
                                         ┌────────────────┐     ┌─────────────────┐
                                         │ TwoFactorAuth  │     │  Permissions    │
                                         │    (2FA)       │     │  (permisos)     │
                                         └────────────────┘     └─────────────────┘

        ┌──────────────┐      ┌──────────────┐       ┌──────────────────┐
        │  Workflows   │◄─────│  Procedures  │◄──────│  Applications    │
        │(flujos trab) │  1  *│  (trámites)  │  1   *│  (solicitudes)   │
        └──────┬───────┘      └──────┬───────┘       └──────┬───────────┘
               │ 1                   │ 1                    │
               │                     │                      ├────► applicantUser: Users
               ▼ *                   ▼ *                    ├────► currentStageTracking: StageTracking
        ┌──────────────┐     ┌────────────────────┐         ├────► rejectionReason: RejectionReasons
        │WorkflowStages│     │RequirementsOfThe   │         │
        │(etapa-flujo) │     │Procedure (requis.) │         │
        └──────┬───────┘     └────────────────────┘         ▼ *
               │                                    ┌────────────────────┐
               ▼                                    │ApplicationStage    │
        ┌──────────────┐                            │History (historial) │
        │ProcessingStage│                           └────────────────────┘
        │  (etapas)    │
        └──────┬───────┘
               │ 1
               ▼ *
        ┌──────────────┐       ┌──────────────┐
        │StageTracking │──────►│    States    │
        │(seguimiento) │       │  (estados)   │
        └──────────────┘       └──────────────┘

        ┌──────────────┐       ┌──────────────────┐
        │  Notification │──────►│NotificationType  │
        │(notificación)│       │(tipo notificac.) │
        └──────────────┘       └──────────────────┘

        ┌──────────────────┐       ┌──────────────────┐
        │DocumentsGenerated│──────►│DocumentTemplates │
        │(docs generados)  │       │  (plantillas)    │
        └──────────┬───────┘       └──────────────────┘
                   │
                   ▼
        ┌──────────────────┐       ┌──────────────────┐
        │DigitalSignatures │       │AttachedDocuments │
        │(firmas digitales)│       │(docs adjuntos)   │
        └──────────────────┘       └──────────────────┘

        ┌──────────────┐       ┌──────────────┐       ┌──────────────┐
        │ RefreshToken  │       │ SessionToken │       │AcademicCalendar│
        │(token refresh)│       │(token sesión)│       │(cal. académico)│
        └──────────────┘       └──────────────┘       └──────────────┘

        ┌──────────────┐       ┌──────────────┐
        │DeadLinerules │       │RejectionReasons│
        │(reglas plazo)│       │(razones rechazo)│
        └──────────────┘       └──────────────┘
```

### Tablas de Unión

| Tabla | Entidad A | Entidad B | Columna A | Columna B |
|---|---|---|---|---|
| `user_roles` | Users | Roles | `iduser` | `idrole` |
| `role_permissions` | Roles | Permissions | `idrole` | `idpermission` |

---

> **Generado automáticamente** para el proyecto SGTE Backend  
> Total de clases documentadas: **29 entidades** · **29 repositorios** · **29 interfaces de servicio** · **30 implementaciones de servicio** · **30 controladores** · **91 DTOs** · **4 clases de configuración** · **6 clases de excepción** · **2 archivos raíz** = **250 clases/interfaces**
