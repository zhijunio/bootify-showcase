package io.bootify.bootify_keycloak_rest.repos;

import io.bootify.bootify_keycloak_rest.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long> {

    User findByOpenid(String openid);

    Page<User> findAllById(Long id, Pageable pageable);

    boolean existsByNameIgnoreCase(String name);

}
