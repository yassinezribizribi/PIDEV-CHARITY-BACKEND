package tn.esprit.examen.nomPrenomClasseExamen.services;


import org.springframework.data.domain.Page;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Comment;

import java.time.LocalDate;
import java.util.List;

public interface IService {
    Comment addCommentandAffectToPost(Long id,Long iduser ,Comment comment);

    List<Comment> findAll();
    void delete(Integer id);
    Comment updateComment(Integer id , Comment commentDetails);



}
