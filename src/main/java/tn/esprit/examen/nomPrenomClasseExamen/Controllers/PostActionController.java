package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.PostActionDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.PostAction;
import tn.esprit.examen.nomPrenomClasseExamen.services.IPostActionServices;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/postActions")
public class PostActionController {

    @Autowired
    private IPostActionServices postActionServices;

    // Ajouter une PostAction et l'assigner à un Post
    @PostMapping("/createPostAction")
    public ResponseEntity<PostAction> createPostAction(@RequestBody PostActionDTO postActionDTO) {
        PostAction createdPostAction = postActionServices.createPostActionAndAssignToPost(postActionDTO);
        return ResponseEntity.ok(createdPostAction);
    }

    // Mettre à jour une PostAction
    @PutMapping("/updatePostAction/{id}")
    public ResponseEntity<PostAction> updatePostAction(@PathVariable Long id, @RequestBody PostActionDTO postActionDTO) {
        PostAction postAction = postActionServices.updatePostAction(id, postActionDTO);
        return ResponseEntity.ok(postAction);
    }

    // Supprimer une PostAction
    @DeleteMapping("/deletePostAction/{id}")
    public ResponseEntity<String> deletePostAction(@PathVariable Long id) {
        postActionServices.deletePostAction(id);
        return ResponseEntity.ok("PostAction supprimée avec succès !");
    }

    // Récupérer une PostAction par ID
    @GetMapping("/getPostActionById/{id}")
    public ResponseEntity<PostAction> getPostActionById(@PathVariable Long id) {
        Optional<PostAction> postAction = postActionServices.getPostActionById(id);
        return postAction.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Récupérer toutes les PostActions
    @GetMapping("/getAllPostActions")
    public ResponseEntity<List<PostAction>> getAllPostActions() {
        List<PostAction> postActionsList = postActionServices.getAllPostActions();
        return ResponseEntity.ok(postActionsList);
    }

/*    // Récupérer les PostActions par Post
    @GetMapping("/getPostActionsByPost/{postId}")
    public ResponseEntity<List<PostAction>> getPostActionsByPost(@PathVariable Long postId) {
        List<PostAction> postActionsList = postActionServices.getPostActionsByPost(postId);
        return ResponseEntity.ok(postActionsList);
    }


 */
}
