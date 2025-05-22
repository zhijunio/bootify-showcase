package io.bootify.bootify_baisc.service;

import io.bootify.bootify_baisc.domain.User;
import io.bootify.bootify_baisc.model.UserDTO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;


@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {

    @Mapping(target = "hash", ignore = true)
    UserDTO updateUserDTO(User user, @MappingTarget UserDTO userDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hash", ignore = true)
    User updateUser(UserDTO userDTO, @MappingTarget User user,
            @Context PasswordEncoder passwordEncoder);

    @AfterMapping
    default void afterUpdateUser(UserDTO userDTO, @MappingTarget User user,
            @Context PasswordEncoder passwordEncoder) {
        user.setHash(passwordEncoder.encode(userDTO.getHash()));
    }

}
