package com.app.uteq.Repository;

import com.app.uteq.Entity.Permissions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface IPermissionsRepository extends JpaRepository<Permissions,Integer> {
    // CREATE -> spi_permission
    @Transactional
    @Modifying
    @Query(value = "CALL spi_permission(:code, :description)", nativeQuery = true)
    void spiPermission(
            @Param("code") String code,
            @Param("description") String description
    );

    // UPDATE -> spu_permission
    @Transactional
    @Modifying
    @Query(value = "CALL spu_permission(:idpermission, :code, :description)", nativeQuery = true)
    void spuPermission(
            @Param("idpermission") Integer idpermission,
            @Param("code") String code,
            @Param("description") String description
    );

    // DELETE -> spd_permission (físico)
    @Transactional
    @Modifying
    @Query(value = "CALL spd_permission(:idpermission)", nativeQuery = true)
    void spdPermission(@Param("idpermission") Integer idpermission);

    // LIST -> fn_list_permissions (FUNCTION -> SELECT)
    @Query(value = "SELECT * FROM fn_list_permissions()", nativeQuery = true)
    List<Object[]> fnListPermissions();
}
