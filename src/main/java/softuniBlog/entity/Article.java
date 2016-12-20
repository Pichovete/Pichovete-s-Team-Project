package softuniBlog.entity;
import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "articles")
public class Article {
    private Integer id;

    private String title;

    private String content;

    private String description;

    private User author;

    private Category category;

    private Set<Tag> tags;

    private Integer articleLikes;

    private Integer articleDislikes;

    private String likedUsers;

    private String dislikedUsers;

    private Set<Comment> comments;

    public Article(String title, String content,String description, User author, Category category, HashSet<Tag> tags, Integer articleLikes, String likedUsers, Integer articleDislikes, String dislikedUsers) {
        this.title = title;
        this.content = content;
        this.description=description;
        this.author = author;
        this.category = category;
        this.tags = tags;
        this.articleLikes = articleLikes;
        this.likedUsers = likedUsers;
        this.articleDislikes = articleDislikes;
        this.dislikedUsers = dislikedUsers;

    }

    public Article() {

    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(nullable = false)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Column(columnDefinition = "text", nullable = false)
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    @Column(columnDefinition = "text", nullable = true)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ManyToOne()
    @JoinColumn(nullable = false, name = "authorId")
    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    @Transient
    public String getSummary() {
        return this.getDescription().substring(0, this.getDescription().length()/2) + "...";
    }

    @ManyToOne()
    @JoinColumn(nullable = false, name = "categoryId")
    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @ManyToMany
    @JoinColumn(table = "articles_tags")
    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    @Column(name = "articleLikes")
    public Integer getArticleLikes() {
        return articleLikes;
    }

    public void setArticleLikes(Integer articleLikes) {
        this.articleLikes = articleLikes;
    }

    @Column(name = "likedUsers")
    public String getLikedUsers() {
        return likedUsers;
    }

    public void setLikedUsers(String likedUsers) {
        this.likedUsers = likedUsers;
    }

    @Column(name = "articleDislikes")
    public Integer getArticleDislikes() {
        return articleDislikes;
    }

    public void setArticleDislikes(Integer articleDislikes) {
        this.articleDislikes = articleDislikes;
    }

    @Column(name = "dislikedUsers")
    public String getDislikedUsers() {
        return dislikedUsers;
    }

    public void setDislikedUsers(String dislikedUsers) {
        this.dislikedUsers = dislikedUsers;
    }

    @Transient
    public Integer likeCount(Set<String> likes){
        return likes.size();
    }
    @OneToMany(mappedBy = "article")
    public Set<Comment> getComments() {
        return comments;
    }

    public void setComments(Set<Comment> comments) {
        this.comments = comments;
    }
}
