package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.AssociationDto;
import tn.esprit.examen.nomPrenomClasseExamen.services.AssociationServices;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/associations")
@AllArgsConstructor
public class AssociationController {
    private final AssociationServices associationServices;

    // For retrieving all associations (token is not needed)
    @GetMapping
    public ResponseEntity<List<AssociationDto>> getAllAssociations() {
        List<AssociationDto> associations = associationServices.getAllAssociations();
        return ResponseEntity.ok(associations);
    }

    // For retrieving a single association by ID (token is not needed)
    @GetMapping("/{id}")
    public ResponseEntity<AssociationDto> getAssociationById(@PathVariable Long id) {
        Optional<AssociationDto> associationDto = associationServices.getAssociationById(id);
        return associationDto.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // For creating a new association (token is needed for user identification)
    @PostMapping
    public ResponseEntity<AssociationDto> createAssociation(@RequestBody AssociationDto associationDto, @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(associationServices.createAssociation(associationDto, token));
    }

    // For updating an existing association (token is needed for user identification)
    @PutMapping("/{id}")
    public ResponseEntity<AssociationDto> updateAssociation(@PathVariable Long id, @RequestBody AssociationDto associationDto, @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(associationServices.updateAssociation(id, associationDto, token));
    }

    // For deleting an association (token is needed for user identification)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssociation(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        associationServices.deleteAssociation(id, token);
        return ResponseEntity.noContent().build();
    }
}
