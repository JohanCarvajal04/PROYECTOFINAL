package com.app.uteq.Repository;

import com.app.uteq.Entity.DeadLinerules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface IDeadLineRulesRepository extends JpaRepository<DeadLinerules,Integer> {
    @Transactional
    @Modifying
    @Query(value = "CALL spi_deadlinerule(:rulename, :procedurecategory, :basedeadlinedays, :warningdaysbefore, :active)", nativeQuery = true)
    void spiDeadlineRule(
            @Param("rulename") String rulename,
            @Param("procedurecategory") String procedurecategory,
            @Param("basedeadlinedays") Integer basedeadlinedays,
            @Param("warningdaysbefore") Integer warningdaysbefore,
            @Param("active") Boolean active
    );

    // UPDATE (spu)
    @Transactional
    @Modifying
    @Query(value = "CALL spu_deadlinerule(:id, :rulename, :procedurecategory, :basedeadlinedays, :warningdaysbefore, :active)", nativeQuery = true)
    void spuDeadlineRule(
            @Param("id") Integer iddeadlinerule,
            @Param("rulename") String rulename,
            @Param("procedurecategory") String procedurecategory,
            @Param("basedeadlinedays") Integer basedeadlinedays,
            @Param("warningdaysbefore") Integer warningdaysbefore,
            @Param("active") Boolean active
    );

    // DELETE lógico (spd)
    @Transactional
    @Modifying
    @Query(value = "CALL spd_deadlinerule(:id)", nativeQuery = true)
    void spdDeadlineRule(@Param("id") Integer iddeadlinerule);

    // LIST (function) -> devuelve filas
    @Query(value = "SELECT * FROM fn_list_deadlinerules(:onlyActive)", nativeQuery = true)
    List<Object[]> fnListDeadlineRules(@Param("onlyActive") Boolean onlyActive);
}
