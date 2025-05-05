package tn.esprit.examen.nomPrenomClasseExamen.Controllers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.UrlResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriberRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Utlis.ValidationUtils;
import tn.esprit.examen.nomPrenomClasseExamen.dto.*;
import tn.esprit.examen.nomPrenomClasseExamen.entities.*;
import tn.esprit.examen.nomPrenomClasseExamen.jwt.JwtUtils;
import tn.esprit.examen.nomPrenomClasseExamen.services.MailerSendService;
import tn.esprit.examen.nomPrenomClasseExamen.services.MissionServices;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.function.Consumer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/missions")
@CrossOrigin(origins = "http://localhost:4200")

@RequiredArgsConstructor
public class MissionController {
    private final MissionServices missionServices;
    private final JwtUtils jwtUtils;
    private final SubscriberRepository subscriberRepository;
    private  final MailerSendService mailerSendService;
    private static final String FILE_DIRECTORY = "C:\\Users\\user\\Desktop\\uploads";

    private static final Logger logger = LoggerFactory.getLogger(MissionController.class);
    @RestController
    @RequestMapping("/api/missions/volunteers")
    public class VolunteerController {

        private final SubscriberRepository subscriberRepository;

        public VolunteerController(SubscriberRepository subscriberRepository) {
            this.subscriberRepository = subscriberRepository;
        }

//        @GetMapping("/{id}/cv")
//        public ResponseEntity<Resource> downloadVolunteerCv(@PathVariable Long id) {
//            Subscriber subscriber = subscriberRepository.findById(id)
//                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Volunteer not found"));
//
//            if (subscriber.getCvFilePath() == null) {
//                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No CV uploaded for this volunteer.");
//            }
//
//            Path filePath = Paths.get("uploads").resolve(subscriber.getCvFilePath()).normalize();
//
//            try {
//                Resource resource = new UrlResource(filePath.toUri());
//                if (!resource.exists()) {
//                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CV file not found.");
//                }
//
//                return ResponseEntity.ok()
//                        .contentType(MediaType.APPLICATION_PDF)
//                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
//                        .body(resource);
//
//            } catch (MalformedURLException ex) {
//                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error loading CV file.");
//            }
//        }
    }
    @GetMapping("/volunteers/{id}/cv")
    public ResponseEntity<Resource> downloadVolunteerCv(@PathVariable Long id) {
        Subscriber subscriber = subscriberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Volunteer not found"));

        String cvFileName = subscriber.getCvFilePath();

        if (cvFileName == null || cvFileName.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No CV uploaded for this volunteer.");
        }

        // Adjust this to your actual upload directory path
        Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads").toAbsolutePath().normalize();
        Path filePath = uploadDir.resolve(cvFileName).normalize();

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CV file not found.");
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error loading CV file.");
        }
    }

    @PatchMapping("/volunteers/{id}/review")
    public ResponseEntity<Map<String, String>> reviewVolunteer(
            @PathVariable Long id,
            @RequestBody ReviewVolunteerRequest request) {

        Subscriber subscriber = subscriberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Volunteer not found"));

        Map<String, String> response = new HashMap<>();

        boolean isApproved = request.isApprove();
        String rejectionReason = request.getRejectionReason();

        if (isApproved) {
            subscriber.setValidationStatus(VolunteerStatus.VALIDATED);
            subscriber.setRejectionReason(null);
            subscriberRepository.save(subscriber);

            missionServices.promotePendingParticipations(subscriber);

            response.put("message", "Volunteer has been validated successfully!");
        } else {
            subscriber.setValidationStatus(VolunteerStatus.NOT_VALIDATED);
            subscriber.setRejectionReason(rejectionReason != null ? rejectionReason : "Rejected without reason");
            subscriberRepository.save(subscriber);

            response.put("message", "Volunteer has been rejected.");
        }

        // ✉️ Send review email with exception handling
        try {
            String emailBody = mailerSendService.createVolunteerReviewEmailBody(
                    subscriber, isApproved, rejectionReason
            );
            mailerSendService.sendEmail(
                    subscriber.getEmail(),
                    isApproved ? "Your Volunteer Application Has Been Validated!" : "Volunteer Application Rejected",
                    emailBody
            );
        } catch (IOException e) {
            // Log the error but continue normal flow
            logger.error("❌ Failed to send review email to volunteer {}: {}", subscriber.getEmail(), e.getMessage(), e);
        }

        return ResponseEntity.ok(response);
    }



    @PostMapping("/upload-cv")
    public ResponseEntity<Map<String, String>> uploadCv(
            @RequestParam("cvFile") MultipartFile cvFile,
            @RequestHeader("Authorization") String token) {

        if (!token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid token format"));
        }

        String jwt = token.substring(7);
        if (!jwtUtils.validateJwtToken(jwt)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or expired token"));
        }

        String email = jwtUtils.getUserNameFromJwtToken(jwt);

        Subscriber subscriber = subscriberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Subscriber not found"));

        if (subscriber.getValidationStatus() == VolunteerStatus.VALIDATED) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "You are already validated. No need to upload CV."));
        }

        try {
            String originalFileName = cvFile.getOriginalFilename();
            if (originalFileName == null || originalFileName.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid CV file name."));
            }

            Path filePath = Paths.get(FILE_DIRECTORY, originalFileName);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, cvFile.getBytes());

            subscriber.setCvFilePath(originalFileName);
            subscriber.setValidationStatus(VolunteerStatus.PENDING_VALIDATION);
            subscriberRepository.save(subscriber);

            return ResponseEntity.ok(Map.of("message", "CV uploaded successfully. Awaiting admin validation."));
        } catch (IOException e) {
            logger.error("Error saving CV file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "File saving error"));
        }
    }


    @GetMapping("/protected/files/{filename}")
    public ResponseEntity<Resource> getCvFile(
            @PathVariable String filename,
            @RequestHeader("Authorization") String token) throws IOException {

        // Validate token format
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Extract and validate JWT
        String jwt = token.substring(7);
        if (!jwtUtils.validateJwtToken(jwt)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Decode filename and resolve path
        String decodedFilename = URLDecoder.decode(filename, StandardCharsets.UTF_8);
        Path filePath = Paths.get(FILE_DIRECTORY, decodedFilename);

        // Verify file existence
        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Create resource and headers
        Resource resource = new UrlResource(filePath.toUri());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
    @PostMapping("/participations/{id}/review")
    public ResponseEntity<?> reviewParticipation(@PathVariable Long id,
                                                 @RequestBody ReviewParticipationDTO participationDTO) {
        missionServices.reviewParticipation(id, participationDTO.getConfirmed(), participationDTO.getRejectionReason());
        return ResponseEntity.ok("Review completed");
    }


    @PostMapping("/mission-roles/{roleId}/apply")
    public ResponseEntity<?> applyToMissionRole(@PathVariable Long roleId) {
        try {
            // Call service to handle the logic
            missionServices.applyToMissionRole(roleId);

            // Success case - Return success message
            return ResponseEntity.ok(Map.of("message", "✅ Application submitted successfully"));

        } catch (IllegalStateException | IllegalArgumentException e) {
            // Handle specific errors
            if (e.getMessage().contains("Mission already started")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "message", "❌ This mission has already started. You cannot apply."
                ));
            }
            if (e.getMessage().contains("Mission role is full")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "message", "❌ This mission role is already full."
                ));
            }
            if (e.getMessage().contains("CV required")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "message", "❌ You need to submit your CV before applying for this role."
                ));
            }
            if (e.getMessage().contains("Double participation")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "message", "❌ You cannot participate in two roles in the same mission. Please cancel one."
                ));
            }

            // Generic error handling
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", e.getMessage()
            ));

        } catch (Exception e) {
            // Catch any unexpected errors
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "❌ An unexpected error occurred."
            ));
        }
    }



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

    @PostMapping("/createmission")
    public ResponseEntity<?> createMission(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("location") String location,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam("status") String status,
            @RequestParam(value = "missionLogo", required = false) MultipartFile missionLogo,
            @RequestParam(value = "missionRoles", required = false) String missionRolesJson, // JSON array as string
            @RequestHeader("Authorization") String token) {

        try {


            String jwt = token.substring(7); // Remove "Bearer "

            MissionDTO missionDto = new MissionDTO();
            missionDto.setTitle(title);
            missionDto.setDescription(description);
            missionDto.setLocation(location);

            // ✅ Convert LocalDate → java.util.Date
            ZoneId zoneId = ZoneId.systemDefault();
            Date utilStartDate = Date.from(startDate.atStartOfDay(zoneId).toInstant());
            Date utilEndDate = Date.from(endDate.atStartOfDay(zoneId).toInstant());
            missionDto.setStartDate(utilStartDate);
            missionDto.setEndDate(utilEndDate);

            missionDto.setStatus(MissionStatus.valueOf(status.toUpperCase()));

            // ✅ Parse mission roles JSON if provided
            if (missionRolesJson != null && !missionRolesJson.isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();
                List<MissionRoleDTO> roles = Arrays.asList(mapper.readValue(missionRolesJson, MissionRoleDTO[].class));
                missionDto.setMissionRoles(roles);
            }

            // ✅ Create mission
            Mission mission = missionServices.createMission(missionDto, missionLogo, jwt);
            return ResponseEntity.ok(mission);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid mission roles format.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating mission: " + e.getMessage());
        }
    }


    @PutMapping("update/{id}")
    public ResponseEntity<?> updateMission(@PathVariable Long id,
                                           @Valid @RequestBody MissionDTO updatedMissionDTO,
                                           BindingResult bindingResult,
                                           @RequestHeader("Authorization") String authorizationHeader) {

        ResponseEntity<?> errorResponse = ValidationUtils.handleValidationErrors(bindingResult);
        if (errorResponse != null) return errorResponse;

        // 🔥 Extract JWT token (remove "Bearer ")
        String jwtToken = authorizationHeader.replace("Bearer ", "").trim();

        Mission updatedMission = missionServices.updateMission(id, updatedMissionDTO, jwtToken);

        return ResponseEntity.ok(MissionDTO.fromMission(updatedMission));
    }

    @GetMapping("/mission-roles/{missionId}")
    public ResponseEntity<?> getMissionRolesByMissionId(@PathVariable Long missionId) {
        try {
            Set<MissionRole> missionRoles = missionServices.getMissionRolesByMissionId(missionId);
            return ResponseEntity.ok(missionRoles);
        } catch (Exception e) {
            logger.error("❌ Error fetching MissionRoles: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }





    @DeleteMapping("delete/{id}")
    public ResponseEntity<Void> deleteMission(@PathVariable Long id) {
        missionServices.deleteMission(id);
        return ResponseEntity.noContent().build();
    }



    // GET /participations/mine
    @GetMapping("/mine")
    public ResponseEntity<List<ParticipationMissionDTO>> getMyMissionParticipations(
            @RequestHeader("Authorization") String jwtToken) {
        String token = jwtToken.replace("Bearer ", "");
        return ResponseEntity.ok(missionServices.getMyMissionParticipations(token));
    }

    @DeleteMapping("/cancel/{id}")
    public ResponseEntity<?> cancelParticipation(@PathVariable Long id, @RequestHeader("Authorization") String jwtToken) {
        String token = jwtToken.replace("Bearer ", "");
        missionServices.cancelParticipation(id, token);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/missions/upcoming")
    public ResponseEntity<String> triggerMissionReminders() {
        missionServices.scheduleAllUpcomingMissionReminders();
        return ResponseEntity.ok("Mission reminders scheduled successfully.");
    }

}