package softuniBlog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import softuniBlog.entity.Category;
import softuniBlog.entity.Tag;
import softuniBlog.repository.CategoryRepository;
import softuniBlog.repository.TagRepository;

import java.util.List;

@Controller
public class TagController {
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/tag/{name}")
    public String articlesWithTag(Model model, @PathVariable String name){
        Tag tag = this.tagRepository.findByName(name);

        List<Category> categories = this.categoryRepository.findAll();

        if(tag == null){
            return "redirect:/";
        }

        model.addAttribute("categories", categories);
        model.addAttribute("view", "tag/articles");
        model.addAttribute("tag", tag);

        return "base-layout";
    }
}
