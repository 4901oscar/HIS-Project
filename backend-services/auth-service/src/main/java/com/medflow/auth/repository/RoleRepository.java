package com.medflow.auth.repository;

import com.medflow.auth.domain.Role;
import com.medflow.auth.domain.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Find role by name
     * @param name the role name to search for
     * @return Optional containing the role if found
     */
    Optional<Role> findByName(RoleName name);
}
