package softuniBlog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.thymeleaf.util.StringUtils;
import softuniBlog.entity.Article;
import softuniBlog.entity.Category;
import softuniBlog.entity.User;
import softuniBlog.repository.ArticleRepository;
import softuniBlog.repository.CategoryRepository;
import softuniBlog.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Controller
public class HomeController {

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ArticleRepository articleRepository;

    @GetMapping("/")
    public String index(Model model) {

        List<Category> categories = this.categoryRepository.findAll();

        List<Article> articles = this.articleRepository.findAll();

        model.addAttribute("articles", articles);
        model.addAttribute("view", "home/index");
        model.addAttribute("categories", categories);

        return "base-layout";
    }

    @RequestMapping("/error/403")
    public String accessDenied(Model model){
        model.addAttribute("view", "error/403");

        return "base-layout";
    }

    @GetMapping("/category/{id}")
    public String listArticles(Model model, @PathVariable Integer id){
        if(!this.categoryRepository.exists(id)){
            return "redirect:/";
        }

        List<Category> categories = this.categoryRepository.findAll();

        Category category = this.categoryRepository.findOne(id);
        Set<Article> articles = category.getArticles();

        model.addAttribute("articles", articles);
        model.addAttribute("category", category);
        model.addAttribute("categories", categories);
        model.addAttribute("view", "home/list-articles");

        return "base-layout";
    }

    @GetMapping("/search")
    public String search(HttpServletRequest request, Model model) {
        String query = request.getParameter("query");

        System.out.println(query);

        List<Category> categories = this.categoryRepository.findAll();

        List<Article> articles = this.articleRepository.findAll();

        List<Article> foundArticles = new ArrayList<>();

        for (Article article : articles) {
            boolean articleContainsQuery = StringUtils.containsIgnoreCase(article.getTitle(), query, Locale.getDefault());
            if (articleContainsQuery) {
                foundArticles.add(article);
            }
        }

        model.addAttribute("categories", categories);
        model.addAttribute("articles", foundArticles);
        model.addAttribute("view", "home/search");

        return "base-layout";
    }
}
