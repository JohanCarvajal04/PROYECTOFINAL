package com.app.uteq.Repository;

import com.app.uteq.Entity.States;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface IStatesRepository extends JpaRepository<States,Integer> {
    // CREATE
    @Transactional
    @Modifying
    @Query(value = "CALL spi_state(:statename, :statedescription, :statecategory)", nativeQuery = true)
    void spiState(
            @Param("statename") String statename,
            @Param("statedescription") String statedescription,
            @Param("statecategory") String statecategory
    );

    // UPDATE
    @Transactional
    @Modifying
    @Query(value = "CALL spu_state(:idstate, :statename, :statedescription, :statecategory)", nativeQuery = true)
    void spuState(
            @Param("idstate") Integer idstate,
            @Param("statename") String statename,
            @Param("statedescription") String statedescription,
            @Param("statecategory") String statecategory
    );

    // DELETE
    @Transactional
    @Modifying
    @Query(value = "CALL spd_state(:idstate)", nativeQuery = true)
    void spdState(@Param("idstate") Integer idstate);

    // LIST (FUNCTION)
    @Query(value = "SELECT * FROM fn_list_states(:category)", nativeQuery = true)
    List<Object[]> fnListStates(@Param("category") String category);
}
