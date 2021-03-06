package softuniBlog.controller;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import softuniBlog.bindingModel.UserBindingModel;
import softuniBlog.bindingModel.UserEditBindingModel;
import softuniBlog.entity.Category;
import softuniBlog.entity.Role;
import softuniBlog.entity.User;
import softuniBlog.repository.CategoryRepository;
import softuniBlog.repository.RoleRepository;
import softuniBlog.repository.UserRepository;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class UserController {

    @Autowired
    RoleRepository roleRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CategoryRepository categoryRepository;

    @GetMapping("/register")
    public String register(Model model) {
        List<Category> categories = this.categoryRepository.findAll();
        model.addAttribute("categories", categories);

        model.addAttribute("view", "user/register");

        return "base-layout";
    }

    @PostMapping("/register")
    public String registerProcess(UserBindingModel userBindingModel, RedirectAttributes redirectAttributes){

        if (!userBindingModel.getPassword().equals(userBindingModel.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("errors", "Passwords do not match");

            return "redirect:/register";
        }
        if (userBindingModel.getPassword().length() < 8){
            redirectAttributes.addFlashAttribute("errors", "Password length must be at least 8 characters");

            return "redirect:/register";
        }
        boolean userAlreadyExists = this.userRepository.findByEmail(userBindingModel.getEmail()) != null;

        if (userAlreadyExists){
            redirectAttributes.addFlashAttribute("errors", "User with the same email already exist.");

            return "redirect:/register";
        }

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();


        User user = new User(
                userBindingModel.getEmail(),
                userBindingModel.getFullName(),
                bCryptPasswordEncoder.encode(userBindingModel.getPassword()),
                userBindingModel.getAddress(),
                userBindingModel.getPicture().getOriginalFilename()
        );

        Role userRole = this.roleRepository.findByName("ROLE_USER");

        user.addRole(userRole);

        String root = System.getProperty("user.dir");

        MultipartFile file = userBindingModel.getPicture();
        if (file != null){
            String originalFileName = user.getFullName() + file.getOriginalFilename();
            File imageFile=new File(root + "\\src\\main\\resources\\static\\images\\", originalFileName);

            try {
                file.transferTo(imageFile);
                user.setPicture(originalFileName);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }


        this.userRepository.saveAndFlush(user);

        return "redirect:/login";
    }


    @GetMapping("/login")
    public String login(Model model){
        List<Category> categories = this.categoryRepository.findAll();
        model.addAttribute("categories", categories);

        model.addAttribute("view", "user/login");

        return "base-layout";
    }

    @RequestMapping(value="/logout", method = RequestMethod.GET)
    public String logoutPage (HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        return "redirect:/login?logout";
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public String profilePage(Model model){
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        User user = this.userRepository.findByEmail(principal.getUsername());

        List<Category> categories = categoryRepository.findAll();

        model.addAttribute("categories", categories);
        model.addAttribute("user", user);
        model.addAttribute("view", "user/profile");

        return "base-layout";
    }

    @GetMapping("/profile/{id}")
    @PreAuthorize("isAuthenticated()")
    public String userProfilePage(@PathVariable Integer id ,Model model){
        List<Category> categories = categoryRepository.findAll();
        User user = this.userRepository.findOne(id);

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        User userEntity = this.userRepository.findByEmail(principal.getUsername());

        if(userEntity.getId().equals(user.getId())) {
            model.addAttribute("categories", categories);
            model.addAttribute("user", user);
            model.addAttribute("view", "user/profile");
            return "base-layout";
        }

        model.addAttribute("categories", categories);
        model.addAttribute("user", user);
        model.addAttribute("view", "user/profilePage");

        return "base-layout";
    }

    @GetMapping("/user/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String edit(@PathVariable Integer id, Model model){
        User user = this.userRepository.findOne(id);

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        User userEntity = this.userRepository.findByEmail(principal.getUsername());

        if(!user.getId().equals(userEntity.getId())){

            model.addAttribute("user", user);
            model.addAttribute("view", "error/403");

            return "base-layout";
        }

        model.addAttribute("user", user);
        model.addAttribute("view", "user/edit");

        return "base-layout";
    }
    @PostMapping("/user/edit/{id}")
    public String editProcess(@PathVariable Integer id,
                              UserEditBindingModel userBindingModel){

        User user = this.userRepository.findOne(id);

        if(!StringUtils.isEmpty(userBindingModel.getPassword())
                && !StringUtils.isEmpty(userBindingModel.getConfirmPassword())){

            if(userBindingModel.getPassword().equals(userBindingModel.getConfirmPassword())){
                BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

                user.setPassword(bCryptPasswordEncoder.encode(userBindingModel.getPassword()));
            }
        }

        String root = System.getProperty("user.dir");

        MultipartFile file = userBindingModel.getPicture();
        if (file != null){
            String originalFileName = user.getFullName() + file.getOriginalFilename();
            File imageFile=new File(root + "\\src\\main\\resources\\static\\images\\", originalFileName);

            try {
                file.transferTo(imageFile);
                user.setPicture(originalFileName);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        user.setFullName(userBindingModel.getFullName());
        user.setEmail(userBindingModel.getEmail());
        user.setAddress(userBindingModel.getAddress());


        this.userRepository.saveAndFlush(user);

        return "redirect:/profile";
    }
}

