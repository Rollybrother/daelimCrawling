package com.daelim.crawling.mainProgram.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("user", new UserVO());
        return "signup";
    }

    @PostMapping("/signup")
    public String signupSubmit(@ModelAttribute UserVO user) {
        userService.save(user, passwordEncoder);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }
    
    @GetMapping("/check-username")
    @ResponseBody
    public String checkUsername(@RequestParam("id") String id) {
        boolean exists = userService.isIdTaken(id);
        if (exists) {
            return "사용할 수 없는 아이디입니다";
        } else {
            return "사용 가능한 아이디입니다";
        }
    }
}
