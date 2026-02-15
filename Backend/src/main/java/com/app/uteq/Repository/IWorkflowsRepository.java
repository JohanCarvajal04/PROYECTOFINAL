package com.app.uteq.Repository;

import com.app.uteq.Entity.Workflows;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface IWorkflowsRepository extends JpaRepository<Workflows,Integer> {
    // CREATE -> spi_workflow
    @Transactional
    @Modifying
    @Query(value = "CALL spi_workflow(:workflowname, :workflowdescription, :active)", nativeQuery = true)
    void spiWorkflow(
            @Param("workflowname") String workflowname,
            @Param("workflowdescription") String workflowdescription,
            @Param("active") Boolean active
    );

    // UPDATE -> spu_workflow
    @Transactional
    @Modifying
    @Query(value = "CALL spu_workflow(:idworkflow, :workflowname, :workflowdescription, :active)", nativeQuery = true)
    void spuWorkflow(
            @Param("idworkflow") Integer idworkflow,
            @Param("workflowname") String workflowname,
            @Param("workflowdescription") String workflowdescription,
            @Param("active") Boolean active
    );

    // DELETE lógico -> spd_workflow
    @Transactional
    @Modifying
    @Query(value = "CALL spd_workflow(:idworkflow)", nativeQuery = true)
    void spdWorkflow(@Param("idworkflow") Integer idworkflow);

    // LIST -> fn_list_workflows (FUNCTION -> SELECT)
    @Query(value = "SELECT * FROM fn_list_workflows(:onlyActive)", nativeQuery = true)
    List<Object[]> fnListWorkflows(@Param("onlyActive") Boolean onlyActive);
}
