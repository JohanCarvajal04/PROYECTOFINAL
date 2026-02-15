package com.app.uteq.Repository;

import com.app.uteq.Entity.DocumentTemplates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface IDocumentTemplatesRepository extends JpaRepository<DocumentTemplates,Integer> {
    // CREATE -> spi_documenttemplate
    @Transactional
    @Modifying
    @Query(value = "CALL spi_documenttemplate(:templatename, :templatecode, :templatepath, :documenttype, :version, :requiressignature, :active)", nativeQuery = true)
    void spiDocumentTemplate(
            @Param("templatename") String templatename,
            @Param("templatecode") String templatecode,
            @Param("templatepath") String templatepath,
            @Param("documenttype") String documenttype,
            @Param("version") String version,
            @Param("requiressignature") Boolean requiressignature,
            @Param("active") Boolean active
    );

    // UPDATE -> spu_documenttemplate
    @Transactional
    @Modifying
    @Query(value = "CALL spu_documenttemplate(:idtemplate, :templatename, :templatecode, :templatepath, :documenttype, :version, :requiressignature, :active)", nativeQuery = true)
    void spuDocumentTemplate(
            @Param("idtemplate") Integer idtemplate,
            @Param("templatename") String templatename,
            @Param("templatecode") String templatecode,
            @Param("templatepath") String templatepath,
            @Param("documenttype") String documenttype,
            @Param("version") String version,
            @Param("requiressignature") Boolean requiressignature,
            @Param("active") Boolean active
    );

    // DELETE lógico -> spd_documenttemplate
    @Transactional
    @Modifying
    @Query(value = "CALL spd_documenttemplate(:idtemplate)", nativeQuery = true)
    void spdDocumentTemplate(@Param("idtemplate") Integer idtemplate);

    // LIST -> fn_list_documenttemplates (FUNCTION -> SELECT)
    @Query(value = "SELECT * FROM fn_list_documenttemplates(:onlyActive)", nativeQuery = true)
    List<Object[]> fnListDocumentTemplates(@Param("onlyActive") Boolean onlyActive);
}
