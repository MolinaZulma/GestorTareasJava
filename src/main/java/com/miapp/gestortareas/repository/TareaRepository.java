package com.miapp.gestortareas.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.miapp.gestortareas.model.TareaModel;
import com.miapp.gestortareas.model.UserModel;

public interface TareaRepository extends JpaRepository<TareaModel, Long> {
    Page<TareaModel> findByUsuario(UserModel usuario, Pageable pageable);
    void deleteByUsuario(UserModel usuario);
}
