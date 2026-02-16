# SGTE Backend — Manual Técnico Completo

> **Sistema de Gestión de Trámites Estudiantiles (SGTE)**  
> Backend desarrollado con Spring Boot 3.2.2 · Java 21 · PostgreSQL  
> Última actualización: Febrero 2026

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

### 1.1 Stack Tecnológico

| Componente | Tecnología | Versión |
|---|---|---|
| Framework | Spring Boot | 3.2.2 |
| Lenguaje | Java | 21 |
| Base de datos | PostgreSQL | - |
| Seguridad | Spring Security + OAuth2 Resource Server (JWT RS256) | - |
| Autenticación 2FA | Google Authenticator (TOTP) vía `googleauth` | 1.5.0 |
| Validación | Jakarta Bean Validation (`spring-boot-starter-validation`) | - |
| Build | Maven | - |
| Reducción boilerplate | Lombok | - |
| Criptografía | BouncyCastle (`bcpkix-jdk18on`) | 1.83 |
| Driver BD | PostgreSQL JDBC | runtime |

### 1.2 Arquitectura en Capas

```
┌─────────────────────────────────────────────────────────────────┐
│                      CAPA DE PRESENTACIÓN                       │
│                       Controladores (30)                        │
│  Reciben HTTP, validan DTOs con @Valid, delegan a servicios     │
│  Seguridad: @PreAuthorize("isAuthenticated()") a nivel clase    │
│  + @PreAuthorize("hasAuthority('XXX')") a nivel método          │
├─────────────────────────────────────────────────────────────────┤
│                     CAPA DE NEGOCIO                             │
│                Servicios — Interfaces (29)                      │
│            Contratos de las operaciones de negocio              │
├─────────────────────────────────────────────────────────────────┤
│                  CAPA DE IMPLEMENTACIÓN                         │
│            Servicios — Implementaciones (29+1)                  │
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

### 1.3 Roles del Sistema

| Rol | Descripción |
|---|---|
| `ROLE_ADMIN` | Administrador del sistema, acceso total |
| `ROLE_STUDENT` | Estudiante, puede crear y dar seguimiento a solicitudes |
| `ROLE_COORDINATOR` | Coordinador de carrera, gestiona estudiantes y trámites |
| `ROLE_DEAN` | Decano de facultad, aprueba operaciones académicas |

### 1.4 Sistema de Permisos

El backend implementa un sistema de seguridad basado en **permisos granulares**:

1. Cada `Rol` tiene asociado un conjunto de `Permissions` (relación M:N tabla `role_permissions`)
2. `CustomUserDetailsService` carga roles + permisos individuales como `GrantedAuthority`
3. Los controladores usan `@PreAuthorize("hasAuthority('CODIGO_PERMISO')")` en cada método
4. Aprox. **140 códigos de permisos únicos** en el sistema

### 1.5 Patrones de Acceso a Datos

El backend implementa **dos patrones** de acceso a datos que coexisten:

| Patrón | Entidades | Cantidad | Descripción |
|---|---|---|---|
| **A — Stored Procedures (SP)** | Catálogos | 11 repos | Operaciones CRUD delegadas a `CALL spi_*`, `CALL spu_*`, `CALL spd_*` y `SELECT * FROM fn_list_*`. Resultados `Object[]` mapeados con `SpResultConverter` a DTOs de respuesta. |
| **B — JPA directo** | Negocio | 18 repos | `JpaRepository` con queries derivadas, `.save()`, `.findById()`, `.deleteById()`. Validación rica y lógica de dominio compleja. |

**Servicios con Patrón SP (11):** AcademicCalendar, Careers, Configuration, DeadLineRules, DocumentTemplates, Faculties, Permissions, ProcessingStage, RejectionReasons, States, Workflows

**Servicios con Patrón JPA (18):** Applications, ApplicationStageHistory, AttachedDocuments, Credentials, DigitalSignatures, DocumentsGenerated, Notification, NotificationType, Procedures, RefreshToken, RequirementsOfTheProcedure, Roles, SessionToken, StageTracking, Students, TwoFactorAuth, Users, WorkflowStages

---

## 2. Archivos Raíz

### 2.1 `BackendApplication.java`

Punto de entrada principal de la aplicación Spring Boot.

```java
@SpringBootApplication
@EnableConfigurationProperties(RsaKeyConfig.class)
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

- Habilita la lectura de claves RSA mediante `@EnableConfigurationProperties(RsaKeyConfig.class)`
- Define el bean `PasswordEncoder` usando `BCryptPasswordEncoder` para el hashing seguro de contraseñas

### 2.2 `GenerateKeyPair.java`

Utilidad standalone (no es un bean de Spring) para generar un par de claves RSA de 2048 bits.

- Escribe `public.pem` y `private.pem` en `src/main/resources/certs/`
- Usa BouncyCastle `PemWriter` para formato PEM
- Se ejecuta una sola vez durante la configuración inicial del proyecto
- No se incluye en el flujo normal de la aplicación

---

## 3. Configuración (Config)

### 3.1 `RsaKeyConfig.java`

Record de Java que vincula las propiedades `rsa.public-key` y `rsa.private-key` del `application.properties` a objetos RSA tipados.

```java
@ConfigurationProperties(prefix = "rsa")
public record RsaKeyConfig(RSAPublicKey publicKey, RSAPrivateKey privateKey) {}
```

Spring Boot automáticamente lee los archivos PEM, parsea el contenido y lo inyecta como objetos `RSAPublicKey` / `RSAPrivateKey`.

### 3.2 `SecurityConfig.java`

Configuración central de seguridad. **161 líneas**, 7 beans.

**Anotaciones de clase:**
- `@Configuration`
- `@EnableWebSecurity`
- `@EnableMethodSecurity` — habilita `@PreAuthorize` a nivel método

**Beans definidos:**

| Bean | Descripción |
|---|---|
| `AuthenticationManager` | `DaoAuthenticationProvider` con `CustomUserDetailsService` + BCrypt |
| `SecurityFilterChain` | Cadena de filtros HTTP (CORS, CSRF off, JWT, STATELESS) |
| `CorsConfigurationSource` | Orígenes configurables vía `cors.allowed-origins` |
| `JwtAuthenticationConverter` | Converter personalizado de claims JWT a authorities |
| `jwtGrantedAuthoritiesConverter` | Convierte claim `scope` a authorities duales |
| `JwtDecoder` | `NimbusJwtDecoder` con clave pública RSA |
| `JwtEncoder` | `NimbusJwtEncoder` con par de claves RSA |

**Reglas de Autorización (SecurityFilterChain):**

| Patrón URL | Acceso |
|---|---|
| `/api/v1/auth/**` | `permitAll()` |
| `/api/v1/2fa/validate` | `permitAll()` |
| `/api/v1/2fa/validate-backup` | `permitAll()` |
| Cualquier otro | `authenticated()` |

**JWT Converter personalizado:** Convierte el claim `scope` del JWT en autoridades duales:
- `SCOPE_ROLE_ADMIN` → para `hasAuthority('SCOPE_ROLE_ADMIN')`
- `ROLE_ADMIN` → para `hasRole('ADMIN')`
- Permisos individuales como `CAL_CREAR` → para `hasAuthority('CAL_CREAR')`

**Configuración de sesiones:** `SessionCreationPolicy.STATELESS` — sin sesiones HTTP, cada request se autentica vía JWT.

### 3.3 `SpResultConverter.java`

Clase de utilidad estática para convertir resultados `Object[]` de Stored Procedures a valores Java tipados.

| Método | Entrada → Salida |
|---|---|
| `toInt(Object)` | `Number` → `Integer` |
| `toStr(Object)` | `Object` → `String` |
| `toBool(Object)` | `Number/Boolean` → `Boolean` |
| `toLocalDateTime(Object)` | `Timestamp/Date` → `LocalDateTime` |
| `toLocalDate(Object)` | `sql.Date/util.Date` → `LocalDate` |

Maneja `null` de forma segura. Utilizada por las implementaciones de servicios SP para evitar duplicación de código.

### 3.4 `StringListConverter.java`

JPA `AttributeConverter<List<String>, String>` que almacena una `List<String>` como texto delimitado por comas en la base de datos.

```java
@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {
    // convertToDatabaseColumn: List<"a","b","c"> → "a,b,c"  
    // convertToEntityAttribute: "a,b,c" → List<"a","b","c">
}
```

Utilizado por la entidad `TwoFactorAuth` para persistir los códigos de respaldo de 2FA.

---

## 4. Seguridad y Autenticación

### 4.1 `CustomUserDetailsService.java`

**Ubicación:** `Services/Impl/` · **Anotaciones:** `@Service`, `@Transactional(readOnly = true)`

Implementa `UserDetailsService` de Spring Security. Es el puente entre la base de datos y el sistema de autenticación.

**Flujo de `loadUserByUsername(email)`:**

1. Busca usuario por `institutionalEmail` en `IUsersRepository`
2. Valida que existan credenciales asociadas
3. Construye las `GrantedAuthority`:
   - Para cada rol del usuario: `ROLE_XXX` (ej: `ROLE_ADMIN`)
   - Para cada permiso de cada rol: código del permiso (ej: `CAL_CREAR`, `SOL_LISTAR`)
   - Permisos deduplicados vía `Set`
4. Retorna `org.springframework.security.core.userdetails.User` con:
   - `username` = email institucional
   - `password` = hash BCrypt
   - `authorities` = roles + permisos
   - `accountNonLocked` = `!credentials.accountLocked`
   - `enabled` = `user.active`

### 4.2 `AuthController.java`

**Base path:** `/api/v1/auth` · **226 líneas** · Sin `@PreAuthorize` (endpoints públicos)

| Endpoint | Método | Descripción |
|---|---|---|
| `POST /token` | `generateToken()` | Genera JWT (access + refresh token) |
| `POST /2fa-verify` | `verify2FA()` | Verifica código 2FA y genera tokens completos |
| `POST /logout` | `logout()` | Revoca todos los refresh tokens del usuario |

**Flujo de autenticación con `POST /token`:**

```
1. Cliente envía grantType=password, username, password, withRefreshToken
2. AuthenticationManager autentica (DaoAuthenticationProvider + BCrypt)
3. Si falla → registerFailedAttempt (puede bloquear cuenta tras 5 intentos)
4. Si éxito → registerSuccessfulLogin (resetea contador de intentos)
5. Verifica si usuario tiene 2FA habilitado:
   a. SI → genera pre_auth_token (JWT de 5 min con claim requires_2fa=true)
           retorna {pre_auth_token, requires_2fa: true}
   b. NO → genera tokens completos (access 15min + refresh 24h)
6. Para grantType=refresh_token → decodifica, valida, rota refresh token
```

**Flujo de `POST /2fa-verify`:**

```
1. Recibe preAuthToken + code (TOTP) o backupCode
2. Decodifica preAuthToken, valida que sea tipo "pre_auth"
3. Valida código TOTP via twoFactorAuthService.validateCode() 
   o código de respaldo via twoFactorAuthService.validateBackupCode()
4. Si válido → genera tokens completos (access + refresh)
5. Si inválido → 401 "Código 2FA inválido"
```

**Estructura de JWT generados:**

| Campo | Access Token | Refresh Token | Pre-Auth Token |
|---|---|---|---|
| `subject` | email | email | email |
| `issuer` | sgte-backend | sgte-backend | sgte-backend |
| `expiresAt` | +15 minutos | +24 horas | +5 minutos |
| `scope` | roles + permisos | roles + permisos | — |
| `token_type` | access_token | refresh_token | pre_auth |
| `requires_2fa` | — | — | true |

### 4.3 `TwoFactorAuthController.java`

**Base path:** `/api/v1/2fa` · **121 líneas**

| Endpoint | Método | Permiso | Descripción |
|---|---|---|---|
| `POST /setup` | `setup()` | `AUTH2FA_CONFIGURAR` | Genera clave secreta + QR URI + 8 backup codes |
| `POST /verify` | `verify()` | `AUTH2FA_VERIFICAR` | Primera verificación TOTP → activa 2FA |
| `DELETE /disable` | `disable()` | `AUTH2FA_DESACTIVAR` | Desactiva 2FA (requiere código TOTP) |
| `GET /status` | `status()` | `AUTH2FA_ESTADO` | Consulta estado de 2FA |
| `POST /backup-codes/regenerate` | `regenerateBackupCodes()` | `AUTH2FA_REGENERAR` | Regenera backup codes |
| `POST /validate` | `validateCode()` | Público | Valida TOTP durante login |
| `POST /validate-backup` | `validateBackupCode()` | Público | Valida backup code durante login |

### 4.4 Flujo Completo de Login

```
┌──────────┐     POST /auth/token      ┌──────────────┐
│  Cliente  │ ──────────────────────── → │ AuthController │
└──────────┘  grantType=password        └──────┬───────┘
                                               │
                                    ┌──────────▼──────────┐
                                    │ AuthenticationManager│
                                    │ (BCrypt + UserDetails)│
                                    └──────────┬──────────┘
                                               │
                                    ┌──────────▼──────────┐
                                    │ ¿2FA habilitado?     │
                                    └──┬──────────────┬───┘
                                  SI   │              │  NO
                          ┌────────────▼─┐    ┌───────▼────────┐
                          │ pre_auth_token│    │ access_token   │
                          │ (5 min)       │    │ refresh_token  │
                          └────────┬─────┘    └────────────────┘
                                   │
                    POST /auth/2fa-verify
                          │
                   ┌──────▼──────┐
                   │ Verificar   │
                   │ TOTP/Backup │
                   └──────┬──────┘
                          │ válido
                   ┌──────▼────────┐
                   │ access_token  │
                   │ refresh_token │
                   └───────────────┘
```

---

## 5. Entidades (Entity)

Todas las entidades son clases JPA anotadas con `@Entity` y `@Table`. Utilizan Lombok para reducir boilerplate. Total: **29 entidades**.

### 5.1 `AcademicCalendar`

**Tabla:** `academiccalendar` · **Lombok:** `@Data`, `@Builder` · **Patrón:** SP

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

### 5.2 `Applications`

**Tabla:** `applications` · **Lombok:** `@Data`, `@Builder` · **Patrón:** JPA

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| id | Integer | `idapplication` | PK, auto-increment |
| applicationCode | String | `applicationcode` | NOT NULL, UNIQUE, length=100 |
| creationDate | LocalDateTime | `creationdate` | NOT NULL |
| estimatedCompletionDate | LocalDate | `estimatedcompletiondate` | NOT NULL |
| actualCompletionDate | LocalDateTime | `actualcompletiondate` | Nullable |
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

### 5.3 `ApplicationStageHistory`

**Tabla:** `applicationstagehistory` · **Lombok:** `@Data`, `@Builder` · **Patrón:** JPA

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| id | Integer | `idhistory` | PK, auto-increment |
| enteredAt | LocalDateTime | `enteredat` | NOT NULL |
| exitedAt | LocalDateTime | `exitedat` | Nullable |
| comments | String | `comments` | TEXT |

**Relaciones:**

| Tipo | Campo | Entidad destino | FK |
|---|---|---|---|
| @ManyToOne | application | Applications | `applicationidapplication` |
| @ManyToOne | stageTracking | StageTracking | `stagetrackingid` |
| @ManyToOne | processedByUser | Users | `processedbyuserid` (nullable) |

---

### 5.4 `AttachedDocuments`

**Tabla:** `attacheddocuments` · **Lombok:** `@Data`, `@Builder` · **Patrón:** JPA

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

### 5.5 `Careers`

**Tabla:** `careers` · **Lombok:** `@Data`, `@Builder` · **Patrón:** SP

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idCareer | Integer | `idcareer` | PK, auto-increment |
| careerName | String | `careername` | NOT NULL, length=255 |
| careerCode | String | `careercode` | length=50 |

**Relaciones:**

| Tipo | Campo | Entidad destino | FK |
|---|---|---|---|
| @ManyToOne (LAZY) | faculty | Faculties | `facultiesidfaculty` (@JsonIgnore) |
| @ManyToOne (LAZY) | coordinator | Users | `coordinatoriduser` (@JsonIgnore) |

---

### 5.6 `Configurations`

**Tabla:** `configurations` · **Lombok:** `@Data`, `@Builder` · **Patrón:** SP

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

### 5.7 `Credentials`

**Tabla:** `credentials` · **Lombok:** `@Data`, `@Builder` · **Patrón:** JPA

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| id | Integer | `idcredentials` | PK, auto-increment |
| passwordHash | String | `passwordhash` | NOT NULL |
| dateModification | LocalDateTime | `datemodification` | Nullable |
| lastLogin | LocalDateTime | `lastlogin` | Nullable |
| failedAttempts | Integer | `failedattempts` | NOT NULL, default=`0` |
| accountLocked | Boolean | `accountlocked` | NOT NULL, default=`false` |
| passwordExpiryDate | LocalDate | `passwordexpirydate` | Nullable |

**Relaciones:** Ninguna (referenciada por `Users.credentials` y `TwoFactorAuth.credentials`)

---

### 5.8 `DeadLinerules`

**Tabla:** `deadlinerules` · **Lombok:** `@Data`, `@Builder` · **Patrón:** SP

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

### 5.9 `DigitalSignatures`

**Tabla:** `digitalsignatures` · **Lombok:** `@Data`, `@Builder` · **Patrón:** JPA

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

### 5.10 `DocumentsGenerated`

**Tabla:** `documentsgenerated` · **Lombok:** `@Data`, `@Builder` · **Patrón:** JPA

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| id | Integer | `iddocumentgenerated` | PK, auto-increment |
| documentType | String | `documenttype` | NOT NULL, length=255 |
| documentPath | String | `documentpath` | NOT NULL, length=500 |
| generatedAt | LocalDateTime | `generatedat` | NOT NULL |
| signatureTimestamp | LocalDateTime | `signaturetimestamp` | Nullable |

**Relaciones:**

| Tipo | Campo | Entidad destino | FK |
|---|---|---|---|
| @ManyToOne | application | Applications | `applicationsidapplication` |
| @ManyToOne | template | DocumentTemplates | `templateid` (nullable) |
| @ManyToOne | generatedByUser | Users | `generatedbyuserid` |
| @ManyToOne | digitalSignature | DigitalSignatures | `digitalsignatureid` (nullable) |

---

### 5.11 `DocumentTemplates`

**Tabla:** `documenttemplates` · **Lombok:** `@Data`, `@Builder` · **Patrón:** SP

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
| updatedAt | LocalDateTime | `updatedat` | Nullable |

**Relaciones:** Ninguna

---

### 5.12 `Faculties`

**Tabla:** `faculties` · **Lombok:** `@Data`, `@Builder` · **Patrón:** SP

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idFaculty | Integer | `idfaculty` | PK, auto-increment |
| facultyName | String | `facultyname` | NOT NULL, length=255 |
| facultyCode | String | `facultycode` | length=50 |

**Relaciones:**

| Tipo | Campo | Entidad destino | FK |
|---|---|---|---|
| @ManyToOne (LAZY) | dean | Users | `deaniduser` (@JsonIgnore, nullable) |

---

### 5.13 `Notification`

**Tabla:** `notification` · **Lombok:** `@Data`, `@Builder` · **Patrón:** JPA

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| id | Integer | `idnotification` | PK, auto-increment |
| notificationName | String | `notificationname` | NOT NULL, length=255 |
| message | String | `message` | TEXT |
| sentAt | LocalDateTime | `sentat` | Nullable |
| deliveryStatus | String | `deliverystatus` | NOT NULL, length=50, default=`"pending"` |
| deliveryChannel | String | `deliverychannel` | length=50 |
| readAt | LocalDateTime | `readat` | Nullable |
| errorMessage | String | `errormessage` | TEXT |
| retryCount | Integer | `retrycount` | NOT NULL, default=`0` |

**Relaciones:**

| Tipo | Campo | Entidad destino | FK |
|---|---|---|---|
| @ManyToOne | notificationType | NotificationType | `notificationtypeidnotificationtype` |
| @ManyToOne | application | Applications | `applicationid` (nullable) |
| @ManyToOne | recipientUser | Users | `recipientuserid` |

---

### 5.14 `NotificationType`

**Tabla:** `notificationtype` · **Lombok:** `@Data`, `@Builder` · **Patrón:** JPA

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idNotificationType | Integer | `idnotificationtype` | PK, auto-increment |
| nameTypeNotification | String | `nametypenotification` | NOT NULL, length=255 |
| templateCode | String | `templatecode` | length=50 |
| priorityLevel | String | `prioritylevel` | length=20 |

**Relaciones:** Ninguna

---

### 5.15 `Permissions`

**Tabla:** `permissions` · **Lombok:** `@Data`, `@Builder` · **Patrón:** SP

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idPermission | Integer | `idpermission` | PK, auto-increment |
| code | String | `code` | NOT NULL, UNIQUE, length=100 |
| description | String | `description` | Nullable |

**Relaciones:** Referenciado por `Roles.permissions` (M:N)

---

### 5.16 `Procedures`

**Tabla:** `procedures` · **Lombok:** `@Data`, `@Builder` · **Patrón:** JPA

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idProcedure | Integer | `idprocedure` | PK, auto-increment |
| nameProcedure | String | `nameprocedure` | NOT NULL, length=255 |
| procedureCode | String | `procedurecode` | NOT NULL, UNIQUE, length=50 |
| description | String | `description` | Nullable |
| estimatedDurationDays | Integer | `estimateddurationdays` | Nullable |
| requires2fa | Boolean | `requires2fa` | NOT NULL, default=`false` |
| active | Boolean | `active` | NOT NULL, default=`true` |
| createdAt | LocalDateTime | `createdat` | NOT NULL |

**Relaciones:**

| Tipo | Campo | Entidad destino | FK |
|---|---|---|---|
| @ManyToOne (LAZY) | workflow | Workflows | `workflowsidworkflow` (@JsonIgnore) |
| @ManyToOne (LAZY) | academicCalendar | AcademicCalendar | `academiccalendaridacademiccalendar` (@JsonIgnore, nullable) |
| @ManyToOne (LAZY) | deadLineRule | DeadLinerules | `deadlineruleid` (@JsonIgnore, nullable) |

---

### 5.17 `ProcessingStage`

**Tabla:** `processingstage` · **Lombok:** `@Data`, `@Builder` · **Patrón:** SP

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idProcessingStage | Integer | `idprocessingstage` | PK, auto-increment |
| stageName | String | `stagename` | NOT NULL, length=255 |
| stageCode | String | `stagecode` | NOT NULL, UNIQUE, length=50 |
| stageDescription | String | `stagedescription` | Nullable |
| stageOrder | Integer | `stageorder` | NOT NULL |
| requiresApproval | Boolean | `requiresapproval` | NOT NULL, default=`false` |
| maxDurationDays | Integer | `maxdurationdays` | Nullable |

**Relaciones:** Ninguna

---

### 5.18 `RefreshToken`

**Tabla:** `refresh_tokens` · **Lombok:** `@Data`, `@Builder` · **Patrón:** JPA

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

### 5.19 `RejectionReasons`

**Tabla:** `rejectionreasons` · **Lombok:** `@Data`, `@Builder` · **Patrón:** SP

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idRejectionReason | Integer | `idrejectionreason` | PK, auto-increment |
| reasonCode | String | `reasoncode` | NOT NULL, UNIQUE, length=50 |
| reasonDescription | String | `reasondescription` | NOT NULL |
| category | String | `category` | NOT NULL, length=100 |
| active | Boolean | `active` | NOT NULL, default=`true` |

**Relaciones:** Ninguna

---

### 5.20 `RequirementsOfTheProcedure`

**Tabla:** `requirementsoftheprocedure` · **Lombok:** `@Data`, `@Builder` · **Patrón:** JPA

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| id | Integer | `idrequirementsoftheprocedure` | PK, auto-increment |
| requirementName | String | `requirementname` | NOT NULL, length=255 |
| requirementDescription | String | `requirementdescription` | TEXT |
| requirementType | String | `requirementtype` | NOT NULL, length=50 |
| isMandatory | Boolean | `ismandatory` | NOT NULL, default=`true` |
| displayOrder | Integer | `displayorder` | Nullable |

**Relaciones:**

| Tipo | Campo | Entidad destino | FK |
|---|---|---|---|
| @ManyToOne | procedure | Procedures | `proceduresidprocedure` |

---

### 5.21 `Roles`

**Tabla:** `roles` · **Lombok:** `@Getter`, `@Setter`, `@Builder` · **Patrón:** JPA

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idRole | Integer | `idrole` | PK, auto-increment |
| roleName | String | `rolename` | NOT NULL, UNIQUE, length=100 |
| roleDescription | String | `roledescription` | TEXT |

**Relaciones:**

| Tipo | Campo | Entidad destino | FK |
|---|---|---|---|
| @ManyToMany (EAGER) | permissions | Permissions | Tabla intermedia `role_permissions` |

> Usa `@ToString(exclude = "permissions")` y `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` para evitar recursión.

---

### 5.22 `SessionToken`

**Tabla:** `sessiontokens` · **Lombok:** `@Data`, `@Builder` · **Patrón:** JPA

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| id | Integer | `idsession` | PK, auto-increment |
| token | String | `token` | NOT NULL, UNIQUE, TEXT |
| ipAddress | String | `ipaddress` | length=45 |
| userAgent | String | `useragent` | TEXT |
| createdAt | LocalDateTime | `createdat` | NOT NULL |
| expiresAt | LocalDateTime | `expiresat` | NOT NULL |
| lastActivity | LocalDateTime | `lastactivity` | Nullable |

**Relaciones:**

| Tipo | Campo | Entidad destino | FK |
|---|---|---|---|
| @ManyToOne | user | Users | `userid` |

---

### 5.23 `StageTracking`

**Tabla:** `stagetracking` · **Lombok:** `@Data`, `@Builder` · **Patrón:** JPA

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| id | Integer | `idstagetracking` | PK, auto-increment |
| enteredAt | LocalDateTime | `enteredat` | NOT NULL |
| completedAt | LocalDateTime | `completedat` | Nullable |
| notes | String | `notes` | TEXT |

**Relaciones:**

| Tipo | Campo | Entidad destino | FK |
|---|---|---|---|
| @ManyToOne | state | States | `stateidstate` |
| @ManyToOne | processingStage | ProcessingStage | `processingstageidprocessingstage` |
| @ManyToOne | assignedToUser | Users | `assignedtouserid` (nullable) |

---

### 5.24 `States`

**Tabla:** `states` · **Lombok:** `@Data`, `@Builder` · **Patrón:** SP

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idState | Integer | `idstate` | PK, auto-increment |
| stateName | String | `statename` | NOT NULL, UNIQUE, length=100 |
| stateDescription | String | `statedescription` | Nullable |
| stateCategory | String | `statecategory` | NOT NULL, length=50 |

**Relaciones:** Ninguna

---

### 5.25 `Students`

**Tabla:** `students` · **Lombok:** `@Data`, `@Builder` · **Patrón:** JPA

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| id | Integer | `idstudent` | PK, auto-increment |
| semester | String | `semester` | NOT NULL, length=255 |
| parallel | String | `parallel` | NOT NULL, length=1 |
| enrollmentDate | LocalDate | `enrollmentdate` | Nullable |
| status | String | `status` | NOT NULL, length=50, default=`"activo"` |

**Relaciones:**

| Tipo | Campo | Entidad destino | FK |
|---|---|---|---|
| @ManyToOne | user | Users | `usersiduser` |
| @ManyToOne | career | Careers | `careersidcareer` |

**Estados válidos del estudiante (máquina de estados):** `activo` → `promovido` / `graduado` / `retirado` → `reactivado`

---

### 5.26 `TwoFactorAuth`

**Tabla:** `twofactorauth` · **Lombok:** `@Data`, `@Builder` · **Patrón:** JPA

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| id | Integer | `id2fa` | PK, auto-increment |
| enabled | Boolean | `enabled` | NOT NULL, default=`false` |
| secretKey | String | `secretkey` | TEXT |
| backupCodes | List\<String\> | `backupcodes` | TEXT (JPA Converter: comma-delimited) |
| verifiedAt | LocalDateTime | `verifiedat` | Nullable |

**Relaciones:**

| Tipo | Campo | Entidad destino | FK |
|---|---|---|---|
| @OneToOne | credentials | Credentials | `credentialsidcredentials` (UNIQUE) |

---

### 5.27 `Users`

**Tabla:** `users` · **Lombok:** `@Getter`, `@Setter`, `@Builder` · **Patrón:** JPA

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idUser | Integer | `iduser` | PK, auto-increment |
| names | String | `names` | NOT NULL, length=255 |
| surnames | String | `surnames` | NOT NULL, length=255 |
| cardId | String | `cardid` | NOT NULL, UNIQUE, length=10 |
| institutionalEmail | String | `institutionalemail` | NOT NULL, UNIQUE, length=255 |
| personalMail | String | `personalmail` | UNIQUE, length=255 |
| phoneNumber | String | `phonenumber` | length=15 |
| statement | Boolean | `statement` | NOT NULL, default=`true` |
| createdAt | LocalDateTime | `createdat` | NOT NULL |
| updatedAt | LocalDateTime | `updatedat` | Nullable |
| active | Boolean | `active` | NOT NULL, default=`true` |

**Relaciones:**

| Tipo | Campo | Entidad destino | FK |
|---|---|---|---|
| @ManyToOne | configuration | Configurations | `configurationsidconfiguration` |
| @OneToOne | credentials | Credentials | `credentialsidcredentials` |
| @ManyToMany (EAGER) | roles | Roles | Tabla intermedia `user_roles` |

> Usa `@ToString(exclude = {"roles", "credentials"})` y `@EqualsAndHashCode(onlyExplicitlyIncluded = true)`.

---

### 5.28 `Workflows`

**Tabla:** `workflows` · **Lombok:** `@Data`, `@Builder` · **Patrón:** SP

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idWorkflow | Integer | `idworkflow` | PK, auto-increment |
| workflowName | String | `workflowname` | NOT NULL, length=255 |
| workflowDescription | String | `workflowdescription` | Nullable |
| createdAt | LocalDateTime | `createdat` | NOT NULL |
| active | Boolean | `active` | NOT NULL, default=`true` |

**Relaciones:** Ninguna

---

### 5.29 `WorkflowStages`

**Tabla:** `workflowstages` · **Lombok:** `@Data`, `@Builder` · **Patrón:** JPA

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idWorkflowStage | Integer | `idworkflowstage` | PK, auto-increment |
| sequenceOrder | Integer | `sequenceorder` | NOT NULL |
| isOptional | Boolean | `isoptional` | NOT NULL, default=`false` |

**Relaciones:**

| Tipo | Campo | Entidad destino | FK |
|---|---|---|---|
| @ManyToOne (LAZY) | workflow | Workflows | `workflowidworkflow` (@JsonIgnore) |
| @ManyToOne (LAZY) | processingStage | ProcessingStage | `processingstageidprocessingstage` (@JsonIgnore) |

---

## 6. Repositorios (Repository)

Todos extienden `JpaRepository<Entity, ID>`. Total: **29 repositorios**.

### 6.1 Repositorios con Patrón SP (11)

Estos repositorios definen `@Query(nativeQuery = true)` con `CALL sp*` y `SELECT * FROM fn_*`.

| Repositorio | Entidad | SP Insert | SP Update | SP Delete | Función Lista |
|---|---|---|---|---|---|
| `IAcademicCalendarRepository` | AcademicCalendar | `spi_academiccalendar` | `spu_academiccalendar` | `spd_academiccalendar` | `fn_list_academiccalendar` |
| `ICareersRepository` | Careers | `spi_career` | `spu_career` | `spd_career` | `fn_list_careers` |
| `IConfigurationsRepository` | Configurations | `spi_configuration` | `spu_configuration` | `spd_configuration` | `fn_list_configurations` |
| `IDeadLineRulesRepository` | DeadLinerules | `spi_deadlinerule` | `spu_deadlinerule` | `spd_deadlinerule` | `fn_list_deadlinerules` |
| `IDocumentTemplatesRepository` | DocumentTemplates | `spi_documenttemplate` | `spu_documenttemplate` | `spd_documenttemplate` | `fn_list_documenttemplates` |
| `IFacultiesRepository` | Faculties | `spi_faculty` | `spu_faculty` | `spd_faculty` | `fn_list_faculties` |
| `IPermissionsRepository` | Permissions | `spi_permission` | `spu_permission` | `spd_permission` | `fn_list_permissions` |
| `IProcessingStageRepository` | ProcessingStage | `spi_processingstage` | `spu_processingstage` | `spd_processingstage` | `fn_list_processingstage` |
| `IRejectionReasonsRepository` | RejectionReasons | `spi_rejectionreason` | `spu_rejectionreason` | `spd_rejectionreason` | `fn_list_rejectionreasons` |
| `IStatesRepository` | States | `spi_state` | `spu_state` | `spd_state` | `fn_list_states` |
| `IWorkflowsRepository` | Workflows | `spi_workflow` | `spu_workflow` | `spd_workflow` | `fn_list_workflows` |

### 6.2 Repositorios con Patrón JPA (18)

Estos repositorios usan queries derivadas de Spring Data y/o JPA CRUD heredado.

| Repositorio | Entidad | Queries Derivadas |
|---|---|---|
| `IApplicationsRepository` | Applications | `findByApplicationCode`, `findByApplicantUserIdUser`, `findByPriority`, `findByProcedureIdProcedure`, `existsByApplicationCode` |
| `IApplicationStageHistoryRepository` | ApplicationStageHistory | (vacío — solo CRUD heredado) |
| `IAttachedDocumentsRepository` | AttachedDocuments | (vacío) |
| `ICredentialsRepository` | Credentials | (vacío) |
| `IDigitalSignaturesRepository` | DigitalSignatures | (vacío) |
| `IDocumentsGeneratedRepository` | DocumentsGenerated | (vacío) |
| `INotificationRepository` | Notification | (vacío) |
| `INotificationTypeRepository` | NotificationType | (vacío) |
| `IProceduresRepository` | Procedures | `findByProcedureCode`, `findByActiveTrue`, `findByActiveFalse`, `findByWorkflowIdWorkflow`, `existsByProcedureCode`, `existsByNameProcedure` |
| `IRefreshTokenRepository` | RefreshToken | `findByToken`, `findByUserAndRevokedFalse`, SP: `spu_revoke_all_refresh_tokens` |
| `IRequirementsOfTheProcedureRepository` | RequirementsOfTheProcedure | (vacío) |
| `IRolesRepository` | Roles | `findByRoleName`, `existsByRoleName` |
| `ISessionTokenRepository` | SessionToken | (vacío) |
| `IStageTrackingRepository` | StageTracking | (vacío) |
| `IStudentsRepository` | Students | `findByUserIdUser`, `findByCareerIdCareer`, `findBySemesterAndParallel`, `findByStatus`, `existsByUserIdUser` |
| `ITwoFactorAuthRepository` | TwoFactorAuth | `findByCredentials_Id`, `existsByCredentials_IdAndEnabledTrue` |
| `IUsersRepository` | Users | `findByInstitutionalEmail`, `findByCardId`, `findByPersonalMail`, `findByActiveTrue`, `findByActiveFalse`, `findByRolesIdRole`, `existsByInstitutionalEmail`, `existsByCardId` |
| `IWorkflowStagesRepository` | WorkflowStages | (vacío) |

> **Nota:** `IRefreshTokenRepository` es un caso mixto: usa queries derivadas para búsqueda + un SP (`spu_revoke_all_refresh_tokens`) para revocación masiva.

---

## 7. Servicios — Interfaces (Services)

Total: **29 interfaces** en `com.app.uteq.Services`.

### 7.1 Servicios con Patrón SP (11)

Estas interfaces definen operaciones CRUD que internamente invocan SPs.

| Interfaz | Métodos |
|---|---|
| `IAcademicCalendarService` | `createcalendar(...)`, `modifycalendar(UAcademicCalendarRequest)`, `deletecalendar(Integer)`, `listarCalendarios(Boolean)` |
| `ICareersService` | `createCareers(CCareersRequest)`, `updateCareers(UCareersRequest)`, `deleteCareers(Integer)`, `listCareers(Integer)` |
| `IConfigurationService` | `createConfiguration(CConfigurationRequest)`, `updateConfiguration(UConfigurationRequest)`, `deleteConfiguration(Integer)`, `listConfiguration()` |
| `IDeadLineRulesService` | `createDeadlinerule(CDeadlineRuleRequest)`, `updateDeadlinerule(UDeadlineRuleRequest)`, `deleteDeadlinerule(Integer)`, `listDeadlinerule(Boolean)` |
| `IDocumentTemplatesService` | `createDocumenttemplate(CDocumentTemplateRequest)`, `updateDocumenttemplate(UDocumentTemplateRequest)`, `deleteDocumenttemplate(Integer)`, `listDocumenttemplate(Boolean)` |
| `IFacultiesService` | `createFaculty(CFacultyRequest)`, `updateFaculty(UFacultyRequest)`, `deleteFaculty(Integer)`, `listFaculty()` |
| `IPermissionsService` | `createPermission(CPermissionRequest)`, `updatePermission(UPermissionRequest)`, `deletePermission(Integer)`, `listPermission()` |
| `IProcessingStageService` | `createProcessingstage(CProcessingStageRequest)`, `updateProcessingstage(UProcessingStageRequest)`, `deleteProcessingstage(Integer)`, `listProcessingstage()` |
| `IRejectionReasonsService` | `createRejectreason(CRejectionReasonRequest)`, `updateRejectreason(URejectionReasonRequest)`, `deleteRejectreason(Integer)`, `listRejectreason(Boolean)` |
| `IStatesService` | `createStates(CStateRequest)`, `updateStates(UStateRequest)`, `deleteStates(Integer)`, `listStates(String)` |
| `IWorkflowsService` | `createWorkflow(CWorkflowRequest)`, `updateWorkflow(UWorkflowRequest)`, `deleteWorkflow(Integer)`, `listWorkflow(Boolean)` |

### 7.2 Servicios con Patrón JPA (18)

Interfaces que combinan CRUD básico y operaciones de negocio.

#### `IApplicationsService` (13 métodos)
```
findAll(), findById(Integer), save(Applications), deleteById(Integer)
findAllApplications(), findApplicationById(Integer)
findByApplicantUserId(Integer), findByPriority(String)
createApplication(CApplicationRequest), updateApplication(Integer, UApplicationRequest)
deleteApplication(Integer)
resolveApplication(Integer, String), rejectApplication(Integer, Integer)
```

#### `IApplicationStageHistoryService` (4 métodos)
```
findAll(), findById(Integer), save(ApplicationStageHistory), deleteById(Integer)
```

#### `IAttachedDocumentsService` (4 métodos)
```
findAll(), findById(Integer), save(AttachedDocuments), deleteById(Integer)
```

#### `ICredentialsService` (17 métodos)
```
findAll(), findById(Integer), save(Credentials), deleteById(Integer)
findAllCredentials(), findCredentialById(Integer)
createCredential(CCredentialRequest)
changePassword(Integer, String, String)
lockAccount(Integer), unlockAccount(Integer)
registerFailedAttempt(Integer), registerSuccessfulLogin(Integer)
resetPassword(Integer), isPasswordExpired(Integer)
verifyCredentialOwnership(Integer, String)
registerFailedAttemptByEmail(String), registerSuccessfulLoginByEmail(String)
```

#### `IDigitalSignaturesService` (4 métodos)
```
findAll(), findById(Integer), save(DigitalSignatures), deleteById(Integer)
```

#### `IDocumentsGeneratedService` (4 métodos)
```
findAll(), findById(Integer), save(DocumentsGenerated), deleteById(Integer)
```

#### `INotificationService` (4 métodos)
```
findAll(), findById(Integer), save(Notification), deleteById(Integer)
```

#### `INotificationTypeService` (4 métodos)
```
findAll(), findById(Integer), save(NotificationType), deleteById(Integer)
```

#### `IProceduresService` (15 métodos)
```
findAll(), findById(Integer), save(Procedures), deleteById(Integer)
findAllProcedures(), findAllIncludingInactive()
findProcedureById(Integer), findByProcedureCode(String)
findByWorkflow(Integer)
createProcedure(CProcedureRequest), updateProcedure(Integer, UProcedureRequest)
activateProcedure(Integer), deactivateProcedure(Integer)
deleteProcedure(Integer), requires2FA(Integer)
```

#### `IRefreshTokenService` (4 métodos)
```
findAll(), findById(Long), save(RefreshToken), deleteById(Long)
```

#### `IRequirementsOfTheProcedureService` (4 métodos)
```
findAll(), findById(Integer), save(RequirementsOfTheProcedure), deleteById(Integer)
```

#### `IRolesService` (14 métodos)
```
findAll(), findById(Integer), save(Roles), deleteById(Integer)
findAllRoles(), findRoleById(Integer), findByRoleName(String)
createRole(CRoleRequest), updateRole(Integer, URoleRequest)
deleteRole(Integer)
assignPermissions(Integer, Set<Integer>), removePermissions(Integer, Set<Integer>)
assignRoleToUser(Integer, Integer), removeRoleFromUser(Integer, Integer)
```

#### `ISessionTokenService` (4 métodos)
```
findAll(), findById(Integer), save(SessionToken), deleteById(Integer)
```

#### `IStageTrackingService` (4 métodos)
```
findAll(), findById(Integer), save(StageTracking), deleteById(Integer)
```

#### `IStudentsService` (17 métodos)
```
findAll(), findById(Integer), save(Students), deleteById(Integer)
findAllStudents(), findStudentById(Integer)
findByCareer(Integer), findBySemesterAndParallel(String, String)
findByUserId(Integer)
enrollStudent(CStudentRequest), updateStudent(Integer, UStudentRequest)
changeStatus(Integer, String)
promoteToNextSemester(Integer), graduate(Integer)
withdraw(Integer), reactivate(Integer)
```

#### `ITwoFactorAuthService` (12 métodos)
```
findAll(), findById(Integer), save(TwoFactorAuth), deleteById(Integer)
setup2FA(String), verifyAndEnable2FA(String, int)
disable2FA(String, int), validateCode(String, int)
validateBackupCode(String, String), is2FAEnabled(String)
getStatus(String), regenerateBackupCodes(String, int)
```

#### `IUsersService` (12 métodos)
```
findAll(), findById(Integer), save(Users), deleteById(Integer)
findAllUsers(), findUserById(Integer)
createUser(CUserRequest), updateUser(Integer, UUserRequest)
deleteUser(Integer)
deactivateUser(Integer), activateUser(Integer)
findByInstitutionalEmail(String)
```

#### `IWorkflowStagesService` (4 métodos)
```
findAll(), findById(Integer), save(WorkflowStages), deleteById(Integer)
```

---

## 8. Servicios — Implementaciones (Services/Impl)

Total: **29 ServiceImpl + 1 CustomUserDetailsService = 30 implementaciones**.

Todas las implementaciones llevan las anotaciones:
- `@Service` — Registra como bean de Spring  
- `@RequiredArgsConstructor` — Inyección de dependencias por constructor (Lombok)
- `@Transactional` — Soporte transaccional automático en todos los métodos públicos

### 8.1 Implementaciones SP (11)

Patrón uniforme: Método → invoca SP del repositorio → resultado `Object[]` → `SpResultConverter` → DTO Response.

| Clase | Repositorio | SPs Usados |
|---|---|---|
| `AcademicCalendarServiceImpl` | `IAcademicCalendarRepository` | `spi_academiccalendar`, `spu_academiccalendar`, `spd_academiccalendar`, `fn_list_academiccalendar` |
| `CareersServiceImpl` | `ICareersRepository` | `spi_career`, `spu_career`, `spd_career`, `fn_list_careers` |
| `ConfigurationServiceImpl` | `IConfigurationsRepository` | `spi_configuration`, `spu_configuration`, `spd_configuration`, `fn_list_configurations` |
| `DeadLineRulesServiceImpl` | `IDeadLineRulesRepository` | `spi_deadlinerule`, `spu_deadlinerule`, `spd_deadlinerule`, `fn_list_deadlinerules` |
| `DocumentTemplatesServiceImpl` | `IDocumentTemplatesRepository` | `spi_documenttemplate`, `spu_documenttemplate`, `spd_documenttemplate`, `fn_list_documenttemplates` |
| `FacultiesServiceImpl` | `IFacultiesRepository` | `spi_faculty`, `spu_faculty`, `spd_faculty`, `fn_list_faculties` |
| `PermissionsServiceImpl` | `IPermissionsRepository` | `spi_permission`, `spu_permission`, `spd_permission`, `fn_list_permissions` |
| `ProcessingStageServiceImpl` | `IProcessingStageRepository` | `spi_processingstage`, `spu_processingstage`, `spd_processingstage`, `fn_list_processingstage` |
| `RejectionReasonsServiceImpl` | `IRejectionReasonsRepository` | `spi_rejectionreason`, `spu_rejectionreason`, `spd_rejectionreason`, `fn_list_rejectionreasons` |
| `StatesServiceImpl` | `IStatesRepository` | `spi_state`, `spu_state`, `spd_state`, `fn_list_states` |
| `WorkflowsServiceImpl` | `IWorkflowsRepository` | `spi_workflow`, `spu_workflow`, `spd_workflow`, `fn_list_workflows` |

### 8.2 Implementaciones JPA (18)

#### `ApplicationsServiceImpl` — 219 líneas, 13 métodos

Gestiona solicitudes/trámites estudiantiles con lógica de negocio avanzada.

**Validaciones:** Código de solicitud único, auto-generado con formato `SOL-{timestamp}`. Prioridad válida: `baja`, `normal`, `alta`, `urgente`.

**Lógica de negocio:**
- `resolveApplication(id, resolution)` — Establece resolución y fecha de completación
- `rejectApplication(id, rejectionReasonId)` — Asocia motivo de rechazo validando existencia

---

#### `ApplicationStageHistoryServiceImpl` — CRUD básico (4 métodos)

---

#### `AttachedDocumentsServiceImpl` — CRUD básico (4 métodos)

---

#### `CredentialsServiceImpl` — 213 líneas, 17 métodos

Gestión avanzada de credenciales con política de seguridad.

**Políticas de seguridad implementadas:**
- **Máximo 5 intentos fallidos** → bloqueo automático de cuenta (`accountLocked = true`)
- **Expiración de contraseña** → 90 días desde la fecha de modificación
- **Reset de contraseña** → genera password temporal de 12 caracteres
- **Cambio de contraseña** → valida password anterior, hashea con BCrypt, actualiza `dateModification` y `passwordExpiryDate`
- **Registro de login exitoso** → resetea `failedAttempts` a 0, actualiza `lastLogin`

---

#### `DigitalSignaturesServiceImpl` — CRUD básico (4 métodos)

---

#### `DocumentsGeneratedServiceImpl` — CRUD básico (4 métodos)

---

#### `NotificationServiceImpl` — CRUD básico (4 métodos)

---

#### `NotificationTypeServiceImpl` — CRUD básico (4 métodos)

---

#### `ProceduresServiceImpl` — 194 líneas, 15 métodos

Gestión de trámites/procedimientos con lógica de negocio.

**Características:**
- Código de procedimiento auto-generado (`PROC-{timestamp}`)
- Nombre y código único validados
- Activación/desactivación con estados (`active = true/false`)
- Consulta `requires2FA` para verificar si un trámite requiere autenticación de dos factores
- Filtro por workflow asociado

---

#### `RefreshTokenServiceImpl` — CRUD básico (4 métodos)

---

#### `RequirementsOfTheProcedureServiceImpl` — CRUD básico (4 métodos)

---

#### `RolesServiceImpl` — 202 líneas, 14 métodos

Gestión de roles con protección de roles del sistema y gestión de permisos.

**Lógica de negocio:**
- **Roles protegidos** (no eliminables): `ROLE_ADMIN`, `ROLE_STUDENT`, `ROLE_COORDINATOR`, `ROLE_DEAN`
- `assignPermissions(roleId, permissionIds)` → asigna permisos al rol
- `removePermissions(roleId, permissionIds)` → remueve permisos del rol
- `assignRoleToUser(roleId, userId)` → asigna rol a usuario
- `removeRoleFromUser(roleId, userId)` → remueve rol de usuario
- Nombre de rol único validado

---

#### `SessionTokenServiceImpl` — CRUD básico (4 métodos)

---

#### `StageTrackingServiceImpl` — CRUD básico (4 métodos)

---

#### `StudentsServiceImpl` — 254 líneas, 17 métodos

Gestión de estudiantes con máquina de estados completa.

**Máquina de estados:**

```
           enrollStudent()
               ↓
           ┌──────┐
           │activo│
           └──┬───┘
              │
    ┌─────────┼─────────┐
    ↓         ↓         ↓
promover  graduar    retirar
    ↓         ↓         ↓
┌────────┐ ┌────────┐ ┌────────┐
│promovido│ │graduado│ │retirado│
└────────┘ └────────┘ └────┬───┘
                           ↓
                       reactivar
                           ↓
                       ┌──────┐
                       │activo│
                       └──────┘
```

**Validaciones:**
- Un usuario solo puede ser estudiante una vez (`existsByUserIdUser`)
- `promoteToNextSemester()` → incrementa semestre numéricamente (1→2, 2→3...)
- Solo estudiantes `activo` pueden ser promovidos/graduados/retirados
- Solo estudiantes `retirado` pueden ser reactivados

---

#### `TwoFactorAuthServiceImpl` — 243 líneas, 12 métodos

Autenticación de dos factores con Google Authenticator (TOTP).

**Funcionalidades:**
- `setup2FA(email)` → genera secretKey, construye URI para QR (`otpauth://totp/SGTE:{email}?secret=...&issuer=SGTE`), genera 8 códigos de respaldo
- `verifyAndEnable2FA(email, code)` → verifica primer código TOTP y activa 2FA
- `disable2FA(email, code)` → desactiva 2FA (requiere código válido como confirmación)
- `validateCode(email, code)` → valida código TOTP de 6 dígitos
- `validateBackupCode(email, backupCode)` → valida y consume código de respaldo (un solo uso)
- `regenerateBackupCodes(email, code)` → genera nuevos 8 códigos de respaldo

---

#### `UsersServiceImpl` — 178 líneas, 12 métodos

Gestión de usuarios con soft-delete y validaciones de unicidad.

**Validaciones:**
- Email institucional único
- Cédula (cardId) única
- `deleteUser(id)` → **soft delete** (`active = false`, `statement = false`)
- `deactivateUser(id)` / `activateUser(id)` → toggle de estado activo

---

#### `WorkflowStagesServiceImpl` — CRUD básico (4 métodos)

---

### 8.3 `CustomUserDetailsService` — 69 líneas

Ver sección [4.1](#41-customuserdetailsservicejava) para detalles completos.

---

## 9. Controladores (Controllers)

Total: **30 controladores**. Todos bajo `com.app.uteq.Controllers`.

**Convención de seguridad:**
- Nivel de clase: `@PreAuthorize("isAuthenticated()")` (excepto AuthController y TwoFactorAuthController)
- Nivel de método: `@PreAuthorize("hasAuthority('CODIGO_PERMISO')")`

**Convención de versionado API:** Todos los endpoints usan prefijo `/api/v1/`

### 9.1 `AcademicCalendarController`

**Base:** `/api/v1/academic-calendar` · **Seguridad:** `isAuthenticated()`

| HTTP | Path | Permiso | Método |
|---|---|---|---|
| POST | `/` | `CAL_CREAR` | `createCalendar(@Valid @RequestBody IAcademicCalendarRequest)` |
| PUT | `/{idacademiccalendar}` | `CAL_MODIFICAR` | `actualizar(@PathVariable Integer, @Valid @RequestBody UAcademicCalendarRequest)` |
| DELETE | `/{id}` | `CAL_ELIMINAR` | `eliminar(@PathVariable Integer)` |
| GET | `/` | `CAL_LISTAR` | `listar(@RequestParam(required=false) Boolean onlyActive)` |

---

### 9.2 `ApplicationsController`

**Base:** `/api/v1/applications` · **Seguridad:** `isAuthenticated()`

| HTTP | Path | Permiso | Método |
|---|---|---|---|
| GET | `/` | `SOL_LISTAR` | `findAll()` |
| GET | `/{id}` | `SOL_VER` | `findById(@PathVariable Integer)` |
| GET | `/user/{userId}` | `SOL_VER` | `findByUserId(@PathVariable Integer)` |
| GET | `/priority/{priority}` | `SOL_LISTAR` | `findByPriority(@PathVariable String)` |
| POST | `/` | `SOL_CREAR` | `create(@Valid @RequestBody CApplicationRequest)` |
| PUT | `/{id}` | `SOL_MODIFICAR` | `update(@PathVariable Integer, @Valid @RequestBody UApplicationRequest)` |
| DELETE | `/{id}` | `SOL_ELIMINAR` | `delete(@PathVariable Integer)` |
| PATCH | `/{id}/resolve` | `SOL_RESOLVER` | `resolve(@PathVariable Integer, @RequestParam String)` |
| PATCH | `/{id}/reject` | `SOL_RECHAZAR` | `reject(@PathVariable Integer, @RequestParam Integer)` |

---

### 9.3 `ApplicationStageHistoryController`

**Base:** `/api/v1/application-stage-history` · **Seguridad:** `isAuthenticated()`

| HTTP | Path | Permiso | Método |
|---|---|---|---|
| GET | `/` | `HIST_LISTAR` | `findAll()` |
| GET | `/{id}` | `HIST_VER` | `findById(@PathVariable Integer)` |
| POST | `/` | `HIST_CREAR` | `create(@RequestBody ApplicationStageHistory)` |
| PUT | `/{id}` | `HIST_MODIFICAR` | `update(@PathVariable Integer, @RequestBody ApplicationStageHistory)` |
| DELETE | `/{id}` | `HIST_ELIMINAR` | `delete(@PathVariable Integer)` |

---

### 9.4 `AttachedDocumentsController`

**Base:** `/api/v1/attached-documents` · **Seguridad:** `isAuthenticated()`

| HTTP | Path | Permiso | Método |
|---|---|---|---|
| GET | `/` | `DOCADJ_LISTAR` | `findAll()` |
| GET | `/{id}` | `DOCADJ_VER` | `findById(@PathVariable Integer)` |
| POST | `/` | `DOCADJ_CREAR` | `create(@RequestBody AttachedDocuments)` |
| PUT | `/{id}` | `DOCADJ_MODIFICAR` | `update(@PathVariable Integer, @RequestBody AttachedDocuments)` |
| DELETE | `/{id}` | `DOCADJ_ELIMINAR` | `delete(@PathVariable Integer)` |

---

### 9.5 `AuthController`

**Base:** `/api/v1/auth` · **Seguridad:** Público (`permitAll`)

Ver sección [4.2](#42-authcontrollerjava) para detalles completos.

| HTTP | Path | Permiso | Método |
|---|---|---|---|
| POST | `/token` | Ninguno | `generateToken(grantType, username, password, withRefreshToken, refreshToken)` |
| POST | `/2fa-verify` | Ninguno | `verify2FA(preAuthToken, code, backupCode)` |
| POST | `/logout` | Req. Auth | `logout(Authentication)` |

---

### 9.6 `CareerController`

**Base:** `/api/v1/careers` · **Seguridad:** `isAuthenticated()`

| HTTP | Path | Permiso | Método |
|---|---|---|---|
| POST | `/` | `CARRERA_CREAR` | `create(@Valid @RequestBody CCareersRequest)` |
| PUT | `/{idcareer}` | `CARRERA_MODIFICAR` | `update(@PathVariable Integer, @Valid @RequestBody UCareersRequest)` |
| DELETE | `/{idcareer}` | `CARRERA_ELIMINAR` | `delete(@PathVariable Integer)` |
| GET | `/` | `CARRERA_LISTAR` | `list(@RequestParam(required=false) Integer facultyid)` |

---

### 9.7 `ConfigurationController`

**Base:** `/api/v1/configuration` · **Seguridad:** `isAuthenticated()`

| HTTP | Path | Permiso | Método |
|---|---|---|---|
| POST | `/` | `CONFIG_CREAR` | `create(@Valid @RequestBody CConfigurationRequest)` |
| PUT | `/{id}` | `CONFIG_MODIFICAR` | `update(@PathVariable Integer, @Valid @RequestBody UConfigurationRequest)` |
| DELETE | `/{idconfiguration}` | `CONFIG_ELIMINAR` | `delete(@PathVariable Integer)` |
| GET | `/` | `CONFIG_LISTAR` | `list()` |

---

### 9.8 `CredentialsController`

**Base:** `/api/v1/credentials` · **Seguridad:** `isAuthenticated()`

| HTTP | Path | Permiso | Método |
|---|---|---|---|
| GET | `/` | `CRED_LISTAR` | `findAll()` |
| GET | `/{id}` | `CRED_VER` | `findById(@PathVariable Integer)` |
| POST | `/` | `CRED_CREAR` | `create(@Valid @RequestBody CCredentialRequest)` |
| POST | `/{id}/change-password` | `CRED_CAMBIAR_PASS` | `changePassword(@PathVariable Integer, @RequestBody Map<String,String>, Authentication)` |
| POST | `/{id}/reset-password` | `CRED_RESETEAR_PASS` | `resetPassword(@PathVariable Integer)` |
| GET | `/{id}/password-expired` | (clase) | `isPasswordExpired(@PathVariable Integer)` |
| POST | `/{id}/lock` | `CRED_BLOQUEAR` | `lockAccount(@PathVariable Integer)` |
| POST | `/{id}/unlock` | `CRED_DESBLOQUEAR` | `unlockAccount(@PathVariable Integer)` |
| DELETE | `/{id}` | `CRED_ELIMINAR` | `delete(@PathVariable Integer)` |

---

### 9.9 `DeadlineruleControllers`

**Base:** `/api/v1/deadlinerules` · **Seguridad:** `isAuthenticated()`

| HTTP | Path | Permiso | Método |
|---|---|---|---|
| POST | `/` | `REGLA_CREAR` | `create(@RequestBody CDeadlineRuleRequest)` |
| PUT | `/{id}` | `REGLA_MODIFICAR` | `update(@PathVariable Integer, @RequestBody UDeadlineRuleRequest)` |
| DELETE | `/{id}` | `REGLA_ELIMINAR` | `delete(@PathVariable Integer)` |
| GET | `/` | `REGLA_LISTAR` | `list(@RequestParam(required=false) Boolean onlyActive)` |

---

### 9.10 `DigitalSignaturesController`

**Base:** `/api/v1/digital-signatures` · **Seguridad:** `isAuthenticated()`

| HTTP | Path | Permiso | Método |
|---|---|---|---|
| GET | `/` | `FIRMA_LISTAR` | `findAll()` |
| GET | `/{id}` | `FIRMA_VER` | `findById(@PathVariable Integer)` |
| POST | `/` | `FIRMA_CREAR` | `create(@RequestBody DigitalSignatures)` |
| PUT | `/{id}` | `FIRMA_MODIFICAR` | `update(@PathVariable Integer, @RequestBody DigitalSignatures)` |
| DELETE | `/{id}` | `FIRMA_ELIMINAR` | `delete(@PathVariable Integer)` |

---

### 9.11 `DocumentsGeneratedController`

**Base:** `/api/v1/documents-generated` · **Seguridad:** `isAuthenticated()`

| HTTP | Path | Permiso | Método |
|---|---|---|---|
| GET | `/` | `DOCGEN_LISTAR` | `findAll()` |
| GET | `/{id}` | `DOCGEN_VER` | `findById(@PathVariable Integer)` |
| POST | `/` | `DOCGEN_CREAR` | `create(@RequestBody DocumentsGenerated)` |
| PUT | `/{id}` | `DOCGEN_MODIFICAR` | `update(@PathVariable Integer, @RequestBody DocumentsGenerated)` |
| DELETE | `/{id}` | `DOCGEN_ELIMINAR` | `delete(@PathVariable Integer)` |

---

### 9.12 `DocumentTemplateController`

**Base:** `/api/v1/document-templates` · **Seguridad:** `isAuthenticated()`

| HTTP | Path | Permiso | Método |
|---|---|---|---|
| POST | `/` | `PLANTILLA_CREAR` | `create(@Valid @RequestBody CDocumentTemplateRequest)` |
| PUT | `/{id}` | `PLANTILLA_MODIFICAR` | `update(@PathVariable Integer, @Valid @RequestBody UDocumentTemplateRequest)` |
| DELETE | `/{idtemplate}` | `PLANTILLA_ELIMINAR` | `delete(@PathVariable Integer)` |
| GET | `/` | `PLANTILLA_LISTAR` | `list(@RequestParam(required=false) Boolean onlyActive)` |

---

### 9.13 `FacultyController`

**Base:** `/api/v1/faculty` · **Seguridad:** `isAuthenticated()`

| HTTP | Path | Permiso | Método |
|---|---|---|---|
| POST | `/` | `FACULTAD_CREAR` | `create(@Valid @RequestBody CFacultyRequest)` |
| PUT | `/{id}` | `FACULTAD_MODIFICAR` | `update(@PathVariable Integer, @Valid @RequestBody UFacultyRequest)` |
| DELETE | `/{idfaculty}` | `FACULTAD_ELIMINAR` | `delete(@PathVariable Integer)` |
| GET | `/` | `FACULTAD_LISTAR` | `list()` |

---

### 9.14 `NotificationController`

**Base:** `/api/v1/notifications` · **Seguridad:** `isAuthenticated()`

| HTTP | Path | Permiso | Método |
|---|---|---|---|
| GET | `/` | `NOTIF_LISTAR` | `findAll()` |
| GET | `/{id}` | `NOTIF_VER` | `findById(@PathVariable Integer)` |
| POST | `/` | `NOTIF_CREAR` | `create(@RequestBody Notification)` |
| PUT | `/{id}` | `NOTIF_MODIFICAR` | `update(@PathVariable Integer, @RequestBody Notification)` |
| DELETE | `/{id}` | `NOTIF_ELIMINAR` | `delete(@PathVariable Integer)` |

---

### 9.15 `NotificationTypeController`

**Base:** `/api/v1/notification-types` · **Seguridad:** `isAuthenticated()`

| HTTP | Path | Permiso | Método |
|---|---|---|---|
| GET | `/` | `TIPNOTIF_LISTAR` | `findAll()` |
| GET | `/{id}` | `TIPNOTIF_VER` | `findById(@PathVariable Integer)` |
| POST | `/` | `TIPNOTIF_CREAR` | `create(@RequestBody NotificationType)` |
| PUT | `/{id}` | `TIPNOTIF_MODIFICAR` | `update(@PathVariable Integer, @RequestBody NotificationType)` |
| DELETE | `/{id}` | `TIPNOTIF_ELIMINAR` | `delete(@PathVariable Integer)` |

---

### 9.16 `PermissionController`

**Base:** `/api/v1/permissions` · **Seguridad:** `isAuthenticated()`

| HTTP | Path | Permiso | Método |
|---|---|---|---|
| POST | `/` | `PERMISO_CREAR` | `create(@Valid @RequestBody CPermissionRequest)` |
| PUT | `/{id}` | `PERMISO_MODIFICAR` | `update(@PathVariable Integer, @Valid @RequestBody UPermissionRequest)` |
| DELETE | `/{idpermission}` | `PERMISO_ELIMINAR` | `delete(@PathVariable Integer)` |
| GET | `/` | `PERMISO_LISTAR` | `list()` |

---

### 9.17 `ProceduresController`

**Base:** `/api/v1/procedures` · **Seguridad:** `isAuthenticated()`

| HTTP | Path | Permiso | Método |
|---|---|---|---|
| GET | `/legacy` | `TRAMITE_LISTAR` | `findAllLegacy()` |
| GET | `/legacy/{id}` | `TRAMITE_VER` | `findByIdLegacy(@PathVariable Integer)` |
| GET | `/` | `TRAMITE_LISTAR` | `findAll()` |
| GET | `/all` | `TRAMITE_LISTAR` | `findAllIncludingInactive()` |
| GET | `/{id}` | `TRAMITE_VER` | `findById(@PathVariable Integer)` |
| GET | `/code/{code}` | `TRAMITE_VER` | `findByCode(@PathVariable String)` |
| GET | `/workflow/{workflowId}` | `TRAMITE_LISTAR` | `findByWorkflow(@PathVariable Integer)` |
| POST | `/` | `TRAMITE_CREAR` | `create(@Valid @RequestBody CProcedureRequest)` |
| PUT | `/{id}` | `TRAMITE_MODIFICAR` | `update(@PathVariable Integer, @Valid @RequestBody UProcedureRequest)` |
| POST | `/{id}/activate` | `TRAMITE_ACTIVAR` | `activate(@PathVariable Integer)` |
| POST | `/{id}/deactivate` | `TRAMITE_DESACTIVAR` | `deactivate(@PathVariable Integer)` |
| GET | `/{id}/requires-2fa` | `TRAMITE_VER` | `requires2FA(@PathVariable Integer)` |
| DELETE | `/{id}` | `TRAMITE_ELIMINAR` | `delete(@PathVariable Integer)` |

---

### 9.18 `ProcessingStageController`

**Base:** `/api/v1/processing-stages` · **Seguridad:** `isAuthenticated()`

| HTTP | Path | Permiso | Método |
|---|---|---|---|
| POST | `/` | `ETAPA_CREAR` | `create(@Valid @RequestBody CProcessingStageRequest)` |
| PUT | `/{id}` | `ETAPA_MODIFICAR` | `update(@PathVariable Integer, @Valid @RequestBody UProcessingStageRequest)` |
| DELETE | `/{idprocessingstage}` | `ETAPA_ELIMINAR` | `delete(@PathVariable Integer)` |
| GET | `/` | `ETAPA_LISTAR` | `list()` |

---

### 9.19 `RefreshTokenController`

**Base:** `/api/v1/refresh-tokens` · **Seguridad:** `isAuthenticated()`

| HTTP | Path | Permiso | Método |
|---|---|---|---|
| GET | `/` | `TOKEN_LISTAR` | `findAll()` |
| GET | `/{id}` | `TOKEN_VER` | `findById(@PathVariable Long)` |
| POST | `/` | `TOKEN_CREAR` | `create(@RequestBody RefreshToken)` |
| PUT | `/{id}` | `TOKEN_MODIFICAR` | `update(@PathVariable Long, @RequestBody RefreshToken)` |
| DELETE | `/{id}` | `TOKEN_ELIMINAR` | `delete(@PathVariable Long)` |

---

### 9.20 `RejectReasonController`

**Base:** `/api/v1/reject-reason` · **Seguridad:** `isAuthenticated()`

| HTTP | Path | Permiso | Método |
|---|---|---|---|
| POST | `/` | `RECHAZO_CREAR` | `create(@Valid @RequestBody CRejectionReasonRequest)` |
| PUT | `/{id}` | `RECHAZO_MODIFICAR` | `update(@PathVariable Integer, @Valid @RequestBody URejectionReasonRequest)` |
| DELETE | `/{idrejectionreason}` | `RECHAZO_ELIMINAR` | `delete(@PathVariable Integer)` |
| GET | `/` | `RECHAZO_LISTAR` | `list(@RequestParam(required=false) Boolean onlyActive)` |

---

### 9.21 `RequirementsController`

**Base:** `/api/v1/requirements` · **Seguridad:** `isAuthenticated()`

| HTTP | Path | Permiso | Método |
|---|---|---|---|
| GET | `/` | `REQUISITO_LISTAR` | `findAll()` |
| GET | `/{id}` | `REQUISITO_VER` | `findById(@PathVariable Integer)` |
| POST | `/` | `REQUISITO_CREAR` | `create(@RequestBody RequirementsOfTheProcedure)` |
| PUT | `/{id}` | `REQUISITO_MODIFICAR` | `update(@PathVariable Integer, @RequestBody RequirementsOfTheProcedure)` |
| DELETE | `/{id}` | `REQUISITO_ELIMINAR` | `delete(@PathVariable Integer)` |

---

### 9.22 `RolesController`

**Base:** `/api/v1/roles` · **Seguridad:** `isAuthenticated()`

| HTTP | Path | Permiso | Método |
|---|---|---|---|
| GET | `/legacy` | `ROL_LISTAR` | `findAllLegacy()` |
| GET | `/legacy/{id}` | `ROL_VER` | `findByIdLegacy(@PathVariable Integer)` |
| GET | `/` | `ROL_LISTAR` | `findAll()` |
| GET | `/{id}` | `ROL_VER` | `findById(@PathVariable Integer)` |
| GET | `/name/{roleName}` | `ROL_VER` | `findByName(@PathVariable String)` |
| POST | `/` | `ROL_CREAR` | `create(@Valid @RequestBody CRoleRequest)` |
| PUT | `/{id}` | `ROL_MODIFICAR` | `update(@PathVariable Integer, @Valid @RequestBody URoleRequest)` |
| DELETE | `/{id}` | `ROL_ELIMINAR` | `delete(@PathVariable Integer)` |
| POST | `/{roleId}/permissions` | `ROL_ASIGNAR_PERMISO` | `assignPermissions(@PathVariable Integer, @RequestBody Set<Integer>)` |
| DELETE | `/{roleId}/permissions` | `ROL_REMOVER_PERMISO` | `removePermissions(@PathVariable Integer, @RequestBody Set<Integer>)` |
| POST | `/{roleId}/users/{userId}` | `ROL_ASIGNAR_USUARIO` | `assignRoleToUser(@PathVariable Integer, @PathVariable Integer)` |
| DELETE | `/{roleId}/users/{userId}` | `ROL_REMOVER_USUARIO` | `removeRoleFromUser(@PathVariable Integer, @PathVariable Integer)` |

---

### 9.23 `SessionTokenController`

**Base:** `/api/v1/session-tokens` · **Seguridad:** `isAuthenticated()`

| HTTP | Path | Permiso | Método |
|---|---|---|---|
| GET | `/` | `SESION_LISTAR` | `findAll()` |
| GET | `/{id}` | `SESION_VER` | `findById(@PathVariable Integer)` |
| POST | `/` | `SESION_CREAR` | `create(@RequestBody SessionToken)` |
| PUT | `/{id}` | `SESION_MODIFICAR` | `update(@PathVariable Integer, @RequestBody SessionToken)` |
| DELETE | `/{id}` | `SESION_ELIMINAR` | `delete(@PathVariable Integer)` |

---

### 9.24 `StageTrackingController`

**Base:** `/api/v1/stage-tracking` · **Seguridad:** `isAuthenticated()`

| HTTP | Path | Permiso | Método |
|---|---|---|---|
| GET | `/` | `SEGUIMIENTO_LISTAR` | `findAll()` |
| GET | `/{id}` | `SEGUIMIENTO_VER` | `findById(@PathVariable Integer)` |
| POST | `/` | `SEGUIMIENTO_CREAR` | `create(@RequestBody StageTracking)` |
| PUT | `/{id}` | `SEGUIMIENTO_MODIFICAR` | `update(@PathVariable Integer, @RequestBody StageTracking)` |
| DELETE | `/{id}` | `SEGUIMIENTO_ELIMINAR` | `delete(@PathVariable Integer)` |

---

### 9.25 `StatesController`

**Base:** `/api/v1/states` · **Seguridad:** `isAuthenticated()`

| HTTP | Path | Permiso | Método |
|---|---|---|---|
| POST | `/` | `ESTADO_CREAR` | `create(@Valid @RequestBody CStateRequest)` |
| PUT | `/{id}` | `ESTADO_MODIFICAR` | `update(@PathVariable Integer, @Valid @RequestBody UStateRequest)` |
| DELETE | `/{idstate}` | `ESTADO_ELIMINAR` | `delete(@PathVariable Integer)` |
| GET | `/` | `ESTADO_LISTAR` | `list(@RequestParam(required=false) String category)` |

---

### 9.26 `StudentsController`

**Base:** `/api/v1/students` · **Seguridad:** `isAuthenticated()`

| HTTP | Path | Permiso | Método |
|---|---|---|---|
| GET | `/legacy` | `ESTUDIANTE_LISTAR` | `findAllLegacy()` |
| GET | `/legacy/{id}` | `ESTUDIANTE_VER` | `findByIdLegacy(@PathVariable Integer)` |
| GET | `/` | `ESTUDIANTE_LISTAR` | `findAll()` |
| GET | `/{id}` | `ESTUDIANTE_VER` | `findById(@PathVariable Integer)` |
| GET | `/career/{careerId}` | `ESTUDIANTE_LISTAR` | `findByCareer(@PathVariable Integer)` |
| GET | `/semester/{s}/parallel/{p}` | `ESTUDIANTE_LISTAR` | `findBySemesterAndParallel(@PathVariable, @PathVariable)` |
| GET | `/user/{userId}` | `ESTUDIANTE_VER` | `findByUserId(@PathVariable Integer)` |
| POST | `/` | `ESTUDIANTE_CREAR` | `enrollStudent(@Valid @RequestBody CStudentRequest)` |
| PUT | `/{id}` | `ESTUDIANTE_MODIFICAR` | `updateStudent(@PathVariable Integer, @Valid @RequestBody UStudentRequest)` |
| PATCH | `/{id}/status` | `ESTUDIANTE_MODIFICAR` | `changeStatus(@PathVariable Integer, @RequestParam String)` |
| POST | `/{id}/promote` | `ESTUDIANTE_PROMOVER` | `promoteToNextSemester(@PathVariable Integer)` |
| POST | `/{id}/graduate` | `ESTUDIANTE_GRADUAR` | `graduate(@PathVariable Integer)` |
| POST | `/{id}/withdraw` | `ESTUDIANTE_RETIRAR` | `withdraw(@PathVariable Integer)` |
| POST | `/{id}/reactivate` | `ESTUDIANTE_REACTIVAR` | `reactivate(@PathVariable Integer)` |
| DELETE | `/{id}` | `ESTUDIANTE_ELIMINAR` | `delete(@PathVariable Integer)` |

---

### 9.27 `TwoFactorAuthController`

**Base:** `/api/v1/2fa` · **Seguridad:** Mixta (ver sección 4.3)

Ver sección [4.3](#43-twofactorauthcontrollerjava) para detalles completos.

---

### 9.28 `UsersController`

**Base:** `/api/v1/users` · **Seguridad:** `isAuthenticated()`

| HTTP | Path | Permiso | Método |
|---|---|---|---|
| GET | `/` | `USUARIO_LISTAR` | `findAll()` |
| GET | `/{id}` | `USUARIO_VER` | `findById(@PathVariable Integer)` |
| POST | `/` | `USUARIO_CREAR` | `create(@Valid @RequestBody CUserRequest)` |
| PUT | `/{id}` | `USUARIO_MODIFICAR` | `update(@PathVariable Integer, @Valid @RequestBody UUserRequest)` |
| DELETE | `/{id}` | `USUARIO_ELIMINAR` | `delete(@PathVariable Integer)` |
| PATCH | `/{id}/deactivate` | `USUARIO_DESACTIVAR` | `deactivate(@PathVariable Integer)` |
| PATCH | `/{id}/activate` | `USUARIO_ACTIVAR` | `activate(@PathVariable Integer)` |

---

### 9.29 `WorkFlowsController`

**Base:** `/api/v1/work-flows` · **Seguridad:** `isAuthenticated()`

| HTTP | Path | Permiso | Método |
|---|---|---|---|
| POST | `/` | `FLUJO_CREAR` | `create(@Valid @RequestBody CWorkflowRequest)` |
| PUT | `/{id}` | `FLUJO_MODIFICAR` | `update(@PathVariable Integer, @Valid @RequestBody UWorkflowRequest)` |
| DELETE | `/{idworkflow}` | `FLUJO_ELIMINAR` | `delete(@PathVariable Integer)` |
| GET | `/` | `FLUJO_LISTAR` | `list(@RequestParam(required=false) Boolean onlyActive)` |

---

### 9.30 `WorkflowStagesController`

**Base:** `/api/v1/workflow-stages` · **Seguridad:** `isAuthenticated()`

| HTTP | Path | Permiso | Método |
|---|---|---|---|
| GET | `/` | `FLUJOETAPA_LISTAR` | `findAll()` |
| GET | `/{id}` | `FLUJOETAPA_VER` | `findById(@PathVariable Integer)` |
| POST | `/` | `FLUJOETAPA_CREAR` | `create(@RequestBody WorkflowStages)` |
| PUT | `/{id}` | `FLUJOETAPA_MODIFICAR` | `update(@PathVariable Integer, @RequestBody WorkflowStages)` |
| DELETE | `/{id}` | `FLUJOETAPA_ELIMINAR` | `delete(@PathVariable Integer)` |

---

## 10. DTOs (Data Transfer Objects)

Total: **90 DTOs** en `com.app.uteq.Dtos`. Convención de nombres:

| Prefijo | Significado | Uso |
|---|---|---|
| `C` | Create | DTO para operaciones de creación (POST) |
| `U` | Update | DTO para operaciones de actualización (PUT) |
| `I` | Insert | Alias de Create (usado en AcademicCalendar) |
| `D` | Delete | DTO para operaciones de eliminación |
| — (sin prefijo) | Response | DTO de respuesta |

### 10.1 DTOs de Respuesta (Response) — 29

| DTO | Entidad asociada |
|---|---|
| `AcademicCalendarResponse` | AcademicCalendar |
| `ApplicationResponse` | Applications |
| `ApplicationStageHistoryResponse` | ApplicationStageHistory |
| `AttachedDocumentResponse` | AttachedDocuments |
| `CareersResponse` | Careers |
| `ConfigurationResponse` | Configurations |
| `CredentialResponse` | Credentials |
| `DeadlineRuleResponse` | DeadLinerules |
| `DigitalSignatureResponse` | DigitalSignatures |
| `DocumentGeneratedResponse` | DocumentsGenerated |
| `DocumentTemplateResponse` | DocumentTemplates |
| `FacultyResponse` | Faculties |
| `NotificationResponse` | Notification |
| `NotificationTypeResponse` | NotificationType |
| `PermissionResponse` | Permissions |
| `ProcedureResponse` | Procedures |
| `ProcessingStageResponse` | ProcessingStage |
| `RefreshTokenResponse` | RefreshToken |
| `RejectionReasonResponse` | RejectionReasons |
| `RequirementResponse` | RequirementsOfTheProcedure |
| `RoleResponse` | Roles |
| `SessionTokenResponse` | SessionToken |
| `StageTrackingResponse` | StageTracking |
| `StateResponse` | States |
| `StudentResponse` | Students |
| `TwoFactorAuthResponse` | TwoFactorAuth |
| `TwoFactorSetupResponse` | TwoFactorAuth (setup) |
| `UserResponse` | Users |
| `WorkflowResponse` | Workflows |
| `WorkflowStageResponse` | WorkflowStages |

### 10.2 DTOs de Creación (Create/Insert) — 29

| DTO | Usado en |
|---|---|
| `CApplicationRequest` | `POST /api/v1/applications` |
| `CApplicationStageHistoryRequest` | `POST /api/v1/application-stage-history` |
| `CAttachedDocumentRequest` | `POST /api/v1/attached-documents` |
| `CCareersRequest` | `POST /api/v1/careers` |
| `CConfigurationRequest` | `POST /api/v1/configuration` |
| `CCredentialRequest` | `POST /api/v1/credentials` |
| `CDeadlineRuleRequest` | `POST /api/v1/deadlinerules` |
| `CDigitalSignatureRequest` | `POST /api/v1/digital-signatures` |
| `CDocumentGeneratedRequest` | `POST /api/v1/documents-generated` |
| `CDocumentTemplateRequest` | `POST /api/v1/document-templates` |
| `CFacultyRequest` | `POST /api/v1/faculty` |
| `CNotificationRequest` | `POST /api/v1/notifications` |
| `CNotificationTypeRequest` | `POST /api/v1/notification-types` |
| `CPermissionRequest` | `POST /api/v1/permissions` |
| `CProcedureRequest` | `POST /api/v1/procedures` |
| `CProcessingStageRequest` | `POST /api/v1/processing-stages` |
| `CRefreshTokenRequest` | `POST /api/v1/refresh-tokens` |
| `CRejectionReasonRequest` | `POST /api/v1/reject-reason` |
| `CRequirementRequest` | `POST /api/v1/requirements` |
| `CRoleRequest` | `POST /api/v1/roles` |
| `CSessionTokenRequest` | `POST /api/v1/session-tokens` |
| `CStageTrackingRequest` | `POST /api/v1/stage-tracking` |
| `CStateRequest` | `POST /api/v1/states` |
| `CStudentRequest` | `POST /api/v1/students` |
| `CUserRequest` | `POST /api/v1/users` |
| `CWorkflowRequest` | `POST /api/v1/work-flows` |
| `CWorkflowStageRequest` | `POST /api/v1/workflow-stages` |
| `IAcademicCalendarRequest` | `POST /api/v1/academic-calendar` |
| `DAcademicCalendarRequest` | `DELETE /api/v1/academic-calendar` |

### 10.3 DTOs de Actualización (Update) — 29

| DTO | Usado en |
|---|---|
| `UAcademicCalendarRequest` | `PUT /api/v1/academic-calendar/{id}` |
| `UApplicationRequest` | `PUT /api/v1/applications/{id}` |
| `UApplicationStageHistoryRequest` | `PUT /api/v1/application-stage-history/{id}` |
| `UAttachedDocumentRequest` | `PUT /api/v1/attached-documents/{id}` |
| `UCareersRequest` | `PUT /api/v1/careers/{id}` |
| `UConfigurationRequest` | `PUT /api/v1/configuration/{id}` |
| `UCredentialRequest` | `PUT /api/v1/credentials/{id}` |
| `UDeadlineRuleRequest` | `PUT /api/v1/deadlinerules/{id}` |
| `UDigitalSignatureRequest` | `PUT /api/v1/digital-signatures/{id}` |
| `UDocumentGeneratedRequest` | `PUT /api/v1/documents-generated/{id}` |
| `UDocumentTemplateRequest` | `PUT /api/v1/document-templates/{id}` |
| `UFacultyRequest` | `PUT /api/v1/faculty/{id}` |
| `UNotificationRequest` | `PUT /api/v1/notifications/{id}` |
| `UNotificationTypeRequest` | `PUT /api/v1/notification-types/{id}` |
| `UPermissionRequest` | `PUT /api/v1/permissions/{id}` |
| `UProcedureRequest` | `PUT /api/v1/procedures/{id}` |
| `UProcessingStageRequest` | `PUT /api/v1/processing-stages/{id}` |
| `URefreshTokenRequest` | `PUT /api/v1/refresh-tokens/{id}` |
| `URejectionReasonRequest` | `PUT /api/v1/reject-reason/{id}` |
| `URequirementRequest` | `PUT /api/v1/requirements/{id}` |
| `URoleRequest` | `PUT /api/v1/roles/{id}` |
| `USessionTokenRequest` | `PUT /api/v1/session-tokens/{id}` |
| `UStageTrackingRequest` | `PUT /api/v1/stage-tracking/{id}` |
| `UStateRequest` | `PUT /api/v1/states/{id}` |
| `UStudentRequest` | `PUT /api/v1/students/{id}` |
| `UUserRequest` | `PUT /api/v1/users/{id}` |
| `UWorkflowRequest` | `PUT /api/v1/work-flows/{id}` |
| `UWorkflowStageRequest` | `PUT /api/v1/workflow-stages/{id}` |

### 10.4 DTOs Especiales — 3

| DTO | Uso |
|---|---|
| `ApiErrorResponse` | Respuesta estándar de error del `GlobalExceptionHandler` |
| `TwoFactorVerifyRequest` | Envío de código TOTP para verificación 2FA |
| `TwoFactorBackupRequest` | Envío de código de respaldo para verificación 2FA |

---

## 11. Excepciones y Manejo Global de Errores

### 11.1 Excepciones Personalizadas (5)

| Clase | HTTP Status | Descripción |
|---|---|---|
| `BadRequestException` | 400 Bad Request | Solicitud inválida genérica |
| `BusinessException` | 422 Unprocessable Entity | Error de lógica de negocio (incluye `errorCode`) |
| `DuplicateResourceException` | 409 Conflict | Recurso duplicado (incluye `resourceName`, `fieldName`, `fieldValue`) |
| `ResourceNotFoundException` | 404 Not Found | Recurso no encontrado (incluye `resourceName`, `fieldName`, `fieldValue`) |
| `UnauthorizedException` | 401 Unauthorized | No autorizado |

### 11.2 `GlobalExceptionHandler`

**Ubicación:** `com.app.uteq.Exceptions` · **Anotación:** `@RestControllerAdvice` · **178 líneas, 11 handlers**

Captura todas las excepciones y las transforma en respuestas JSON estandarizadas usando `ApiErrorResponse`.

| Excepción capturada | HTTP | Título del error |
|---|---|---|
| `MethodArgumentNotValidException` | 400 | "Error de Validación" (con errores por campo) |
| `MissingServletRequestParameterException` | 400 | "Parámetro Faltante" |
| `MethodArgumentTypeMismatchException` | 400 | "Tipo Inválido" |
| `ResourceNotFoundException` | 404 | "Recurso No Encontrado" |
| `DuplicateResourceException` | 409 | "Recurso Duplicado" |
| `BadRequestException` | 400 | "Solicitud Inválida" |
| `BusinessException` | 422 | "Error de Negocio" (incluye errorCode) |
| `UnauthorizedException` | 401 | "No Autorizado" |
| `AccessDeniedException` | 403 | "Acceso Denegado" |
| `AuthenticationException` / `BadCredentialsException` | 401 | "Error de Autenticación" |
| `Exception` (fallback) | 500 | "Error Interno" (logueado, mensaje genérico) |

### 11.3 Estructura de `ApiErrorResponse`

```json
{
    "status": 400,
    "error": "Error de Validación",
    "message": "Campo 'email' no puede estar vacío",
    "timestamp": "2026-02-16T10:30:00",
    "path": "/api/v1/users",
    "fieldErrors": {
        "email": "no puede estar vacío",
        "cardId": "debe tener exactamente 10 caracteres"
    }
}
```

---

## 12. Configuración de application.properties

```properties
# === Aplicación ===
spring.application.name=Backend
server.port=8080

# === Base de Datos PostgreSQL ===
spring.datasource.url=jdbc:postgresql://localhost:5432/SGTE_V1
spring.datasource.username=postgres
spring.datasource.password=12345
spring.datasource.driver-class-name=org.postgresql.Driver

# === JPA / Hibernate ===
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# === Logging SQL ===
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.orm.jdbc.bind=TRACE
logging.level.org.hibernate.type.descriptor.sql=TRACE

# === Claves RSA para JWT ===
rsa.public-key=classpath:certs/public.pem
rsa.private-key=classpath:certs/private.pem

# === CORS ===
cors.allowed-origins=http://localhost:4200
```

| Propiedad | Valor | Descripción |
|---|---|---|
| `server.port` | `8080` | Puerto del servidor |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/SGTE_V1` | URL de la base de datos |
| `spring.jpa.hibernate.ddl-auto` | `update` | Auto-genera/actualiza esquema DDL |
| `rsa.public-key` | `classpath:certs/public.pem` | Clave pública RSA para JWT |
| `rsa.private-key` | `classpath:certs/private.pem` | Clave privada RSA para JWT |
| `cors.allowed-origins` | `http://localhost:4200` | Frontend Angular permitido |

---

## 13. Dependencias Maven (pom.xml)

| Dependencia | GroupId | Versión | Scope |
|---|---|---|---|
| Spring Boot Starter Web | `org.springframework.boot` | 3.2.2 | compile |
| Spring Boot Starter Data JPA | `org.springframework.boot` | 3.2.2 | compile |
| Spring Boot Starter Security | `org.springframework.boot` | 3.2.2 | compile |
| Spring Boot Starter OAuth2 Resource Server | `org.springframework.boot` | 3.2.2 | compile |
| Spring Boot Starter Validation | `org.springframework.boot` | 3.2.2 | compile |
| Spring Boot DevTools | `org.springframework.boot` | 3.2.2 | runtime |
| PostgreSQL JDBC Driver | `org.postgresql` | — | runtime |
| Lombok | `org.projectlombok` | — | compile, optional |
| BouncyCastle (bcpkix-jdk18on) | `org.bouncycastle` | 1.83 | compile |
| Google Authenticator (googleauth) | `com.warrenstrange` | 1.5.0 | compile |
| Spring Boot Starter Test | `org.springframework.boot` | 3.2.2 | test |
| Spring Security Test | `org.springframework.security` | — | test |

---

## 14. Diagrama de Relaciones entre Entidades

```
┌──────────────┐     ┌──────────────┐     ┌─────────────────┐
│   Faculties  │◄────│   Careers    │     │ AcademicCalendar│
│  (dean→User) │     │(coord→User)  │     └────────┬────────┘
└──────────────┘     │(faculty→Fac) │              │
                     └──────┬───────┘              │
                            │                      │
                     ┌──────▼───────┐     ┌────────▼────────┐
                     │   Students   │     │   Procedures    │
                     │ (user→Users) │     │ (workflow→WF)   │
                     │(career→Car)  │     │ (calendar→AC)   │
                     └──────────────┘     │ (deadline→DLR)  │
                                          └────────┬────────┘
                                                   │
┌──────────────┐                          ┌────────▼────────┐
│Configurations│◄─────┐                   │  Applications   │
└──────────────┘      │                   │ (procedure→Proc)│
                      │                   │ (user→Users)    │
┌──────────────┐  ┌───▼──────┐            │(stageTrack→ST) │
│  Credentials │◄─│  Users   │◄───────────│(rejectReason→RR)│
└──────┬───────┘  │(config)  │            └───┬──────┬──────┘
       │          │(creds)   │                │      │
       │          │(roles M:M)│               │      │
       │          └───┬──────┘                │      │
       │              │                       │      │
┌──────▼───────┐  ┌───▼──────┐     ┌─────────▼──┐ ┌─▼──────────────┐
│TwoFactorAuth │  │  Roles   │     │ Attached   │ │ Documents      │
│(creds 1:1)   │  │(perms M:M)│    │ Documents  │ │ Generated      │
└──────────────┘  └───┬──────┘     │(app→App)   │ │(app→App)       │
                      │            │(req→ReqProc)│ │(template→DocT) │
                  ┌───▼──────┐     │(user→Users)│ │(user→Users)    │
                  │Permissions│    └────────────┘ │(digSign→DS)    │
                  └──────────┘                    └────────────────┘

┌──────────────┐     ┌──────────────┐     ┌─────────────────┐
│   Workflows  │◄────│WorkflowStages│────►│ ProcessingStage │
└──────────────┘     │(workflow→WF) │     └────────┬────────┘
                     │(stage→PS)    │              │
                     └──────────────┘     ┌────────▼────────┐
                                          │  StageTracking  │
┌──────────────┐                          │ (state→States)  │
│    States    │◄─────────────────────────│ (stage→PS)      │
└──────────────┘                          │ (user→Users)    │
                                          └────────┬────────┘
                                                   │
                                          ┌────────▼──────────────┐
                                          │ApplicationStageHistory│
                                          │ (app→Applications)    │
                                          │ (stageTrack→ST)       │
                                          │ (user→Users)          │
                                          └───────────────────────┘

┌──────────────┐     ┌──────────────┐     ┌─────────────────┐
│ Notification │────►│Notification  │     │ RejectionReasons│
│(type→NotType)│     │   Type       │     └─────────────────┘
│(app→App)     │     └──────────────┘
│(user→Users)  │                          ┌─────────────────┐
└──────────────┘     ┌──────────────┐     │  DeadLinerules  │
                     │ SessionToken │     └─────────────────┘
                     │ (user→Users) │
                     └──────────────┘     ┌─────────────────┐
                                          │DocumentTemplates│
┌──────────────┐     ┌──────────────┐     └─────────────────┘
│ RefreshToken │     │   Digital    │
│ (user→Users) │     │ Signatures  │     ┌─────────────────┐
└──────────────┘     │ (user→Users) │    │ Requirements    │
                     └──────────────┘     │  OfProcedure    │
                                          │(procedure→Proc)│
                                          └─────────────────┘
```

**Tablas intermedias (M:M):**
- `user_roles` → Users ↔ Roles
- `role_permissions` → Roles ↔ Permissions

---

## 15. Catálogo de Permisos

### 15.1 Permisos por Módulo

| Módulo | Prefijo | Permisos |
|---|---|---|
| Calendario Académico | `CAL_` | `CAL_CREAR`, `CAL_MODIFICAR`, `CAL_ELIMINAR`, `CAL_LISTAR` |
| Solicitudes | `SOL_` | `SOL_CREAR`, `SOL_MODIFICAR`, `SOL_ELIMINAR`, `SOL_LISTAR`, `SOL_VER`, `SOL_RESOLVER`, `SOL_RECHAZAR` |
| Historial de Etapas | `HIST_` | `HIST_CREAR`, `HIST_MODIFICAR`, `HIST_ELIMINAR`, `HIST_LISTAR`, `HIST_VER` |
| Documentos Adjuntos | `DOCADJ_` | `DOCADJ_CREAR`, `DOCADJ_MODIFICAR`, `DOCADJ_ELIMINAR`, `DOCADJ_LISTAR`, `DOCADJ_VER` |
| Carreras | `CARRERA_` | `CARRERA_CREAR`, `CARRERA_MODIFICAR`, `CARRERA_ELIMINAR`, `CARRERA_LISTAR` |
| Configuración | `CONFIG_` | `CONFIG_CREAR`, `CONFIG_MODIFICAR`, `CONFIG_ELIMINAR`, `CONFIG_LISTAR` |
| Credenciales | `CRED_` | `CRED_CREAR`, `CRED_ELIMINAR`, `CRED_LISTAR`, `CRED_VER`, `CRED_CAMBIAR_PASS`, `CRED_RESETEAR_PASS`, `CRED_BLOQUEAR`, `CRED_DESBLOQUEAR` |
| Reglas de Plazo | `REGLA_` | `REGLA_CREAR`, `REGLA_MODIFICAR`, `REGLA_ELIMINAR`, `REGLA_LISTAR` |
| Firmas Digitales | `FIRMA_` | `FIRMA_CREAR`, `FIRMA_MODIFICAR`, `FIRMA_ELIMINAR`, `FIRMA_LISTAR`, `FIRMA_VER` |
| Documentos Generados | `DOCGEN_` | `DOCGEN_CREAR`, `DOCGEN_MODIFICAR`, `DOCGEN_ELIMINAR`, `DOCGEN_LISTAR`, `DOCGEN_VER` |
| Plantillas | `PLANTILLA_` | `PLANTILLA_CREAR`, `PLANTILLA_MODIFICAR`, `PLANTILLA_ELIMINAR`, `PLANTILLA_LISTAR` |
| Facultades | `FACULTAD_` | `FACULTAD_CREAR`, `FACULTAD_MODIFICAR`, `FACULTAD_ELIMINAR`, `FACULTAD_LISTAR` |
| Notificaciones | `NOTIF_` | `NOTIF_CREAR`, `NOTIF_MODIFICAR`, `NOTIF_ELIMINAR`, `NOTIF_LISTAR`, `NOTIF_VER` |
| Tipos de Notificación | `TIPNOTIF_` | `TIPNOTIF_CREAR`, `TIPNOTIF_MODIFICAR`, `TIPNOTIF_ELIMINAR`, `TIPNOTIF_LISTAR`, `TIPNOTIF_VER` |
| Permisos | `PERMISO_` | `PERMISO_CREAR`, `PERMISO_MODIFICAR`, `PERMISO_ELIMINAR`, `PERMISO_LISTAR` |
| Trámites | `TRAMITE_` | `TRAMITE_CREAR`, `TRAMITE_MODIFICAR`, `TRAMITE_ELIMINAR`, `TRAMITE_LISTAR`, `TRAMITE_VER`, `TRAMITE_ACTIVAR`, `TRAMITE_DESACTIVAR` |
| Etapas de Procesamiento | `ETAPA_` | `ETAPA_CREAR`, `ETAPA_MODIFICAR`, `ETAPA_ELIMINAR`, `ETAPA_LISTAR` |
| Refresh Tokens | `TOKEN_` | `TOKEN_CREAR`, `TOKEN_MODIFICAR`, `TOKEN_ELIMINAR`, `TOKEN_LISTAR`, `TOKEN_VER` |
| Motivos de Rechazo | `RECHAZO_` | `RECHAZO_CREAR`, `RECHAZO_MODIFICAR`, `RECHAZO_ELIMINAR`, `RECHAZO_LISTAR` |
| Requisitos | `REQUISITO_` | `REQUISITO_CREAR`, `REQUISITO_MODIFICAR`, `REQUISITO_ELIMINAR`, `REQUISITO_LISTAR`, `REQUISITO_VER` |
| Roles | `ROL_` | `ROL_CREAR`, `ROL_MODIFICAR`, `ROL_ELIMINAR`, `ROL_LISTAR`, `ROL_VER`, `ROL_ASIGNAR_PERMISO`, `ROL_REMOVER_PERMISO`, `ROL_ASIGNAR_USUARIO`, `ROL_REMOVER_USUARIO` |
| Sesiones | `SESION_` | `SESION_CREAR`, `SESION_MODIFICAR`, `SESION_ELIMINAR`, `SESION_LISTAR`, `SESION_VER` |
| Seguimiento | `SEGUIMIENTO_` | `SEGUIMIENTO_CREAR`, `SEGUIMIENTO_MODIFICAR`, `SEGUIMIENTO_ELIMINAR`, `SEGUIMIENTO_LISTAR`, `SEGUIMIENTO_VER` |
| Estados | `ESTADO_` | `ESTADO_CREAR`, `ESTADO_MODIFICAR`, `ESTADO_ELIMINAR`, `ESTADO_LISTAR` |
| Estudiantes | `ESTUDIANTE_` | `ESTUDIANTE_CREAR`, `ESTUDIANTE_MODIFICAR`, `ESTUDIANTE_ELIMINAR`, `ESTUDIANTE_LISTAR`, `ESTUDIANTE_VER`, `ESTUDIANTE_PROMOVER`, `ESTUDIANTE_GRADUAR`, `ESTUDIANTE_RETIRAR`, `ESTUDIANTE_REACTIVAR` |
| Auth 2FA | `AUTH2FA_` | `AUTH2FA_CONFIGURAR`, `AUTH2FA_VERIFICAR`, `AUTH2FA_DESACTIVAR`, `AUTH2FA_ESTADO`, `AUTH2FA_REGENERAR` |
| Usuarios | `USUARIO_` | `USUARIO_CREAR`, `USUARIO_MODIFICAR`, `USUARIO_ELIMINAR`, `USUARIO_LISTAR`, `USUARIO_VER`, `USUARIO_DESACTIVAR`, `USUARIO_ACTIVAR` |
| Flujos de Trabajo | `FLUJO_` | `FLUJO_CREAR`, `FLUJO_MODIFICAR`, `FLUJO_ELIMINAR`, `FLUJO_LISTAR` |
| Etapas de Flujo | `FLUJOETAPA_` | `FLUJOETAPA_CREAR`, `FLUJOETAPA_MODIFICAR`, `FLUJOETAPA_ELIMINAR`, `FLUJOETAPA_LISTAR`, `FLUJOETAPA_VER` |

---

## 16. Resumen Estadístico del Proyecto

| Categoría | Cantidad |
|---|---|
| **Entidades JPA** | 29 |
| **Repositorios** | 29 (11 SP + 18 JPA) |
| **Interfaces de Servicio** | 29 |
| **Implementaciones de Servicio** | 30 (29 ServiceImpl + CustomUserDetailsService) |
| **Controladores** | 30 |
| **DTOs** | 90 (29 Response + 29 Create + 29 Update + 3 especiales) |
| **Excepciones personalizadas** | 5 |
| **Clases de Configuración** | 4 (SecurityConfig, RsaKeyConfig, SpResultConverter, StringListConverter) |
| **Endpoints REST totales** | ~168 |
| **Códigos de permiso** | ~140 |
| **Procedimientos almacenados** | 44 (11 × 4: spi, spu, spd, fn_list) + 1 (spu_revoke_all_refresh_tokens) |
| **Tablas intermedias** | 2 (user_roles, role_permissions) |
| **Archivos fuente Java** | ~249 |
| **Roles del sistema** | 4 (ADMIN, STUDENT, COORDINATOR, DEAN) |

### Estructura de Paquetes

```
com.app.uteq/
├── BackendApplication.java          (1 archivo)
├── GenerateKeyPair.java             (1 archivo)
├── Config/                          (4 archivos)
│   ├── RsaKeyConfig.java
│   ├── SecurityConfig.java
│   ├── SpResultConverter.java
│   └── StringListConverter.java
├── Controllers/                     (30 archivos)
├── Dtos/                            (90 archivos)
├── Entity/                          (29 archivos)
├── Exceptions/                      (6 archivos: 5 excepciones + GlobalExceptionHandler)
├── Repository/                      (29 archivos)
└── Services/                        (29 interfaces)
    └── Impl/                        (30 implementaciones)
```

---

> **Fin del Manual Técnico — SGTE Backend v1.0**
