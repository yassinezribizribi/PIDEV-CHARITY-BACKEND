package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.MissionDTO;
import tn.esprit.examen.nomPrenomClasseExamen.services.MissionServices;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/missions")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class MissionController {
    private final MissionServices missionServices;

    @GetMapping("/by-location/{location}")
    public ResponseEntity<List<MissionDTO>> getMissionsByLocation(@PathVariable String location) {
        return ResponseEntity.ok(missionServices.getMissionsByLocation(location));
    }

    @GetMapping("/by-start-date/{startDate}")
    public ResponseEntity<List<MissionDTO>> getMissionByStartDate(@PathVariable String startDate) {
        return ResponseEntity.ok(missionServices.findMissionByStartDate(startDate));
    }

    @GetMapping("/getall")
    public ResponseEntity<List<MissionDTO>> getAllMissions() {
        // Convert List<Mission> to List<MissionDTO>
        List<MissionDTO> missionDTOs = missionServices.getAllMissions().stream()
                .map(MissionDTO::fromMission)
                .collect(Collectors.toList());
        return ResponseEntity.ok(missionDTOs);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<MissionDTO> getMissionById(@PathVariable Long id) {
        // Convert Mission to MissionDTO
        return ResponseEntity.ok(MissionDTO.fromMission(missionServices.getMissionById(id)));
    }

    @PostMapping("/create")
    public ResponseEntity<MissionDTO> createMission(@RequestBody MissionDTO missionDTO) {
        return ResponseEntity.status(201).body(missionServices.createMission(missionDTO));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<MissionDTO> updateMission(
            @PathVariable Long id,
            @RequestBody MissionDTO updatedMissionDTO) {
        return ResponseEntity.ok(missionServices.updateMission(id, updatedMissionDTO));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteMission(@PathVariable Long id) {
        missionServices.deleteMission(id);
        return ResponseEntity.noContent().build();
    }
}