package tn.esprit.examen.nomPrenomClasseExamen.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tn.esprit.examen.nomPrenomClasseExamen.entities.PostAction;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Posts;
import tn.esprit.examen.nomPrenomClasseExamen.entities.TypeAction;
import tn.esprit.examen.nomPrenomClasseExamen.entities.User;

import java.util.List;
import java.util.Optional;

public interface PostActionRepository extends JpaRepository<PostAction, Long> {
 //   List<PostAction> findByPosts(Posts post);

// Méthode avec JPQL pour compter les likes par post
@Query("SELECT p.idPosts, COUNT(pa) " +
        "FROM Posts p LEFT JOIN p.postActions pa " +
        "WHERE pa.typeAction = tn.esprit.examen.nomPrenomClasseExamen.entities.TypeAction.LIKE " +
        "GROUP BY p.idPosts")
List<Object[]> countLikesByPost();
}