-- PROCEDIMIENTOS ALMACENADOS DE SEGURIDAD Y USUARIOS

-- -----------------------------------------------------
-- 1. GESTIÓN DE USUARIOS (USERS)
-- -----------------------------------------------------

-- Insertar Usuario
DROP PROCEDURE IF EXISTS public.spi_user(character varying, character varying, character varying, character varying, character varying, character varying, integer, integer);
CREATE OR REPLACE PROCEDURE public.spi_user(
    IN p_names character varying,
    IN p_surnames character varying,
    IN p_cardid character varying,
    IN p_institutionalemail character varying,
    IN p_personalmail character varying,
    IN p_phonenumber character varying,
    IN p_configid integer,
    IN p_credentialid integer,
    OUT p_iduser integer
)
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

-- Actualizar Usuario
DROP PROCEDURE IF EXISTS public.spu_user(integer, character varying, character varying, character varying, character varying, character varying, character varying);
CREATE OR REPLACE PROCEDURE public.spu_user(
    IN p_iduser integer,
    IN p_names character varying,
    IN p_surnames character varying,
    IN p_cardid character varying,
    IN p_institutionalemail character varying,
    IN p_personalmail character varying,
    IN p_phonenumber character varying
)
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

-- Listar Usuarios (Básico)
DROP FUNCTION IF EXISTS public.fn_list_users();
CREATE OR REPLACE FUNCTION public.fn_list_users()
RETURNS TABLE(
    iduser integer, names varchar, surnames varchar, email varchar, active boolean
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY 
    SELECT u.iduser, u.names, u.surnames, u.institutionalemail, u.active 
    FROM public.users u
    ORDER BY u.surnames;
END; $$;

-- -----------------------------------------------------
-- 2. GESTIÓN DE CREDENCIALES (AUTH)
-- -----------------------------------------------------

-- Insertar Credencial
DROP PROCEDURE IF EXISTS public.spi_credential(character varying, date, integer);
CREATE OR REPLACE PROCEDURE public.spi_credential(
    IN p_passwordhash character varying,
    IN p_expirydate date,
    OUT p_idcredential integer
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO public.credentials (
        passwordhash, passwordexpirydate, active, failedattempts, accountlocked
    ) VALUES (
        p_passwordhash, p_expirydate, true, 0, false
    ) RETURNING idcredentials INTO p_idcredential;
END; $$;

-- Obtener Usuario por Email (Para Login)
-- Retorna info básica + hash de contraseña + ids necesarios
DROP FUNCTION IF EXISTS public.fn_get_user_for_login(character varying);
CREATE OR REPLACE FUNCTION public.fn_get_user_for_login(p_email character varying)
RETURNS TABLE (
    iduser integer,
    email character varying,
    passwordhash character varying,
    idcredential integer,
    active boolean,
    accountlocked boolean
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT u.iduser, u.institutionalemail, c.passwordhash, c.idcredentials, u.active, c.accountlocked
    FROM public.users u
    JOIN public.credentials c ON u.credentialsidcredentials = c.idcredentials
    WHERE u.institutionalemail = p_email AND u.active = true;
END; $$;

-- -----------------------------------------------------
-- 3. GESTIÓN DE ROLES Y PERMISOS
-- -----------------------------------------------------

-- Asignar Rol a Usuario
DROP PROCEDURE IF EXISTS public.spi_user_role(integer, integer);
CREATE OR REPLACE PROCEDURE public.spi_user_role(IN p_iduser integer, IN p_idrole integer)
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM public.user_roles WHERE iduser = p_iduser AND idrole = p_idrole) THEN
        INSERT INTO public.user_roles (iduser, idrole) VALUES (p_iduser, p_idrole);
    END IF;
END; $$;

-- Obtener Roles y Permisos de un Usuario (Como String para Spring Security)
-- Retorna lista de 'ROLE_ADMIN', 'PERM_READ', etc.
DROP FUNCTION IF EXISTS public.fn_get_user_authorities(integer);
CREATE OR REPLACE FUNCTION public.fn_get_user_authorities(p_iduser integer)
RETURNS TABLE (authority character varying)
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

-- -----------------------------------------------------
-- 4. REFRESH TOKENS
-- -----------------------------------------------------

-- Insertar Refresh Token
DROP PROCEDURE IF EXISTS public.spi_refresh_token(integer, text, timestamp, character varying);
CREATE OR REPLACE PROCEDURE public.spi_refresh_token(
    IN p_userid integer,
    IN p_token text,
    IN p_expires timestamp,
    IN p_device character varying
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO public.refresh_tokens (
        user_id, token, expires_at, created_at, revoked, device_info
    ) VALUES (
        p_userid, p_token, p_expires, NOW(), false, p_device
    );
END; $$;

-- Revocar Token (Logout o Rotación)
DROP PROCEDURE IF EXISTS public.spu_revoke_token(text);
CREATE OR REPLACE PROCEDURE public.spu_revoke_token(IN p_token text)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE public.refresh_tokens SET revoked = true 
    WHERE token = p_token;
END; $$;

-- Buscar Refresh Token
DROP FUNCTION IF EXISTS public.fn_find_refresh_token(text);
CREATE OR REPLACE FUNCTION public.fn_find_refresh_token(p_token text)
RETURNS TABLE(
    id bigint, token text, expires_at timestamp, revoked boolean, user_id integer
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY SELECT rt.id, rt.token, rt.expires_at, rt.revoked, rt.user_id
    FROM public.refresh_tokens rt
    WHERE rt.token = p_token;
END; $$;
