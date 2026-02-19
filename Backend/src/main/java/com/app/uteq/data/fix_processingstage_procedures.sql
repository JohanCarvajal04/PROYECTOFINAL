-- CORRECCIÓN DE PROCEDIMIENTOS DE PROCESSINGSTAGE
-- Cambia TABLA 'processingstages' a 'processingstage'
-- Corrige tipos de datos (TEXT -> CHARACTER VARYING) para coincidir con la tabla

-- 1. Función Listar
DROP FUNCTION IF EXISTS public.fn_list_processingstage();
CREATE OR REPLACE FUNCTION public.fn_list_processingstage()
RETURNS TABLE(
    idprocessingstage integer,
    stagename character varying,
    stagecode character varying,
    stagedescription character varying,
    stageorder integer,
    requiresapproval boolean,
    maxdurationdays integer,
    createdat timestamp without time zone,
    updatedat timestamp without time zone
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY SELECT ps.idprocessingstage, ps.stagename, ps.stagecode, ps.stagedescription, ps.stageorder, ps.requiresapproval, ps.maxdurationdays, ps.createdat, ps.updatedat
    FROM processingstage ps ORDER BY ps.stageorder;
END; $$;

-- 2. Procedimiento Insertar (SPI)
DROP PROCEDURE IF EXISTS public.spi_processingstage(character varying, character varying, text, integer, boolean, integer);
CREATE OR REPLACE PROCEDURE public.spi_processingstage(
    IN p_stagename character varying, 
    IN p_stagecode character varying, 
    IN p_stagedescription character varying,
    IN p_stageorder integer, 
    IN p_requiresapproval boolean, 
    IN p_maxdurationdays integer
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO processingstage (stagename, stagecode, stagedescription, stageorder, requiresapproval, maxdurationdays, createdat)
    VALUES (p_stagename, p_stagecode, p_stagedescription, p_stageorder, p_requiresapproval, p_maxdurationdays, NOW());
END; $$;


-- 3. Procedimiento Actualizar (SPU)
DROP PROCEDURE IF EXISTS public.spu_processingstage(integer, character varying, character varying, text, integer, boolean, integer);
CREATE OR REPLACE PROCEDURE public.spu_processingstage(
    IN p_idprocessingstage integer, 
    IN p_stagename character varying, 
    IN p_stagecode character varying, 
    IN p_stagedescription character varying,
    IN p_stageorder integer, 
    IN p_requiresapproval boolean, 
    IN p_maxdurationdays integer
)
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


-- 4. Procedimiento Eliminar (SPD)
DROP PROCEDURE IF EXISTS public.spd_processingstage(integer);
CREATE OR REPLACE PROCEDURE public.spd_processingstage(IN p_idprocessingstage integer)
LANGUAGE plpgsql
AS $$
BEGIN
    DELETE FROM processingstage WHERE idprocessingstage = p_idprocessingstage;
END; $$;
