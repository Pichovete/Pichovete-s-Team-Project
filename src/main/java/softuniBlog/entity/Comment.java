package softuniBlog.entity;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by User on 19.12.2016 г..
 */
@Entity
@Table(name="comments")
public class Comment {
    private Integer id;
    private String text;



    private Article article;
    private User user;
    private Date date;

    public Comment() {
    }

    public Comment(String text, Article article, User user, Date date) {
        this.text = text;
        this.article=article;
        this.user=user;
        this.date=date;
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
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    @ManyToOne
    @JoinColumn(name="articleId")
    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }
    @ManyToOne
    @JoinColumn(name="user")
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    @Column(nullable = false)
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}

