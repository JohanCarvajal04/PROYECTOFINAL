package com.app.uteq.Repository;

import com.app.uteq.Entity.Careers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ICareersRepository extends JpaRepository<Careers,Integer> {
    // CREATE -> spi_career
    @Transactional
    @Modifying
    @Query(value = "CALL spi_career(:careername, :careercode, :facultiesidfaculty, :coordinatoriduser)", nativeQuery = true)
    void spiCareer(
            @Param("careername") String careername,
            @Param("careercode") String careercode,
            @Param("facultiesidfaculty") Integer facultiesidfaculty,
            @Param("coordinatoriduser") Integer coordinatoriduser
    );

    // UPDATE -> spu_career
    @Transactional
    @Modifying
    @Query(value = "CALL spu_career(:idcareer, :careername, :careercode, :facultiesidfaculty, :coordinatoriduser)", nativeQuery = true)
    void spuCareer(
            @Param("idcareer") Integer idcareer,
            @Param("careername") String careername,
            @Param("careercode") String careercode,
            @Param("facultiesidfaculty") Integer facultiesidfaculty,
            @Param("coordinatoriduser") Integer coordinatoriduser
    );

    // DELETE -> spd_career
    @Transactional
    @Modifying
    @Query(value = "CALL spd_career(:idcareer)", nativeQuery = true)
    void spdCareer(@Param("idcareer") Integer idcareer);

    // LIST -> fn_list_careers (FUNCTION -> SELECT)
    @Query(value = "SELECT * FROM fn_list_careers(:facultyid)", nativeQuery = true)
    List<Object[]> fnListCareers(@Param("facultyid") Integer facultyid);
}
