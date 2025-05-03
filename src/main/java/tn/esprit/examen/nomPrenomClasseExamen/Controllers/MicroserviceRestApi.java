package tn.esprit.examen.nomPrenomClasseExamen.Controllers;


import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Comment;
import tn.esprit.examen.nomPrenomClasseExamen.services.IService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/comment")
@AllArgsConstructor
public class MicroserviceRestApi {
    @Autowired
    IService service;




    @PostMapping("/ajouterComment/{id}/{iduser}")
    Comment addCommentandAffectToPost(@PathVariable Long id,@PathVariable Long iduser, @RequestBody Comment comment) {
        return  service.addCommentandAffectToPost(id,iduser,comment);
    }


    @GetMapping("/comment_list")
    public List<Comment> findAll() {
        return service.findAll();
    }


    @DeleteMapping("/deleteComment/{id}")
    public String delete(@PathVariable Integer id) {
        service.delete(id);
        return "Comment deleted successfully";
    }


    @PutMapping ("/updateComment/{id}")
    Comment updateComment(@PathVariable Integer id, @RequestBody Comment comment) {
        return  service.updateComment(id, comment);
    }


}
