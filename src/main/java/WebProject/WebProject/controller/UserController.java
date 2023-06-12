package WebProject.WebProject.controller;

import WebProject.WebProject.entity.Cart;
import WebProject.WebProject.entity.User;
import WebProject.WebProject.model.Mail;
import WebProject.WebProject.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Controller
public class UserController {
    @Autowired
    UserService userService;

    @Autowired
    CartService cartService;

    @Autowired
    MailService mailService;

    @Autowired
    CloudinaryService cloudinaryService;

    @Autowired
    HttpSession session;

    @Autowired
    CookieService cookie;

    @GetMapping("/signin")
    public String sigInView(Model model) throws Exception {
        Cookie loginName = cookie.read("loginName");
        Cookie pass = cookie.read("pass");
        if (loginName != null)
            model.addAttribute("loginName", loginName.getValue());
        if (pass != null) {
            String decodedValue = new String(Base64.getDecoder().decode(pass.getValue()));
            model.addAttribute("pass", decodedValue);
        }

        return "signin";
    }

    @GetMapping("/signup")
    public String signUpView(Model model) {
        return "signup";
    }

    @GetMapping("/contact")
    public String contactView(Model model) {
        return "contact";
    }

    @GetMapping("/about")
    public String aboutView(Model model) {
        return "about";
    }

    @GetMapping("/blog")
    public String blogView(Model model) {
        return "blog";
    }

    @GetMapping("/blog-details")
    public String blogDetailView(Model model) {
        return "blog-details";
    }

    @PostMapping("/signin")
    public String signIn(@ModelAttribute("login-name") String id, @ModelAttribute("password") String password,
                         @RequestParam(value = "remember", defaultValue = "false") boolean remember, Model model) throws Exception {
        User user = userService.findByIdAndRole(id, "user");
        if (user != null) {
            String decodedValue = new String(Base64.getDecoder().decode(user.getPassword()));
            if (decodedValue.equals(password)) {
                if (remember) {
                    cookie.create("user_name", user.getId(), 3);
                    cookie.create("login_name", user.getId(), 3);
                    cookie.create("pass", user.getPassword(), 3);
                    cookie.create("remember", "remember", 3);
                } else {
                    cookie.create("login_name", user.getId(), 3);
                    cookie.delete("pass");
                }
                session.setAttribute("acc", user);
                List<Cart> listCart = cartService.getAllCartByUserId(user.getId());
                session.setAttribute("countCart", listCart.size());
                return "redirect:/home";
            } else {
                model.addAttribute("errorLogin", "UserName or Password is not correct!");
                return "signin";
            }
        } else {
            model.addAttribute("errorLogin", "UserName or Password is not correct!");
            return "signin";
        }

    }

    @PostMapping("/signup")
    public String signUp(@ModelAttribute("username") String id, @ModelAttribute("your_email") String email,
                         @ModelAttribute("fullname") String fullName, @ModelAttribute("password") String password,@ModelAttribute("phone") String phone,
                         @ModelAttribute("confirm_password") String confirmPassword, Model model) throws Exception {

        User user = userService.findByIdAndRole(id, "user");

        if (user == null) {
            String encodedValue = Base64.getEncoder().encodeToString(password.getBytes());
            String avatar = "https://haycafe.vn/wp-content/uploads/2022/02/Avatar-trang-den.png";
            User newUser = new User(id, "default", "user", encodedValue, fullName, avatar, email, phone , 1, null, null);
            userService.saveUser(newUser);
            return "redirect:/signin";
        } else {
            model.addAttribute("errorSignUp", "Account already exists!");
            return "signup";
        }
    }

    @GetMapping("/signout")
    public String signOut(Model model) {
        session.setAttribute("acc", null);
        cookie.delete("remember");
        return "redirect:/home";
    }

    @GetMapping("/myprofile")
    public String myProfile(Model model, HttpServletRequest request) {
        User user = (User) session.getAttribute("acc");
        String referer = request.getHeader("Referer");
        String messageChangeProfile = (String) session.getAttribute("messageChangeProfile");
        model.addAttribute("messageChangeProfile", messageChangeProfile);
        session.setAttribute("messageChangeProfile", null);
        if (user == null) {
            return "redirect:" + referer;
        } else {
            String error_change_pass = (String) session.getAttribute("error_change_pass");
            String ChangePassSuccess = (String) session.getAttribute("ChangePassSuccess");
            model.addAttribute("error_change_pass", error_change_pass);
            model.addAttribute("ChangePassSuccess", ChangePassSuccess);
            session.setAttribute("error_change_pass", null);
            session.setAttribute("ChangePassSuccess", null);
            model.addAttribute("user", user);
            return "myprofile";
        }

    }

    @PostMapping("/changepassword")
    public String changePassword(Model model, @ModelAttribute("current_password") String currentPassword,
                                 @ModelAttribute("new_password") String newPassword,
                                 @ModelAttribute("confirm_password") String confirm_password, HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        User user = (User) session.getAttribute("acc");
        String decodedValue = new String(Base64.getDecoder().decode(user.getPassword()));
        if (!decodedValue.equals(currentPassword)) {
            session.setAttribute("error_change_pass", "Current Password not correct!");
            return "redirect:/myprofile";
        } else {
            if (!newPassword.equals(confirm_password)) {
                session.setAttribute("error_change_pass", "Confirm New Password not valid!");
                return "redirect:/myprofile";
            } else {
                String encodedValue = Base64.getEncoder().encodeToString(newPassword.getBytes());
                user.setPassword(encodedValue);
                userService.saveUser(user);
                session.setAttribute("acc", user);
            }
        }
        session.setAttribute("ChangePassSuccess", "ChangePassSuccess");
        return "redirect:" + referer;
    }

    @PostMapping("/changeProfile")
    public String changeProfile(Model model, @ModelAttribute("avatar") MultipartFile avatar,
                                @ModelAttribute("fullname") String fullName, @ModelAttribute("phone") String phone,
                                @ModelAttribute("email") String email) throws IOException {
        User user = (User) session.getAttribute("acc");
        if (user != null) {
            if (!avatar.isEmpty()) {
                String url = cloudinaryService.uploadFile(avatar);
                user.setAvatar(url);
            }
            user.setUserName(fullName);
            user.setEmail(email);
            user.setPhoneNumber(phone);
            userService.saveUser(user);
            session.setAttribute("acc", user);
            session.setAttribute("messageChangeProfile", "Change Success.");
            return "redirect:/myprofile";
        } else {
            return "rediect:/home";
        }
    }

    @GetMapping("/forgot")
    public String forgotView(Model model) {
        String errorForgot = (String) session.getAttribute("error_forgot");
        model.addAttribute("error_forgot", errorForgot);
        session.setAttribute("error_forgot", null);
        model.addAttribute("forgot", "Forgot Password");
        return "signin";
    }

    @PostMapping("/forgot")
    public String forgotHandel(@ModelAttribute("login-name") String id, Model model) throws Exception {
        User user = userService.findByIdAndRole(id, "user");
        if (user == null) {
            session.setAttribute("error_forgot", "UserName is not correct!");
            return "redirect:/forgot";
        } else {
            session.setAttribute("userForgot", user);
            return "redirect:/newpass";
        }
    }


    @GetMapping("/newpass")
    public String newPassView(Model model) {
        String errorNewPass = (String) session.getAttribute("errorNewPass");
        session.setAttribute("error_newpass", null);
        model.addAttribute("errorNewPass", errorNewPass);
        model.addAttribute("forgot", "Forgot Password");
        model.addAttribute("changePass", "changePass");
        return "signin";
    }

    @PostMapping("/newpass")
    public String newPassHandel(@ModelAttribute("new_pass") String newPass,
                                @ModelAttribute("confirm_new_pass") String confirmNewPass, Model model) throws Exception {
        if (newPass.equals(confirmNewPass)) {
            String encodedValue = Base64.getEncoder().encodeToString(newPass.getBytes());
            User userForgot = (User) session.getAttribute("userForgot");
            userForgot.setPassword(encodedValue);
            userService.saveUser(userForgot);
            return "redirect:/signin";
        } else {
            session.setAttribute("error_new_pass", "Confirm New Password not valid!");
            return "redirect:/newpass";
        }

    }
}
