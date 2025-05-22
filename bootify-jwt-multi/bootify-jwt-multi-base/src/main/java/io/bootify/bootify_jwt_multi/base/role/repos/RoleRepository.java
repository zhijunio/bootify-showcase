package io.bootify.bootify_jwt_multi.base.role.repos;

import io.bootify.bootify_jwt_multi.base.role.domain.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RoleRepository extends JpaRepository<Role, Long> {

    Page<Role> findAllById(Long id, Pageable pageable);

    boolean existsByNameIgnoreCase(String name);

}
