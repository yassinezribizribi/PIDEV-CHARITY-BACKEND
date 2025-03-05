package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.RequiredArgsConstructor;
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
    //    @GetMapping("/by-location/{location}")
//    public List<MissionDTO> getMissionsByLocation(@PathVariable String location) {
//        return missionServices.getMissionsByLocation(location);
//    }
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

    @PostMapping("create")
    public ResponseEntity<MissionDTO> createMission(@RequestBody MissionDTO missionDTO) {
        MissionDTO createdMission = missionServices.createMission(missionDTO);
        return ResponseEntity.status(201).body(createdMission);
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
