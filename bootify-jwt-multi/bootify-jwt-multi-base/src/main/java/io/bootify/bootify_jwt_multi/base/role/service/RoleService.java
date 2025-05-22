package io.bootify.bootify_jwt_multi.base.role.service;

import io.bootify.bootify_jwt_multi.base.role.model.RoleDTO;
import io.bootify.bootify_jwt_multi.base.util.ReferencedWarning;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface RoleService {

    Page<RoleDTO> findAll(String filter, Pageable pageable);

    RoleDTO get(Long id);

    Long create(RoleDTO roleDTO);

    void update(Long id, RoleDTO roleDTO);

    void delete(Long id);

    boolean nameExists(String name);

    ReferencedWarning getReferencedWarning(Long id);

}
