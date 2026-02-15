package com.app.uteq.Repository;

import com.app.uteq.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUsersRepository extends JpaRepository<Users, Integer> {
}
