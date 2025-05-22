package io.bootify.bootify_baisc.repos;

import io.bootify.bootify_baisc.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long> {

    User findByNameIgnoreCase(String name);

    Page<User> findAllById(Long id, Pageable pageable);

    boolean existsByNameIgnoreCase(String name);

}
