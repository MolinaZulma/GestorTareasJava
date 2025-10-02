package com.miapp.gestortareas.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.miapp.gestortareas.model.UserModel;

public interface UserRepository extends JpaRepository<UserModel, Long> {
    Optional<UserModel> findByEmail(String email);
    boolean existsByEmail(String email);
}
