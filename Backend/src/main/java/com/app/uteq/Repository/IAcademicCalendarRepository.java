package com.app.uteq.Repository;

import com.app.uteq.Entity.AcademicCalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IAcademicCalendarRepository extends JpaRepository<AcademicCalendar,Integer> {

    @Query(value = "SELECT * FROM fn_list_academiccalendar(:onlyActive)", nativeQuery = true)
    List<Object[]> fnListAcademicCalendar(@Param("onlyActive") Boolean onlyActive);

    @Transactional
    @Modifying
    @Query(value = "CALL spi_academiccalendar(:calendarname, :academicperiod, :startdate, :enddate, :active)", nativeQuery = true)
    void spiCreateCalendar(
            @Param("calendarname") String calendarname,
            @Param("academicperiod") String academicperiod,
            @Param("startdate") LocalDate startdate,
            @Param("enddate") LocalDate enddate,
            @Param("active") Boolean active
    );

    @Transactional
    @Modifying
    @Query(value = "CALL spu_academiccalendar(:id, :calendarname, :academicperiod, :startdate, :enddate, :active)", nativeQuery = true)
    void spuUpdateCalendar(
            @Param("id") Integer idacademiccalendar,
            @Param("calendarname") String calendarname,
            @Param("academicperiod") String academicperiod,
            @Param("startdate") LocalDate startdate,
            @Param("enddate") LocalDate enddate,
            @Param("active") Boolean active
    );

    @Transactional
    @Modifying
    @Query(value = "CALL spd_academiccalendar(:id)", nativeQuery = true)
    void spdAcademicCalendar(@Param("id") Integer idacademiccalendar);

}
