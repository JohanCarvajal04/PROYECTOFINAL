--
-- PostgreSQL database dump
--

-- Dumped from database version 17.2
-- Dumped by pg_dump version 17.2

-- Started on 2026-02-18 23:34:45

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

DROP DATABASE bdgte;
--
-- TOC entry 5397 (class 1262 OID 33886)
-- Name: bdgte; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE bdgte WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'Spanish_Spain.1252';


ALTER DATABASE bdgte OWNER TO postgres;

\connect bdgte

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 299 (class 1255 OID 35125)
-- Name: fn_find_refresh_token(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.fn_find_refresh_token(p_token text) RETURNS TABLE(id bigint, token text, expires_at timestamp without time zone, revoked boolean, user_id integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY SELECT rt.id, rt.token, rt.expires_at, rt.revoked, rt.user_id
    FROM public.refresh_tokens rt
    WHERE rt.token = p_token;
END; $$;


ALTER FUNCTION public.fn_find_refresh_token(p_token text) OWNER TO postgres;

--
-- TOC entry 345 (class 1255 OID 35122)
-- Name: fn_get_user_authorities(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.fn_get_user_authorities(p_iduser integer) RETURNS TABLE(authority character varying)
    LANGUAGE plpgsql
    AS $$
BEGIN
    -- Roles
    RETURN QUERY
    SELECT ('ROLE_' || r.rolename)::varchar
    FROM public.roles r
    JOIN public.user_roles ur ON r.idrole = ur.idrole
    WHERE ur.iduser = p_iduser AND r.active = true;

    -- Permisos (A través de Roles)
    RETURN QUERY
    SELECT p.code::varchar
    FROM public.permissions p
    JOIN public.role_permissions rp ON p.idpermission = rp.idpermission
    JOIN public.user_roles ur ON rp.idrole = ur.idrole
    WHERE ur.iduser = p_iduser;
END; $$;


ALTER FUNCTION public.fn_get_user_authorities(p_iduser integer) OWNER TO postgres;

--
-- TOC entry 343 (class 1255 OID 35120)
-- Name: fn_get_user_for_login(character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.fn_get_user_for_login(p_email character varying) RETURNS TABLE(iduser integer, email character varying, passwordhash character varying, idcredential integer, active boolean, accountlocked boolean)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY
    SELECT u.iduser, u.institutionalemail, c.passwordhash, c.idcredentials, u.active, c.accountlocked
    FROM public.users u
    JOIN public.credentials c ON u.credentialsidcredentials = c.idcredentials
    WHERE u.institutionalemail = p_email AND u.active = true;
END; $$;


ALTER FUNCTION public.fn_get_user_for_login(p_email character varying) OWNER TO postgres;

--
-- TOC entry 278 (class 1255 OID 33887)
-- Name: fn_list_academiccalendar(boolean); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.fn_list_academiccalendar(p_onlyactive boolean DEFAULT NULL::boolean) RETURNS TABLE(idacademiccalendar integer, calendarname character varying, academicperiod character varying, startdate date, enddate date, active boolean, createdat timestamp without time zone, updatedat timestamp without time zone)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY SELECT ac.idacademiccalendar, ac.calendarname, ac.academicperiod, ac.startdate, ac.enddate, ac.active, ac.createdat, ac.updatedat
    FROM academic_calendar ac WHERE (p_onlyActive IS NULL OR ac.active = p_onlyActive) ORDER BY ac.startdate DESC;
END; $$;


ALTER FUNCTION public.fn_list_academiccalendar(p_onlyactive boolean) OWNER TO postgres;

--
-- TOC entry 279 (class 1255 OID 33888)
-- Name: fn_list_careers(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.fn_list_careers(p_facultyid integer DEFAULT NULL::integer) RETURNS TABLE(idcareer integer, careername character varying, careercode character varying, facultiesidfaculty integer, coordinatoriduser integer, createdat timestamp without time zone, updatedat timestamp without time zone)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY SELECT c.idcareer, c.careername, c.careercode, c.facultiesidfaculty, c.coordinatoriduser, c.createdat, c.updatedat
    FROM careers c WHERE (p_facultyid IS NULL OR c.facultiesidfaculty = p_facultyid) ORDER BY c.careername;
END; $$;


ALTER FUNCTION public.fn_list_careers(p_facultyid integer) OWNER TO postgres;

--
-- TOC entry 280 (class 1255 OID 33889)
-- Name: fn_list_configurations(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.fn_list_configurations() RETURNS TABLE(idconfiguration integer, profilepicturepath character varying, signaturepath character varying, enable_sms boolean, enable_email boolean, enable_whatsapp boolean, notificationfrequency character varying, createdat timestamp without time zone, updatedat timestamp without time zone)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY SELECT c.idconfiguration, c.profilepicturepath, c.signaturepath, c.enable_sms, c.enable_email, c.enable_whatsapp, c.notificationfrequency, c.createdat, c.updatedat
    FROM configurations c ORDER BY c.idconfiguration;
END; $$;


ALTER FUNCTION public.fn_list_configurations() OWNER TO postgres;

--
-- TOC entry 289 (class 1255 OID 33890)
-- Name: fn_list_deadlinerules(boolean); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.fn_list_deadlinerules(p_onlyactive boolean DEFAULT NULL::boolean) RETURNS TABLE(iddeadlinerule integer, rulename character varying, procedurecategory character varying, basedeadlinedays integer, warningdaysbefore integer, active boolean, createdat timestamp without time zone, updatedat timestamp without time zone)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY SELECT d.iddeadlinerule, d.rulename, d.procedurecategory, d.basedeadlinedays, d.warningdaysbefore, d.active, d.createdat, d.updatedat
    FROM deadlinerules d WHERE (p_onlyActive IS NULL OR d.active = p_onlyActive) ORDER BY d.rulename;
END; $$;


ALTER FUNCTION public.fn_list_deadlinerules(p_onlyactive boolean) OWNER TO postgres;

--
-- TOC entry 291 (class 1255 OID 33891)
-- Name: fn_list_documenttemplates(boolean); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.fn_list_documenttemplates(p_onlyactive boolean DEFAULT NULL::boolean) RETURNS TABLE(idtemplate integer, templatename character varying, templatecode character varying, templatepath character varying, documenttype character varying, version character varying, requiressignature boolean, active boolean, createdat timestamp without time zone, updatedat timestamp without time zone)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY SELECT dt.idtemplate, dt.templatename, dt.templatecode, dt.templatepath, dt.documenttype, dt.version, dt.requiressignature, dt.active, dt.createdat, dt.updatedat
    FROM documenttemplates dt WHERE (p_onlyActive IS NULL OR dt.active = p_onlyActive) ORDER BY dt.templatename;
END; $$;


ALTER FUNCTION public.fn_list_documenttemplates(p_onlyactive boolean) OWNER TO postgres;

--
-- TOC entry 292 (class 1255 OID 33892)
-- Name: fn_list_faculties(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.fn_list_faculties() RETURNS TABLE(idfaculty integer, facultyname character varying, facultycode character varying, deaniduser integer, createdat timestamp without time zone, updatedat timestamp without time zone)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY SELECT f.idfaculty, f.facultyname, f.facultycode, f.deaniduser, f.createdat, f.updatedat
    FROM faculties f ORDER BY f.facultyname;
END; $$;


ALTER FUNCTION public.fn_list_faculties() OWNER TO postgres;

--
-- TOC entry 293 (class 1255 OID 33893)
-- Name: fn_list_permissions(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.fn_list_permissions() RETURNS TABLE(idpermission integer, code character varying, description character varying, createdat timestamp without time zone, updatedat timestamp without time zone)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY SELECT p.idpermission, p.code, p.description, p.createdat, p.updatedat FROM permissions p ORDER BY p.code;
END; $$;


ALTER FUNCTION public.fn_list_permissions() OWNER TO postgres;

--
-- TOC entry 335 (class 1255 OID 35102)
-- Name: fn_list_processingstage(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.fn_list_processingstage() RETURNS TABLE(idprocessingstage integer, stagename character varying, stagecode character varying, stagedescription character varying, stageorder integer, requiresapproval boolean, maxdurationdays integer, createdat timestamp without time zone, updatedat timestamp without time zone)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY SELECT ps.idprocessingstage, ps.stagename, ps.stagecode, ps.stagedescription, ps.stageorder, ps.requiresapproval, ps.maxdurationdays, ps.createdat, ps.updatedat
    FROM processingstage ps ORDER BY ps.stageorder;
END; $$;


ALTER FUNCTION public.fn_list_processingstage() OWNER TO postgres;

--
-- TOC entry 297 (class 1255 OID 33895)
-- Name: fn_list_rejectionreasons(boolean); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.fn_list_rejectionreasons(p_onlyactive boolean DEFAULT NULL::boolean) RETURNS TABLE(idrejectionreason integer, reasoncode character varying, reasondescription text, category character varying, active boolean, createdat timestamp without time zone, updatedat timestamp without time zone)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY SELECT rr.idrejectionreason, rr.reasoncode, rr.reasondescription, rr.category, rr.active, rr.createdat, rr.updatedat
    FROM rejectionreasons rr WHERE (p_onlyActive IS NULL OR rr.active = p_onlyActive) ORDER BY rr.reasoncode;
END; $$;


ALTER FUNCTION public.fn_list_rejectionreasons(p_onlyactive boolean) OWNER TO postgres;

--
-- TOC entry 313 (class 1255 OID 33896)
-- Name: fn_list_states(character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.fn_list_states(p_category character varying DEFAULT NULL::character varying) RETURNS TABLE(idstate integer, statename character varying, statedescription text, statecategory character varying, createdat timestamp without time zone, updatedat timestamp without time zone)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY SELECT s.idstate, s.statename, s.statedescription, s.statecategory, s.createdat, s.updatedat
    FROM states s WHERE (p_category IS NULL OR s.statecategory = p_category) ORDER BY s.statecategory, s.statename;
END; $$;


ALTER FUNCTION public.fn_list_states(p_category character varying) OWNER TO postgres;

--
-- TOC entry 341 (class 1255 OID 35118)
-- Name: fn_list_users(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.fn_list_users() RETURNS TABLE(iduser integer, names character varying, surnames character varying, email character varying, active boolean)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY 
    SELECT u.iduser, u.names, u.surnames, u.institutionalemail, u.active 
    FROM public.users u
    ORDER BY u.surnames;
END; $$;


ALTER FUNCTION public.fn_list_users() OWNER TO postgres;

--
-- TOC entry 314 (class 1255 OID 33897)
-- Name: fn_list_workflows(boolean); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.fn_list_workflows(p_onlyactive boolean DEFAULT NULL::boolean) RETURNS TABLE(idworkflow integer, workflowname character varying, workflowdescription text, active boolean, createdat timestamp without time zone, updatedat timestamp without time zone)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY SELECT w.idworkflow, w.workflowname, w.workflowdescription, w.active, w.createdat, w.updatedat
    FROM workflows w WHERE (p_onlyActive IS NULL OR w.active = p_onlyActive) ORDER BY w.workflowname;
END; $$;


ALTER FUNCTION public.fn_list_workflows(p_onlyactive boolean) OWNER TO postgres;

--
-- TOC entry 315 (class 1255 OID 33898)
-- Name: sp_assign_admin_all_permissions(); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.sp_assign_admin_all_permissions()
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_admin_role_id INTEGER;
    v_perm RECORD;
BEGIN
    SELECT idrole INTO v_admin_role_id FROM roles WHERE rolename = 'ROLE_ADMIN';
    IF v_admin_role_id IS NOT NULL THEN
        FOR v_perm IN SELECT idpermission FROM permissions LOOP
            INSERT INTO roles_permissions (roles_idrole, permissions_idpermission) VALUES (v_admin_role_id, v_perm.idpermission) ON CONFLICT DO NOTHING;
        END LOOP;
        RAISE NOTICE 'Admin sincronizado.';
    END IF;
END; $$;


ALTER PROCEDURE public.sp_assign_admin_all_permissions() OWNER TO postgres;

--
-- TOC entry 316 (class 1255 OID 33899)
-- Name: sp_assign_role_permissions(character varying, text[]); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.sp_assign_role_permissions(IN p_rolename character varying, IN p_permission_codes text[])
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_role_id INTEGER;
    v_perm_code TEXT;
    v_perm_id INTEGER;
BEGIN
    SELECT idrole INTO v_role_id FROM roles WHERE rolename = p_rolename;
    IF v_role_id IS NOT NULL THEN
        FOREACH v_perm_code IN ARRAY p_permission_codes LOOP
            SELECT idpermission INTO v_perm_id FROM permissions WHERE code = v_perm_code;
            IF v_perm_id IS NOT NULL THEN
                INSERT INTO roles_permissions (roles_idrole, permissions_idpermission) VALUES (v_role_id, v_perm_id) ON CONFLICT DO NOTHING;
            END IF;
        END LOOP;
    END IF;
END; $$;


ALTER PROCEDURE public.sp_assign_role_permissions(IN p_rolename character varying, IN p_permission_codes text[]) OWNER TO postgres;

--
-- TOC entry 317 (class 1255 OID 33900)
-- Name: sp_seed_default_role_permissions(); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.sp_seed_default_role_permissions()
    LANGUAGE plpgsql
    AS $$
BEGIN
    CALL sp_assign_admin_all_permissions();
    CALL sp_assign_role_permissions('ROLE_STUDENT', ARRAY['SOL_CREAR', 'SOL_VER', 'CAL_LISTAR', 'AUTH2FA_ESTADO']);
    RAISE NOTICE 'Roles configurados.';
END; $$;


ALTER PROCEDURE public.sp_seed_default_role_permissions() OWNER TO postgres;

--
-- TOC entry 318 (class 1255 OID 33901)
-- Name: sp_seed_permissions(); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.sp_seed_permissions()
    LANGUAGE plpgsql
    AS $$
BEGIN
    INSERT INTO permissions (code, description, createdat) VALUES
    ('CAL_CREAR', 'Crear calendario académico', NOW()), ('CAL_MODIFICAR', 'Modificar calendario académico', NOW()), ('CAL_ELIMINAR', 'Eliminar calendario académico', NOW()), ('CAL_LISTAR', 'Listar calendarios académicos', NOW()),
    ('SOL_LISTAR', 'Listar solicitudes', NOW()), ('SOL_VER', 'Ver detalle de solicitud', NOW()), ('SOL_CREAR', 'Crear solicitud', NOW()), ('SOL_MODIFICAR', 'Modificar solicitud', NOW()), ('SOL_RESOLVER', 'Resolver solicitud', NOW()),
    ('CARRERA_CREAR', 'Crear carrera', NOW()), ('CARRERA_MODIFICAR', 'Modificar carrera', NOW()), ('CARRERA_ELIMINAR', 'Eliminar carrera', NOW()), ('CARRERA_LISTAR', 'Listar carreras', NOW()),
    ('ESTUDIANTE_LISTAR', 'Listar estudiantes', NOW()), ('ESTUDIANTE_VER', 'Ver detalle de estudiante', NOW()), ('ESTUDIANTE_CREAR', 'Matricular estudiante', NOW()), ('ESTUDIANTE_MODIFICAR', 'Modificar datos de estudiante', NOW()),
    ('AUTH2FA_CONFIGURAR', 'Configurar autenticación 2FA', NOW()), ('AUTH2FA_VERIFICAR', 'Verificar código 2FA', NOW()), ('AUTH2FA_ESTADO', 'Consultar estado 2FA', NOW()),
    ('ROL_LISTAR', 'Listar roles', NOW()), ('ROL_ASIGNAR_PERMISO', 'Asignar permisos a un rol', NOW()), ('USUARIO_LISTAR', 'Listar usuarios', NOW())
    ON CONFLICT (code) DO NOTHING;
    RAISE NOTICE 'Permisos básicos insertados.';
END; $$;


ALTER PROCEDURE public.sp_seed_permissions() OWNER TO postgres;

--
-- TOC entry 319 (class 1255 OID 33902)
-- Name: spd_academiccalendar(integer); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spd_academiccalendar(IN p_id integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE academic_calendar SET active = false, updatedat = NOW() WHERE idacademiccalendar = p_id;
END; $$;


ALTER PROCEDURE public.spd_academiccalendar(IN p_id integer) OWNER TO postgres;

--
-- TOC entry 320 (class 1255 OID 33903)
-- Name: spd_career(integer); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spd_career(IN p_idcareer integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
    DELETE FROM careers WHERE idcareer = p_idcareer;
END; $$;


ALTER PROCEDURE public.spd_career(IN p_idcareer integer) OWNER TO postgres;

--
-- TOC entry 321 (class 1255 OID 33904)
-- Name: spd_configuration(integer); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spd_configuration(IN p_idconfiguration integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
    DELETE FROM configurations WHERE idconfiguration = p_idconfiguration;
END; $$;


ALTER PROCEDURE public.spd_configuration(IN p_idconfiguration integer) OWNER TO postgres;

--
-- TOC entry 281 (class 1255 OID 33905)
-- Name: spd_deadlinerule(integer); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spd_deadlinerule(IN p_id integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE deadlinerules SET active = false, updatedat = NOW() WHERE iddeadlinerule = p_id;
END; $$;


ALTER PROCEDURE public.spd_deadlinerule(IN p_id integer) OWNER TO postgres;

--
-- TOC entry 282 (class 1255 OID 33906)
-- Name: spd_documenttemplate(integer); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spd_documenttemplate(IN p_idtemplate integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE documenttemplates SET active = false, updatedat = NOW() WHERE idtemplate = p_idtemplate;
END; $$;


ALTER PROCEDURE public.spd_documenttemplate(IN p_idtemplate integer) OWNER TO postgres;

--
-- TOC entry 283 (class 1255 OID 33907)
-- Name: spd_faculty(integer); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spd_faculty(IN p_idfaculty integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
    DELETE FROM faculties WHERE idfaculty = p_idfaculty;
END; $$;


ALTER PROCEDURE public.spd_faculty(IN p_idfaculty integer) OWNER TO postgres;

--
-- TOC entry 284 (class 1255 OID 33908)
-- Name: spd_permission(integer); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spd_permission(IN p_idpermission integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
    DELETE FROM permissions WHERE idpermission = p_idpermission;
END; $$;


ALTER PROCEDURE public.spd_permission(IN p_idpermission integer) OWNER TO postgres;

--
-- TOC entry 338 (class 1255 OID 35105)
-- Name: spd_processingstage(integer); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spd_processingstage(IN p_idprocessingstage integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
    DELETE FROM processingstage WHERE idprocessingstage = p_idprocessingstage;
END; $$;


ALTER PROCEDURE public.spd_processingstage(IN p_idprocessingstage integer) OWNER TO postgres;

--
-- TOC entry 285 (class 1255 OID 33910)
-- Name: spd_rejectionreason(integer); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spd_rejectionreason(IN p_idrejectionreason integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE rejectionreasons SET active = false, updatedat = NOW() WHERE idrejectionreason = p_idrejectionreason;
END; $$;


ALTER PROCEDURE public.spd_rejectionreason(IN p_idrejectionreason integer) OWNER TO postgres;

--
-- TOC entry 286 (class 1255 OID 33911)
-- Name: spd_state(integer); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spd_state(IN p_idstate integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
    DELETE FROM states WHERE idstate = p_idstate;
END; $$;


ALTER PROCEDURE public.spd_state(IN p_idstate integer) OWNER TO postgres;

--
-- TOC entry 287 (class 1255 OID 33912)
-- Name: spd_workflow(integer); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spd_workflow(IN p_idworkflow integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE workflows SET active = false, updatedat = NOW() WHERE idworkflow = p_idworkflow;
END; $$;


ALTER PROCEDURE public.spd_workflow(IN p_idworkflow integer) OWNER TO postgres;

--
-- TOC entry 288 (class 1255 OID 33913)
-- Name: spi_academiccalendar(character varying, character varying, date, date, boolean); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spi_academiccalendar(IN p_calendarname character varying, IN p_academicperiod character varying, IN p_startdate date, IN p_enddate date, IN p_active boolean)
    LANGUAGE plpgsql
    AS $$
BEGIN
    INSERT INTO academic_calendar (calendarname, academicperiod, startdate, enddate, active, createdat)
    VALUES (p_calendarname, p_academicperiod, p_startdate, p_enddate, p_active, NOW());
END; $$;


ALTER PROCEDURE public.spi_academiccalendar(IN p_calendarname character varying, IN p_academicperiod character varying, IN p_startdate date, IN p_enddate date, IN p_active boolean) OWNER TO postgres;

--
-- TOC entry 294 (class 1255 OID 33914)
-- Name: spi_career(character varying, character varying, integer, integer); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spi_career(IN p_careername character varying, IN p_careercode character varying, IN p_facultiesidfaculty integer, IN p_coordinatoriduser integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
    INSERT INTO careers (careername, careercode, facultiesidfaculty, coordinatoriduser, createdat)
    VALUES (p_careername, p_careercode, p_facultiesidfaculty, p_coordinatoriduser, NOW());
END; $$;


ALTER PROCEDURE public.spi_career(IN p_careername character varying, IN p_careercode character varying, IN p_facultiesidfaculty integer, IN p_coordinatoriduser integer) OWNER TO postgres;

--
-- TOC entry 311 (class 1255 OID 33915)
-- Name: spi_configuration(character varying, character varying, boolean, boolean, boolean, character varying); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spi_configuration(IN p_profilepicturepath character varying, IN p_signaturepath character varying, IN p_enable_sms boolean, IN p_enable_email boolean, IN p_enable_whatsapp boolean, IN p_notificationfrequency character varying)
    LANGUAGE plpgsql
    AS $$
BEGIN
    INSERT INTO configurations (profilepicturepath, signaturepath, enable_sms, enable_email, enable_whatsapp, notificationfrequency, createdat)
    VALUES (p_profilepicturepath, p_signaturepath, p_enable_sms, p_enable_email, p_enable_whatsapp, p_notificationfrequency, NOW());
END; $$;


ALTER PROCEDURE public.spi_configuration(IN p_profilepicturepath character varying, IN p_signaturepath character varying, IN p_enable_sms boolean, IN p_enable_email boolean, IN p_enable_whatsapp boolean, IN p_notificationfrequency character varying) OWNER TO postgres;

--
-- TOC entry 342 (class 1255 OID 35119)
-- Name: spi_credential(character varying, date); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spi_credential(IN p_passwordhash character varying, IN p_expirydate date, OUT p_idcredential integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
    INSERT INTO public.credentials (
        passwordhash, passwordexpirydate, active, failedattempts, accountlocked
    ) VALUES (
        p_passwordhash, p_expirydate, true, 0, false
    ) RETURNING idcredentials INTO p_idcredential;
END; $$;


ALTER PROCEDURE public.spi_credential(IN p_passwordhash character varying, IN p_expirydate date, OUT p_idcredential integer) OWNER TO postgres;

--
-- TOC entry 295 (class 1255 OID 33916)
-- Name: spi_deadlinerule(character varying, character varying, integer, integer, boolean); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spi_deadlinerule(IN p_rulename character varying, IN p_procedurecategory character varying, IN p_basedeadlinedays integer, IN p_warningdaysbefore integer, IN p_active boolean)
    LANGUAGE plpgsql
    AS $$
BEGIN
    INSERT INTO deadlinerules (rulename, procedurecategory, basedeadlinedays, warningdaysbefore, active, createdat)
    VALUES (p_rulename, p_procedurecategory, p_basedeadlinedays, p_warningdaysbefore, p_active, NOW());
END; $$;


ALTER PROCEDURE public.spi_deadlinerule(IN p_rulename character varying, IN p_procedurecategory character varying, IN p_basedeadlinedays integer, IN p_warningdaysbefore integer, IN p_active boolean) OWNER TO postgres;

--
-- TOC entry 300 (class 1255 OID 33917)
-- Name: spi_documenttemplate(character varying, character varying, character varying, character varying, character varying, boolean, boolean); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spi_documenttemplate(IN p_templatename character varying, IN p_templatecode character varying, IN p_templatepath character varying, IN p_documenttype character varying, IN p_version character varying, IN p_requiressignature boolean, IN p_active boolean)
    LANGUAGE plpgsql
    AS $$
BEGIN
    INSERT INTO documenttemplates (templatename, templatecode, templatepath, documenttype, version, requiressignature, active, createdat)
    VALUES (p_templatename, p_templatecode, p_templatepath, p_documenttype, p_version, p_requiressignature, p_active, NOW());
END; $$;


ALTER PROCEDURE public.spi_documenttemplate(IN p_templatename character varying, IN p_templatecode character varying, IN p_templatepath character varying, IN p_documenttype character varying, IN p_version character varying, IN p_requiressignature boolean, IN p_active boolean) OWNER TO postgres;

--
-- TOC entry 290 (class 1255 OID 33918)
-- Name: spi_faculty(character varying, character varying, integer); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spi_faculty(IN p_facultyname character varying, IN p_facultycode character varying, IN p_deaniduser integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
    INSERT INTO faculties (facultyname, facultycode, deaniduser, createdat)
    VALUES (p_facultyname, p_facultycode, p_deaniduser, NOW());
END; $$;


ALTER PROCEDURE public.spi_faculty(IN p_facultyname character varying, IN p_facultycode character varying, IN p_deaniduser integer) OWNER TO postgres;

--
-- TOC entry 296 (class 1255 OID 33919)
-- Name: spi_permission(character varying, character varying); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spi_permission(IN p_code character varying, IN p_description character varying)
    LANGUAGE plpgsql
    AS $$
BEGIN
    INSERT INTO permissions (code, description, createdat) VALUES (p_code, p_description, NOW());
END; $$;


ALTER PROCEDURE public.spi_permission(IN p_code character varying, IN p_description character varying) OWNER TO postgres;

--
-- TOC entry 336 (class 1255 OID 35103)
-- Name: spi_processingstage(character varying, character varying, character varying, integer, boolean, integer); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spi_processingstage(IN p_stagename character varying, IN p_stagecode character varying, IN p_stagedescription character varying, IN p_stageorder integer, IN p_requiresapproval boolean, IN p_maxdurationdays integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
    INSERT INTO processingstage (stagename, stagecode, stagedescription, stageorder, requiresapproval, maxdurationdays, createdat)
    VALUES (p_stagename, p_stagecode, p_stagedescription, p_stageorder, p_requiresapproval, p_maxdurationdays, NOW());
END; $$;


ALTER PROCEDURE public.spi_processingstage(IN p_stagename character varying, IN p_stagecode character varying, IN p_stagedescription character varying, IN p_stageorder integer, IN p_requiresapproval boolean, IN p_maxdurationdays integer) OWNER TO postgres;

--
-- TOC entry 346 (class 1255 OID 35123)
-- Name: spi_refresh_token(integer, text, timestamp without time zone, character varying); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spi_refresh_token(IN p_userid integer, IN p_token text, IN p_expires timestamp without time zone, IN p_device character varying)
    LANGUAGE plpgsql
    AS $$
BEGIN
    INSERT INTO public.refresh_tokens (
        user_id, token, expires_at, created_at, revoked, device_info
    ) VALUES (
        p_userid, p_token, p_expires, NOW(), false, p_device
    );
END; $$;


ALTER PROCEDURE public.spi_refresh_token(IN p_userid integer, IN p_token text, IN p_expires timestamp without time zone, IN p_device character varying) OWNER TO postgres;

--
-- TOC entry 322 (class 1255 OID 33921)
-- Name: spi_rejectionreason(character varying, text, character varying, boolean); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spi_rejectionreason(IN p_reasoncode character varying, IN p_reasondescription text, IN p_category character varying, IN p_active boolean)
    LANGUAGE plpgsql
    AS $$
BEGIN
    INSERT INTO rejectionreasons (reasoncode, reasondescription, category, active, createdat)
    VALUES (p_reasoncode, p_reasondescription, p_category, p_active, NOW());
END; $$;


ALTER PROCEDURE public.spi_rejectionreason(IN p_reasoncode character varying, IN p_reasondescription text, IN p_category character varying, IN p_active boolean) OWNER TO postgres;

--
-- TOC entry 323 (class 1255 OID 33922)
-- Name: spi_state(character varying, text, character varying); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spi_state(IN p_statename character varying, IN p_statedescription text, IN p_statecategory character varying)
    LANGUAGE plpgsql
    AS $$
BEGIN
    INSERT INTO states (statename, statedescription, statecategory, createdat)
    VALUES (p_statename, p_statedescription, p_statecategory, NOW());
END; $$;


ALTER PROCEDURE public.spi_state(IN p_statename character varying, IN p_statedescription text, IN p_statecategory character varying) OWNER TO postgres;

--
-- TOC entry 339 (class 1255 OID 35116)
-- Name: spi_user(character varying, character varying, character varying, character varying, character varying, character varying, integer, integer); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spi_user(IN p_names character varying, IN p_surnames character varying, IN p_cardid character varying, IN p_institutionalemail character varying, IN p_personalmail character varying, IN p_phonenumber character varying, IN p_configid integer, IN p_credentialid integer, OUT p_iduser integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
    INSERT INTO public.users (
        names, surnames, cardid, institutionalemail, personalmail, 
        phonenumber, configurationsidconfiguration, credentialsidcredentials, 
        createdat, active, statement
    ) VALUES (
        p_names, p_surnames, p_cardid, p_institutionalemail, p_personalmail, 
        p_phonenumber, p_configid, p_credentialid, 
        NOW(), true, true
    ) RETURNING iduser INTO p_iduser;
END; $$;


ALTER PROCEDURE public.spi_user(IN p_names character varying, IN p_surnames character varying, IN p_cardid character varying, IN p_institutionalemail character varying, IN p_personalmail character varying, IN p_phonenumber character varying, IN p_configid integer, IN p_credentialid integer, OUT p_iduser integer) OWNER TO postgres;

--
-- TOC entry 344 (class 1255 OID 35121)
-- Name: spi_user_role(integer, integer); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spi_user_role(IN p_iduser integer, IN p_idrole integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM public.user_roles WHERE iduser = p_iduser AND idrole = p_idrole) THEN
        INSERT INTO public.user_roles (iduser, idrole) VALUES (p_iduser, p_idrole);
    END IF;
END; $$;


ALTER PROCEDURE public.spi_user_role(IN p_iduser integer, IN p_idrole integer) OWNER TO postgres;

--
-- TOC entry 324 (class 1255 OID 33923)
-- Name: spi_workflow(character varying, text, boolean); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spi_workflow(IN p_workflowname character varying, IN p_workflowdescription text, IN p_active boolean)
    LANGUAGE plpgsql
    AS $$
BEGIN
    INSERT INTO workflows (workflowname, workflowdescription, active, createdat)
    VALUES (p_workflowname, p_workflowdescription, p_active, NOW());
END; $$;


ALTER PROCEDURE public.spi_workflow(IN p_workflowname character varying, IN p_workflowdescription text, IN p_active boolean) OWNER TO postgres;

--
-- TOC entry 325 (class 1255 OID 33924)
-- Name: spu_academiccalendar(integer, character varying, character varying, date, date, boolean); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spu_academiccalendar(IN p_id integer, IN p_calendarname character varying, IN p_academicperiod character varying, IN p_startdate date, IN p_enddate date, IN p_active boolean)
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE academic_calendar SET calendarname = p_calendarname, academicperiod = p_academicperiod, startdate = p_startdate, enddate = p_enddate, active = p_active, updatedat = NOW()
    WHERE idacademiccalendar = p_id;
END; $$;


ALTER PROCEDURE public.spu_academiccalendar(IN p_id integer, IN p_calendarname character varying, IN p_academicperiod character varying, IN p_startdate date, IN p_enddate date, IN p_active boolean) OWNER TO postgres;

--
-- TOC entry 326 (class 1255 OID 33925)
-- Name: spu_career(integer, character varying, character varying, integer, integer); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spu_career(IN p_idcareer integer, IN p_careername character varying, IN p_careercode character varying, IN p_facultiesidfaculty integer, IN p_coordinatoriduser integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE careers SET careername = p_careername, careercode = p_careercode, facultiesidfaculty = p_facultiesidfaculty, coordinatoriduser = p_coordinatoriduser, updatedat = NOW()
    WHERE idcareer = p_idcareer;
END; $$;


ALTER PROCEDURE public.spu_career(IN p_idcareer integer, IN p_careername character varying, IN p_careercode character varying, IN p_facultiesidfaculty integer, IN p_coordinatoriduser integer) OWNER TO postgres;

--
-- TOC entry 327 (class 1255 OID 33926)
-- Name: spu_configuration(integer, character varying, character varying, boolean, boolean, boolean, character varying); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spu_configuration(IN p_idconfiguration integer, IN p_profilepicturepath character varying, IN p_signaturepath character varying, IN p_enable_sms boolean, IN p_enable_email boolean, IN p_enable_whatsapp boolean, IN p_notificationfrequency character varying)
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE configurations SET profilepicturepath = p_profilepicturepath, signaturepath = p_signaturepath, enable_sms = p_enable_sms, enable_email = p_enable_email, enable_whatsapp = p_enable_whatsapp, notificationfrequency = p_notificationfrequency, updatedat = NOW()
    WHERE idconfiguration = p_idconfiguration;
END; $$;


ALTER PROCEDURE public.spu_configuration(IN p_idconfiguration integer, IN p_profilepicturepath character varying, IN p_signaturepath character varying, IN p_enable_sms boolean, IN p_enable_email boolean, IN p_enable_whatsapp boolean, IN p_notificationfrequency character varying) OWNER TO postgres;

--
-- TOC entry 328 (class 1255 OID 33927)
-- Name: spu_deadlinerule(integer, character varying, character varying, integer, integer, boolean); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spu_deadlinerule(IN p_id integer, IN p_rulename character varying, IN p_procedurecategory character varying, IN p_basedeadlinedays integer, IN p_warningdaysbefore integer, IN p_active boolean)
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE deadlinerules SET rulename = p_rulename, procedurecategory = p_procedurecategory, basedeadlinedays = p_basedeadlinedays, warningdaysbefore = p_warningdaysbefore, active = p_active, updatedat = NOW()
    WHERE iddeadlinerule = p_id;
END; $$;


ALTER PROCEDURE public.spu_deadlinerule(IN p_id integer, IN p_rulename character varying, IN p_procedurecategory character varying, IN p_basedeadlinedays integer, IN p_warningdaysbefore integer, IN p_active boolean) OWNER TO postgres;

--
-- TOC entry 329 (class 1255 OID 33928)
-- Name: spu_documenttemplate(integer, character varying, character varying, character varying, character varying, character varying, boolean, boolean); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spu_documenttemplate(IN p_idtemplate integer, IN p_templatename character varying, IN p_templatecode character varying, IN p_templatepath character varying, IN p_documenttype character varying, IN p_version character varying, IN p_requiressignature boolean, IN p_active boolean)
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE documenttemplates SET templatename = p_templatename, templatecode = p_templatecode, templatepath = p_templatepath, documenttype = p_documenttype, version = p_version, requiressignature = p_requiressignature, active = p_active, updatedat = NOW()
    WHERE idtemplate = p_idtemplate;
END; $$;


ALTER PROCEDURE public.spu_documenttemplate(IN p_idtemplate integer, IN p_templatename character varying, IN p_templatecode character varying, IN p_templatepath character varying, IN p_documenttype character varying, IN p_version character varying, IN p_requiressignature boolean, IN p_active boolean) OWNER TO postgres;

--
-- TOC entry 330 (class 1255 OID 33929)
-- Name: spu_faculty(integer, character varying, character varying, integer); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spu_faculty(IN p_idfaculty integer, IN p_facultyname character varying, IN p_facultycode character varying, IN p_deaniduser integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE faculties SET facultyname = p_facultyname, facultycode = p_facultycode, deaniduser = p_deaniduser, updatedat = NOW()
    WHERE idfaculty = p_idfaculty;
END; $$;


ALTER PROCEDURE public.spu_faculty(IN p_idfaculty integer, IN p_facultyname character varying, IN p_facultycode character varying, IN p_deaniduser integer) OWNER TO postgres;

--
-- TOC entry 331 (class 1255 OID 33930)
-- Name: spu_permission(integer, character varying, character varying); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spu_permission(IN p_idpermission integer, IN p_code character varying, IN p_description character varying)
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE permissions SET code = p_code, description = p_description, updatedat = NOW() WHERE idpermission = p_idpermission;
END; $$;


ALTER PROCEDURE public.spu_permission(IN p_idpermission integer, IN p_code character varying, IN p_description character varying) OWNER TO postgres;

--
-- TOC entry 337 (class 1255 OID 35104)
-- Name: spu_processingstage(integer, character varying, character varying, character varying, integer, boolean, integer); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spu_processingstage(IN p_idprocessingstage integer, IN p_stagename character varying, IN p_stagecode character varying, IN p_stagedescription character varying, IN p_stageorder integer, IN p_requiresapproval boolean, IN p_maxdurationdays integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE processingstage 
    SET stagename = p_stagename, 
        stagecode = p_stagecode, 
        stagedescription = p_stagedescription, 
        stageorder = p_stageorder, 
        requiresapproval = p_requiresapproval, 
        maxdurationdays = p_maxdurationdays, 
        updatedat = NOW()
    WHERE idprocessingstage = p_idprocessingstage;
END; $$;


ALTER PROCEDURE public.spu_processingstage(IN p_idprocessingstage integer, IN p_stagename character varying, IN p_stagecode character varying, IN p_stagedescription character varying, IN p_stageorder integer, IN p_requiresapproval boolean, IN p_maxdurationdays integer) OWNER TO postgres;

--
-- TOC entry 332 (class 1255 OID 33932)
-- Name: spu_rejectionreason(integer, character varying, text, character varying, boolean); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spu_rejectionreason(IN p_idrejectionreason integer, IN p_reasoncode character varying, IN p_reasondescription text, IN p_category character varying, IN p_active boolean)
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE rejectionreasons SET reasoncode = p_reasoncode, reasondescription = p_reasondescription, category = p_category, active = p_active, updatedat = NOW()
    WHERE idrejectionreason = p_idrejectionreason;
END; $$;


ALTER PROCEDURE public.spu_rejectionreason(IN p_idrejectionreason integer, IN p_reasoncode character varying, IN p_reasondescription text, IN p_category character varying, IN p_active boolean) OWNER TO postgres;

--
-- TOC entry 333 (class 1255 OID 33933)
-- Name: spu_revoke_all_refresh_tokens(integer); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spu_revoke_all_refresh_tokens(IN p_user_id integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.refresh_tokens SET revoked = true WHERE user_id = p_user_id AND revoked = false;
    RAISE NOTICE 'Tokens revocados para el usuario ID: %', p_user_id;
END; $$;


ALTER PROCEDURE public.spu_revoke_all_refresh_tokens(IN p_user_id integer) OWNER TO postgres;

--
-- TOC entry 347 (class 1255 OID 35124)
-- Name: spu_revoke_token(text); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spu_revoke_token(IN p_token text)
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.refresh_tokens SET revoked = true 
    WHERE token = p_token;
END; $$;


ALTER PROCEDURE public.spu_revoke_token(IN p_token text) OWNER TO postgres;

--
-- TOC entry 334 (class 1255 OID 33934)
-- Name: spu_state(integer, character varying, text, character varying); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spu_state(IN p_idstate integer, IN p_statename character varying, IN p_statedescription text, IN p_statecategory character varying)
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE states SET statename = p_statename, statedescription = p_statedescription, statecategory = p_statecategory, updatedat = NOW()
    WHERE idstate = p_idstate;
END; $$;


ALTER PROCEDURE public.spu_state(IN p_idstate integer, IN p_statename character varying, IN p_statedescription text, IN p_statecategory character varying) OWNER TO postgres;

--
-- TOC entry 340 (class 1255 OID 35117)
-- Name: spu_user(integer, character varying, character varying, character varying, character varying, character varying, character varying); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spu_user(IN p_iduser integer, IN p_names character varying, IN p_surnames character varying, IN p_cardid character varying, IN p_institutionalemail character varying, IN p_personalmail character varying, IN p_phonenumber character varying)
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.users SET
        names = p_names,
        surnames = p_surnames,
        cardid = p_cardid,
        institutionalemail = p_institutionalemail,
        personalmail = p_personalmail,
        phonenumber = p_phonenumber,
        updatedat = NOW()
    WHERE iduser = p_iduser;
END; $$;


ALTER PROCEDURE public.spu_user(IN p_iduser integer, IN p_names character varying, IN p_surnames character varying, IN p_cardid character varying, IN p_institutionalemail character varying, IN p_personalmail character varying, IN p_phonenumber character varying) OWNER TO postgres;

--
-- TOC entry 298 (class 1255 OID 33935)
-- Name: spu_workflow(integer, character varying, text, boolean); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.spu_workflow(IN p_idworkflow integer, IN p_workflowname character varying, IN p_workflowdescription text, IN p_active boolean)
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE workflows SET workflowname = p_workflowname, workflowdescription = p_workflowdescription, active = p_active, updatedat = NOW()
    WHERE idworkflow = p_idworkflow;
END; $$;


ALTER PROCEDURE public.spu_workflow(IN p_idworkflow integer, IN p_workflowname character varying, IN p_workflowdescription text, IN p_active boolean) OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 217 (class 1259 OID 33936)
-- Name: academiccalendar; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.academiccalendar (
    idacademiccalendar integer NOT NULL,
    calendarname character varying(255) NOT NULL,
    academicperiod character varying(100) NOT NULL,
    startdate date NOT NULL,
    enddate date NOT NULL,
    active boolean DEFAULT true NOT NULL,
    createdat timestamp(6) without time zone,
    updatedat timestamp(6) without time zone
);


ALTER TABLE public.academiccalendar OWNER TO postgres;

--
-- TOC entry 218 (class 1259 OID 33940)
-- Name: academiccalendar_idacademiccalendar_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.academiccalendar_idacademiccalendar_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.academiccalendar_idacademiccalendar_seq OWNER TO postgres;

--
-- TOC entry 5398 (class 0 OID 0)
-- Dependencies: 218
-- Name: academiccalendar_idacademiccalendar_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.academiccalendar_idacademiccalendar_seq OWNED BY public.academiccalendar.idacademiccalendar;


--
-- TOC entry 219 (class 1259 OID 33941)
-- Name: applications; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.applications (
    idapplication integer NOT NULL,
    applicationcode character varying(100) NOT NULL,
    creationdate timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    estimatedcompletiondate date NOT NULL,
    actualcompletiondate timestamp without time zone,
    applicationdetails text,
    applicationresolution text,
    rejectionreasonid integer,
    currentstagetrackingid integer NOT NULL,
    proceduresidprocedure integer NOT NULL,
    applicantuserid integer NOT NULL,
    priority character varying(20) DEFAULT 'normal'::character varying NOT NULL
);


ALTER TABLE public.applications OWNER TO postgres;

--
-- TOC entry 220 (class 1259 OID 33948)
-- Name: applications_idapplication_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.applications_idapplication_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.applications_idapplication_seq OWNER TO postgres;

--
-- TOC entry 5399 (class 0 OID 0)
-- Dependencies: 220
-- Name: applications_idapplication_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.applications_idapplication_seq OWNED BY public.applications.idapplication;


--
-- TOC entry 221 (class 1259 OID 33949)
-- Name: applicationstagehistory; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.applicationstagehistory (
    idhistory integer NOT NULL,
    applicationidapplication integer NOT NULL,
    stagetrackingid integer NOT NULL,
    enteredat timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    exitedat timestamp without time zone,
    processedbyuserid integer,
    comments text
);


ALTER TABLE public.applicationstagehistory OWNER TO postgres;

--
-- TOC entry 222 (class 1259 OID 33955)
-- Name: applicationstagehistory_idhistory_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.applicationstagehistory_idhistory_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.applicationstagehistory_idhistory_seq OWNER TO postgres;

--
-- TOC entry 5400 (class 0 OID 0)
-- Dependencies: 222
-- Name: applicationstagehistory_idhistory_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.applicationstagehistory_idhistory_seq OWNED BY public.applicationstagehistory.idhistory;


--
-- TOC entry 223 (class 1259 OID 33956)
-- Name: attacheddocuments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.attacheddocuments (
    idattacheddocument integer NOT NULL,
    applicationsidapplication integer NOT NULL,
    requirementid integer,
    filename character varying(255) NOT NULL,
    filepath character varying(500) NOT NULL,
    filesizebytes bigint NOT NULL,
    mimetype character varying(100) NOT NULL,
    uploaddate timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    uploadedbyuserid integer NOT NULL
);


ALTER TABLE public.attacheddocuments OWNER TO postgres;

--
-- TOC entry 224 (class 1259 OID 33962)
-- Name: attacheddocuments_idattacheddocument_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.attacheddocuments_idattacheddocument_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.attacheddocuments_idattacheddocument_seq OWNER TO postgres;

--
-- TOC entry 5401 (class 0 OID 0)
-- Dependencies: 224
-- Name: attacheddocuments_idattacheddocument_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.attacheddocuments_idattacheddocument_seq OWNED BY public.attacheddocuments.idattacheddocument;


--
-- TOC entry 225 (class 1259 OID 33963)
-- Name: careers; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.careers (
    idcareer integer NOT NULL,
    careername character varying(255) NOT NULL,
    careercode character varying(50),
    facultiesidfaculty integer NOT NULL,
    coordinatoriduser integer,
    createdat timestamp(6) without time zone,
    updatedat timestamp(6) without time zone
);


ALTER TABLE public.careers OWNER TO postgres;

--
-- TOC entry 226 (class 1259 OID 33966)
-- Name: careers_idcareer_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.careers_idcareer_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.careers_idcareer_seq OWNER TO postgres;

--
-- TOC entry 5402 (class 0 OID 0)
-- Dependencies: 226
-- Name: careers_idcareer_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.careers_idcareer_seq OWNED BY public.careers.idcareer;


--
-- TOC entry 227 (class 1259 OID 33967)
-- Name: configurations; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.configurations (
    idconfiguration integer NOT NULL,
    profilepicturepath character varying(500),
    signaturepath character varying(500),
    enable_sms boolean DEFAULT false NOT NULL,
    enable_email boolean DEFAULT true NOT NULL,
    enable_whatsapp boolean DEFAULT false NOT NULL,
    notificationfrequency character varying(50) DEFAULT 'real_time'::character varying NOT NULL,
    active boolean NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    language character varying(10),
    notifications boolean NOT NULL,
    theme character varying(20),
    updatedat timestamp(6) without time zone
);


ALTER TABLE public.configurations OWNER TO postgres;

--
-- TOC entry 228 (class 1259 OID 33976)
-- Name: configurations_idconfiguration_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.configurations_idconfiguration_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.configurations_idconfiguration_seq OWNER TO postgres;

--
-- TOC entry 5403 (class 0 OID 0)
-- Dependencies: 228
-- Name: configurations_idconfiguration_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.configurations_idconfiguration_seq OWNED BY public.configurations.idconfiguration;


--
-- TOC entry 229 (class 1259 OID 33977)
-- Name: credentials; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.credentials (
    idcredentials integer NOT NULL,
    passwordhash character varying(255) NOT NULL,
    datemodification timestamp without time zone,
    lastlogin timestamp without time zone,
    failedattempts integer DEFAULT 0 NOT NULL,
    accountlocked boolean DEFAULT false NOT NULL,
    passwordexpirydate date,
    active boolean NOT NULL
);


ALTER TABLE public.credentials OWNER TO postgres;

--
-- TOC entry 230 (class 1259 OID 33982)
-- Name: credentials_idcredentials_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.credentials_idcredentials_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.credentials_idcredentials_seq OWNER TO postgres;

--
-- TOC entry 5404 (class 0 OID 0)
-- Dependencies: 230
-- Name: credentials_idcredentials_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.credentials_idcredentials_seq OWNED BY public.credentials.idcredentials;


--
-- TOC entry 231 (class 1259 OID 33983)
-- Name: deadlinerules; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.deadlinerules (
    iddeadlinerule integer NOT NULL,
    rulename character varying(255) NOT NULL,
    procedurecategory character varying(100) NOT NULL,
    basedeadlinedays integer NOT NULL,
    warningdaysbefore integer DEFAULT 3 NOT NULL,
    active boolean DEFAULT true NOT NULL,
    createdat timestamp(6) without time zone,
    updatedat timestamp(6) without time zone
);


ALTER TABLE public.deadlinerules OWNER TO postgres;

--
-- TOC entry 232 (class 1259 OID 33988)
-- Name: deadlinerules_iddeadlinerule_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.deadlinerules_iddeadlinerule_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.deadlinerules_iddeadlinerule_seq OWNER TO postgres;

--
-- TOC entry 5405 (class 0 OID 0)
-- Dependencies: 232
-- Name: deadlinerules_iddeadlinerule_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.deadlinerules_iddeadlinerule_seq OWNED BY public.deadlinerules.iddeadlinerule;


--
-- TOC entry 233 (class 1259 OID 33989)
-- Name: digitalsignatures; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.digitalsignatures (
    iddigitalsignature integer NOT NULL,
    useriduser integer NOT NULL,
    certificatepath character varying(500) NOT NULL,
    certificateserial character varying(255) NOT NULL,
    issuer character varying(255) NOT NULL,
    validfrom date NOT NULL,
    validuntil date NOT NULL,
    signaturealgorithm character varying(100) NOT NULL,
    active boolean DEFAULT true NOT NULL,
    createdat timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.digitalsignatures OWNER TO postgres;

--
-- TOC entry 234 (class 1259 OID 33996)
-- Name: digitalsignatures_iddigitalsignature_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.digitalsignatures_iddigitalsignature_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.digitalsignatures_iddigitalsignature_seq OWNER TO postgres;

--
-- TOC entry 5406 (class 0 OID 0)
-- Dependencies: 234
-- Name: digitalsignatures_iddigitalsignature_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.digitalsignatures_iddigitalsignature_seq OWNED BY public.digitalsignatures.iddigitalsignature;


--
-- TOC entry 235 (class 1259 OID 33997)
-- Name: documentsgenerated; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.documentsgenerated (
    iddocumentgenerated integer NOT NULL,
    applicationsidapplication integer NOT NULL,
    templateid integer,
    documenttype character varying(255) NOT NULL,
    documentpath character varying(500) NOT NULL,
    generatedat timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    generatedbyuserid integer NOT NULL,
    digitalsignatureid integer,
    signaturetimestamp timestamp without time zone
);


ALTER TABLE public.documentsgenerated OWNER TO postgres;

--
-- TOC entry 236 (class 1259 OID 34003)
-- Name: documentsgenerated_iddocumentgenerated_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.documentsgenerated_iddocumentgenerated_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.documentsgenerated_iddocumentgenerated_seq OWNER TO postgres;

--
-- TOC entry 5407 (class 0 OID 0)
-- Dependencies: 236
-- Name: documentsgenerated_iddocumentgenerated_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.documentsgenerated_iddocumentgenerated_seq OWNED BY public.documentsgenerated.iddocumentgenerated;


--
-- TOC entry 237 (class 1259 OID 34004)
-- Name: documenttemplates; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.documenttemplates (
    idtemplate integer NOT NULL,
    templatename character varying(255) NOT NULL,
    templatecode character varying(50) NOT NULL,
    templatepath character varying(500) NOT NULL,
    documenttype character varying(100) NOT NULL,
    version character varying(20) NOT NULL,
    requiressignature boolean DEFAULT false NOT NULL,
    active boolean DEFAULT true NOT NULL,
    createdat timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updatedat timestamp without time zone
);


ALTER TABLE public.documenttemplates OWNER TO postgres;

--
-- TOC entry 238 (class 1259 OID 34012)
-- Name: documenttemplates_idtemplate_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.documenttemplates_idtemplate_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.documenttemplates_idtemplate_seq OWNER TO postgres;

--
-- TOC entry 5408 (class 0 OID 0)
-- Dependencies: 238
-- Name: documenttemplates_idtemplate_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.documenttemplates_idtemplate_seq OWNED BY public.documenttemplates.idtemplate;


--
-- TOC entry 239 (class 1259 OID 34013)
-- Name: faculties; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.faculties (
    idfaculty integer NOT NULL,
    facultyname character varying(255) NOT NULL,
    facultycode character varying(50),
    deaniduser integer,
    createdat timestamp(6) without time zone,
    updatedat timestamp(6) without time zone,
    dean_iduser integer
);


ALTER TABLE public.faculties OWNER TO postgres;

--
-- TOC entry 240 (class 1259 OID 34016)
-- Name: faculties_idfaculty_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.faculties_idfaculty_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.faculties_idfaculty_seq OWNER TO postgres;

--
-- TOC entry 5409 (class 0 OID 0)
-- Dependencies: 240
-- Name: faculties_idfaculty_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.faculties_idfaculty_seq OWNED BY public.faculties.idfaculty;


--
-- TOC entry 241 (class 1259 OID 34017)
-- Name: notification; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.notification (
    idnotification integer NOT NULL,
    notificationname character varying(255) NOT NULL,
    message text,
    notificationtypeidnotificationtype integer NOT NULL,
    applicationid integer,
    recipientuserid integer NOT NULL,
    sentat timestamp without time zone,
    deliverystatus character varying(50) DEFAULT 'pending'::character varying NOT NULL,
    deliverychannel character varying(50),
    readat timestamp without time zone,
    errormessage text,
    retrycount integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.notification OWNER TO postgres;

--
-- TOC entry 242 (class 1259 OID 34024)
-- Name: notification_idnotification_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.notification_idnotification_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.notification_idnotification_seq OWNER TO postgres;

--
-- TOC entry 5410 (class 0 OID 0)
-- Dependencies: 242
-- Name: notification_idnotification_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.notification_idnotification_seq OWNED BY public.notification.idnotification;


--
-- TOC entry 243 (class 1259 OID 34025)
-- Name: notificationtype; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.notificationtype (
    idnotificationtype integer NOT NULL,
    nametypenotification character varying(255) NOT NULL,
    templatecode character varying(50),
    prioritylevel character varying(20)
);


ALTER TABLE public.notificationtype OWNER TO postgres;

--
-- TOC entry 244 (class 1259 OID 34028)
-- Name: notificationtype_idnotificationtype_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.notificationtype_idnotificationtype_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.notificationtype_idnotificationtype_seq OWNER TO postgres;

--
-- TOC entry 5411 (class 0 OID 0)
-- Dependencies: 244
-- Name: notificationtype_idnotificationtype_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.notificationtype_idnotificationtype_seq OWNED BY public.notificationtype.idnotificationtype;


--
-- TOC entry 245 (class 1259 OID 34029)
-- Name: permissions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.permissions (
    idpermission integer NOT NULL,
    code character varying(100) NOT NULL,
    description character varying(255),
    createdat timestamp(6) without time zone NOT NULL,
    updatedat timestamp(6) without time zone
);


ALTER TABLE public.permissions OWNER TO postgres;

--
-- TOC entry 246 (class 1259 OID 34032)
-- Name: permissions_idpermission_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.permissions_idpermission_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.permissions_idpermission_seq OWNER TO postgres;

--
-- TOC entry 5412 (class 0 OID 0)
-- Dependencies: 246
-- Name: permissions_idpermission_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.permissions_idpermission_seq OWNED BY public.permissions.idpermission;


--
-- TOC entry 247 (class 1259 OID 34033)
-- Name: procedures; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.procedures (
    idprocedure integer NOT NULL,
    nameprocedure character varying(255) NOT NULL,
    procedurecode character varying(50) NOT NULL,
    description character varying(255),
    workflowsidworkflow integer NOT NULL,
    academiccalendaridacademiccalendar integer,
    deadlineruleid integer,
    estimateddurationdays integer,
    requires2fa boolean DEFAULT false NOT NULL,
    active boolean DEFAULT true NOT NULL,
    createdat timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updatedat timestamp(6) without time zone
);


ALTER TABLE public.procedures OWNER TO postgres;

--
-- TOC entry 248 (class 1259 OID 34041)
-- Name: procedures_idprocedure_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.procedures_idprocedure_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.procedures_idprocedure_seq OWNER TO postgres;

--
-- TOC entry 5413 (class 0 OID 0)
-- Dependencies: 248
-- Name: procedures_idprocedure_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.procedures_idprocedure_seq OWNED BY public.procedures.idprocedure;


--
-- TOC entry 249 (class 1259 OID 34042)
-- Name: processingstage; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.processingstage (
    idprocessingstage integer NOT NULL,
    stagename character varying(255) NOT NULL,
    stagecode character varying(50) NOT NULL,
    stagedescription character varying(255),
    stageorder integer NOT NULL,
    requiresapproval boolean DEFAULT false NOT NULL,
    maxdurationdays integer,
    createdat timestamp(6) without time zone,
    updatedat timestamp(6) without time zone
);


ALTER TABLE public.processingstage OWNER TO postgres;

--
-- TOC entry 250 (class 1259 OID 34048)
-- Name: processingstage_idprocessingstage_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.processingstage_idprocessingstage_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.processingstage_idprocessingstage_seq OWNER TO postgres;

--
-- TOC entry 5414 (class 0 OID 0)
-- Dependencies: 250
-- Name: processingstage_idprocessingstage_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.processingstage_idprocessingstage_seq OWNED BY public.processingstage.idprocessingstage;


--
-- TOC entry 251 (class 1259 OID 34049)
-- Name: refresh_tokens; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.refresh_tokens (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    device_info character varying(255),
    expires_at timestamp(6) without time zone NOT NULL,
    revoked boolean NOT NULL,
    token text NOT NULL,
    user_id integer NOT NULL
);


ALTER TABLE public.refresh_tokens OWNER TO postgres;

--
-- TOC entry 252 (class 1259 OID 34054)
-- Name: refresh_tokens_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.refresh_tokens_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.refresh_tokens_id_seq OWNER TO postgres;

--
-- TOC entry 5415 (class 0 OID 0)
-- Dependencies: 252
-- Name: refresh_tokens_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.refresh_tokens_id_seq OWNED BY public.refresh_tokens.id;


--
-- TOC entry 253 (class 1259 OID 34055)
-- Name: rejectionreasons; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.rejectionreasons (
    idrejectionreason integer NOT NULL,
    reasoncode character varying(50) NOT NULL,
    reasondescription character varying(255) NOT NULL,
    category character varying(100) NOT NULL,
    active boolean DEFAULT true NOT NULL,
    createdat timestamp(6) without time zone,
    updatedat timestamp(6) without time zone
);


ALTER TABLE public.rejectionreasons OWNER TO postgres;

--
-- TOC entry 254 (class 1259 OID 34059)
-- Name: rejectionreasons_idrejectionreason_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.rejectionreasons_idrejectionreason_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.rejectionreasons_idrejectionreason_seq OWNER TO postgres;

--
-- TOC entry 5416 (class 0 OID 0)
-- Dependencies: 254
-- Name: rejectionreasons_idrejectionreason_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.rejectionreasons_idrejectionreason_seq OWNED BY public.rejectionreasons.idrejectionreason;


--
-- TOC entry 255 (class 1259 OID 34060)
-- Name: requirementsoftheprocedure; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.requirementsoftheprocedure (
    idrequirementsoftheprocedure integer NOT NULL,
    proceduresidprocedure integer NOT NULL,
    requirementname character varying(255) NOT NULL,
    requirementdescription text,
    requirementtype character varying(50) NOT NULL,
    ismandatory boolean DEFAULT true NOT NULL,
    displayorder integer
);


ALTER TABLE public.requirementsoftheprocedure OWNER TO postgres;

--
-- TOC entry 256 (class 1259 OID 34066)
-- Name: requirementsoftheprocedure_idrequirementsoftheprocedure_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.requirementsoftheprocedure_idrequirementsoftheprocedure_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.requirementsoftheprocedure_idrequirementsoftheprocedure_seq OWNER TO postgres;

--
-- TOC entry 5417 (class 0 OID 0)
-- Dependencies: 256
-- Name: requirementsoftheprocedure_idrequirementsoftheprocedure_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.requirementsoftheprocedure_idrequirementsoftheprocedure_seq OWNED BY public.requirementsoftheprocedure.idrequirementsoftheprocedure;


--
-- TOC entry 257 (class 1259 OID 34067)
-- Name: role_permissions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.role_permissions (
    idrole integer NOT NULL,
    idpermission integer NOT NULL
);


ALTER TABLE public.role_permissions OWNER TO postgres;

--
-- TOC entry 258 (class 1259 OID 34070)
-- Name: roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.roles (
    idrole integer NOT NULL,
    rolename character varying(100) NOT NULL,
    roledescription character varying(255),
    active boolean NOT NULL,
    description character varying(255),
    name character varying(50) NOT NULL
);


ALTER TABLE public.roles OWNER TO postgres;

--
-- TOC entry 259 (class 1259 OID 34075)
-- Name: roles_idrole_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.roles_idrole_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.roles_idrole_seq OWNER TO postgres;

--
-- TOC entry 5418 (class 0 OID 0)
-- Dependencies: 259
-- Name: roles_idrole_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.roles_idrole_seq OWNED BY public.roles.idrole;


--
-- TOC entry 260 (class 1259 OID 34076)
-- Name: sessiontokens; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sessiontokens (
    idsession integer NOT NULL,
    userid integer NOT NULL,
    token character varying(255) NOT NULL,
    ipaddress character varying(45),
    useragent character varying(255),
    createdat timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    expiresat timestamp without time zone NOT NULL,
    isactive boolean NOT NULL,
    revokedat timestamp(6) without time zone,
    usersiduser integer NOT NULL,
    lastactivity timestamp(6) without time zone
);


ALTER TABLE public.sessiontokens OWNER TO postgres;

--
-- TOC entry 261 (class 1259 OID 34082)
-- Name: sessiontokens_idsession_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sessiontokens_idsession_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sessiontokens_idsession_seq OWNER TO postgres;

--
-- TOC entry 5419 (class 0 OID 0)
-- Dependencies: 261
-- Name: sessiontokens_idsession_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.sessiontokens_idsession_seq OWNED BY public.sessiontokens.idsession;


--
-- TOC entry 262 (class 1259 OID 34083)
-- Name: stagetracking; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.stagetracking (
    idstagetracking integer NOT NULL,
    stateidstate integer NOT NULL,
    processingstageidprocessingstage integer NOT NULL,
    enteredat timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    completedat timestamp without time zone,
    assignedtouserid integer,
    notes text
);


ALTER TABLE public.stagetracking OWNER TO postgres;

--
-- TOC entry 263 (class 1259 OID 34089)
-- Name: stagetracking_idstagetracking_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.stagetracking_idstagetracking_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.stagetracking_idstagetracking_seq OWNER TO postgres;

--
-- TOC entry 5420 (class 0 OID 0)
-- Dependencies: 263
-- Name: stagetracking_idstagetracking_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.stagetracking_idstagetracking_seq OWNED BY public.stagetracking.idstagetracking;


--
-- TOC entry 264 (class 1259 OID 34090)
-- Name: states; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.states (
    idstate integer NOT NULL,
    statename character varying(100) NOT NULL,
    statedescription character varying(255),
    statecategory character varying(50) NOT NULL,
    createdat timestamp(6) without time zone,
    updatedat timestamp(6) without time zone
);


ALTER TABLE public.states OWNER TO postgres;

--
-- TOC entry 265 (class 1259 OID 34093)
-- Name: states_idstate_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.states_idstate_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.states_idstate_seq OWNER TO postgres;

--
-- TOC entry 5421 (class 0 OID 0)
-- Dependencies: 265
-- Name: states_idstate_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.states_idstate_seq OWNED BY public.states.idstate;


--
-- TOC entry 266 (class 1259 OID 34094)
-- Name: students; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.students (
    idstudent integer NOT NULL,
    semester character varying(255) NOT NULL,
    parallel character varying(1) NOT NULL,
    usersiduser integer NOT NULL,
    careersidcareer integer NOT NULL,
    enrollmentdate date,
    status character varying(50) DEFAULT 'active'::character varying NOT NULL,
    id_student bigint NOT NULL,
    academic_period character varying(50),
    active boolean NOT NULL,
    enrollment_number character varying(20) NOT NULL,
    updated_at timestamp(6) without time zone
);


ALTER TABLE public.students OWNER TO postgres;

--
-- TOC entry 267 (class 1259 OID 34098)
-- Name: students_id_student_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.students ALTER COLUMN id_student ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.students_id_student_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 268 (class 1259 OID 34099)
-- Name: students_idstudent_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.students_idstudent_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.students_idstudent_seq OWNER TO postgres;

--
-- TOC entry 5422 (class 0 OID 0)
-- Dependencies: 268
-- Name: students_idstudent_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.students_idstudent_seq OWNED BY public.students.idstudent;


--
-- TOC entry 269 (class 1259 OID 34100)
-- Name: twofactorauth; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.twofactorauth (
    id2fa integer NOT NULL,
    backupcodes text,
    enabled boolean NOT NULL,
    secretkey text,
    verifiedat timestamp(6) without time zone,
    credentialsidcredentials integer NOT NULL
);


ALTER TABLE public.twofactorauth OWNER TO postgres;

--
-- TOC entry 270 (class 1259 OID 34105)
-- Name: twofactorauth_id2fa_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.twofactorauth_id2fa_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.twofactorauth_id2fa_seq OWNER TO postgres;

--
-- TOC entry 5423 (class 0 OID 0)
-- Dependencies: 270
-- Name: twofactorauth_id2fa_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.twofactorauth_id2fa_seq OWNED BY public.twofactorauth.id2fa;


--
-- TOC entry 271 (class 1259 OID 34106)
-- Name: user_roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_roles (
    iduser integer NOT NULL,
    idrole integer NOT NULL
);


ALTER TABLE public.user_roles OWNER TO postgres;

--
-- TOC entry 272 (class 1259 OID 34109)
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    iduser integer NOT NULL,
    names character varying(255) NOT NULL,
    surnames character varying(255) NOT NULL,
    cardid character varying(10) NOT NULL,
    institutionalemail character varying(255) NOT NULL,
    personalmail character varying(255),
    phonenumber character varying(15),
    statement boolean DEFAULT true NOT NULL,
    configurationsidconfiguration integer NOT NULL,
    credentialsidcredentials integer,
    createdat timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updatedat timestamp without time zone,
    active boolean DEFAULT true NOT NULL
);


ALTER TABLE public.users OWNER TO postgres;

--
-- TOC entry 273 (class 1259 OID 34117)
-- Name: users_iduser_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_iduser_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_iduser_seq OWNER TO postgres;

--
-- TOC entry 5424 (class 0 OID 0)
-- Dependencies: 273
-- Name: users_iduser_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_iduser_seq OWNED BY public.users.iduser;


--
-- TOC entry 274 (class 1259 OID 34118)
-- Name: workflows; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.workflows (
    idworkflow integer NOT NULL,
    workflowname character varying(255) NOT NULL,
    workflowdescription character varying(255),
    createdat timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    active boolean DEFAULT true NOT NULL,
    updatedat timestamp(6) without time zone
);


ALTER TABLE public.workflows OWNER TO postgres;

--
-- TOC entry 275 (class 1259 OID 34125)
-- Name: workflows_idworkflow_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.workflows_idworkflow_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.workflows_idworkflow_seq OWNER TO postgres;

--
-- TOC entry 5425 (class 0 OID 0)
-- Dependencies: 275
-- Name: workflows_idworkflow_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.workflows_idworkflow_seq OWNED BY public.workflows.idworkflow;


--
-- TOC entry 276 (class 1259 OID 34126)
-- Name: workflowstages; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.workflowstages (
    idworkflowstage integer NOT NULL,
    workflowidworkflow integer NOT NULL,
    processingstageidprocessingstage integer NOT NULL,
    sequenceorder integer NOT NULL,
    isoptional boolean DEFAULT false NOT NULL
);


ALTER TABLE public.workflowstages OWNER TO postgres;

--
-- TOC entry 277 (class 1259 OID 34130)
-- Name: workflowstages_idworkflowstage_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.workflowstages_idworkflowstage_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.workflowstages_idworkflowstage_seq OWNER TO postgres;

--
-- TOC entry 5426 (class 0 OID 0)
-- Dependencies: 277
-- Name: workflowstages_idworkflowstage_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.workflowstages_idworkflowstage_seq OWNED BY public.workflowstages.idworkflowstage;


--
-- TOC entry 4950 (class 2604 OID 34131)
-- Name: academiccalendar idacademiccalendar; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.academiccalendar ALTER COLUMN idacademiccalendar SET DEFAULT nextval('public.academiccalendar_idacademiccalendar_seq'::regclass);


--
-- TOC entry 4952 (class 2604 OID 34132)
-- Name: applications idapplication; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.applications ALTER COLUMN idapplication SET DEFAULT nextval('public.applications_idapplication_seq'::regclass);


--
-- TOC entry 4955 (class 2604 OID 34133)
-- Name: applicationstagehistory idhistory; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.applicationstagehistory ALTER COLUMN idhistory SET DEFAULT nextval('public.applicationstagehistory_idhistory_seq'::regclass);


--
-- TOC entry 4957 (class 2604 OID 34134)
-- Name: attacheddocuments idattacheddocument; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.attacheddocuments ALTER COLUMN idattacheddocument SET DEFAULT nextval('public.attacheddocuments_idattacheddocument_seq'::regclass);


--
-- TOC entry 4959 (class 2604 OID 34135)
-- Name: careers idcareer; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.careers ALTER COLUMN idcareer SET DEFAULT nextval('public.careers_idcareer_seq'::regclass);


--
-- TOC entry 4960 (class 2604 OID 34136)
-- Name: configurations idconfiguration; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.configurations ALTER COLUMN idconfiguration SET DEFAULT nextval('public.configurations_idconfiguration_seq'::regclass);


--
-- TOC entry 4965 (class 2604 OID 34137)
-- Name: credentials idcredentials; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.credentials ALTER COLUMN idcredentials SET DEFAULT nextval('public.credentials_idcredentials_seq'::regclass);


--
-- TOC entry 4968 (class 2604 OID 34138)
-- Name: deadlinerules iddeadlinerule; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.deadlinerules ALTER COLUMN iddeadlinerule SET DEFAULT nextval('public.deadlinerules_iddeadlinerule_seq'::regclass);


--
-- TOC entry 4971 (class 2604 OID 34139)
-- Name: digitalsignatures iddigitalsignature; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.digitalsignatures ALTER COLUMN iddigitalsignature SET DEFAULT nextval('public.digitalsignatures_iddigitalsignature_seq'::regclass);


--
-- TOC entry 4974 (class 2604 OID 34140)
-- Name: documentsgenerated iddocumentgenerated; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.documentsgenerated ALTER COLUMN iddocumentgenerated SET DEFAULT nextval('public.documentsgenerated_iddocumentgenerated_seq'::regclass);


--
-- TOC entry 4976 (class 2604 OID 34141)
-- Name: documenttemplates idtemplate; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.documenttemplates ALTER COLUMN idtemplate SET DEFAULT nextval('public.documenttemplates_idtemplate_seq'::regclass);


--
-- TOC entry 4980 (class 2604 OID 34142)
-- Name: faculties idfaculty; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.faculties ALTER COLUMN idfaculty SET DEFAULT nextval('public.faculties_idfaculty_seq'::regclass);


--
-- TOC entry 4981 (class 2604 OID 34143)
-- Name: notification idnotification; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notification ALTER COLUMN idnotification SET DEFAULT nextval('public.notification_idnotification_seq'::regclass);


--
-- TOC entry 4984 (class 2604 OID 34144)
-- Name: notificationtype idnotificationtype; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notificationtype ALTER COLUMN idnotificationtype SET DEFAULT nextval('public.notificationtype_idnotificationtype_seq'::regclass);


--
-- TOC entry 4985 (class 2604 OID 34145)
-- Name: permissions idpermission; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permissions ALTER COLUMN idpermission SET DEFAULT nextval('public.permissions_idpermission_seq'::regclass);


--
-- TOC entry 4986 (class 2604 OID 34146)
-- Name: procedures idprocedure; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.procedures ALTER COLUMN idprocedure SET DEFAULT nextval('public.procedures_idprocedure_seq'::regclass);


--
-- TOC entry 4990 (class 2604 OID 34147)
-- Name: processingstage idprocessingstage; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.processingstage ALTER COLUMN idprocessingstage SET DEFAULT nextval('public.processingstage_idprocessingstage_seq'::regclass);


--
-- TOC entry 4992 (class 2604 OID 34148)
-- Name: refresh_tokens id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.refresh_tokens ALTER COLUMN id SET DEFAULT nextval('public.refresh_tokens_id_seq'::regclass);


--
-- TOC entry 4993 (class 2604 OID 34149)
-- Name: rejectionreasons idrejectionreason; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rejectionreasons ALTER COLUMN idrejectionreason SET DEFAULT nextval('public.rejectionreasons_idrejectionreason_seq'::regclass);


--
-- TOC entry 4995 (class 2604 OID 34150)
-- Name: requirementsoftheprocedure idrequirementsoftheprocedure; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.requirementsoftheprocedure ALTER COLUMN idrequirementsoftheprocedure SET DEFAULT nextval('public.requirementsoftheprocedure_idrequirementsoftheprocedure_seq'::regclass);


--
-- TOC entry 4997 (class 2604 OID 34151)
-- Name: roles idrole; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles ALTER COLUMN idrole SET DEFAULT nextval('public.roles_idrole_seq'::regclass);


--
-- TOC entry 4998 (class 2604 OID 34152)
-- Name: sessiontokens idsession; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sessiontokens ALTER COLUMN idsession SET DEFAULT nextval('public.sessiontokens_idsession_seq'::regclass);


--
-- TOC entry 5000 (class 2604 OID 34153)
-- Name: stagetracking idstagetracking; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stagetracking ALTER COLUMN idstagetracking SET DEFAULT nextval('public.stagetracking_idstagetracking_seq'::regclass);


--
-- TOC entry 5002 (class 2604 OID 34154)
-- Name: states idstate; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.states ALTER COLUMN idstate SET DEFAULT nextval('public.states_idstate_seq'::regclass);


--
-- TOC entry 5003 (class 2604 OID 34155)
-- Name: students idstudent; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.students ALTER COLUMN idstudent SET DEFAULT nextval('public.students_idstudent_seq'::regclass);


--
-- TOC entry 5005 (class 2604 OID 34156)
-- Name: twofactorauth id2fa; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twofactorauth ALTER COLUMN id2fa SET DEFAULT nextval('public.twofactorauth_id2fa_seq'::regclass);


--
-- TOC entry 5006 (class 2604 OID 34157)
-- Name: users iduser; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN iduser SET DEFAULT nextval('public.users_iduser_seq'::regclass);


--
-- TOC entry 5010 (class 2604 OID 34158)
-- Name: workflows idworkflow; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.workflows ALTER COLUMN idworkflow SET DEFAULT nextval('public.workflows_idworkflow_seq'::regclass);


--
-- TOC entry 5013 (class 2604 OID 34159)
-- Name: workflowstages idworkflowstage; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.workflowstages ALTER COLUMN idworkflowstage SET DEFAULT nextval('public.workflowstages_idworkflowstage_seq'::regclass);


--
-- TOC entry 5331 (class 0 OID 33936)
-- Dependencies: 217
-- Data for Name: academiccalendar; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.academiccalendar (idacademiccalendar, calendarname, academicperiod, startdate, enddate, active, createdat, updatedat) FROM stdin;
\.


--
-- TOC entry 5333 (class 0 OID 33941)
-- Dependencies: 219
-- Data for Name: applications; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.applications (idapplication, applicationcode, creationdate, estimatedcompletiondate, actualcompletiondate, applicationdetails, applicationresolution, rejectionreasonid, currentstagetrackingid, proceduresidprocedure, applicantuserid, priority) FROM stdin;
\.


--
-- TOC entry 5335 (class 0 OID 33949)
-- Dependencies: 221
-- Data for Name: applicationstagehistory; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.applicationstagehistory (idhistory, applicationidapplication, stagetrackingid, enteredat, exitedat, processedbyuserid, comments) FROM stdin;
\.


--
-- TOC entry 5337 (class 0 OID 33956)
-- Dependencies: 223
-- Data for Name: attacheddocuments; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.attacheddocuments (idattacheddocument, applicationsidapplication, requirementid, filename, filepath, filesizebytes, mimetype, uploaddate, uploadedbyuserid) FROM stdin;
\.


--
-- TOC entry 5339 (class 0 OID 33963)
-- Dependencies: 225
-- Data for Name: careers; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.careers (idcareer, careername, careercode, facultiesidfaculty, coordinatoriduser, createdat, updatedat) FROM stdin;
\.


--
-- TOC entry 5341 (class 0 OID 33967)
-- Dependencies: 227
-- Data for Name: configurations; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.configurations (idconfiguration, profilepicturepath, signaturepath, enable_sms, enable_email, enable_whatsapp, notificationfrequency, active, createdat, language, notifications, theme, updatedat) FROM stdin;
1	\N	\N	f	t	f	real_time	t	2026-02-15 12:01:47.493275	es	t	light	\N
2	\N	\N	f	t	f	real_time	t	2026-02-18 05:12:15.831701	es	t	dark	\N
\.


--
-- TOC entry 5343 (class 0 OID 33977)
-- Dependencies: 229
-- Data for Name: credentials; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.credentials (idcredentials, passwordhash, datemodification, lastlogin, failedattempts, accountlocked, passwordexpirydate, active) FROM stdin;
1	$2a$12$VQ8fD/YnB.U.oX6f5f.f.OuC.FvH.f/f.f/f.f/f.f/f.f/f.f/f.	\N	\N	0	f	\N	t
2	$2a$12$TBm/SwBLxJePISiZsLyTxuUoxtUJR5QkUBS902fc0H9WAOOv6zggm	\N	\N	0	f	\N	t
3	$2a$10$/O8OHh6w8ZUAKimY4.lczerzL0jKaRLXfe//vkbvcrBWim1nTfWHq	\N	\N	0	f	2026-08-18	t
\.


--
-- TOC entry 5345 (class 0 OID 33983)
-- Dependencies: 231
-- Data for Name: deadlinerules; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.deadlinerules (iddeadlinerule, rulename, procedurecategory, basedeadlinedays, warningdaysbefore, active, createdat, updatedat) FROM stdin;
\.


--
-- TOC entry 5347 (class 0 OID 33989)
-- Dependencies: 233
-- Data for Name: digitalsignatures; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.digitalsignatures (iddigitalsignature, useriduser, certificatepath, certificateserial, issuer, validfrom, validuntil, signaturealgorithm, active, createdat) FROM stdin;
\.


--
-- TOC entry 5349 (class 0 OID 33997)
-- Dependencies: 235
-- Data for Name: documentsgenerated; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.documentsgenerated (iddocumentgenerated, applicationsidapplication, templateid, documenttype, documentpath, generatedat, generatedbyuserid, digitalsignatureid, signaturetimestamp) FROM stdin;
\.


--
-- TOC entry 5351 (class 0 OID 34004)
-- Dependencies: 237
-- Data for Name: documenttemplates; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.documenttemplates (idtemplate, templatename, templatecode, templatepath, documenttype, version, requiressignature, active, createdat, updatedat) FROM stdin;
\.


--
-- TOC entry 5353 (class 0 OID 34013)
-- Dependencies: 239
-- Data for Name: faculties; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.faculties (idfaculty, facultyname, facultycode, deaniduser, createdat, updatedat, dean_iduser) FROM stdin;
\.


--
-- TOC entry 5355 (class 0 OID 34017)
-- Dependencies: 241
-- Data for Name: notification; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.notification (idnotification, notificationname, message, notificationtypeidnotificationtype, applicationid, recipientuserid, sentat, deliverystatus, deliverychannel, readat, errormessage, retrycount) FROM stdin;
\.


--
-- TOC entry 5357 (class 0 OID 34025)
-- Dependencies: 243
-- Data for Name: notificationtype; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.notificationtype (idnotificationtype, nametypenotification, templatecode, prioritylevel) FROM stdin;
\.


--
-- TOC entry 5359 (class 0 OID 34029)
-- Dependencies: 245
-- Data for Name: permissions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.permissions (idpermission, code, description, createdat, updatedat) FROM stdin;
1	USUARIO_LISTAR	Listar usuarios del sistema	2026-02-17 02:50:32.556109	\N
2	USUARIO_VER	Ver detalle de un usuario	2026-02-17 02:50:32.556109	\N
3	USUARIO_CREAR	Crear un nuevo usuario	2026-02-17 02:50:32.556109	\N
4	USUARIO_MODIFICAR	Modificar datos de un usuario	2026-02-17 02:50:32.556109	\N
5	USUARIO_ELIMINAR	Eliminar un usuario	2026-02-17 02:50:32.556109	\N
6	USUARIO_ACTIVAR	Reactivar un usuario desactivado	2026-02-17 02:50:32.556109	\N
7	USUARIO_DESACTIVAR	Desactivar un usuario (borrado lógico)	2026-02-17 02:50:32.556109	\N
8	ESTUDIANTE_LISTAR	Listar estudiantes	2026-02-17 02:50:32.556109	\N
9	ESTUDIANTE_VER	Ver detalle de un estudiante	2026-02-17 02:50:32.556109	\N
10	ESTUDIANTE_CREAR	Matricular un nuevo estudiante	2026-02-17 02:50:32.556109	\N
11	ESTUDIANTE_MODIFICAR	Modificar datos de un estudiante	2026-02-17 02:50:32.556109	\N
12	ESTUDIANTE_ELIMINAR	Eliminar un estudiante	2026-02-17 02:50:32.556109	\N
13	ESTUDIANTE_PROMOVER	Promover estudiante al siguiente semestre	2026-02-17 02:50:32.556109	\N
14	ESTUDIANTE_GRADUAR	Cambiar estado de estudiante a graduado	2026-02-17 02:50:32.556109	\N
15	ESTUDIANTE_RETIRAR	Cambiar estado de estudiante a retirado	2026-02-17 02:50:32.556109	\N
16	ESTUDIANTE_REACTIVAR	Reactivar un estudiante retirado/inactivo	2026-02-17 02:50:32.556109	\N
17	ROL_LISTAR	Listar roles del sistema	2026-02-17 02:50:32.556109	\N
18	ROL_VER	Ver detalle de un rol	2026-02-17 02:50:32.556109	\N
19	ROL_CREAR	Crear un nuevo rol	2026-02-17 02:50:32.556109	\N
20	ROL_MODIFICAR	Modificar un rol existente	2026-02-17 02:50:32.556109	\N
21	ROL_ELIMINAR	Eliminar un rol (excepto protegidos)	2026-02-17 02:50:32.556109	\N
22	ROL_ASIGNAR_PERMISO	Asignar permisos a un rol	2026-02-17 02:50:32.556109	\N
23	ROL_REMOVER_PERMISO	Remover permisos de un rol	2026-02-17 02:50:32.556109	\N
24	ROL_ASIGNAR_USUARIO	Asignar un rol a un usuario	2026-02-17 02:50:32.556109	\N
25	ROL_REMOVER_USUARIO	Remover un rol de un usuario	2026-02-17 02:50:32.556109	\N
26	CRED_LISTAR	Listar credenciales	2026-02-17 02:50:32.556109	\N
27	CRED_VER	Ver detalle de credencial	2026-02-17 02:50:32.556109	\N
28	CRED_CREAR	Crear credencial	2026-02-17 02:50:32.556109	\N
29	CRED_CAMBIAR_PASS	Cambiar contraseña propia	2026-02-17 02:50:32.556109	\N
30	CRED_RESETEAR_PASS	Resetear contraseña de otro usuario	2026-02-17 02:50:32.556109	\N
31	CRED_BLOQUEAR	Bloquear una cuenta	2026-02-17 02:50:32.556109	\N
32	CRED_DESBLOQUEAR	Desbloquear una cuenta	2026-02-17 02:50:32.556109	\N
33	CRED_ELIMINAR	Eliminar una credencial	2026-02-17 02:50:32.556109	\N
34	AUTH2FA_CONFIGURAR	Configurar 2FA (generar clave TOTP)	2026-02-17 02:50:32.556109	\N
35	AUTH2FA_VERIFICAR	Verificar y activar 2FA	2026-02-17 02:50:32.556109	\N
36	AUTH2FA_DESACTIVAR	Desactivar 2FA	2026-02-17 02:50:32.556109	\N
37	AUTH2FA_ESTADO	Consultar estado de 2FA	2026-02-17 02:50:32.556109	\N
38	AUTH2FA_REGENERAR	Regenerar códigos de respaldo	2026-02-17 02:50:32.556109	\N
39	SOL_LISTAR	Listar solicitudes	2026-02-17 02:50:32.556109	\N
40	SOL_VER	Ver detalle de una solicitud	2026-02-17 02:50:32.556109	\N
41	SOL_CREAR	Crear una solicitud	2026-02-17 02:50:32.556109	\N
42	SOL_MODIFICAR	Modificar una solicitud	2026-02-17 02:50:32.556109	\N
43	SOL_ELIMINAR	Eliminar una solicitud	2026-02-17 02:50:32.556109	\N
44	SOL_RESOLVER	Resolver una solicitud	2026-02-17 02:50:32.556109	\N
45	SOL_RECHAZAR	Rechazar una solicitud	2026-02-17 02:50:32.556109	\N
46	TRAMITE_LISTAR	Listar trámites	2026-02-17 02:50:32.556109	\N
47	TRAMITE_VER	Ver detalle de un trámite	2026-02-17 02:50:32.556109	\N
48	TRAMITE_CREAR	Crear un trámite	2026-02-17 02:50:32.556109	\N
49	TRAMITE_MODIFICAR	Modificar un trámite	2026-02-17 02:50:32.556109	\N
50	TRAMITE_ACTIVAR	Activar un trámite	2026-02-17 02:50:32.556109	\N
51	TRAMITE_DESACTIVAR	Desactivar un trámite	2026-02-17 02:50:32.556109	\N
52	TRAMITE_ELIMINAR	Eliminar un trámite	2026-02-17 02:50:32.556109	\N
53	CAL_CREAR	Crear período académico	2026-02-17 02:50:32.556109	\N
54	CAL_MODIFICAR	Modificar período académico	2026-02-17 02:50:32.556109	\N
55	CAL_ELIMINAR	Eliminar período académico	2026-02-17 02:50:32.556109	\N
56	CAL_LISTAR	Listar períodos académicos	2026-02-17 02:50:32.556109	\N
57	CARRERA_CREAR	Crear una carrera	2026-02-17 02:50:32.556109	\N
58	CARRERA_MODIFICAR	Modificar una carrera	2026-02-17 02:50:32.556109	\N
59	CARRERA_ELIMINAR	Eliminar una carrera	2026-02-17 02:50:32.556109	\N
60	CARRERA_LISTAR	Listar carreras	2026-02-17 02:50:32.556109	\N
61	CONFIG_CREAR	Crear configuración	2026-02-17 02:50:32.556109	\N
62	CONFIG_MODIFICAR	Modificar configuración	2026-02-17 02:50:32.556109	\N
63	CONFIG_ELIMINAR	Eliminar configuración	2026-02-17 02:50:32.556109	\N
64	CONFIG_LISTAR	Listar configuraciones	2026-02-17 02:50:32.556109	\N
65	REGLA_CREAR	Crear regla de plazo	2026-02-17 02:50:32.556109	\N
66	REGLA_MODIFICAR	Modificar regla de plazo	2026-02-17 02:50:32.556109	\N
67	REGLA_ELIMINAR	Eliminar regla de plazo	2026-02-17 02:50:32.556109	\N
68	REGLA_LISTAR	Listar reglas de plazo	2026-02-17 02:50:32.556109	\N
69	PLANTILLA_CREAR	Crear plantilla de documento	2026-02-17 02:50:32.556109	\N
70	PLANTILLA_MODIFICAR	Modificar plantilla de documento	2026-02-17 02:50:32.556109	\N
71	PLANTILLA_ELIMINAR	Eliminar plantilla de documento	2026-02-17 02:50:32.556109	\N
72	PLANTILLA_LISTAR	Listar plantillas de documentos	2026-02-17 02:50:32.556109	\N
73	FACULTAD_CREAR	Crear una facultad	2026-02-17 02:50:32.556109	\N
74	FACULTAD_MODIFICAR	Modificar una facultad	2026-02-17 02:50:32.556109	\N
75	FACULTAD_ELIMINAR	Eliminar una facultad	2026-02-17 02:50:32.556109	\N
76	FACULTAD_LISTAR	Listar facultades	2026-02-17 02:50:32.556109	\N
77	PERMISO_CREAR	Crear un permiso	2026-02-17 02:50:32.556109	\N
78	PERMISO_MODIFICAR	Modificar un permiso	2026-02-17 02:50:32.556109	\N
79	PERMISO_ELIMINAR	Eliminar un permiso	2026-02-17 02:50:32.556109	\N
80	PERMISO_LISTAR	Listar permisos	2026-02-17 02:50:32.556109	\N
81	ETAPA_CREAR	Crear etapa de procesamiento	2026-02-17 02:50:32.556109	\N
82	ETAPA_MODIFICAR	Modificar etapa de procesamiento	2026-02-17 02:50:32.556109	\N
83	ETAPA_ELIMINAR	Eliminar etapa de procesamiento	2026-02-17 02:50:32.556109	\N
84	ETAPA_LISTAR	Listar etapas de procesamiento	2026-02-17 02:50:32.556109	\N
85	RECHAZO_CREAR	Crear motivo de rechazo	2026-02-17 02:50:32.556109	\N
86	RECHAZO_MODIFICAR	Modificar motivo de rechazo	2026-02-17 02:50:32.556109	\N
87	RECHAZO_ELIMINAR	Eliminar motivo de rechazo	2026-02-17 02:50:32.556109	\N
88	RECHAZO_LISTAR	Listar motivos de rechazo	2026-02-17 02:50:32.556109	\N
89	ESTADO_CREAR	Crear un estado	2026-02-17 02:50:32.556109	\N
90	ESTADO_MODIFICAR	Modificar un estado	2026-02-17 02:50:32.556109	\N
91	ESTADO_ELIMINAR	Eliminar un estado	2026-02-17 02:50:32.556109	\N
92	ESTADO_LISTAR	Listar estados	2026-02-17 02:50:32.556109	\N
93	FLUJO_CREAR	Crear flujo de trabajo	2026-02-17 02:50:32.556109	\N
94	FLUJO_MODIFICAR	Modificar flujo de trabajo	2026-02-17 02:50:32.556109	\N
95	FLUJO_ELIMINAR	Eliminar flujo de trabajo	2026-02-17 02:50:32.556109	\N
96	FLUJO_LISTAR	Listar flujos de trabajo	2026-02-17 02:50:32.556109	\N
97	FLUJOETAPA_LISTAR	Listar relaciones flujo-etapa	2026-02-17 02:50:32.556109	\N
98	FLUJOETAPA_VER	Ver detalle de flujo-etapa	2026-02-17 02:50:32.556109	\N
99	FLUJOETAPA_CREAR	Crear relación flujo-etapa	2026-02-17 02:50:32.556109	\N
100	FLUJOETAPA_MODIFICAR	Modificar relación flujo-etapa	2026-02-17 02:50:32.556109	\N
101	FLUJOETAPA_ELIMINAR	Eliminar relación flujo-etapa	2026-02-17 02:50:32.556109	\N
102	SEGUIMIENTO_LISTAR	Listar seguimientos de etapas	2026-02-17 02:50:32.556109	\N
103	SEGUIMIENTO_VER	Ver detalle de seguimiento	2026-02-17 02:50:32.556109	\N
104	SEGUIMIENTO_CREAR	Crear seguimiento	2026-02-17 02:50:32.556109	\N
105	SEGUIMIENTO_MODIFICAR	Modificar seguimiento	2026-02-17 02:50:32.556109	\N
106	SEGUIMIENTO_ELIMINAR	Eliminar seguimiento	2026-02-17 02:50:32.556109	\N
107	SESION_LISTAR	Listar sesiones activas	2026-02-17 02:50:32.556109	\N
108	SESION_VER	Ver detalle de sesión	2026-02-17 02:50:32.556109	\N
109	SESION_CREAR	Crear sesión	2026-02-17 02:50:32.556109	\N
110	SESION_MODIFICAR	Modificar sesión	2026-02-17 02:50:32.556109	\N
111	SESION_ELIMINAR	Eliminar sesión	2026-02-17 02:50:32.556109	\N
112	TOKEN_LISTAR	Listar refresh tokens	2026-02-17 02:50:32.556109	\N
113	TOKEN_VER	Ver detalle de refresh token	2026-02-17 02:50:32.556109	\N
114	TOKEN_ELIMINAR	Revocar un refresh token	2026-02-17 02:50:32.556109	\N
115	TIPNOTIF_LISTAR	Listar tipos de notificación	2026-02-17 02:50:32.556109	\N
116	TIPNOTIF_VER	Ver detalle de tipo de notificación	2026-02-17 02:50:32.556109	\N
117	TIPNOTIF_CREAR	Crear tipo de notificación	2026-02-17 02:50:32.556109	\N
118	TIPNOTIF_MODIFICAR	Modificar tipo de notificación	2026-02-17 02:50:32.556109	\N
119	TIPNOTIF_ELIMINAR	Eliminar tipo de notificación	2026-02-17 02:50:32.556109	\N
120	NOTIF_LISTAR	Listar notificaciones	2026-02-17 02:50:32.556109	\N
121	NOTIF_VER	Ver detalle de notificación	2026-02-17 02:50:32.556109	\N
122	NOTIF_CREAR	Crear una notificación	2026-02-17 02:50:32.556109	\N
123	NOTIF_MODIFICAR	Modificar una notificación	2026-02-17 02:50:32.556109	\N
124	NOTIF_ELIMINAR	Eliminar una notificación	2026-02-17 02:50:32.556109	\N
125	DOCGEN_LISTAR	Listar documentos generados	2026-02-17 02:50:32.556109	\N
126	DOCGEN_VER	Ver detalle de documento generado	2026-02-17 02:50:32.556109	\N
127	DOCGEN_CREAR	Crear registro de documento generado	2026-02-17 02:50:32.556109	\N
128	DOCGEN_MODIFICAR	Modificar registro de documento generado	2026-02-17 02:50:32.556109	\N
129	DOCGEN_ELIMINAR	Eliminar registro de documento generado	2026-02-17 02:50:32.556109	\N
130	FIRMA_LISTAR	Listar firmas digitales	2026-02-17 02:50:32.556109	\N
131	FIRMA_VER	Ver detalle de firma digital	2026-02-17 02:50:32.556109	\N
132	FIRMA_CREAR	Registrar firma digital	2026-02-17 02:50:32.556109	\N
133	FIRMA_MODIFICAR	Modificar firma digital	2026-02-17 02:50:32.556109	\N
134	FIRMA_ELIMINAR	Eliminar firma digital	2026-02-17 02:50:32.556109	\N
135	DOCADJ_LISTAR	Listar documentos adjuntos	2026-02-17 02:50:32.556109	\N
136	DOCADJ_VER	Ver detalle de documento adjunto	2026-02-17 02:50:32.556109	\N
137	DOCADJ_CREAR	Subir documento adjunto	2026-02-17 02:50:32.556109	\N
138	DOCADJ_MODIFICAR	Modificar metadatos de documento adjunto	2026-02-17 02:50:32.556109	\N
139	DOCADJ_ELIMINAR	Eliminar documento adjunto	2026-02-17 02:50:32.556109	\N
140	HIST_LISTAR	Listar historial de etapas	2026-02-17 02:50:32.556109	\N
141	HIST_VER	Ver detalle de historial	2026-02-17 02:50:32.556109	\N
142	HIST_CREAR	Crear registro de historial	2026-02-17 02:50:32.556109	\N
143	HIST_MODIFICAR	Modificar registro de historial	2026-02-17 02:50:32.556109	\N
144	HIST_ELIMINAR	Eliminar registro de historial	2026-02-17 02:50:32.556109	\N
145	REQUISITO_LISTAR	Listar requisitos de trámites	2026-02-17 02:50:32.556109	\N
146	REQUISITO_VER	Ver detalle de requisito	2026-02-17 02:50:32.556109	\N
147	REQUISITO_CREAR	Crear un requisito	2026-02-17 02:50:32.556109	\N
148	REQUISITO_MODIFICAR	Modificar un requisito	2026-02-17 02:50:32.556109	\N
149	REQUISITO_ELIMINAR	Eliminar un requisito	2026-02-17 02:50:32.556109	\N
150	EMAIL_ENVIAR	Enviar correos electrónicos	2026-02-17 02:50:32.556109	\N
\.


--
-- TOC entry 5361 (class 0 OID 34033)
-- Dependencies: 247
-- Data for Name: procedures; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.procedures (idprocedure, nameprocedure, procedurecode, description, workflowsidworkflow, academiccalendaridacademiccalendar, deadlineruleid, estimateddurationdays, requires2fa, active, createdat, updatedat) FROM stdin;
\.


--
-- TOC entry 5363 (class 0 OID 34042)
-- Dependencies: 249
-- Data for Name: processingstage; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.processingstage (idprocessingstage, stagename, stagecode, stagedescription, stageorder, requiresapproval, maxdurationdays, createdat, updatedat) FROM stdin;
\.


--
-- TOC entry 5365 (class 0 OID 34049)
-- Dependencies: 251
-- Data for Name: refresh_tokens; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.refresh_tokens (id, created_at, device_info, expires_at, revoked, token, user_id) FROM stdin;
1	2026-02-18 06:27:15.707874	UNKNOWN	2026-02-25 06:27:15.679013	f	eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJwaWd1YXZlQHV0ZXEuZWR1LmVjIiwiaWF0IjoxNzcxNDE0MDM1LCJleHAiOjE3NzIwMTg4MzV9.Y3q66_TrVlYC8BwW2sal2YRb3jdJIR_Q3N2gyQuUCJA	6
\.


--
-- TOC entry 5367 (class 0 OID 34055)
-- Dependencies: 253
-- Data for Name: rejectionreasons; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.rejectionreasons (idrejectionreason, reasoncode, reasondescription, category, active, createdat, updatedat) FROM stdin;
\.


--
-- TOC entry 5369 (class 0 OID 34060)
-- Dependencies: 255
-- Data for Name: requirementsoftheprocedure; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.requirementsoftheprocedure (idrequirementsoftheprocedure, proceduresidprocedure, requirementname, requirementdescription, requirementtype, ismandatory, displayorder) FROM stdin;
\.


--
-- TOC entry 5371 (class 0 OID 34067)
-- Dependencies: 257
-- Data for Name: role_permissions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.role_permissions (idrole, idpermission) FROM stdin;
4	1
4	2
4	3
4	4
4	5
4	6
4	7
4	8
4	9
4	10
4	11
4	12
4	13
4	14
4	15
4	16
4	17
4	18
4	19
4	20
4	21
4	22
4	23
4	24
4	25
4	26
4	27
4	28
4	29
4	30
4	31
4	32
4	33
4	34
4	35
4	36
4	37
4	38
4	39
4	40
4	41
4	42
4	43
4	44
4	45
4	46
4	47
4	48
4	49
4	50
4	51
4	52
4	53
4	54
4	55
4	56
4	57
4	58
4	59
4	60
4	61
4	62
4	63
4	64
4	65
4	66
4	67
4	68
4	69
4	70
4	71
4	72
4	73
4	74
4	75
4	76
4	77
4	78
4	79
4	80
4	81
4	82
4	83
4	84
4	85
4	86
4	87
4	88
4	89
4	90
4	91
4	92
4	93
4	94
4	95
4	96
4	97
4	98
4	99
4	100
4	101
4	102
4	103
4	104
4	105
4	106
4	107
4	108
4	109
4	110
4	111
4	112
4	113
4	114
4	115
4	116
4	117
4	118
4	119
4	120
4	121
4	122
4	123
4	124
4	125
4	126
4	127
4	128
4	129
4	130
4	131
4	132
4	133
4	134
4	135
4	136
4	137
4	138
4	139
4	140
4	141
4	142
4	143
4	144
4	145
4	146
4	147
4	148
4	149
4	150
1	29
1	34
1	35
1	36
1	37
1	38
1	39
1	40
1	41
1	46
1	47
1	102
1	103
1	120
1	121
1	135
1	136
1	137
1	140
1	141
1	145
1	146
2	1
2	2
2	8
2	9
2	10
2	11
2	12
2	13
2	14
2	15
2	16
2	27
2	29
2	34
2	35
2	36
2	37
2	38
2	39
2	40
2	41
2	42
2	43
2	44
2	45
2	46
2	47
2	102
2	103
2	104
2	105
2	120
2	121
2	122
2	125
2	126
2	127
2	135
2	136
2	137
2	138
2	140
2	141
2	142
2	145
2	146
2	150
3	1
3	2
3	8
3	9
3	10
3	11
3	12
3	13
3	14
3	15
3	16
3	27
3	29
3	34
3	35
3	36
3	37
3	38
3	39
3	40
3	41
3	42
3	43
3	44
3	45
3	46
3	47
3	102
3	103
3	104
3	105
3	120
3	121
3	122
3	125
3	126
3	127
3	135
3	136
3	137
3	138
3	140
3	141
3	142
3	145
3	146
3	150
\.


--
-- TOC entry 5372 (class 0 OID 34070)
-- Dependencies: 258
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.roles (idrole, rolename, roledescription, active, description, name) FROM stdin;
1	ROLE_STUDENT	Usuario que solicita trámites académicos	t	\N	ESTUDIANTE
2	ROLE_COORDINATOR	Responsable de revisar trámites de carrera	t	\N	COORDINADOR
3	ROLE_DEAN	Autoridad máxima de facultad para aprobación final	t	\N	DECANO
4	ROLE_ADMIN	Gestión total del sistema y seguridad	t	\N	ADMINISTRADOR
\.


--
-- TOC entry 5374 (class 0 OID 34076)
-- Dependencies: 260
-- Data for Name: sessiontokens; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sessiontokens (idsession, userid, token, ipaddress, useragent, createdat, expiresat, isactive, revokedat, usersiduser, lastactivity) FROM stdin;
\.


--
-- TOC entry 5376 (class 0 OID 34083)
-- Dependencies: 262
-- Data for Name: stagetracking; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.stagetracking (idstagetracking, stateidstate, processingstageidprocessingstage, enteredat, completedat, assignedtouserid, notes) FROM stdin;
\.


--
-- TOC entry 5378 (class 0 OID 34090)
-- Dependencies: 264
-- Data for Name: states; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.states (idstate, statename, statedescription, statecategory, createdat, updatedat) FROM stdin;
\.


--
-- TOC entry 5380 (class 0 OID 34094)
-- Dependencies: 266
-- Data for Name: students; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.students (idstudent, semester, parallel, usersiduser, careersidcareer, enrollmentdate, status, id_student, academic_period, active, enrollment_number, updated_at) FROM stdin;
\.


--
-- TOC entry 5383 (class 0 OID 34100)
-- Dependencies: 269
-- Data for Name: twofactorauth; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.twofactorauth (id2fa, backupcodes, enabled, secretkey, verifiedat, credentialsidcredentials) FROM stdin;
\.


--
-- TOC entry 5385 (class 0 OID 34106)
-- Dependencies: 271
-- Data for Name: user_roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_roles (iduser, idrole) FROM stdin;
5	4
6	3
\.


--
-- TOC entry 5386 (class 0 OID 34109)
-- Dependencies: 272
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (iduser, names, surnames, cardid, institutionalemail, personalmail, phonenumber, statement, configurationsidconfiguration, credentialsidcredentials, createdat, updatedat, active) FROM stdin;
1	Roberto Javier	Pico Intriago	1309998877	rj_pico@uteq.edu.ec	rober_pico@hotmail.com	0948887766	t	1	\N	2026-02-16 23:12:41.262769	\N	t
2	Jamir Alberto	Anchundia Castro	1206554433	janchundiac@uteq.edu.ec	jamir.dev@gmail.com	0991234567	t	1	\N	2026-02-16 23:14:44.434099	\N	t
3	María Fernanda	Rodríguez Solís	1207889911	mrodriguezs@uteq.edu.ec	mafer.solis@outlook.com	0987654321	t	1	\N	2026-02-16 23:14:55.246253	\N	t
4	Carlos Andrés	Mendoza Vera	1204445566	cmendozav@uteq.edu.ec	carlos.mendoza.v@gmail.com	0965554433	t	1	\N	2026-02-16 23:15:07.715072	\N	t
5	Admin	Sistema	0000000000	admin@universidad.edu.ec	admin.personal@mail.com	0999999999	t	2	2	2026-02-18 05:13:47.689298	\N	t
6	Admin	Piguave	0999999999	piguave@uteq.edu.ec	admin.piguave@system.local	0999999999	t	1	3	2026-02-18 06:21:07.726932	\N	t
\.


--
-- TOC entry 5388 (class 0 OID 34118)
-- Dependencies: 274
-- Data for Name: workflows; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.workflows (idworkflow, workflowname, workflowdescription, createdat, active, updatedat) FROM stdin;
\.


--
-- TOC entry 5390 (class 0 OID 34126)
-- Dependencies: 276
-- Data for Name: workflowstages; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.workflowstages (idworkflowstage, workflowidworkflow, processingstageidprocessingstage, sequenceorder, isoptional) FROM stdin;
\.


--
-- TOC entry 5427 (class 0 OID 0)
-- Dependencies: 218
-- Name: academiccalendar_idacademiccalendar_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.academiccalendar_idacademiccalendar_seq', 1, false);


--
-- TOC entry 5428 (class 0 OID 0)
-- Dependencies: 220
-- Name: applications_idapplication_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.applications_idapplication_seq', 1, false);


--
-- TOC entry 5429 (class 0 OID 0)
-- Dependencies: 222
-- Name: applicationstagehistory_idhistory_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.applicationstagehistory_idhistory_seq', 1, false);


--
-- TOC entry 5430 (class 0 OID 0)
-- Dependencies: 224
-- Name: attacheddocuments_idattacheddocument_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.attacheddocuments_idattacheddocument_seq', 1, false);


--
-- TOC entry 5431 (class 0 OID 0)
-- Dependencies: 226
-- Name: careers_idcareer_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.careers_idcareer_seq', 1, false);


--
-- TOC entry 5432 (class 0 OID 0)
-- Dependencies: 228
-- Name: configurations_idconfiguration_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.configurations_idconfiguration_seq', 2, true);


--
-- TOC entry 5433 (class 0 OID 0)
-- Dependencies: 230
-- Name: credentials_idcredentials_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.credentials_idcredentials_seq', 3, true);


--
-- TOC entry 5434 (class 0 OID 0)
-- Dependencies: 232
-- Name: deadlinerules_iddeadlinerule_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.deadlinerules_iddeadlinerule_seq', 1, false);


--
-- TOC entry 5435 (class 0 OID 0)
-- Dependencies: 234
-- Name: digitalsignatures_iddigitalsignature_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.digitalsignatures_iddigitalsignature_seq', 1, false);


--
-- TOC entry 5436 (class 0 OID 0)
-- Dependencies: 236
-- Name: documentsgenerated_iddocumentgenerated_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.documentsgenerated_iddocumentgenerated_seq', 1, false);


--
-- TOC entry 5437 (class 0 OID 0)
-- Dependencies: 238
-- Name: documenttemplates_idtemplate_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.documenttemplates_idtemplate_seq', 1, false);


--
-- TOC entry 5438 (class 0 OID 0)
-- Dependencies: 240
-- Name: faculties_idfaculty_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.faculties_idfaculty_seq', 1, false);


--
-- TOC entry 5439 (class 0 OID 0)
-- Dependencies: 242
-- Name: notification_idnotification_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.notification_idnotification_seq', 1, false);


--
-- TOC entry 5440 (class 0 OID 0)
-- Dependencies: 244
-- Name: notificationtype_idnotificationtype_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.notificationtype_idnotificationtype_seq', 1, false);


--
-- TOC entry 5441 (class 0 OID 0)
-- Dependencies: 246
-- Name: permissions_idpermission_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.permissions_idpermission_seq', 151, true);


--
-- TOC entry 5442 (class 0 OID 0)
-- Dependencies: 248
-- Name: procedures_idprocedure_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.procedures_idprocedure_seq', 1, false);


--
-- TOC entry 5443 (class 0 OID 0)
-- Dependencies: 250
-- Name: processingstage_idprocessingstage_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.processingstage_idprocessingstage_seq', 1, false);


--
-- TOC entry 5444 (class 0 OID 0)
-- Dependencies: 252
-- Name: refresh_tokens_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.refresh_tokens_id_seq', 1, true);


--
-- TOC entry 5445 (class 0 OID 0)
-- Dependencies: 254
-- Name: rejectionreasons_idrejectionreason_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.rejectionreasons_idrejectionreason_seq', 1, false);


--
-- TOC entry 5446 (class 0 OID 0)
-- Dependencies: 256
-- Name: requirementsoftheprocedure_idrequirementsoftheprocedure_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.requirementsoftheprocedure_idrequirementsoftheprocedure_seq', 1, false);


--
-- TOC entry 5447 (class 0 OID 0)
-- Dependencies: 259
-- Name: roles_idrole_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.roles_idrole_seq', 4, true);


--
-- TOC entry 5448 (class 0 OID 0)
-- Dependencies: 261
-- Name: sessiontokens_idsession_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.sessiontokens_idsession_seq', 1, false);


--
-- TOC entry 5449 (class 0 OID 0)
-- Dependencies: 263
-- Name: stagetracking_idstagetracking_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.stagetracking_idstagetracking_seq', 1, false);


--
-- TOC entry 5450 (class 0 OID 0)
-- Dependencies: 265
-- Name: states_idstate_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.states_idstate_seq', 1, false);


--
-- TOC entry 5451 (class 0 OID 0)
-- Dependencies: 267
-- Name: students_id_student_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.students_id_student_seq', 1, false);


--
-- TOC entry 5452 (class 0 OID 0)
-- Dependencies: 268
-- Name: students_idstudent_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.students_idstudent_seq', 1, false);


--
-- TOC entry 5453 (class 0 OID 0)
-- Dependencies: 270
-- Name: twofactorauth_id2fa_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.twofactorauth_id2fa_seq', 1, false);


--
-- TOC entry 5454 (class 0 OID 0)
-- Dependencies: 273
-- Name: users_iduser_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_iduser_seq', 6, true);


--
-- TOC entry 5455 (class 0 OID 0)
-- Dependencies: 275
-- Name: workflows_idworkflow_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.workflows_idworkflow_seq', 1, false);


--
-- TOC entry 5456 (class 0 OID 0)
-- Dependencies: 277
-- Name: workflowstages_idworkflowstage_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.workflowstages_idworkflowstage_seq', 1, false);


--
-- TOC entry 5016 (class 2606 OID 34161)
-- Name: academiccalendar academiccalendar_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.academiccalendar
    ADD CONSTRAINT academiccalendar_pkey PRIMARY KEY (idacademiccalendar);


--
-- TOC entry 5018 (class 2606 OID 34163)
-- Name: applications applications_applicationcode_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.applications
    ADD CONSTRAINT applications_applicationcode_key UNIQUE (applicationcode);


--
-- TOC entry 5020 (class 2606 OID 34165)
-- Name: applications applications_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.applications
    ADD CONSTRAINT applications_pkey PRIMARY KEY (idapplication);


--
-- TOC entry 5026 (class 2606 OID 34167)
-- Name: applicationstagehistory applicationstagehistory_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.applicationstagehistory
    ADD CONSTRAINT applicationstagehistory_pkey PRIMARY KEY (idhistory);


--
-- TOC entry 5030 (class 2606 OID 34169)
-- Name: attacheddocuments attacheddocuments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.attacheddocuments
    ADD CONSTRAINT attacheddocuments_pkey PRIMARY KEY (idattacheddocument);


--
-- TOC entry 5035 (class 2606 OID 34171)
-- Name: careers careers_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.careers
    ADD CONSTRAINT careers_pkey PRIMARY KEY (idcareer);


--
-- TOC entry 5038 (class 2606 OID 34173)
-- Name: configurations configurations_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.configurations
    ADD CONSTRAINT configurations_pkey PRIMARY KEY (idconfiguration);


--
-- TOC entry 5040 (class 2606 OID 34175)
-- Name: credentials credentials_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.credentials
    ADD CONSTRAINT credentials_pkey PRIMARY KEY (idcredentials);


--
-- TOC entry 5042 (class 2606 OID 34177)
-- Name: deadlinerules deadlinerules_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.deadlinerules
    ADD CONSTRAINT deadlinerules_pkey PRIMARY KEY (iddeadlinerule);


--
-- TOC entry 5044 (class 2606 OID 34179)
-- Name: digitalsignatures digitalsignatures_certificateserial_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.digitalsignatures
    ADD CONSTRAINT digitalsignatures_certificateserial_key UNIQUE (certificateserial);


--
-- TOC entry 5046 (class 2606 OID 34181)
-- Name: digitalsignatures digitalsignatures_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.digitalsignatures
    ADD CONSTRAINT digitalsignatures_pkey PRIMARY KEY (iddigitalsignature);


--
-- TOC entry 5049 (class 2606 OID 34183)
-- Name: documentsgenerated documentsgenerated_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.documentsgenerated
    ADD CONSTRAINT documentsgenerated_pkey PRIMARY KEY (iddocumentgenerated);


--
-- TOC entry 5055 (class 2606 OID 34185)
-- Name: documenttemplates documenttemplates_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.documenttemplates
    ADD CONSTRAINT documenttemplates_pkey PRIMARY KEY (idtemplate);


--
-- TOC entry 5057 (class 2606 OID 34187)
-- Name: documenttemplates documenttemplates_templatecode_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.documenttemplates
    ADD CONSTRAINT documenttemplates_templatecode_key UNIQUE (templatecode);


--
-- TOC entry 5059 (class 2606 OID 34189)
-- Name: faculties faculties_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.faculties
    ADD CONSTRAINT faculties_pkey PRIMARY KEY (idfaculty);


--
-- TOC entry 5064 (class 2606 OID 34191)
-- Name: notification notification_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notification
    ADD CONSTRAINT notification_pkey PRIMARY KEY (idnotification);


--
-- TOC entry 5066 (class 2606 OID 34193)
-- Name: notificationtype notificationtype_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notificationtype
    ADD CONSTRAINT notificationtype_pkey PRIMARY KEY (idnotificationtype);


--
-- TOC entry 5068 (class 2606 OID 34195)
-- Name: permissions permissions_code_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permissions
    ADD CONSTRAINT permissions_code_key UNIQUE (code);


--
-- TOC entry 5070 (class 2606 OID 34197)
-- Name: permissions permissions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permissions
    ADD CONSTRAINT permissions_pkey PRIMARY KEY (idpermission);


--
-- TOC entry 5075 (class 2606 OID 34199)
-- Name: procedures procedures_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.procedures
    ADD CONSTRAINT procedures_pkey PRIMARY KEY (idprocedure);


--
-- TOC entry 5077 (class 2606 OID 34201)
-- Name: procedures procedures_procedurecode_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.procedures
    ADD CONSTRAINT procedures_procedurecode_key UNIQUE (procedurecode);


--
-- TOC entry 5079 (class 2606 OID 34203)
-- Name: processingstage processingstage_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.processingstage
    ADD CONSTRAINT processingstage_pkey PRIMARY KEY (idprocessingstage);


--
-- TOC entry 5081 (class 2606 OID 34205)
-- Name: processingstage processingstage_stagecode_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.processingstage
    ADD CONSTRAINT processingstage_stagecode_key UNIQUE (stagecode);


--
-- TOC entry 5083 (class 2606 OID 34207)
-- Name: refresh_tokens refresh_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT refresh_tokens_pkey PRIMARY KEY (id);


--
-- TOC entry 5087 (class 2606 OID 34209)
-- Name: rejectionreasons rejectionreasons_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rejectionreasons
    ADD CONSTRAINT rejectionreasons_pkey PRIMARY KEY (idrejectionreason);


--
-- TOC entry 5089 (class 2606 OID 34211)
-- Name: rejectionreasons rejectionreasons_reasoncode_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rejectionreasons
    ADD CONSTRAINT rejectionreasons_reasoncode_key UNIQUE (reasoncode);


--
-- TOC entry 5092 (class 2606 OID 34213)
-- Name: requirementsoftheprocedure requirementsoftheprocedure_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.requirementsoftheprocedure
    ADD CONSTRAINT requirementsoftheprocedure_pkey PRIMARY KEY (idrequirementsoftheprocedure);


--
-- TOC entry 5094 (class 2606 OID 34215)
-- Name: role_permissions role_permissions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role_permissions
    ADD CONSTRAINT role_permissions_pkey PRIMARY KEY (idrole, idpermission);


--
-- TOC entry 5096 (class 2606 OID 34217)
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (idrole);


--
-- TOC entry 5098 (class 2606 OID 34219)
-- Name: roles roles_rolename_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_rolename_key UNIQUE (rolename);


--
-- TOC entry 5103 (class 2606 OID 34221)
-- Name: sessiontokens sessiontokens_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sessiontokens
    ADD CONSTRAINT sessiontokens_pkey PRIMARY KEY (idsession);


--
-- TOC entry 5105 (class 2606 OID 34223)
-- Name: sessiontokens sessiontokens_token_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sessiontokens
    ADD CONSTRAINT sessiontokens_token_key UNIQUE (token);


--
-- TOC entry 5110 (class 2606 OID 34225)
-- Name: stagetracking stagetracking_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stagetracking
    ADD CONSTRAINT stagetracking_pkey PRIMARY KEY (idstagetracking);


--
-- TOC entry 5112 (class 2606 OID 34227)
-- Name: states states_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.states
    ADD CONSTRAINT states_pkey PRIMARY KEY (idstate);


--
-- TOC entry 5114 (class 2606 OID 34229)
-- Name: states states_statename_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.states
    ADD CONSTRAINT states_statename_key UNIQUE (statename);


--
-- TOC entry 5118 (class 2606 OID 34231)
-- Name: students students_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.students
    ADD CONSTRAINT students_pkey PRIMARY KEY (idstudent);


--
-- TOC entry 5122 (class 2606 OID 34233)
-- Name: twofactorauth twofactorauth_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twofactorauth
    ADD CONSTRAINT twofactorauth_pkey PRIMARY KEY (id2fa);


--
-- TOC entry 5085 (class 2606 OID 34235)
-- Name: refresh_tokens uk_ghpmfn23vmxfu3spu3lfg4r2d; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT uk_ghpmfn23vmxfu3spu3lfg4r2d UNIQUE (token);


--
-- TOC entry 5124 (class 2606 OID 34237)
-- Name: twofactorauth uk_gx5tcx8nn6mlhqi9x8i3vg2p7; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twofactorauth
    ADD CONSTRAINT uk_gx5tcx8nn6mlhqi9x8i3vg2p7 UNIQUE (credentialsidcredentials);


--
-- TOC entry 5120 (class 2606 OID 34239)
-- Name: students ukbvli2c86kppxltwfx6qjd8klw; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.students
    ADD CONSTRAINT ukbvli2c86kppxltwfx6qjd8klw UNIQUE (enrollment_number);


--
-- TOC entry 5100 (class 2606 OID 34241)
-- Name: roles ukofx66keruapi6vyqpv6f2or37; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT ukofx66keruapi6vyqpv6f2or37 UNIQUE (name);


--
-- TOC entry 5128 (class 2606 OID 34243)
-- Name: users uq_users_cardid; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uq_users_cardid UNIQUE (cardid);


--
-- TOC entry 5130 (class 2606 OID 34245)
-- Name: users uq_users_institutionalemail; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uq_users_institutionalemail UNIQUE (institutionalemail);


--
-- TOC entry 5132 (class 2606 OID 34247)
-- Name: users uq_users_personalmail; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uq_users_personalmail UNIQUE (personalmail);


--
-- TOC entry 5140 (class 2606 OID 34249)
-- Name: workflowstages uq_workflowstagesequence; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.workflowstages
    ADD CONSTRAINT uq_workflowstagesequence UNIQUE (workflowidworkflow, sequenceorder);


--
-- TOC entry 5126 (class 2606 OID 34251)
-- Name: user_roles user_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_pkey PRIMARY KEY (iduser, idrole);


--
-- TOC entry 5134 (class 2606 OID 34253)
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (iduser);


--
-- TOC entry 5136 (class 2606 OID 34255)
-- Name: workflows workflows_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.workflows
    ADD CONSTRAINT workflows_pkey PRIMARY KEY (idworkflow);


--
-- TOC entry 5142 (class 2606 OID 34257)
-- Name: workflowstages workflowstages_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.workflowstages
    ADD CONSTRAINT workflowstages_pkey PRIMARY KEY (idworkflowstage);


--
-- TOC entry 5027 (class 1259 OID 34258)
-- Name: idx_apphistory_application; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_apphistory_application ON public.applicationstagehistory USING btree (applicationidapplication);


--
-- TOC entry 5028 (class 1259 OID 34259)
-- Name: idx_apphistory_stagetracking; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_apphistory_stagetracking ON public.applicationstagehistory USING btree (stagetrackingid);


--
-- TOC entry 5021 (class 1259 OID 34260)
-- Name: idx_applications_applicant; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_applications_applicant ON public.applications USING btree (applicantuserid);


--
-- TOC entry 5022 (class 1259 OID 34261)
-- Name: idx_applications_procedure; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_applications_procedure ON public.applications USING btree (proceduresidprocedure);


--
-- TOC entry 5023 (class 1259 OID 34262)
-- Name: idx_applications_rejectionreason; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_applications_rejectionreason ON public.applications USING btree (rejectionreasonid);


--
-- TOC entry 5024 (class 1259 OID 34263)
-- Name: idx_applications_stagetracking; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_applications_stagetracking ON public.applications USING btree (currentstagetrackingid);


--
-- TOC entry 5031 (class 1259 OID 34264)
-- Name: idx_attacheddocs_application; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_attacheddocs_application ON public.attacheddocuments USING btree (applicationsidapplication);


--
-- TOC entry 5032 (class 1259 OID 34265)
-- Name: idx_attacheddocs_requirement; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_attacheddocs_requirement ON public.attacheddocuments USING btree (requirementid);


--
-- TOC entry 5033 (class 1259 OID 34266)
-- Name: idx_attacheddocs_uploader; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_attacheddocs_uploader ON public.attacheddocuments USING btree (uploadedbyuserid);


--
-- TOC entry 5036 (class 1259 OID 34267)
-- Name: idx_careers_faculties; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_careers_faculties ON public.careers USING btree (facultiesidfaculty);


--
-- TOC entry 5047 (class 1259 OID 34268)
-- Name: idx_digitalsignatures_user; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_digitalsignatures_user ON public.digitalsignatures USING btree (useriduser);


--
-- TOC entry 5050 (class 1259 OID 34269)
-- Name: idx_docsgenerated_application; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_docsgenerated_application ON public.documentsgenerated USING btree (applicationsidapplication);


--
-- TOC entry 5051 (class 1259 OID 34270)
-- Name: idx_docsgenerated_signature; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_docsgenerated_signature ON public.documentsgenerated USING btree (digitalsignatureid);


--
-- TOC entry 5052 (class 1259 OID 34271)
-- Name: idx_docsgenerated_template; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_docsgenerated_template ON public.documentsgenerated USING btree (templateid);


--
-- TOC entry 5053 (class 1259 OID 34272)
-- Name: idx_docsgenerated_user; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_docsgenerated_user ON public.documentsgenerated USING btree (generatedbyuserid);


--
-- TOC entry 5060 (class 1259 OID 34273)
-- Name: idx_notification_application; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_notification_application ON public.notification USING btree (applicationid);


--
-- TOC entry 5061 (class 1259 OID 34274)
-- Name: idx_notification_recipient; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_notification_recipient ON public.notification USING btree (recipientuserid);


--
-- TOC entry 5062 (class 1259 OID 34275)
-- Name: idx_notification_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_notification_type ON public.notification USING btree (notificationtypeidnotificationtype);


--
-- TOC entry 5071 (class 1259 OID 34276)
-- Name: idx_procedures_calendar; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_procedures_calendar ON public.procedures USING btree (academiccalendaridacademiccalendar);


--
-- TOC entry 5072 (class 1259 OID 34277)
-- Name: idx_procedures_deadlinerule; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_procedures_deadlinerule ON public.procedures USING btree (deadlineruleid);


--
-- TOC entry 5073 (class 1259 OID 34278)
-- Name: idx_procedures_workflow; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_procedures_workflow ON public.procedures USING btree (workflowsidworkflow);


--
-- TOC entry 5090 (class 1259 OID 34279)
-- Name: idx_requirements_procedure; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_requirements_procedure ON public.requirementsoftheprocedure USING btree (proceduresidprocedure);


--
-- TOC entry 5101 (class 1259 OID 34280)
-- Name: idx_sessions_user; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sessions_user ON public.sessiontokens USING btree (userid);


--
-- TOC entry 5106 (class 1259 OID 34281)
-- Name: idx_stagetracking_assigneduser; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_stagetracking_assigneduser ON public.stagetracking USING btree (assignedtouserid);


--
-- TOC entry 5107 (class 1259 OID 34282)
-- Name: idx_stagetracking_stage; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_stagetracking_stage ON public.stagetracking USING btree (processingstageidprocessingstage);


--
-- TOC entry 5108 (class 1259 OID 34283)
-- Name: idx_stagetracking_state; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_stagetracking_state ON public.stagetracking USING btree (stateidstate);


--
-- TOC entry 5115 (class 1259 OID 34284)
-- Name: idx_students_careers; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_students_careers ON public.students USING btree (careersidcareer);


--
-- TOC entry 5116 (class 1259 OID 34285)
-- Name: idx_students_users; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_students_users ON public.students USING btree (usersiduser);


--
-- TOC entry 5137 (class 1259 OID 34286)
-- Name: idx_workflowstages_processing; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_workflowstages_processing ON public.workflowstages USING btree (processingstageidprocessingstage);


--
-- TOC entry 5138 (class 1259 OID 34287)
-- Name: idx_workflowstages_workflow; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_workflowstages_workflow ON public.workflowstages USING btree (workflowidworkflow);


--
-- TOC entry 5143 (class 2606 OID 34288)
-- Name: applications applications_applicantuserid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.applications
    ADD CONSTRAINT applications_applicantuserid_fkey FOREIGN KEY (applicantuserid) REFERENCES public.users(iduser);


--
-- TOC entry 5144 (class 2606 OID 34293)
-- Name: applications applications_currentstagetrackingid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.applications
    ADD CONSTRAINT applications_currentstagetrackingid_fkey FOREIGN KEY (currentstagetrackingid) REFERENCES public.stagetracking(idstagetracking);


--
-- TOC entry 5145 (class 2606 OID 34298)
-- Name: applications applications_proceduresidprocedure_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.applications
    ADD CONSTRAINT applications_proceduresidprocedure_fkey FOREIGN KEY (proceduresidprocedure) REFERENCES public.procedures(idprocedure);


--
-- TOC entry 5147 (class 2606 OID 34303)
-- Name: applicationstagehistory applicationstagehistory_applicationidapplication_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.applicationstagehistory
    ADD CONSTRAINT applicationstagehistory_applicationidapplication_fkey FOREIGN KEY (applicationidapplication) REFERENCES public.applications(idapplication) ON DELETE CASCADE;


--
-- TOC entry 5148 (class 2606 OID 34308)
-- Name: applicationstagehistory applicationstagehistory_processedbyuserid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.applicationstagehistory
    ADD CONSTRAINT applicationstagehistory_processedbyuserid_fkey FOREIGN KEY (processedbyuserid) REFERENCES public.users(iduser);


--
-- TOC entry 5149 (class 2606 OID 34313)
-- Name: applicationstagehistory applicationstagehistory_stagetrackingid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.applicationstagehistory
    ADD CONSTRAINT applicationstagehistory_stagetrackingid_fkey FOREIGN KEY (stagetrackingid) REFERENCES public.stagetracking(idstagetracking);


--
-- TOC entry 5150 (class 2606 OID 34318)
-- Name: attacheddocuments attacheddocuments_applicationsidapplication_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.attacheddocuments
    ADD CONSTRAINT attacheddocuments_applicationsidapplication_fkey FOREIGN KEY (applicationsidapplication) REFERENCES public.applications(idapplication) ON DELETE CASCADE;


--
-- TOC entry 5151 (class 2606 OID 34323)
-- Name: attacheddocuments attacheddocuments_requirementid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.attacheddocuments
    ADD CONSTRAINT attacheddocuments_requirementid_fkey FOREIGN KEY (requirementid) REFERENCES public.requirementsoftheprocedure(idrequirementsoftheprocedure);


--
-- TOC entry 5152 (class 2606 OID 34328)
-- Name: attacheddocuments attacheddocuments_uploadedbyuserid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.attacheddocuments
    ADD CONSTRAINT attacheddocuments_uploadedbyuserid_fkey FOREIGN KEY (uploadedbyuserid) REFERENCES public.users(iduser);


--
-- TOC entry 5153 (class 2606 OID 34333)
-- Name: careers careers_facultiesidfaculty_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.careers
    ADD CONSTRAINT careers_facultiesidfaculty_fkey FOREIGN KEY (facultiesidfaculty) REFERENCES public.faculties(idfaculty);


--
-- TOC entry 5155 (class 2606 OID 34338)
-- Name: digitalsignatures digitalsignatures_useriduser_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.digitalsignatures
    ADD CONSTRAINT digitalsignatures_useriduser_fkey FOREIGN KEY (useriduser) REFERENCES public.users(iduser);


--
-- TOC entry 5156 (class 2606 OID 34343)
-- Name: documentsgenerated documentsgenerated_applicationsidapplication_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.documentsgenerated
    ADD CONSTRAINT documentsgenerated_applicationsidapplication_fkey FOREIGN KEY (applicationsidapplication) REFERENCES public.applications(idapplication) ON DELETE CASCADE;


--
-- TOC entry 5157 (class 2606 OID 34348)
-- Name: documentsgenerated documentsgenerated_digitalsignatureid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.documentsgenerated
    ADD CONSTRAINT documentsgenerated_digitalsignatureid_fkey FOREIGN KEY (digitalsignatureid) REFERENCES public.digitalsignatures(iddigitalsignature);


--
-- TOC entry 5158 (class 2606 OID 34353)
-- Name: documentsgenerated documentsgenerated_generatedbyuserid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.documentsgenerated
    ADD CONSTRAINT documentsgenerated_generatedbyuserid_fkey FOREIGN KEY (generatedbyuserid) REFERENCES public.users(iduser);


--
-- TOC entry 5159 (class 2606 OID 34358)
-- Name: documentsgenerated documentsgenerated_templateid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.documentsgenerated
    ADD CONSTRAINT documentsgenerated_templateid_fkey FOREIGN KEY (templateid) REFERENCES public.documenttemplates(idtemplate);


--
-- TOC entry 5168 (class 2606 OID 34363)
-- Name: refresh_tokens fk1lih5y2npsf8u5o3vhdb9y0os; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT fk1lih5y2npsf8u5o3vhdb9y0os FOREIGN KEY (user_id) REFERENCES public.users(iduser);


--
-- TOC entry 5170 (class 2606 OID 35111)
-- Name: role_permissions fk4kjf9ffq1kgfrixd7008yl7wf; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role_permissions
    ADD CONSTRAINT fk4kjf9ffq1kgfrixd7008yl7wf FOREIGN KEY (idrole) REFERENCES public.roles(idrole);


--
-- TOC entry 5171 (class 2606 OID 35106)
-- Name: role_permissions fk5319p0oldd7sd5yr8yjsi2ay9; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role_permissions
    ADD CONSTRAINT fk5319p0oldd7sd5yr8yjsi2ay9 FOREIGN KEY (idpermission) REFERENCES public.permissions(idpermission);


--
-- TOC entry 5146 (class 2606 OID 34368)
-- Name: applications fk_applications_rejectionreason; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.applications
    ADD CONSTRAINT fk_applications_rejectionreason FOREIGN KEY (rejectionreasonid) REFERENCES public.rejectionreasons(idrejectionreason) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- TOC entry 5154 (class 2606 OID 34373)
-- Name: careers fk_careers_coordinator; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.careers
    ADD CONSTRAINT fk_careers_coordinator FOREIGN KEY (coordinatoriduser) REFERENCES public.users(iduser) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- TOC entry 5160 (class 2606 OID 34378)
-- Name: faculties fk_faculties_dean; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.faculties
    ADD CONSTRAINT fk_faculties_dean FOREIGN KEY (deaniduser) REFERENCES public.users(iduser) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- TOC entry 5179 (class 2606 OID 34383)
-- Name: twofactorauth fkgd1du5t2fb6750lw45tyiphx1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.twofactorauth
    ADD CONSTRAINT fkgd1du5t2fb6750lw45tyiphx1 FOREIGN KEY (credentialsidcredentials) REFERENCES public.credentials(idcredentials);


--
-- TOC entry 5161 (class 2606 OID 35097)
-- Name: faculties fkgx33amfj2vn8owbm3jh1g2u5i; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.faculties
    ADD CONSTRAINT fkgx33amfj2vn8owbm3jh1g2u5i FOREIGN KEY (dean_iduser) REFERENCES public.users(iduser);


--
-- TOC entry 5172 (class 2606 OID 34388)
-- Name: sessiontokens fkj9yc93w22y61og04s3r3trx79; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sessiontokens
    ADD CONSTRAINT fkj9yc93w22y61og04s3r3trx79 FOREIGN KEY (usersiduser) REFERENCES public.users(iduser);


--
-- TOC entry 5162 (class 2606 OID 34393)
-- Name: notification notification_applicationid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notification
    ADD CONSTRAINT notification_applicationid_fkey FOREIGN KEY (applicationid) REFERENCES public.applications(idapplication) ON DELETE CASCADE;


--
-- TOC entry 5163 (class 2606 OID 34398)
-- Name: notification notification_notificationtypeidnotificationtype_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notification
    ADD CONSTRAINT notification_notificationtypeidnotificationtype_fkey FOREIGN KEY (notificationtypeidnotificationtype) REFERENCES public.notificationtype(idnotificationtype);


--
-- TOC entry 5164 (class 2606 OID 34403)
-- Name: notification notification_recipientuserid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notification
    ADD CONSTRAINT notification_recipientuserid_fkey FOREIGN KEY (recipientuserid) REFERENCES public.users(iduser) ON DELETE CASCADE;


--
-- TOC entry 5165 (class 2606 OID 34408)
-- Name: procedures procedures_academiccalendaridacademiccalendar_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.procedures
    ADD CONSTRAINT procedures_academiccalendaridacademiccalendar_fkey FOREIGN KEY (academiccalendaridacademiccalendar) REFERENCES public.academiccalendar(idacademiccalendar);


--
-- TOC entry 5166 (class 2606 OID 34413)
-- Name: procedures procedures_deadlineruleid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.procedures
    ADD CONSTRAINT procedures_deadlineruleid_fkey FOREIGN KEY (deadlineruleid) REFERENCES public.deadlinerules(iddeadlinerule);


--
-- TOC entry 5167 (class 2606 OID 34418)
-- Name: procedures procedures_workflowsidworkflow_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.procedures
    ADD CONSTRAINT procedures_workflowsidworkflow_fkey FOREIGN KEY (workflowsidworkflow) REFERENCES public.workflows(idworkflow);


--
-- TOC entry 5169 (class 2606 OID 34423)
-- Name: requirementsoftheprocedure requirementsoftheprocedure_proceduresidprocedure_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.requirementsoftheprocedure
    ADD CONSTRAINT requirementsoftheprocedure_proceduresidprocedure_fkey FOREIGN KEY (proceduresidprocedure) REFERENCES public.procedures(idprocedure) ON DELETE CASCADE;


--
-- TOC entry 5173 (class 2606 OID 34428)
-- Name: sessiontokens sessiontokens_userid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sessiontokens
    ADD CONSTRAINT sessiontokens_userid_fkey FOREIGN KEY (userid) REFERENCES public.users(iduser) ON DELETE CASCADE;


--
-- TOC entry 5174 (class 2606 OID 34433)
-- Name: stagetracking stagetracking_assignedtouserid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stagetracking
    ADD CONSTRAINT stagetracking_assignedtouserid_fkey FOREIGN KEY (assignedtouserid) REFERENCES public.users(iduser);


--
-- TOC entry 5175 (class 2606 OID 34438)
-- Name: stagetracking stagetracking_processingstageidprocessingstage_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stagetracking
    ADD CONSTRAINT stagetracking_processingstageidprocessingstage_fkey FOREIGN KEY (processingstageidprocessingstage) REFERENCES public.processingstage(idprocessingstage);


--
-- TOC entry 5176 (class 2606 OID 34443)
-- Name: stagetracking stagetracking_stateidstate_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stagetracking
    ADD CONSTRAINT stagetracking_stateidstate_fkey FOREIGN KEY (stateidstate) REFERENCES public.states(idstate);


--
-- TOC entry 5177 (class 2606 OID 34448)
-- Name: students students_careersidcareer_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.students
    ADD CONSTRAINT students_careersidcareer_fkey FOREIGN KEY (careersidcareer) REFERENCES public.careers(idcareer);


--
-- TOC entry 5178 (class 2606 OID 34453)
-- Name: students students_usersiduser_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.students
    ADD CONSTRAINT students_usersiduser_fkey FOREIGN KEY (usersiduser) REFERENCES public.users(iduser);


--
-- TOC entry 5180 (class 2606 OID 34458)
-- Name: user_roles user_roles_idrole_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_idrole_fkey FOREIGN KEY (idrole) REFERENCES public.roles(idrole) ON DELETE RESTRICT;


--
-- TOC entry 5181 (class 2606 OID 34463)
-- Name: user_roles user_roles_iduser_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_iduser_fkey FOREIGN KEY (iduser) REFERENCES public.users(iduser) ON DELETE CASCADE;


--
-- TOC entry 5182 (class 2606 OID 34468)
-- Name: users users_configurationsidconfiguration_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_configurationsidconfiguration_fkey FOREIGN KEY (configurationsidconfiguration) REFERENCES public.configurations(idconfiguration);


--
-- TOC entry 5183 (class 2606 OID 34473)
-- Name: users users_credentialsidcredentials_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_credentialsidcredentials_fkey FOREIGN KEY (credentialsidcredentials) REFERENCES public.credentials(idcredentials);


--
-- TOC entry 5184 (class 2606 OID 34478)
-- Name: workflowstages workflowstages_processingstageidprocessingstage_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.workflowstages
    ADD CONSTRAINT workflowstages_processingstageidprocessingstage_fkey FOREIGN KEY (processingstageidprocessingstage) REFERENCES public.processingstage(idprocessingstage);


--
-- TOC entry 5185 (class 2606 OID 34483)
-- Name: workflowstages workflowstages_workflowidworkflow_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.workflowstages
    ADD CONSTRAINT workflowstages_workflowidworkflow_fkey FOREIGN KEY (workflowidworkflow) REFERENCES public.workflows(idworkflow) ON DELETE CASCADE;


-- Completed on 2026-02-18 23:34:46

--
-- PostgreSQL database dump complete
--

