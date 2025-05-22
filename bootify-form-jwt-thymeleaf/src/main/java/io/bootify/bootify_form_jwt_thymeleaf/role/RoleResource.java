package io.bootify.bootify_form_jwt_thymeleaf.role;

import io.bootify.bootify_form_jwt_thymeleaf.util.ReferencedException;
import io.bootify.bootify_form_jwt_thymeleaf.util.ReferencedWarning;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/roles", produces = MediaType.APPLICATION_JSON_VALUE)
public class RoleResource {

    private final RoleService roleService;

    public RoleResource(final RoleService roleService) {
        this.roleService = roleService;
    }

    @Operation(
            parameters = {
                    @Parameter(
                            name = "page",
                            in = ParameterIn.QUERY,
                            schema = @Schema(implementation = Integer.class)
                    ),
                    @Parameter(
                            name = "size",
                            in = ParameterIn.QUERY,
                            schema = @Schema(implementation = Integer.class)
                    ),
                    @Parameter(
                            name = "sort",
                            in = ParameterIn.QUERY,
                            schema = @Schema(implementation = String.class)
                    )
            }
    )
    @GetMapping
    public ResponseEntity<Page<RoleDTO>> getAllRoles(
            @RequestParam(name = "filter", required = false) final String filter,
            @Parameter(hidden = true) @SortDefault(sort = "id") @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(roleService.findAll(filter, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleDTO> getRole(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(roleService.get(id));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createRole(@RequestBody @Valid final RoleDTO roleDTO) {
        final Long createdId = roleService.create(roleDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Long> updateRole(@PathVariable(name = "id") final Long id,
            @RequestBody @Valid final RoleDTO roleDTO) {
        roleService.update(id, roleDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteRole(@PathVariable(name = "id") final Long id) {
        final ReferencedWarning referencedWarning = roleService.getReferencedWarning(id);
        if (referencedWarning != null) {
            throw new ReferencedException(referencedWarning);
        }
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
