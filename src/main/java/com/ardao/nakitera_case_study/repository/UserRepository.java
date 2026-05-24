package com.ardao.nakitera_case_study.repository;

import com.ardao.nakitera_case_study.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
     Optional<User> findUserByUsername(String username);
}

