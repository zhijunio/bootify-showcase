package io.bootify.bootify_form_jwt_thymeleaf.role;

import io.bootify.bootify_form_jwt_thymeleaf.util.ReferencedWarning;
import io.bootify.bootify_form_jwt_thymeleaf.util.WebUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(final RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public String list(@RequestParam(name = "filter", required = false) final String filter,
            @SortDefault(sort = "id") @PageableDefault(size = 20) final Pageable pageable,
            final Model model) {
        final Page<RoleDTO> roles = roleService.findAll(filter, pageable);
        model.addAttribute("roles", roles);
        model.addAttribute("filter", filter);
        model.addAttribute("paginationModel", WebUtils.getPaginationModel(roles));
        return "role/list";
    }

    @GetMapping("/add")
    public String add(@ModelAttribute("role") final RoleDTO roleDTO) {
        return "role/add";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("role") @Valid final RoleDTO roleDTO,
            final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "role/add";
        }
        roleService.create(roleDTO);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("role.create.success"));
        return "redirect:/roles";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable(name = "id") final Long id, final Model model) {
        model.addAttribute("role", roleService.get(id));
        return "role/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable(name = "id") final Long id,
            @ModelAttribute("role") @Valid final RoleDTO roleDTO, final BindingResult bindingResult,
            final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "role/edit";
        }
        roleService.update(id, roleDTO);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("role.update.success"));
        return "redirect:/roles";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable(name = "id") final Long id,
            final RedirectAttributes redirectAttributes) {
        final ReferencedWarning referencedWarning = roleService.getReferencedWarning(id);
        if (referencedWarning != null) {
            redirectAttributes.addFlashAttribute(WebUtils.MSG_ERROR,
                    WebUtils.getMessage(referencedWarning.getKey(), referencedWarning.getParams().toArray()));
        } else {
            roleService.delete(id);
            redirectAttributes.addFlashAttribute(WebUtils.MSG_INFO, WebUtils.getMessage("role.delete.success"));
        }
        return "redirect:/roles";
    }

}
