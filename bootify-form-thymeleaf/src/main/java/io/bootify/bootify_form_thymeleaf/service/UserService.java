package io.bootify.bootify_form_thymeleaf.service;

import io.bootify.bootify_form_thymeleaf.model.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface UserService {

    Page<UserDTO> findAll(String filter, Pageable pageable);

    UserDTO get(Long id);

    Long create(UserDTO userDTO);

    void update(Long id, UserDTO userDTO);

    void delete(Long id);

    boolean nameExists(String name);

}
