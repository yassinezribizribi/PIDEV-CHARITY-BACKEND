package tn.esprit.examen.nomPrenomClasseExamen.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Posts;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscription;

import java.util.List;

public interface PostsRepository extends JpaRepository<Posts, Long> {
    List<Posts> findBySubscription(Subscription subscription);

    @Query("SELECT p.likesCount FROM Posts p WHERE p.idPosts = :postId")
    Long findLikesCountByPostId(@Param("postId") Long postId);

//    @Query("SELECT CASE WHEN COUNT(pl) > 0 THEN true ELSE false END FROM Posts p " +
//            "JOIN p.likedByUsers pl WHERE p.idPosts = :postId AND pl.id = :userId")
//    Boolean hasUserLikedPost(@Param("postId") Long postId, @Param("userId") Long userId);



}