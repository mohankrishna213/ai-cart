package org.techm.samples.config;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute
    public void addRemoteUser(Model model, Principal principal) {
        if (principal != null && !"anonymousUser".equals(principal.getName())) {
            model.addAttribute("remoteUser", principal.getName());
        } else {
            model.addAttribute("remoteUser", null);
        }
    }
}
