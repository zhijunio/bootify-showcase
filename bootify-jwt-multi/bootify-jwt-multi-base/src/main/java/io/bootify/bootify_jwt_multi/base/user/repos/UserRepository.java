package io.bootify.bootify_jwt_multi.base.user.repos;

import io.bootify.bootify_jwt_multi.base.role.domain.Role;
import io.bootify.bootify_jwt_multi.base.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long> {

    User findByNameIgnoreCase(String name);

    User findByOpenid(String openid);

    Page<User> findAllById(Long id, Pageable pageable);

    boolean existsByNameIgnoreCase(String name);

    User findFirstByRole(Role role);

}
