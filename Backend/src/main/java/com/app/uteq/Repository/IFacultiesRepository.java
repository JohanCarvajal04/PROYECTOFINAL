package com.app.uteq.Repository;

import com.app.uteq.Entity.Faculties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface IFacultiesRepository extends JpaRepository<Faculties,Integer> {
    // CREATE -> spi_faculty
    @Transactional
    @Modifying
    @Query(value = "CALL spi_faculty(:facultyname, :facultycode, :deaniduser)", nativeQuery = true)
    void spiFaculty(
            @Param("facultyname") String facultyname,
            @Param("facultycode") String facultycode,
            @Param("deaniduser") Integer deaniduser
    );

    // UPDATE -> spu_faculty
    @Transactional
    @Modifying
    @Query(value = "CALL spu_faculty(:idfaculty, :facultyname, :facultycode, :deaniduser)", nativeQuery = true)
    void spuFaculty(
            @Param("idfaculty") Integer idfaculty,
            @Param("facultyname") String facultyname,
            @Param("facultycode") String facultycode,
            @Param("deaniduser") Integer deaniduser
    );

    // DELETE -> spd_faculty
    @Transactional
    @Modifying
    @Query(value = "CALL spd_faculty(:idfaculty)", nativeQuery = true)
    void spdFaculty(@Param("idfaculty") Integer idfaculty);

    // LIST -> fn_list_faculties (FUNCTION -> SELECT)
    @Query(value = "SELECT * FROM fn_list_faculties()", nativeQuery = true)
    List<Object[]> fnListFaculties();
}
