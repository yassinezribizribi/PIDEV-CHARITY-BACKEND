package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.MissionDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Mission;
import tn.esprit.examen.nomPrenomClasseExamen.services.MissionServices;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/missions")
@CrossOrigin(origins = "http://localhost:4200")

@RequiredArgsConstructor
public class MissionController {
    private final MissionServices missionServices;
    private static final Logger logger = LoggerFactory.getLogger(MissionController.class);

//    @GetMapping("/by-location/{location}")
//    public List<MissionDTO> getMissionsByLocation(@PathVariable String location) {
//        return missionServices.getMissionsByLocation(location);
//    }

    @GetMapping("/my-missions")
    public ResponseEntity<?> getMissionsByAssociationIdFromToken(HttpServletRequest request) {
        try {
            // Extract the JWT token from the request header
            String jwtToken = request.getHeader("Authorization").substring(7);  // Remove "Bearer " prefix
            logger.info("📝 Fetching missions for the association from the token");

            // Call the service method to get the missions
            List<Mission> missions = missionServices.getMissionsByAssociationIdFromToken(jwtToken);
            return ResponseEntity.ok(missions);

        } catch (Exception e) {
            logger.error("❌ Error occurred while fetching missions for the association: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/by-location/{location}")
    public List<MissionDTO> getMissionsByLocation(@PathVariable String location) {
        return missionServices.getMissionsByLocation(location);
    }

    @GetMapping("/by-start-date/{startDate}")
    public ResponseEntity<List<MissionDTO>> getMissionByStartDate(@PathVariable String startDate) {
        List<MissionDTO> missions = missionServices.findMissionByStartDate(startDate);
        return ResponseEntity.ok(missions);
    }

    @GetMapping("getall")
    public ResponseEntity<List<MissionDTO>> getAllMissions() {
        List<MissionDTO> missions = missionServices.getAllMissions()
                .stream()
                .map(MissionDTO::fromMission) // Convert each Mission to MissionDTO
                .collect(Collectors.toList());
        return ResponseEntity.ok(missions);
    }

    @GetMapping("get/{id}")
    public ResponseEntity<MissionDTO> getMissionById(@PathVariable Long id) {
        return ResponseEntity.ok(MissionDTO.fromMission(missionServices.getMissionById(id)));
    }
    @PostMapping("/create")
    public ResponseEntity<?> createMission(@RequestBody MissionDTO missionDTO,
                                           @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // 🔐 Extract the token from the Authorization header
            String jwtToken = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;

            logger.info("📝 POST request received to create a mission: {}", missionDTO);

            // 👉 Call the service layer with token
            MissionDTO createdMission = missionServices.createMission(missionDTO, jwtToken);

            return ResponseEntity.ok(createdMission);
        } catch (Exception e) {
            logger.error("❌ Error occurred while creating mission: {}", e.getMessage(), e);

            // 💬 Return a helpful error message
            String errorMessage = e instanceof RuntimeException ? e.getMessage() : "Unexpected error occurred";
            return ResponseEntity.internalServerError().body("Error: " + errorMessage);
        }
    }


    @PutMapping("update/{id}")
    public ResponseEntity<MissionDTO> updateMission(@PathVariable Long id, @RequestBody MissionDTO updatedMissionDTO) {
        MissionDTO updatedMission = missionServices.updateMission(id, updatedMissionDTO);
        return ResponseEntity.ok(updatedMission);
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<Void> deleteMission(@PathVariable Long id) {
        missionServices.deleteMission(id);
        return ResponseEntity.noContent().build();
    }
}
