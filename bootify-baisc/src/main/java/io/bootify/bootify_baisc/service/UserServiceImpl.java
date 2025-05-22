package io.bootify.bootify_baisc.service;

import io.bootify.bootify_baisc.domain.User;
import io.bootify.bootify_baisc.model.UserDTO;
import io.bootify.bootify_baisc.repos.UserRepository;
import io.bootify.bootify_baisc.util.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserServiceImpl(final UserRepository userRepository,
            final PasswordEncoder passwordEncoder, final UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
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
                .map(user -> userMapper.updateUserDTO(user, new UserDTO()))
                .toList(),
                pageable, page.getTotalElements());
    }

    @Override
    public UserDTO get(final Long id) {
        return userRepository.findById(id)
                .map(user -> userMapper.updateUserDTO(user, new UserDTO()))
                .orElseThrow(NotFoundException::new);
    }

    @Override
    public Long create(final UserDTO userDTO) {
        final User user = new User();
        userMapper.updateUser(userDTO, user, passwordEncoder);
        return userRepository.save(user).getId();
    }

    @Override
    public void update(final Long id, final UserDTO userDTO) {
        final User user = userRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        userMapper.updateUser(userDTO, user, passwordEncoder);
        userRepository.save(user);
    }

    @Override
    public void delete(final Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public boolean nameExists(final String name) {
        return userRepository.existsByNameIgnoreCase(name);
    }

}
