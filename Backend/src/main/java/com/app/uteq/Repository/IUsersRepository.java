package com.app.uteq.Repository;

import com.app.uteq.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

@Repository
public interface IUsersRepository extends JpaRepository<Users, Integer> {
    Optional<Users> findByInstitutionalEmail(String email);
    Optional<Users> findByPersonalMail(String email);

    @Procedure(procedureName = "public.spi_user")
    Integer createUser(
        @Param("p_names") String p_names, 
        @Param("p_surnames") String p_surnames, 
        @Param("p_cardid") String p_cardid, 
        @Param("p_institutionalemail") String p_institutionalemail, 
        @Param("p_personalmail") String p_personalmail, 
        @Param("p_phonenumber") String p_phonenumber, 
        @Param("p_configid") Integer p_configid, 
        @Param("p_credentialid") Integer p_credentialid
    );

    @Procedure(procedureName = "public.spi_user_role")
    void assignRole(@Param("p_iduser") Integer p_iduser, @Param("p_idrole") Integer p_idrole);

    @Procedure(procedureName = "public.spu_user")
    void updateUser(
        @Param("p_iduser") Integer p_iduser,
        @Param("p_names") String p_names, 
        @Param("p_surnames") String p_surnames, 
        @Param("p_cardid") String p_cardid, 
        @Param("p_institutionalemail") String p_institutionalemail, 
        @Param("p_personalmail") String p_personalmail, 
        @Param("p_phonenumber") String p_phonenumber
    );

    @Query(value = "SELECT * FROM public.fn_list_users()", nativeQuery = true)
    List<Object[]> listAllUsers();
}
