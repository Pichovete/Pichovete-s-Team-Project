package softuniBlog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import softuniBlog.bindingModel.ArticleBindingModel;
import softuniBlog.bindingModel.CommentBindingModel;
import softuniBlog.entity.*;
import softuniBlog.repository.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ArticleController {
    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private CommentRepository commentRepository;

    @GetMapping("/article/create")
    @PreAuthorize("isAuthenticated()")
    public String create(Model model){
        model.addAttribute("view", "article/create");

        List<Category> categories = this.categoryRepository.findAll();

        model.addAttribute("categories", categories);

        return "base-layout";
    }

    @PostMapping("/article/create")
    @PreAuthorize("isAuthenticated()")
    public String createProcess(ArticleBindingModel articleBindingModel){
        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                                                                  .getAuthentication()
                                                                  .getPrincipal();
        User userEntity = this.userRepository.findByEmail(user.getUsername());

        Category category = this.categoryRepository.findOne(articleBindingModel.getCategoryId());

        HashSet<Tag> tags = this.findTagsFromString(articleBindingModel.getTagString());

        Integer articleLikes = 0;

        Integer articleDislikes = 0;

        String likedUsers = "";

        String dislikedUsers = "";

        Article articleEntity = new Article(
                articleBindingModel.getTitle(),
                articleBindingModel.getContent().substring(32),
                articleBindingModel.getDescription(),
                userEntity,
                category,
                tags,
                articleLikes,
                likedUsers,
                articleDislikes,
                dislikedUsers

        );

        this.articleRepository.saveAndFlush(articleEntity);

        return "redirect:/";
    }

    @GetMapping("/article/{id}")
    public String details(Model model, @PathVariable Integer id){
        if(!this.articleRepository.exists(id)){
            return "redirect:/";
        }

        if(!(SecurityContextHolder.getContext().getAuthentication()
                instanceof AnonymousAuthenticationToken)){
            UserDetails principal = (UserDetails) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();

            User entityUser = this.userRepository.findByEmail(principal.getUsername());

            model.addAttribute("user", entityUser);
        }

        Article article = this.articleRepository.findOne(id);

        List<Category> categories = this.categoryRepository.findAll();

        List<Comment> comments = new ArrayList<>();
        Collections.sort(comments, new Comparator<Comment>() {
            public int compare(Comment o1, Comment o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });


        for (Comment comment : article.getComments()) {
            comments.add(comment);
        }

        model.addAttribute("categories", categories);
        model.addAttribute("article", article);
        model.addAttribute("comments", comments);
        model.addAttribute("view", "article/details");

        return "base-layout";
    }
    @GetMapping("/article/comment/{id}")
    public String commentGet(Model model,@PathVariable Integer id){
        Article article = this.articleRepository.findOne(id);
        User author = article.getAuthor();
        model.addAttribute("view","article/comment");
        model.addAttribute("article", article);
        return "base-layout";
    }
    @PostMapping("/article/comment/{id}")
    public String commentPost(CommentBindingModel commentBindingModel, @PathVariable Integer id){
        Article article = this.articleRepository.findOne(id);
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User user = this.userRepository.findByEmail(principal.getUsername());
        Date date=new Date();
        Comment comment=new Comment(
                commentBindingModel.getCommentString(),
                article,
                user,
                date
        );
        this.commentRepository.saveAndFlush(comment);

        return "redirect:/article/" + id;
    }

    @GetMapping("/article/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String edit(@PathVariable Integer id, Model model){
        if(!this.articleRepository.exists(id)){
            return "redirect:/";
        }
        Article article = this.articleRepository.findOne(id);

        if(!isUserAuthorOrAdmin(article)){
            return "redirect:/article/" + id;
        }

        List<Category> categories = this.categoryRepository.findAll();

        String tagString = article.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.joining(","));

        model.addAttribute("view", "article/edit");
        model.addAttribute("article", article);
        model.addAttribute("categories", categories);
        model.addAttribute("tags", tagString);

        return "base-layout";
    }

    @PostMapping("/article/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String editProcess(@PathVariable Integer id, ArticleBindingModel articleBindingModel){
        if(!this.articleRepository.exists(id)){
            return "redirect:/";
        }

        Article article = this.articleRepository.findOne(id);

        if(!isUserAuthorOrAdmin(article)){
            return "redirect:/article/" + id;
        }

        Category category = this.categoryRepository.findOne(articleBindingModel.getCategoryId());

        HashSet<Tag> tags = this.findTagsFromString(articleBindingModel.getTagString());

        article.setContent(articleBindingModel.getContent());
        article.setDescription(articleBindingModel.getDescription());
        article.setTitle(articleBindingModel.getTitle());
        article.setCategory(category);
        article.setTags(tags);

        this.articleRepository.saveAndFlush(article);

        return "redirect:/article/" + article.getId();
    }

    @GetMapping("/article/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String delete(Model model, @PathVariable Integer id){
        if(!this.articleRepository.exists(id)){
            return "redirect:/";
        }

        Article article = this.articleRepository.findOne(id);

        if(!isUserAuthorOrAdmin(article)){
            return "redirect:/article/" + id;
        }

        model.addAttribute("article", article);
        model.addAttribute("view", "article/delete");

        return "base-layout";
    }

    @PostMapping("/article/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String deleteProcess(@PathVariable Integer id){
        if(!this.articleRepository.exists(id)){
            return "redirect:/";
        }

        Article article = this.articleRepository.findOne(id);

        if(!isUserAuthorOrAdmin(article)){
            return "redirect:/article/" + id;
        }

        this.articleRepository.delete(article);

        return "redirect:/";
    }

    private boolean isUserAuthorOrAdmin(Article article){
        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User userEntity = this.userRepository.findByEmail(user.getUsername());

        return userEntity.isAdmin() || userEntity.isAuthor(article);
    }

    private HashSet<Tag> findTagsFromString(String tagString){
        HashSet<Tag> tags = new HashSet<>();

        String[] tagNames = tagString.split(",\\s*");

        for (String tagName : tagNames) {
            Tag currentTag = this.tagRepository.findByName(tagName);

            if(currentTag == null){
                currentTag = new Tag(tagName);
                this.tagRepository.saveAndFlush(currentTag);
            }

            tags.add(currentTag);
        }

        return tags;
    }

    @GetMapping("/article/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public String likeProccess(@PathVariable Integer id, RedirectAttributes redirectAttributes){

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        User user = this.userRepository.findByEmail(principal.getUsername());

        Article article = this.articleRepository.findOne(id);

        Set<String> likes = new HashSet<String>(Arrays.asList(article.getLikedUsers().split(",")));

        Set<String> dislikes = new HashSet<String>(Arrays.asList(article.getDislikedUsers().split(",")));

        if(user.isLiked(likes)){
            redirectAttributes.addFlashAttribute("errors", "You already have liked this post!");
            return "redirect:/article/" + article.getId();
        }

        if(user.isDisiked(dislikes)){
            redirectAttributes.addFlashAttribute("errors", "You already have disliked this post!");
            return "redirect:/article/" + article.getId();
        }

        String likedUsers = article.getLikedUsers() + "," + user.getId();

        Integer articleLikes = article.getArticleLikes() + 1;

        article.setArticleLikes(articleLikes);
        article.setLikedUsers(likedUsers);

        this.articleRepository.saveAndFlush(article);

        return "redirect:/article/" + article.getId();

    }

    @GetMapping("/article/{id}/dislike")
    @PreAuthorize("isAuthenticated()")
    public String dislikeProccess(@PathVariable Integer id, RedirectAttributes redirectAttributes){
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        User user = this.userRepository.findByEmail(principal.getUsername());

        Article article = this.articleRepository.findOne(id);

        Set<String> dislikes = new HashSet<String>(Arrays.asList(article.getDislikedUsers().split(",")));

        Set<String> likes = new HashSet<String>(Arrays.asList(article.getLikedUsers().split(",")));

        if(user.isDisiked(dislikes)){
            redirectAttributes.addFlashAttribute("errors", "You already have disliked this post!");
            return "redirect:/article/" + article.getId();
        }

        if(user.isLiked(likes)){
            redirectAttributes.addFlashAttribute("errors", "You already have liked this post!");
            return "redirect:/article/" + article.getId();
        }

        String dislikedUsers = article.getDislikedUsers() + "," + user.getId();

        Integer articleDislikes = article.getArticleDislikes() + 1;

        article.setArticleDislikes(articleDislikes);
        article.setDislikedUsers(dislikedUsers);

        this.articleRepository.saveAndFlush(article);

        return "redirect:/article/" + article.getId();
    }

}
