package com.jotechhub.user;

import com.jotechhub.role.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    List<User> findByRole(RoleType role);

    List<User> findAllByOrderByCreatedAtDesc();

    List<User> findByRoleOrderByCreatedAtDesc(RoleType role);

    List<User> findByActiveOrderByCreatedAtDesc(Boolean active);

    List<User> findByRoleAndActiveOrderByCreatedAtDesc(RoleType role, Boolean active);
}