package com.example.domain.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.domain.entities.User;
import com.example.domain.enums.UserRole;
import com.example.domain.enums.UserStatus;

public interface UserRepository extends JpaRepository<User, Long> {

     Optional<User> findByEmail(String email);
     List<User> findByRole(UserRole role);
     List<User> findByStatus(UserStatus status);
      Optional<User> findByPhone(String phone);

}
