package io.bootify.bootify_keycloak_rest.service;

import io.bootify.bootify_keycloak_rest.domain.User;
import io.bootify.bootify_keycloak_rest.model.UserDTO;
import io.bootify.bootify_keycloak_rest.repos.UserRepository;
import io.bootify.bootify_keycloak_rest.util.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Page<UserDTO> findAll(final String filter, final Pageable pageable) {
        Page<User> page;
        if (filter != null) {
            Long longFilter = null;
            try {
                longFilter = Long.parseLong(filter);
            } catch (final NumberFormatException numberFormatException) {
                // keep null - no parseable input
            }
            page = userRepository.findAllById(longFilter, pageable);
        } else {
            page = userRepository.findAll(pageable);
        }
        return new PageImpl<>(page.getContent()
                .stream()
                .map(user -> mapToDTO(user, new UserDTO()))
                .toList(),
                pageable, page.getTotalElements());
    }

    @Override
    public UserDTO get(final Long id) {
        return userRepository.findById(id)
                .map(user -> mapToDTO(user, new UserDTO()))
                .orElseThrow(NotFoundException::new);
    }

    @Override
    public Long create(final UserDTO userDTO) {
        final User user = new User();
        mapToEntity(userDTO, user);
        return userRepository.save(user).getId();
    }

    @Override
    public void update(final Long id, final UserDTO userDTO) {
        final User user = userRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(userDTO, user);
        userRepository.save(user);
    }

    @Override
    public void delete(final Long id) {
        userRepository.deleteById(id);
    }

    private UserDTO mapToDTO(final User user, final UserDTO userDTO) {
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setHash(user.getHash());
        userDTO.setOpenid(user.getOpenid());
        userDTO.setLoginType(user.getLoginType());
        userDTO.setEmail(user.getEmail());
        userDTO.setFirstNname(user.getFirstNname());
        userDTO.setLastNamme(user.getLastNamme());
        return userDTO;
    }

    private User mapToEntity(final UserDTO userDTO, final User user) {
        user.setName(userDTO.getName());
        user.setHash(userDTO.getHash());
        user.setOpenid(userDTO.getOpenid());
        user.setLoginType(userDTO.getLoginType());
        user.setEmail(userDTO.getEmail());
        user.setFirstNname(userDTO.getFirstNname());
        user.setLastNamme(userDTO.getLastNamme());
        return user;
    }

    @Override
    public boolean nameExists(final String name) {
        return userRepository.existsByNameIgnoreCase(name);
    }

}
