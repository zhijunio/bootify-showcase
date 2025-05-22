package io.bootify.bootify_keycloak_client;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class SessionTestController {

    @GetMapping("/sessionCreate")
    @ResponseBody
    public void sessionCreate(final HttpSession session) {
        session.setAttribute("testAttr", "test");
    }

    @GetMapping("/sessionRead")
    @ResponseBody
    public String sessionRead(final HttpSession session) {
        return ((String)session.getAttribute("testAttr"));
    }

}
