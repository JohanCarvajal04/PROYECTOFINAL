# Procedimientos Almacenados y Funciones PostgreSQL — SGTE Backend

> **Proyecto:** Sistema de Gestión de Trámites Estudiantiles (SGTE)  
> **Base de datos:** PostgreSQL  
> **Fecha:** Febrero 2026  
> **Total:** 33 Stored Procedures + 11 Funciones = **44 objetos de base de datos**

---

## Tabla de Contenidos

1. [Convenciones de Nomenclatura](#1-convenciones-de-nomenclatura)
2. [Resumen General](#2-resumen-general)
3. [Calendario Académico](#3-calendario-académico)
4. [Carreras](#4-carreras)
5. [Configuraciones](#5-configuraciones)
6. [Reglas de Plazos](#6-reglas-de-plazos)
7. [Plantillas de Documentos](#7-plantillas-de-documentos)
8. [Facultades](#8-facultades)
9. [Permisos](#9-permisos)
10. [Etapas de Procesamiento](#10-etapas-de-procesamiento)
11. [Razones de Rechazo](#11-razones-de-rechazo)
12. [Estados](#12-estados)
13. [Flujos de Trabajo](#13-flujos-de-trabajo)
14. [Procedimientos para el Sistema de Permisos](#14-procedimientos-para-el-sistema-de-permisos)
15. [Script Consolidado de Creación](#15-script-consolidado-de-creación)

---

## 1. Convenciones de Nomenclatura

| Prefijo | Tipo | Operación | Ejemplo |
|---------|------|-----------|---------|
| `spi_` | Stored Procedure | **INSERT** (crear registro) | `spi_career` |
| `spu_` | Stored Procedure | **UPDATE** (modificar registro) | `spu_career` |
| `spd_` | Stored Procedure | **DELETE** (eliminar/desactivar) | `spd_career` |
| `fn_list_` | Function | **SELECT** (listar registros) | `fn_list_careers` |

**Reglas:**
- Los procedimientos `spd_` realizan **eliminación lógica** (`active = false`) excepto `spd_permission` y `spd_processingstage` que hacen eliminación física.
- Las funciones `fn_list_` retornan `TABLE` o `SETOF` con columnas tipadas.
- Todos los parámetros usan `snake_case`.

---

## 2. Resumen General

| Módulo | SP Insert | SP Update | SP Delete | Función List | Total |
|--------|-----------|-----------|-----------|--------------|-------|
| Calendario Académico | `spi_academiccalendar` | `spu_academiccalendar` | `spd_academiccalendar` | `fn_list_academiccalendar` | 4 |
| Carreras | `spi_career` | `spu_career` | `spd_career` | `fn_list_careers` | 4 |
| Configuraciones | `spi_configuration` | `spu_configuration` | `spd_configuration` | `fn_list_configurations` | 4 |
| Reglas de Plazos | `spi_deadlinerule` | `spu_deadlinerule` | `spd_deadlinerule` | `fn_list_deadlinerules` | 4 |
| Plantillas de Documentos | `spi_documenttemplate` | `spu_documenttemplate` | `spd_documenttemplate` | `fn_list_documenttemplates` | 4 |
| Facultades | `spi_faculty` | `spu_faculty` | `spd_faculty` | `fn_list_faculties` | 4 |
| Permisos | `spi_permission` | `spu_permission` | `spd_permission` | `fn_list_permissions` | 4 |
| Etapas de Procesamiento | `spi_processingstage` | `spu_processingstage` | `spd_processingstage` | `fn_list_processingstage` | 4 |
| Razones de Rechazo | `spi_rejectionreason` | `spu_rejectionreason` | `spd_rejectionreason` | `fn_list_rejectionreasons` | 4 |
| Estados | `spi_state` | `spu_state` | `spd_state` | `fn_list_states` | 4 |
| Flujos de Trabajo | `spi_workflow` | `spu_workflow` | `spd_workflow` | `fn_list_workflows` | 4 |
| **Total** | **11** | **11** | **11** | **11** | **44** |

---

## 3. Calendario Académico

### 3.1 `spi_academiccalendar` — Crear calendario

```sql
CREATE OR REPLACE PROCEDURE spi_academiccalendar(
    IN p_calendarname VARCHAR(100),
    IN p_academicperiod VARCHAR(50),
    IN p_startdate DATE,
    IN p_enddate DATE,
    IN p_active BOOLEAN
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO academic_calendar (calendarname, academicperiod, startdate, enddate, active, createdat)
    VALUES (p_calendarname, p_academicperiod, p_startdate, p_enddate, p_active, NOW());
END;
$$;
```

**Invocación desde Java:**
```java
@Modifying
@Query(value = "CALL spi_academiccalendar(:calendarname, :academicperiod, :startdate, :enddate, :active)", nativeQuery = true)
void spiCreateCalendar(@Param("calendarname") String calendarname,
                       @Param("academicperiod") String academicperiod,
                       @Param("startdate") LocalDate startdate,
                       @Param("enddate") LocalDate enddate,
                       @Param("active") Boolean active);
```

### 3.2 `spu_academiccalendar` — Actualizar calendario

```sql
CREATE OR REPLACE PROCEDURE spu_academiccalendar(
    IN p_id INTEGER,
    IN p_calendarname VARCHAR(100),
    IN p_academicperiod VARCHAR(50),
    IN p_startdate DATE,
    IN p_enddate DATE,
    IN p_active BOOLEAN
)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE academic_calendar
    SET calendarname = p_calendarname,
        academicperiod = p_academicperiod,
        startdate = p_startdate,
        enddate = p_enddate,
        active = p_active,
        updatedat = NOW()
    WHERE idacademiccalendar = p_id;
END;
$$;
```

### 3.3 `spd_academiccalendar` — Eliminar calendario (lógico)

```sql
CREATE OR REPLACE PROCEDURE spd_academiccalendar(
    IN p_id INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE academic_calendar SET active = false, updatedat = NOW()
    WHERE idacademiccalendar = p_id;
END;
$$;
```

### 3.4 `fn_list_academiccalendar` — Listar calendarios

```sql
CREATE OR REPLACE FUNCTION fn_list_academiccalendar(p_onlyActive BOOLEAN DEFAULT NULL)
RETURNS TABLE (
    idacademiccalendar INTEGER,
    calendarname VARCHAR,
    academicperiod VARCHAR,
    startdate DATE,
    enddate DATE,
    active BOOLEAN,
    createdat TIMESTAMP,
    updatedat TIMESTAMP
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT ac.idacademiccalendar, ac.calendarname, ac.academicperiod,
           ac.startdate, ac.enddate, ac.active, ac.createdat, ac.updatedat
    FROM academic_calendar ac
    WHERE (p_onlyActive IS NULL OR ac.active = p_onlyActive)
    ORDER BY ac.startdate DESC;
END;
$$;
```

---

## 4. Carreras

### 4.1 `spi_career` — Crear carrera

```sql
CREATE OR REPLACE PROCEDURE spi_career(
    IN p_careername VARCHAR(150),
    IN p_careercode VARCHAR(20),
    IN p_facultiesidfaculty INTEGER,
    IN p_coordinatoriduser INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO careers (careername, careercode, facultiesidfaculty, coordinatoriduser, createdat)
    VALUES (p_careername, p_careercode, p_facultiesidfaculty, p_coordinatoriduser, NOW());
END;
$$;
```

### 4.2 `spu_career` — Actualizar carrera

```sql
CREATE OR REPLACE PROCEDURE spu_career(
    IN p_idcareer INTEGER,
    IN p_careername VARCHAR(150),
    IN p_careercode VARCHAR(20),
    IN p_facultiesidfaculty INTEGER,
    IN p_coordinatoriduser INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE careers
    SET careername = p_careername,
        careercode = p_careercode,
        facultiesidfaculty = p_facultiesidfaculty,
        coordinatoriduser = p_coordinatoriduser,
        updatedat = NOW()
    WHERE idcareer = p_idcareer;
END;
$$;
```

### 4.3 `spd_career` — Eliminar carrera (físico)

```sql
CREATE OR REPLACE PROCEDURE spd_career(
    IN p_idcareer INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
    DELETE FROM careers WHERE idcareer = p_idcareer;
END;
$$;
```

### 4.4 `fn_list_careers` — Listar carreras por facultad

```sql
CREATE OR REPLACE FUNCTION fn_list_careers(p_facultyid INTEGER DEFAULT NULL)
RETURNS TABLE (
    idcareer INTEGER,
    careername VARCHAR,
    careercode VARCHAR,
    facultiesidfaculty INTEGER,
    coordinatoriduser INTEGER,
    createdat TIMESTAMP,
    updatedat TIMESTAMP
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT c.idcareer, c.careername, c.careercode,
           c.facultiesidfaculty, c.coordinatoriduser, c.createdat, c.updatedat
    FROM careers c
    WHERE (p_facultyid IS NULL OR c.facultiesidfaculty = p_facultyid)
    ORDER BY c.careername;
END;
$$;
```

---

## 5. Configuraciones

### 5.1 `spi_configuration` — Crear configuración

```sql
CREATE OR REPLACE PROCEDURE spi_configuration(
    IN p_profilepicturepath VARCHAR(500),
    IN p_signaturepath VARCHAR(500),
    IN p_enable_sms BOOLEAN,
    IN p_enable_email BOOLEAN,
    IN p_enable_whatsapp BOOLEAN,
    IN p_notificationfrequency VARCHAR(50)
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO configurations (profilepicturepath, signaturepath, enable_sms, enable_email,
                                enable_whatsapp, notificationfrequency, createdat)
    VALUES (p_profilepicturepath, p_signaturepath, p_enable_sms, p_enable_email,
            p_enable_whatsapp, p_notificationfrequency, NOW());
END;
$$;
```

### 5.2 `spu_configuration` — Actualizar configuración

```sql
CREATE OR REPLACE PROCEDURE spu_configuration(
    IN p_idconfiguration INTEGER,
    IN p_profilepicturepath VARCHAR(500),
    IN p_signaturepath VARCHAR(500),
    IN p_enable_sms BOOLEAN,
    IN p_enable_email BOOLEAN,
    IN p_enable_whatsapp BOOLEAN,
    IN p_notificationfrequency VARCHAR(50)
)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE configurations
    SET profilepicturepath = p_profilepicturepath,
        signaturepath = p_signaturepath,
        enable_sms = p_enable_sms,
        enable_email = p_enable_email,
        enable_whatsapp = p_enable_whatsapp,
        notificationfrequency = p_notificationfrequency,
        updatedat = NOW()
    WHERE idconfiguration = p_idconfiguration;
END;
$$;
```

### 5.3 `spd_configuration` — Eliminar configuración (físico)

```sql
CREATE OR REPLACE PROCEDURE spd_configuration(
    IN p_idconfiguration INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
    DELETE FROM configurations WHERE idconfiguration = p_idconfiguration;
END;
$$;
```

### 5.4 `fn_list_configurations` — Listar configuraciones

```sql
CREATE OR REPLACE FUNCTION fn_list_configurations()
RETURNS TABLE (
    idconfiguration INTEGER,
    profilepicturepath VARCHAR,
    signaturepath VARCHAR,
    enable_sms BOOLEAN,
    enable_email BOOLEAN,
    enable_whatsapp BOOLEAN,
    notificationfrequency VARCHAR,
    createdat TIMESTAMP,
    updatedat TIMESTAMP
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT c.idconfiguration, c.profilepicturepath, c.signaturepath,
           c.enable_sms, c.enable_email, c.enable_whatsapp,
           c.notificationfrequency, c.createdat, c.updatedat
    FROM configurations c
    ORDER BY c.idconfiguration;
END;
$$;
```

---

## 6. Reglas de Plazos

### 6.1 `spi_deadlinerule` — Crear regla de plazo

```sql
CREATE OR REPLACE PROCEDURE spi_deadlinerule(
    IN p_rulename VARCHAR(100),
    IN p_procedurecategory VARCHAR(50),
    IN p_basedeadlinedays INTEGER,
    IN p_warningdaysbefore INTEGER,
    IN p_active BOOLEAN
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO deadlinerules (rulename, procedurecategory, basedeadlinedays,
                                warningdaysbefore, active, createdat)
    VALUES (p_rulename, p_procedurecategory, p_basedeadlinedays,
            p_warningdaysbefore, p_active, NOW());
END;
$$;
```

### 6.2 `spu_deadlinerule` — Actualizar regla de plazo

```sql
CREATE OR REPLACE PROCEDURE spu_deadlinerule(
    IN p_id INTEGER,
    IN p_rulename VARCHAR(100),
    IN p_procedurecategory VARCHAR(50),
    IN p_basedeadlinedays INTEGER,
    IN p_warningdaysbefore INTEGER,
    IN p_active BOOLEAN
)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE deadlinerules
    SET rulename = p_rulename,
        procedurecategory = p_procedurecategory,
        basedeadlinedays = p_basedeadlinedays,
        warningdaysbefore = p_warningdaysbefore,
        active = p_active,
        updatedat = NOW()
    WHERE iddeadlinerule = p_id;
END;
$$;
```

### 6.3 `spd_deadlinerule` — Eliminar regla (lógico)

```sql
CREATE OR REPLACE PROCEDURE spd_deadlinerule(
    IN p_id INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE deadlinerules SET active = false, updatedat = NOW()
    WHERE iddeadlinerule = p_id;
END;
$$;
```

### 6.4 `fn_list_deadlinerules` — Listar reglas

```sql
CREATE OR REPLACE FUNCTION fn_list_deadlinerules(p_onlyActive BOOLEAN DEFAULT NULL)
RETURNS TABLE (
    iddeadlinerule INTEGER,
    rulename VARCHAR,
    procedurecategory VARCHAR,
    basedeadlinedays INTEGER,
    warningdaysbefore INTEGER,
    active BOOLEAN,
    createdat TIMESTAMP,
    updatedat TIMESTAMP
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT d.iddeadlinerule, d.rulename, d.procedurecategory,
           d.basedeadlinedays, d.warningdaysbefore, d.active, d.createdat, d.updatedat
    FROM deadlinerules d
    WHERE (p_onlyActive IS NULL OR d.active = p_onlyActive)
    ORDER BY d.rulename;
END;
$$;
```

---

## 7. Plantillas de Documentos

### 7.1 `spi_documenttemplate` — Crear plantilla

```sql
CREATE OR REPLACE PROCEDURE spi_documenttemplate(
    IN p_templatename VARCHAR(150),
    IN p_templatecode VARCHAR(30),
    IN p_templatepath VARCHAR(500),
    IN p_documenttype VARCHAR(50),
    IN p_version VARCHAR(20),
    IN p_requiressignature BOOLEAN,
    IN p_active BOOLEAN
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO documenttemplates (templatename, templatecode, templatepath,
                                    documenttype, version, requiressignature, active, createdat)
    VALUES (p_templatename, p_templatecode, p_templatepath,
            p_documenttype, p_version, p_requiressignature, p_active, NOW());
END;
$$;
```

### 7.2 `spu_documenttemplate` — Actualizar plantilla

```sql
CREATE OR REPLACE PROCEDURE spu_documenttemplate(
    IN p_idtemplate INTEGER,
    IN p_templatename VARCHAR(150),
    IN p_templatecode VARCHAR(30),
    IN p_templatepath VARCHAR(500),
    IN p_documenttype VARCHAR(50),
    IN p_version VARCHAR(20),
    IN p_requiressignature BOOLEAN,
    IN p_active BOOLEAN
)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE documenttemplates
    SET templatename = p_templatename,
        templatecode = p_templatecode,
        templatepath = p_templatepath,
        documenttype = p_documenttype,
        version = p_version,
        requiressignature = p_requiressignature,
        active = p_active,
        updatedat = NOW()
    WHERE idtemplate = p_idtemplate;
END;
$$;
```

### 7.3 `spd_documenttemplate` — Eliminar plantilla (lógico)

```sql
CREATE OR REPLACE PROCEDURE spd_documenttemplate(
    IN p_idtemplate INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE documenttemplates SET active = false, updatedat = NOW()
    WHERE idtemplate = p_idtemplate;
END;
$$;
```

### 7.4 `fn_list_documenttemplates` — Listar plantillas

```sql
CREATE OR REPLACE FUNCTION fn_list_documenttemplates(p_onlyActive BOOLEAN DEFAULT NULL)
RETURNS TABLE (
    idtemplate INTEGER,
    templatename VARCHAR,
    templatecode VARCHAR,
    templatepath VARCHAR,
    documenttype VARCHAR,
    version VARCHAR,
    requiressignature BOOLEAN,
    active BOOLEAN,
    createdat TIMESTAMP,
    updatedat TIMESTAMP
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT dt.idtemplate, dt.templatename, dt.templatecode, dt.templatepath,
           dt.documenttype, dt.version, dt.requiressignature, dt.active,
           dt.createdat, dt.updatedat
    FROM documenttemplates dt
    WHERE (p_onlyActive IS NULL OR dt.active = p_onlyActive)
    ORDER BY dt.templatename;
END;
$$;
```

---

## 8. Facultades

### 8.1 `spi_faculty` — Crear facultad

```sql
CREATE OR REPLACE PROCEDURE spi_faculty(
    IN p_facultyname VARCHAR(150),
    IN p_facultycode VARCHAR(20),
    IN p_deaniduser INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO faculties (facultyname, facultycode, deaniduser, createdat)
    VALUES (p_facultyname, p_facultycode, p_deaniduser, NOW());
END;
$$;
```

### 8.2 `spu_faculty` — Actualizar facultad

```sql
CREATE OR REPLACE PROCEDURE spu_faculty(
    IN p_idfaculty INTEGER,
    IN p_facultyname VARCHAR(150),
    IN p_facultycode VARCHAR(20),
    IN p_deaniduser INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE faculties
    SET facultyname = p_facultyname,
        facultycode = p_facultycode,
        deaniduser = p_deaniduser,
        updatedat = NOW()
    WHERE idfaculty = p_idfaculty;
END;
$$;
```

### 8.3 `spd_faculty` — Eliminar facultad (físico)

```sql
CREATE OR REPLACE PROCEDURE spd_faculty(
    IN p_idfaculty INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
    DELETE FROM faculties WHERE idfaculty = p_idfaculty;
END;
$$;
```

### 8.4 `fn_list_faculties` — Listar facultades

```sql
CREATE OR REPLACE FUNCTION fn_list_faculties()
RETURNS TABLE (
    idfaculty INTEGER,
    facultyname VARCHAR,
    facultycode VARCHAR,
    deaniduser INTEGER,
    createdat TIMESTAMP,
    updatedat TIMESTAMP
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT f.idfaculty, f.facultyname, f.facultycode,
           f.deaniduser, f.createdat, f.updatedat
    FROM faculties f
    ORDER BY f.facultyname;
END;
$$;
```

---

## 9. Permisos

### 9.1 `spi_permission` — Crear permiso

```sql
CREATE OR REPLACE PROCEDURE spi_permission(
    IN p_code VARCHAR(50),
    IN p_description VARCHAR(200)
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO permissions (code, description, createdat)
    VALUES (p_code, p_description, NOW());
END;
$$;
```

### 9.2 `spu_permission` — Actualizar permiso

```sql
CREATE OR REPLACE PROCEDURE spu_permission(
    IN p_idpermission INTEGER,
    IN p_code VARCHAR(50),
    IN p_description VARCHAR(200)
)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE permissions
    SET code = p_code,
        description = p_description,
        updatedat = NOW()
    WHERE idpermission = p_idpermission;
END;
$$;
```

### 9.3 `spd_permission` — Eliminar permiso (físico)

```sql
CREATE OR REPLACE PROCEDURE spd_permission(
    IN p_idpermission INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
    DELETE FROM permissions WHERE idpermission = p_idpermission;
END;
$$;
```

### 9.4 `fn_list_permissions` — Listar permisos

```sql
CREATE OR REPLACE FUNCTION fn_list_permissions()
RETURNS TABLE (
    idpermission INTEGER,
    code VARCHAR,
    description VARCHAR,
    createdat TIMESTAMP,
    updatedat TIMESTAMP
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT p.idpermission, p.code, p.description, p.createdat, p.updatedat
    FROM permissions p
    ORDER BY p.code;
END;
$$;
```

---

## 10. Etapas de Procesamiento

### 10.1 `spi_processingstage` — Crear etapa

```sql
CREATE OR REPLACE PROCEDURE spi_processingstage(
    IN p_stagename VARCHAR(100),
    IN p_stagecode VARCHAR(30),
    IN p_stagedescription TEXT,
    IN p_stageorder INTEGER,
    IN p_requiresapproval BOOLEAN,
    IN p_maxdurationdays INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO processingstages (stagename, stagecode, stagedescription,
                                   stageorder, requiresapproval, maxdurationdays, createdat)
    VALUES (p_stagename, p_stagecode, p_stagedescription,
            p_stageorder, p_requiresapproval, p_maxdurationdays, NOW());
END;
$$;
```

### 10.2 `spu_processingstage` — Actualizar etapa

```sql
CREATE OR REPLACE PROCEDURE spu_processingstage(
    IN p_idprocessingstage INTEGER,
    IN p_stagename VARCHAR(100),
    IN p_stagecode VARCHAR(30),
    IN p_stagedescription TEXT,
    IN p_stageorder INTEGER,
    IN p_requiresapproval BOOLEAN,
    IN p_maxdurationdays INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE processingstages
    SET stagename = p_stagename,
        stagecode = p_stagecode,
        stagedescription = p_stagedescription,
        stageorder = p_stageorder,
        requiresapproval = p_requiresapproval,
        maxdurationdays = p_maxdurationdays,
        updatedat = NOW()
    WHERE idprocessingstage = p_idprocessingstage;
END;
$$;
```

### 10.3 `spd_processingstage` — Eliminar etapa (físico)

```sql
CREATE OR REPLACE PROCEDURE spd_processingstage(
    IN p_idprocessingstage INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
    DELETE FROM processingstages WHERE idprocessingstage = p_idprocessingstage;
END;
$$;
```

### 10.4 `fn_list_processingstage` — Listar etapas

```sql
CREATE OR REPLACE FUNCTION fn_list_processingstage()
RETURNS TABLE (
    idprocessingstage INTEGER,
    stagename VARCHAR,
    stagecode VARCHAR,
    stagedescription TEXT,
    stageorder INTEGER,
    requiresapproval BOOLEAN,
    maxdurationdays INTEGER,
    createdat TIMESTAMP,
    updatedat TIMESTAMP
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT ps.idprocessingstage, ps.stagename, ps.stagecode, ps.stagedescription,
           ps.stageorder, ps.requiresapproval, ps.maxdurationdays,
           ps.createdat, ps.updatedat
    FROM processingstages ps
    ORDER BY ps.stageorder;
END;
$$;
```

---

## 11. Razones de Rechazo

### 11.1 `spi_rejectionreason` — Crear razón de rechazo

```sql
CREATE OR REPLACE PROCEDURE spi_rejectionreason(
    IN p_reasoncode VARCHAR(30),
    IN p_reasondescription TEXT,
    IN p_category VARCHAR(50),
    IN p_active BOOLEAN
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO rejectionreasons (reasoncode, reasondescription, category, active, createdat)
    VALUES (p_reasoncode, p_reasondescription, p_category, p_active, NOW());
END;
$$;
```

### 11.2 `spu_rejectionreason` — Actualizar razón de rechazo

```sql
CREATE OR REPLACE PROCEDURE spu_rejectionreason(
    IN p_idrejectionreason INTEGER,
    IN p_reasoncode VARCHAR(30),
    IN p_reasondescription TEXT,
    IN p_category VARCHAR(50),
    IN p_active BOOLEAN
)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE rejectionreasons
    SET reasoncode = p_reasoncode,
        reasondescription = p_reasondescription,
        category = p_category,
        active = p_active,
        updatedat = NOW()
    WHERE idrejectionreason = p_idrejectionreason;
END;
$$;
```

### 11.3 `spd_rejectionreason` — Eliminar razón (lógico)

```sql
CREATE OR REPLACE PROCEDURE spd_rejectionreason(
    IN p_idrejectionreason INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE rejectionreasons SET active = false, updatedat = NOW()
    WHERE idrejectionreason = p_idrejectionreason;
END;
$$;
```

### 11.4 `fn_list_rejectionreasons` — Listar razones

```sql
CREATE OR REPLACE FUNCTION fn_list_rejectionreasons(p_onlyActive BOOLEAN DEFAULT NULL)
RETURNS TABLE (
    idrejectionreason INTEGER,
    reasoncode VARCHAR,
    reasondescription TEXT,
    category VARCHAR,
    active BOOLEAN,
    createdat TIMESTAMP,
    updatedat TIMESTAMP
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT rr.idrejectionreason, rr.reasoncode, rr.reasondescription,
           rr.category, rr.active, rr.createdat, rr.updatedat
    FROM rejectionreasons rr
    WHERE (p_onlyActive IS NULL OR rr.active = p_onlyActive)
    ORDER BY rr.reasoncode;
END;
$$;
```

---

## 12. Estados

### 12.1 `spi_state` — Crear estado

```sql
CREATE OR REPLACE PROCEDURE spi_state(
    IN p_statename VARCHAR(50),
    IN p_statedescription TEXT,
    IN p_statecategory VARCHAR(50)
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO states (statename, statedescription, statecategory, createdat)
    VALUES (p_statename, p_statedescription, p_statecategory, NOW());
END;
$$;
```

### 12.2 `spu_state` — Actualizar estado

```sql
CREATE OR REPLACE PROCEDURE spu_state(
    IN p_idstate INTEGER,
    IN p_statename VARCHAR(50),
    IN p_statedescription TEXT,
    IN p_statecategory VARCHAR(50)
)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE states
    SET statename = p_statename,
        statedescription = p_statedescription,
        statecategory = p_statecategory,
        updatedat = NOW()
    WHERE idstate = p_idstate;
END;
$$;
```

### 12.3 `spd_state` — Eliminar estado (físico)

```sql
CREATE OR REPLACE PROCEDURE spd_state(
    IN p_idstate INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
    DELETE FROM states WHERE idstate = p_idstate;
END;
$$;
```

### 12.4 `fn_list_states` — Listar estados

```sql
CREATE OR REPLACE FUNCTION fn_list_states(p_category VARCHAR DEFAULT NULL)
RETURNS TABLE (
    idstate INTEGER,
    statename VARCHAR,
    statedescription TEXT,
    statecategory VARCHAR,
    createdat TIMESTAMP,
    updatedat TIMESTAMP
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT s.idstate, s.statename, s.statedescription,
           s.statecategory, s.createdat, s.updatedat
    FROM states s
    WHERE (p_category IS NULL OR s.statecategory = p_category)
    ORDER BY s.statecategory, s.statename;
END;
$$;
```

---

## 13. Flujos de Trabajo

### 13.1 `spi_workflow` — Crear flujo

```sql
CREATE OR REPLACE PROCEDURE spi_workflow(
    IN p_workflowname VARCHAR(100),
    IN p_workflowdescription TEXT,
    IN p_active BOOLEAN
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO workflows (workflowname, workflowdescription, active, createdat)
    VALUES (p_workflowname, p_workflowdescription, p_active, NOW());
END;
$$;
```

### 13.2 `spu_workflow` — Actualizar flujo

```sql
CREATE OR REPLACE PROCEDURE spu_workflow(
    IN p_idworkflow INTEGER,
    IN p_workflowname VARCHAR(100),
    IN p_workflowdescription TEXT,
    IN p_active BOOLEAN
)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE workflows
    SET workflowname = p_workflowname,
        workflowdescription = p_workflowdescription,
        active = p_active,
        updatedat = NOW()
    WHERE idworkflow = p_idworkflow;
END;
$$;
```

### 13.3 `spd_workflow` — Eliminar flujo (lógico)

```sql
CREATE OR REPLACE PROCEDURE spd_workflow(
    IN p_idworkflow INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE workflows SET active = false, updatedat = NOW()
    WHERE idworkflow = p_idworkflow;
END;
$$;
```

### 13.4 `fn_list_workflows` — Listar flujos

```sql
CREATE OR REPLACE FUNCTION fn_list_workflows(p_onlyActive BOOLEAN DEFAULT NULL)
RETURNS TABLE (
    idworkflow INTEGER,
    workflowname VARCHAR,
    workflowdescription TEXT,
    active BOOLEAN,
    createdat TIMESTAMP,
    updatedat TIMESTAMP
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT w.idworkflow, w.workflowname, w.workflowdescription,
           w.active, w.createdat, w.updatedat
    FROM workflows w
    WHERE (p_onlyActive IS NULL OR w.active = p_onlyActive)
    ORDER BY w.workflowname;
END;
$$;
```

---

## 14. Procedimientos para el Sistema de Permisos

Estos procedimientos adicionales son necesarios para gestionar la asignación de permisos a roles y la inserción masiva de los códigos de permisos utilizados en los controladores del backend.

### 14.1 `sp_seed_permissions` — Insertar todos los permisos del sistema

```sql
CREATE OR REPLACE PROCEDURE sp_seed_permissions()
LANGUAGE plpgsql
AS $$
BEGIN
    -- ═══════════════════════════════════════════
    -- CALENDARIO ACADÉMICO
    -- ═══════════════════════════════════════════
    INSERT INTO permissions (code, description, createdat) VALUES
    ('CAL_CREAR', 'Crear calendario académico', NOW()),
    ('CAL_MODIFICAR', 'Modificar calendario académico', NOW()),
    ('CAL_ELIMINAR', 'Eliminar calendario académico', NOW()),
    ('CAL_LISTAR', 'Listar calendarios académicos', NOW())
    ON CONFLICT (code) DO NOTHING;

    -- ═══════════════════════════════════════════
    -- SOLICITUDES / APPLICATIONS
    -- ═══════════════════════════════════════════
    INSERT INTO permissions (code, description, createdat) VALUES
    ('SOL_LISTAR', 'Listar solicitudes', NOW()),
    ('SOL_VER', 'Ver detalle de solicitud', NOW()),
    ('SOL_CREAR', 'Crear solicitud', NOW()),
    ('SOL_MODIFICAR', 'Modificar solicitud', NOW()),
    ('SOL_ELIMINAR', 'Eliminar solicitud', NOW()),
    ('SOL_RESOLVER', 'Resolver solicitud', NOW()),
    ('SOL_RECHAZAR', 'Rechazar solicitud', NOW())
    ON CONFLICT (code) DO NOTHING;

    -- ═══════════════════════════════════════════
    -- HISTORIAL DE ETAPAS
    -- ═══════════════════════════════════════════
    INSERT INTO permissions (code, description, createdat) VALUES
    ('HIST_LISTAR', 'Listar historial de etapas', NOW()),
    ('HIST_VER', 'Ver detalle de historial', NOW()),
    ('HIST_CREAR', 'Crear registro de historial', NOW()),
    ('HIST_MODIFICAR', 'Modificar historial', NOW()),
    ('HIST_ELIMINAR', 'Eliminar historial', NOW())
    ON CONFLICT (code) DO NOTHING;

    -- ═══════════════════════════════════════════
    -- DOCUMENTOS ADJUNTOS
    -- ═══════════════════════════════════════════
    INSERT INTO permissions (code, description, createdat) VALUES
    ('DOCADJ_LISTAR', 'Listar documentos adjuntos', NOW()),
    ('DOCADJ_VER', 'Ver documento adjunto', NOW()),
    ('DOCADJ_CREAR', 'Crear documento adjunto', NOW()),
    ('DOCADJ_MODIFICAR', 'Modificar documento adjunto', NOW()),
    ('DOCADJ_ELIMINAR', 'Eliminar documento adjunto', NOW())
    ON CONFLICT (code) DO NOTHING;

    -- ═══════════════════════════════════════════
    -- CARRERAS
    -- ═══════════════════════════════════════════
    INSERT INTO permissions (code, description, createdat) VALUES
    ('CARRERA_CREAR', 'Crear carrera', NOW()),
    ('CARRERA_MODIFICAR', 'Modificar carrera', NOW()),
    ('CARRERA_ELIMINAR', 'Eliminar carrera', NOW()),
    ('CARRERA_LISTAR', 'Listar carreras', NOW())
    ON CONFLICT (code) DO NOTHING;

    -- ═══════════════════════════════════════════
    -- CONFIGURACIÓN
    -- ═══════════════════════════════════════════
    INSERT INTO permissions (code, description, createdat) VALUES
    ('CONFIG_CREAR', 'Crear configuración', NOW()),
    ('CONFIG_MODIFICAR', 'Modificar configuración', NOW()),
    ('CONFIG_ELIMINAR', 'Eliminar configuración', NOW()),
    ('CONFIG_LISTAR', 'Listar configuraciones', NOW())
    ON CONFLICT (code) DO NOTHING;

    -- ═══════════════════════════════════════════
    -- CREDENCIALES
    -- ═══════════════════════════════════════════
    INSERT INTO permissions (code, description, createdat) VALUES
    ('CRED_LISTAR', 'Listar credenciales', NOW()),
    ('CRED_VER', 'Ver credencial', NOW()),
    ('CRED_CREAR', 'Crear credencial', NOW()),
    ('CRED_CAMBIAR_PASS', 'Cambiar contraseña', NOW()),
    ('CRED_RESETEAR_PASS', 'Resetear contraseña', NOW()),
    ('CRED_BLOQUEAR', 'Bloquear cuenta', NOW()),
    ('CRED_DESBLOQUEAR', 'Desbloquear cuenta', NOW()),
    ('CRED_ELIMINAR', 'Eliminar credencial', NOW())
    ON CONFLICT (code) DO NOTHING;

    -- ═══════════════════════════════════════════
    -- REGLAS DE PLAZOS
    -- ═══════════════════════════════════════════
    INSERT INTO permissions (code, description, createdat) VALUES
    ('REGLA_CREAR', 'Crear regla de plazo', NOW()),
    ('REGLA_MODIFICAR', 'Modificar regla de plazo', NOW()),
    ('REGLA_ELIMINAR', 'Eliminar regla de plazo', NOW()),
    ('REGLA_LISTAR', 'Listar reglas de plazos', NOW())
    ON CONFLICT (code) DO NOTHING;

    -- ═══════════════════════════════════════════
    -- FIRMAS DIGITALES
    -- ═══════════════════════════════════════════
    INSERT INTO permissions (code, description, createdat) VALUES
    ('FIRMA_LISTAR', 'Listar firmas digitales', NOW()),
    ('FIRMA_VER', 'Ver firma digital', NOW()),
    ('FIRMA_CREAR', 'Crear firma digital', NOW()),
    ('FIRMA_MODIFICAR', 'Modificar firma digital', NOW()),
    ('FIRMA_ELIMINAR', 'Eliminar firma digital', NOW())
    ON CONFLICT (code) DO NOTHING;

    -- ═══════════════════════════════════════════
    -- DOCUMENTOS GENERADOS
    -- ═══════════════════════════════════════════
    INSERT INTO permissions (code, description, createdat) VALUES
    ('DOCGEN_LISTAR', 'Listar documentos generados', NOW()),
    ('DOCGEN_VER', 'Ver documento generado', NOW()),
    ('DOCGEN_CREAR', 'Crear documento generado', NOW()),
    ('DOCGEN_MODIFICAR', 'Modificar documento generado', NOW()),
    ('DOCGEN_ELIMINAR', 'Eliminar documento generado', NOW())
    ON CONFLICT (code) DO NOTHING;

    -- ═══════════════════════════════════════════
    -- PLANTILLAS DE DOCUMENTOS
    -- ═══════════════════════════════════════════
    INSERT INTO permissions (code, description, createdat) VALUES
    ('PLANTILLA_CREAR', 'Crear plantilla de documento', NOW()),
    ('PLANTILLA_MODIFICAR', 'Modificar plantilla de documento', NOW()),
    ('PLANTILLA_ELIMINAR', 'Eliminar plantilla de documento', NOW()),
    ('PLANTILLA_LISTAR', 'Listar plantillas de documentos', NOW())
    ON CONFLICT (code) DO NOTHING;

    -- ═══════════════════════════════════════════
    -- ESTUDIANTES
    -- ═══════════════════════════════════════════
    INSERT INTO permissions (code, description, createdat) VALUES
    ('ESTUDIANTE_LISTAR', 'Listar estudiantes', NOW()),
    ('ESTUDIANTE_VER', 'Ver detalle de estudiante', NOW()),
    ('ESTUDIANTE_CREAR', 'Matricular estudiante', NOW()),
    ('ESTUDIANTE_MODIFICAR', 'Modificar datos de estudiante', NOW()),
    ('ESTUDIANTE_PROMOVER', 'Promover estudiante de semestre', NOW()),
    ('ESTUDIANTE_GRADUAR', 'Graduar estudiante', NOW()),
    ('ESTUDIANTE_RETIRAR', 'Retirar estudiante', NOW()),
    ('ESTUDIANTE_REACTIVAR', 'Reactivar estudiante', NOW()),
    ('ESTUDIANTE_ELIMINAR', 'Eliminar estudiante', NOW())
    ON CONFLICT (code) DO NOTHING;

    -- ═══════════════════════════════════════════
    -- FACULTADES
    -- ═══════════════════════════════════════════
    INSERT INTO permissions (code, description, createdat) VALUES
    ('FACULTAD_CREAR', 'Crear facultad', NOW()),
    ('FACULTAD_MODIFICAR', 'Modificar facultad', NOW()),
    ('FACULTAD_ELIMINAR', 'Eliminar facultad', NOW()),
    ('FACULTAD_LISTAR', 'Listar facultades', NOW())
    ON CONFLICT (code) DO NOTHING;

    -- ═══════════════════════════════════════════
    -- NOTIFICACIONES
    -- ═══════════════════════════════════════════
    INSERT INTO permissions (code, description, createdat) VALUES
    ('NOTIF_LISTAR', 'Listar notificaciones', NOW()),
    ('NOTIF_VER', 'Ver notificación', NOW()),
    ('NOTIF_CREAR', 'Crear notificación', NOW()),
    ('NOTIF_MODIFICAR', 'Modificar notificación', NOW()),
    ('NOTIF_ELIMINAR', 'Eliminar notificación', NOW())
    ON CONFLICT (code) DO NOTHING;

    -- ═══════════════════════════════════════════
    -- TIPOS DE NOTIFICACIÓN
    -- ═══════════════════════════════════════════
    INSERT INTO permissions (code, description, createdat) VALUES
    ('TIPNOTIF_LISTAR', 'Listar tipos de notificación', NOW()),
    ('TIPNOTIF_VER', 'Ver tipo de notificación', NOW()),
    ('TIPNOTIF_CREAR', 'Crear tipo de notificación', NOW()),
    ('TIPNOTIF_MODIFICAR', 'Modificar tipo de notificación', NOW()),
    ('TIPNOTIF_ELIMINAR', 'Eliminar tipo de notificación', NOW())
    ON CONFLICT (code) DO NOTHING;

    -- ═══════════════════════════════════════════
    -- PERMISOS
    -- ═══════════════════════════════════════════
    INSERT INTO permissions (code, description, createdat) VALUES
    ('PERMISO_CREAR', 'Crear permiso', NOW()),
    ('PERMISO_MODIFICAR', 'Modificar permiso', NOW()),
    ('PERMISO_ELIMINAR', 'Eliminar permiso', NOW()),
    ('PERMISO_LISTAR', 'Listar permisos', NOW())
    ON CONFLICT (code) DO NOTHING;

    -- ═══════════════════════════════════════════
    -- TRÁMITES
    -- ═══════════════════════════════════════════
    INSERT INTO permissions (code, description, createdat) VALUES
    ('TRAMITE_LISTAR', 'Listar trámites', NOW()),
    ('TRAMITE_VER', 'Ver detalle de trámite', NOW()),
    ('TRAMITE_CREAR', 'Crear trámite', NOW()),
    ('TRAMITE_MODIFICAR', 'Modificar trámite', NOW()),
    ('TRAMITE_ELIMINAR', 'Eliminar trámite', NOW()),
    ('TRAMITE_ACTIVAR', 'Activar trámite', NOW()),
    ('TRAMITE_DESACTIVAR', 'Desactivar trámite', NOW())
    ON CONFLICT (code) DO NOTHING;

    -- ═══════════════════════════════════════════
    -- ETAPAS DE PROCESAMIENTO
    -- ═══════════════════════════════════════════
    INSERT INTO permissions (code, description, createdat) VALUES
    ('ETAPA_CREAR', 'Crear etapa de procesamiento', NOW()),
    ('ETAPA_MODIFICAR', 'Modificar etapa de procesamiento', NOW()),
    ('ETAPA_ELIMINAR', 'Eliminar etapa de procesamiento', NOW()),
    ('ETAPA_LISTAR', 'Listar etapas de procesamiento', NOW())
    ON CONFLICT (code) DO NOTHING;

    -- ═══════════════════════════════════════════
    -- RAZONES DE RECHAZO
    -- ═══════════════════════════════════════════
    INSERT INTO permissions (code, description, createdat) VALUES
    ('RECHAZO_CREAR', 'Crear razón de rechazo', NOW()),
    ('RECHAZO_MODIFICAR', 'Modificar razón de rechazo', NOW()),
    ('RECHAZO_ELIMINAR', 'Eliminar razón de rechazo', NOW()),
    ('RECHAZO_LISTAR', 'Listar razones de rechazo', NOW())
    ON CONFLICT (code) DO NOTHING;

    -- ═══════════════════════════════════════════
    -- TOKENS DE REFRESCO
    -- ═══════════════════════════════════════════
    INSERT INTO permissions (code, description, createdat) VALUES
    ('TOKEN_LISTAR', 'Listar tokens de refresco', NOW()),
    ('TOKEN_VER', 'Ver token de refresco', NOW()),
    ('TOKEN_CREAR', 'Crear token de refresco', NOW()),
    ('TOKEN_MODIFICAR', 'Modificar token de refresco', NOW()),
    ('TOKEN_ELIMINAR', 'Eliminar token de refresco', NOW())
    ON CONFLICT (code) DO NOTHING;

    -- ═══════════════════════════════════════════
    -- REQUISITOS
    -- ═══════════════════════════════════════════
    INSERT INTO permissions (code, description, createdat) VALUES
    ('REQUISITO_LISTAR', 'Listar requisitos', NOW()),
    ('REQUISITO_VER', 'Ver requisito', NOW()),
    ('REQUISITO_CREAR', 'Crear requisito', NOW()),
    ('REQUISITO_MODIFICAR', 'Modificar requisito', NOW()),
    ('REQUISITO_ELIMINAR', 'Eliminar requisito', NOW())
    ON CONFLICT (code) DO NOTHING;

    -- ═══════════════════════════════════════════
    -- ROLES
    -- ═══════════════════════════════════════════
    INSERT INTO permissions (code, description, createdat) VALUES
    ('ROL_LISTAR', 'Listar roles', NOW()),
    ('ROL_VER', 'Ver detalle de rol', NOW()),
    ('ROL_CREAR', 'Crear rol', NOW()),
    ('ROL_MODIFICAR', 'Modificar rol', NOW()),
    ('ROL_ELIMINAR', 'Eliminar rol', NOW()),
    ('ROL_ASIGNAR_PERMISO', 'Asignar permisos a un rol', NOW()),
    ('ROL_REMOVER_PERMISO', 'Remover permisos de un rol', NOW()),
    ('ROL_ASIGNAR_USUARIO', 'Asignar rol a usuario', NOW()),
    ('ROL_REMOVER_USUARIO', 'Remover rol de usuario', NOW())
    ON CONFLICT (code) DO NOTHING;

    -- ═══════════════════════════════════════════
    -- SESIONES
    -- ═══════════════════════════════════════════
    INSERT INTO permissions (code, description, createdat) VALUES
    ('SESION_LISTAR', 'Listar sesiones', NOW()),
    ('SESION_VER', 'Ver sesión', NOW()),
    ('SESION_CREAR', 'Crear sesión', NOW()),
    ('SESION_MODIFICAR', 'Modificar sesión', NOW()),
    ('SESION_ELIMINAR', 'Eliminar sesión', NOW())
    ON CONFLICT (code) DO NOTHING;

    -- ═══════════════════════════════════════════
    -- SEGUIMIENTO DE ETAPAS
    -- ═══════════════════════════════════════════
    INSERT INTO permissions (code, description, createdat) VALUES
    ('SEGUIMIENTO_LISTAR', 'Listar seguimiento de etapas', NOW()),
    ('SEGUIMIENTO_VER', 'Ver seguimiento', NOW()),
    ('SEGUIMIENTO_CREAR', 'Crear seguimiento', NOW()),
    ('SEGUIMIENTO_MODIFICAR', 'Modificar seguimiento', NOW()),
    ('SEGUIMIENTO_ELIMINAR', 'Eliminar seguimiento', NOW())
    ON CONFLICT (code) DO NOTHING;

    -- ═══════════════════════════════════════════
    -- ESTADOS
    -- ═══════════════════════════════════════════
    INSERT INTO permissions (code, description, createdat) VALUES
    ('ESTADO_CREAR', 'Crear estado', NOW()),
    ('ESTADO_MODIFICAR', 'Modificar estado', NOW()),
    ('ESTADO_ELIMINAR', 'Eliminar estado', NOW()),
    ('ESTADO_LISTAR', 'Listar estados', NOW())
    ON CONFLICT (code) DO NOTHING;

    -- ═══════════════════════════════════════════
    -- USUARIOS
    -- ═══════════════════════════════════════════
    INSERT INTO permissions (code, description, createdat) VALUES
    ('USUARIO_LISTAR', 'Listar usuarios', NOW()),
    ('USUARIO_VER', 'Ver detalle de usuario', NOW()),
    ('USUARIO_CREAR', 'Crear usuario', NOW()),
    ('USUARIO_MODIFICAR', 'Modificar usuario', NOW()),
    ('USUARIO_ELIMINAR', 'Eliminar usuario', NOW()),
    ('USUARIO_ACTIVAR', 'Activar usuario', NOW()),
    ('USUARIO_DESACTIVAR', 'Desactivar usuario', NOW())
    ON CONFLICT (code) DO NOTHING;

    -- ═══════════════════════════════════════════
    -- FLUJOS DE TRABAJO
    -- ═══════════════════════════════════════════
    INSERT INTO permissions (code, description, createdat) VALUES
    ('FLUJO_CREAR', 'Crear flujo de trabajo', NOW()),
    ('FLUJO_MODIFICAR', 'Modificar flujo de trabajo', NOW()),
    ('FLUJO_ELIMINAR', 'Eliminar flujo de trabajo', NOW()),
    ('FLUJO_LISTAR', 'Listar flujos de trabajo', NOW())
    ON CONFLICT (code) DO NOTHING;

    -- ═══════════════════════════════════════════
    -- ETAPAS DE FLUJO
    -- ═══════════════════════════════════════════
    INSERT INTO permissions (code, description, createdat) VALUES
    ('FLUJOETAPA_LISTAR', 'Listar etapas de flujo', NOW()),
    ('FLUJOETAPA_VER', 'Ver etapa de flujo', NOW()),
    ('FLUJOETAPA_CREAR', 'Crear etapa de flujo', NOW()),
    ('FLUJOETAPA_MODIFICAR', 'Modificar etapa de flujo', NOW()),
    ('FLUJOETAPA_ELIMINAR', 'Eliminar etapa de flujo', NOW())
    ON CONFLICT (code) DO NOTHING;

    -- ═══════════════════════════════════════════
    -- 2FA
    -- ═══════════════════════════════════════════
    INSERT INTO permissions (code, description, createdat) VALUES
    ('AUTH2FA_CONFIGURAR', 'Configurar autenticación 2FA', NOW()),
    ('AUTH2FA_VERIFICAR', 'Verificar código 2FA', NOW()),
    ('AUTH2FA_DESACTIVAR', 'Desactivar 2FA', NOW()),
    ('AUTH2FA_ESTADO', 'Consultar estado 2FA', NOW()),
    ('AUTH2FA_REGENERAR', 'Regenerar códigos de respaldo 2FA', NOW())
    ON CONFLICT (code) DO NOTHING;

    RAISE NOTICE 'Todos los permisos han sido insertados correctamente.';
END;
$$;

-- Ejecutar:
-- CALL sp_seed_permissions();
```

### 14.2 `sp_assign_admin_all_permissions` — Asignar todos los permisos al rol ADMIN

```sql
CREATE OR REPLACE PROCEDURE sp_assign_admin_all_permissions()
LANGUAGE plpgsql
AS $$
DECLARE
    v_admin_role_id INTEGER;
    v_perm RECORD;
BEGIN
    -- Obtener ID del rol ADMIN
    SELECT idrole INTO v_admin_role_id FROM roles WHERE rolename = 'ROLE_ADMIN';
    
    IF v_admin_role_id IS NULL THEN
        RAISE EXCEPTION 'El rol ROLE_ADMIN no existe';
    END IF;
    
    -- Insertar todos los permisos para ADMIN
    FOR v_perm IN SELECT idpermission FROM permissions LOOP
        INSERT INTO roles_permissions (roles_idrole, permissions_idpermission)
        VALUES (v_admin_role_id, v_perm.idpermission)
        ON CONFLICT DO NOTHING;
    END LOOP;
    
    RAISE NOTICE 'Todos los permisos asignados al rol ADMIN (id=%)', v_admin_role_id;
END;
$$;

-- Ejecutar:
-- CALL sp_assign_admin_all_permissions();
```

### 14.3 `sp_assign_role_permissions` — Asignar permisos específicos a un rol

```sql
CREATE OR REPLACE PROCEDURE sp_assign_role_permissions(
    IN p_rolename VARCHAR(50),
    IN p_permission_codes TEXT[]
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_role_id INTEGER;
    v_perm_code TEXT;
    v_perm_id INTEGER;
BEGIN
    SELECT idrole INTO v_role_id FROM roles WHERE rolename = p_rolename;
    
    IF v_role_id IS NULL THEN
        RAISE EXCEPTION 'El rol % no existe', p_rolename;
    END IF;
    
    FOREACH v_perm_code IN ARRAY p_permission_codes LOOP
        SELECT idpermission INTO v_perm_id FROM permissions WHERE code = v_perm_code;
        IF v_perm_id IS NOT NULL THEN
            INSERT INTO roles_permissions (roles_idrole, permissions_idpermission)
            VALUES (v_role_id, v_perm_id)
            ON CONFLICT DO NOTHING;
        ELSE
            RAISE WARNING 'Permiso % no encontrado, se omitió', v_perm_code;
        END IF;
    END LOOP;
    
    RAISE NOTICE 'Permisos asignados al rol %', p_rolename;
END;
$$;

-- Ejemplo de uso:
-- CALL sp_assign_role_permissions('ROLE_COORDINATOR', ARRAY[
--     'SOL_LISTAR', 'SOL_VER', 'SOL_CREAR', 'SOL_MODIFICAR',
--     'ESTUDIANTE_LISTAR', 'ESTUDIANTE_VER', 'ESTUDIANTE_CREAR',
--     'CARRERA_LISTAR', 'CARRERA_MODIFICAR'
-- ]);
```

### 14.4 `sp_seed_default_role_permissions` — Configuración inicial por roles

```sql
CREATE OR REPLACE PROCEDURE sp_seed_default_role_permissions()
LANGUAGE plpgsql
AS $$
BEGIN
    -- ═══════════════════════════════════════════
    -- ADMIN: Todos los permisos
    -- ═══════════════════════════════════════════
    CALL sp_assign_admin_all_permissions();

    -- ═══════════════════════════════════════════
    -- COORDINATOR: Gestión académica y trámites
    -- ═══════════════════════════════════════════
    CALL sp_assign_role_permissions('ROLE_COORDINATOR', ARRAY[
        'CAL_LISTAR',
        'SOL_LISTAR', 'SOL_VER', 'SOL_CREAR', 'SOL_MODIFICAR', 'SOL_RESOLVER', 'SOL_RECHAZAR',
        'HIST_LISTAR', 'HIST_VER', 'HIST_CREAR',
        'DOCADJ_LISTAR', 'DOCADJ_VER', 'DOCADJ_CREAR',
        'CARRERA_LISTAR', 'CARRERA_MODIFICAR',
        'CRED_CAMBIAR_PASS',
        'FIRMA_LISTAR', 'FIRMA_VER', 'FIRMA_CREAR',
        'DOCGEN_LISTAR', 'DOCGEN_VER', 'DOCGEN_CREAR',
        'PLANTILLA_LISTAR',
        'ESTUDIANTE_LISTAR', 'ESTUDIANTE_VER', 'ESTUDIANTE_CREAR', 'ESTUDIANTE_MODIFICAR',
        'ESTUDIANTE_PROMOVER', 'ESTUDIANTE_RETIRAR', 'ESTUDIANTE_REACTIVAR',
        'FACULTAD_LISTAR',
        'NOTIF_LISTAR', 'NOTIF_VER',
        'TRAMITE_LISTAR', 'TRAMITE_VER',
        'ETAPA_LISTAR',
        'RECHAZO_LISTAR',
        'REQUISITO_LISTAR', 'REQUISITO_VER',
        'SEGUIMIENTO_LISTAR', 'SEGUIMIENTO_VER', 'SEGUIMIENTO_CREAR',
        'ESTADO_LISTAR',
        'FLUJO_LISTAR',
        'FLUJOETAPA_LISTAR', 'FLUJOETAPA_VER',
        'AUTH2FA_CONFIGURAR', 'AUTH2FA_VERIFICAR', 'AUTH2FA_DESACTIVAR', 'AUTH2FA_ESTADO', 'AUTH2FA_REGENERAR'
    ]);

    -- ═══════════════════════════════════════════
    -- DEAN: Supervisión y aprobaciones
    -- ═══════════════════════════════════════════
    CALL sp_assign_role_permissions('ROLE_DEAN', ARRAY[
        'CAL_LISTAR',
        'SOL_LISTAR', 'SOL_VER', 'SOL_RESOLVER', 'SOL_RECHAZAR',
        'HIST_LISTAR', 'HIST_VER',
        'DOCADJ_LISTAR', 'DOCADJ_VER',
        'CARRERA_LISTAR',
        'CRED_CAMBIAR_PASS',
        'FIRMA_LISTAR', 'FIRMA_VER', 'FIRMA_CREAR',
        'DOCGEN_LISTAR', 'DOCGEN_VER',
        'PLANTILLA_LISTAR',
        'ESTUDIANTE_LISTAR', 'ESTUDIANTE_VER', 'ESTUDIANTE_GRADUAR', 'ESTUDIANTE_REACTIVAR',
        'FACULTAD_LISTAR',
        'NOTIF_LISTAR', 'NOTIF_VER',
        'TRAMITE_LISTAR', 'TRAMITE_VER',
        'ETAPA_LISTAR',
        'RECHAZO_LISTAR',
        'REQUISITO_LISTAR', 'REQUISITO_VER',
        'SEGUIMIENTO_LISTAR', 'SEGUIMIENTO_VER',
        'ESTADO_LISTAR',
        'FLUJO_LISTAR',
        'FLUJOETAPA_LISTAR', 'FLUJOETAPA_VER',
        'AUTH2FA_CONFIGURAR', 'AUTH2FA_VERIFICAR', 'AUTH2FA_DESACTIVAR', 'AUTH2FA_ESTADO', 'AUTH2FA_REGENERAR'
    ]);

    -- ═══════════════════════════════════════════
    -- STUDENT: Acceso limitado a sus propios datos
    -- ═══════════════════════════════════════════
    CALL sp_assign_role_permissions('ROLE_STUDENT', ARRAY[
        'CAL_LISTAR',
        'SOL_LISTAR', 'SOL_VER', 'SOL_CREAR',
        'HIST_LISTAR', 'HIST_VER',
        'DOCADJ_LISTAR', 'DOCADJ_VER', 'DOCADJ_CREAR',
        'CARRERA_LISTAR',
        'CRED_CAMBIAR_PASS',
        'DOCGEN_LISTAR', 'DOCGEN_VER',
        'PLANTILLA_LISTAR',
        'ESTUDIANTE_VER',
        'FACULTAD_LISTAR',
        'NOTIF_LISTAR', 'NOTIF_VER',
        'TRAMITE_LISTAR', 'TRAMITE_VER',
        'ETAPA_LISTAR',
        'REQUISITO_LISTAR', 'REQUISITO_VER',
        'SEGUIMIENTO_LISTAR', 'SEGUIMIENTO_VER',
        'ESTADO_LISTAR',
        'FLUJO_LISTAR',
        'FLUJOETAPA_LISTAR', 'FLUJOETAPA_VER',
        'AUTH2FA_CONFIGURAR', 'AUTH2FA_VERIFICAR', 'AUTH2FA_DESACTIVAR', 'AUTH2FA_ESTADO', 'AUTH2FA_REGENERAR'
    ]);

    RAISE NOTICE 'Permisos por defecto asignados a todos los roles.';
END;
$$;

-- Ejecutar en orden:
-- CALL sp_seed_permissions();
-- CALL sp_seed_default_role_permissions();
```

---

## 15. Script Consolidado de Creación

Para ejecutar todos los procedimientos en la base de datos PostgreSQL, copiar y ejecutar en orden:

```sql
-- ═══════════════════════════════════════════════════════════════
-- PASO 1: Crear todos los procedimientos CRUD (secciones 3-13)
-- ═══════════════════════════════════════════════════════════════
-- Ejecutar cada CREATE OR REPLACE PROCEDURE / FUNCTION de las secciones 3 a 13

-- ═══════════════════════════════════════════════════════════════
-- PASO 2: Crear procedimientos del sistema de permisos (sección 14)
-- ═══════════════════════════════════════════════════════════════
-- Ejecutar sección 14.1, 14.2, 14.3, 14.4

-- ═══════════════════════════════════════════════════════════════
-- PASO 3: Poblar datos iniciales
-- ═══════════════════════════════════════════════════════════════
CALL sp_seed_permissions();
CALL sp_seed_default_role_permissions();

-- ═══════════════════════════════════════════════════════════════
-- VERIFICACIÓN
-- ═══════════════════════════════════════════════════════════════
SELECT COUNT(*) AS total_permisos FROM permissions;
SELECT r.rolename, COUNT(p.idpermission) AS num_permisos
FROM roles r
LEFT JOIN roles_permissions rp ON r.idrole = rp.roles_idrole
LEFT JOIN permissions p ON rp.permissions_idpermission = p.idpermission
GROUP BY r.rolename
ORDER BY r.rolename;
```

---

> **Nota:** Los nombres de tablas y columnas en este documento están basados en el modelo de entidades del backend Java (anotaciones `@Table` y `@Column`). Verifique que coincidan con su esquema real de PostgreSQL antes de ejecutar.
