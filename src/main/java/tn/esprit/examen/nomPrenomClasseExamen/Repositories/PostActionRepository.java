package tn.esprit.examen.nomPrenomClasseExamen.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.PostAction;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Posts;
import tn.esprit.examen.nomPrenomClasseExamen.entities.TypeAction;
import tn.esprit.examen.nomPrenomClasseExamen.entities.User;

import java.util.List;
import java.util.Optional;

public interface PostActionRepository extends JpaRepository<PostAction, Long> {
 //   List<PostAction> findByPosts(Posts post);

}