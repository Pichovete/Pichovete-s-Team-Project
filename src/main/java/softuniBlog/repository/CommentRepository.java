package softuniBlog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import softuniBlog.entity.Comment;

/**
 * Created by User on 19.12.2016 г..
 */
public interface CommentRepository extends JpaRepository<Comment, Integer> {
}
