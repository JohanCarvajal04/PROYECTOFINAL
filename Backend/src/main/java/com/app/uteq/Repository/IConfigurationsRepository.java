package com.app.uteq.Repository;

import com.app.uteq.Entity.Configurations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface IConfigurationsRepository extends JpaRepository<Configurations,Integer> {

    // CREATE
    @Transactional
    @Modifying
    @Query(value = "CALL spi_configuration(:profilepicturepath, :signaturepath, :enable_sms, :enable_email, :enable_whatsapp, :notificationfrequency)", nativeQuery = true)
    void spiConfiguration(
            @Param("profilepicturepath") String profilepicturepath,
            @Param("signaturepath") String signaturepath,
            @Param("enable_sms") Boolean enable_sms,
            @Param("enable_email") Boolean enable_email,
            @Param("enable_whatsapp") Boolean enable_whatsapp,
            @Param("notificationfrequency") String notificationfrequency
    );

    // UPDATE
    @Transactional
    @Modifying
    @Query(value = "CALL spu_configuration(:idconfiguration, :profilepicturepath, :signaturepath, :enable_sms, :enable_email, :enable_whatsapp, :notificationfrequency)", nativeQuery = true)
    void spuConfiguration(
            @Param("idconfiguration") Integer idconfiguration,
            @Param("profilepicturepath") String profilepicturepath,
            @Param("signaturepath") String signaturepath,
            @Param("enable_sms") Boolean enable_sms,
            @Param("enable_email") Boolean enable_email,
            @Param("enable_whatsapp") Boolean enable_whatsapp,
            @Param("notificationfrequency") String notificationfrequency
    );

    // DELETE
    @Transactional
    @Modifying
    @Query(value = "CALL spd_configuration(:idconfiguration)", nativeQuery = true)
    void spdConfiguration(@Param("idconfiguration") Integer idconfiguration);

    // LIST
    @Query(value = "SELECT * FROM fn_list_configurations()", nativeQuery = true)
    List<Object[]> fnListConfigurations();
}
