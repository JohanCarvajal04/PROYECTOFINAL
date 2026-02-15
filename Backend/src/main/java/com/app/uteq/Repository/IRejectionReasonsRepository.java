package com.app.uteq.Repository;

import com.app.uteq.Entity.RejectionReasons;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface IRejectionReasonsRepository extends JpaRepository<RejectionReasons,Integer> {
    // CREATE -> spi_rejectionreason
    @Transactional
    @Modifying
    @Query(value = "CALL spi_rejectionreason(:reasoncode, :reasondescription, :category, :active)", nativeQuery = true)
    void spiRejectionReason(
            @Param("reasoncode") String reasoncode,
            @Param("reasondescription") String reasondescription,
            @Param("category") String category,
            @Param("active") Boolean active
    );

    // UPDATE -> spu_rejectionreason
    @Transactional
    @Modifying
    @Query(value = "CALL spu_rejectionreason(:idrejectionreason, :reasoncode, :reasondescription, :category, :active)", nativeQuery = true)
    void spuRejectionReason(
            @Param("idrejectionreason") Integer idrejectionreason,
            @Param("reasoncode") String reasoncode,
            @Param("reasondescription") String reasondescription,
            @Param("category") String category,
            @Param("active") Boolean active
    );

    // DELETE lógico -> spd_rejectionreason
    @Transactional
    @Modifying
    @Query(value = "CALL spd_rejectionreason(:idrejectionreason)", nativeQuery = true)
    void spdRejectionReason(@Param("idrejectionreason") Integer idrejectionreason);

    // LIST -> fn_list_rejectionreasons (FUNCTION -> SELECT)
    @Query(value = "SELECT * FROM fn_list_rejectionreasons(:onlyActive)", nativeQuery = true)
    List<Object[]> fnListRejectionReasons(@Param("onlyActive") Boolean onlyActive);
}
