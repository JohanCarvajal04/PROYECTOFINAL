package com.app.uteq.Repository;

import com.app.uteq.Entity.ProcessingStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface IProcessingStageRepository extends JpaRepository<ProcessingStage,Integer> {
    // CREATE -> spi_processingstage
    @Transactional
    @Modifying
    @Query(value = "CALL spi_processingstage(:stagename, :stagecode, :stagedescription, :stageorder, :requiresapproval, :maxdurationdays)", nativeQuery = true)
    void spiProcessingStage(
            @Param("stagename") String stagename,
            @Param("stagecode") String stagecode,
            @Param("stagedescription") String stagedescription,
            @Param("stageorder") Integer stageorder,
            @Param("requiresapproval") Boolean requiresapproval,
            @Param("maxdurationdays") Integer maxdurationdays
    );

    // UPDATE -> spu_processingstage
    @Transactional
    @Modifying
    @Query(value = "CALL spu_processingstage(:idprocessingstage, :stagename, :stagecode, :stagedescription, :stageorder, :requiresapproval, :maxdurationdays)", nativeQuery = true)
    void spuProcessingStage(
            @Param("idprocessingstage") Integer idprocessingstage,
            @Param("stagename") String stagename,
            @Param("stagecode") String stagecode,
            @Param("stagedescription") String stagedescription,
            @Param("stageorder") Integer stageorder,
            @Param("requiresapproval") Boolean requiresapproval,
            @Param("maxdurationdays") Integer maxdurationdays
    );

    // DELETE -> spd_processingstage
    @Transactional
    @Modifying
    @Query(value = "CALL spd_processingstage(:idprocessingstage)", nativeQuery = true)
    void spdProcessingStage(@Param("idprocessingstage") Integer idprocessingstage);

    // LIST -> fn_list_processingstage (FUNCTION -> SELECT)
    @Query(value = "SELECT * FROM fn_list_processingstage()", nativeQuery = true)
    List<Object[]> fnListProcessingStage();
}
