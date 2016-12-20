package softuniBlog.entity;

import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    private Integer id;

    private String email;

    private String fullName;

    private String password;

    private String address;

    private String picture;

    private Set<Role> roles;

    private Set<Article> articles;

    private Set<Comment> comments;

    public User(String email, String fullName, String password, String address, String picture) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.address = address;
        this.picture = picture;

        this.roles = new HashSet<>();

        this.articles = new HashSet<>();
    }

    public User() {    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "email", unique = true, nullable = false)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Column(name = "fullName", nullable = false)
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Column(name = "password", length = 60, nullable = false)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "users_roles")
    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    @Column(name = "address")
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @OneToMany(mappedBy = "author")
    public Set<Article> getArticles(){
        return articles;
    }

    public void setArticles(Set<Article> articles){
        this.articles = articles;
    }


    @Column(name = "picture")
    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    @OneToMany(mappedBy = "user")
    public Set<Comment> getComments() {
        return comments;
    }

    public void setComments(Set<Comment> comments) {
        this.comments = comments;
    }


    @Transient
    public boolean isAdmin(){
        return this.getRoles()
                .stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
    }

    @Transient
    public  boolean isLiked(Set<String> likedUsers){
        return likedUsers.contains(this.getId().toString());
    }

    @Transient
    public  boolean isDisiked(Set<String> dislikedUsers){
        return dislikedUsers.contains(this.getId().toString());
    }

    @Transient
    public boolean isAuthor(Article article){
        return Objects.equals(this.getId(),
                article.getAuthor().getId());
    }
}