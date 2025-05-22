package io.bootify.bootify_keycloak_client.controller;

import io.bootify.bootify_keycloak_client.util.WebUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class HomeController {

    @GetMapping("/")
    public String index(
            @RequestParam(name = "logoutsuccess", required = false) final Boolean logoutsuccess,
            final Model model) {
        if (logoutsuccess == Boolean.TRUE) {
            model.addAttribute(WebUtils.MSG_INFO, WebUtils.getMessage("authentication.logout.success"));
        }
        return "home/index";
    }

}
