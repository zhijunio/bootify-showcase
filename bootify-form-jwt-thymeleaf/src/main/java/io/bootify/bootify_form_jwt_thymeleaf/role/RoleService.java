package io.bootify.bootify_form_jwt_thymeleaf.role;

import io.bootify.bootify_form_jwt_thymeleaf.util.ReferencedWarning;
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
