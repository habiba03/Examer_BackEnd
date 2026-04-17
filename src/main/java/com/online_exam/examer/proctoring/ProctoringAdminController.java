package com.online_exam.examer.proctoring;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
@RestController
@RequiredArgsConstructor
public class ProctoringAdminController {

    private final ProctoringService proctoringService;

    @GetMapping("/exam/{examId}/student/{studentId}")
    public ResponseEntity<List<String>> getStudentLogs(
            @PathVariable Long examId,
            @PathVariable Long studentId) throws IOException {

        List<String> logs = proctoringService.getStudentImagesInExam(examId, studentId);
        return ResponseEntity.ok(logs);
    }

//    /// /////////////////////////
//private final ProctoringRepository proctoringRepository;
//    @GetMapping("/exam/{examId}/student/{studentId}/images")
//    public ResponseEntity<List<String>> getStudentImages(
//            @PathVariable Long examId,
//            @PathVariable Long studentId) {
//
//        List<ProctoringEntity> proctoringEntities =
//                proctoringRepository.findByExam_ExamIdAndUser_UserId(examId, studentId);
//
//        // كل صورة تصبح رابط GET
//        List<String> imageUrls = proctoringEntities.stream()
//                .map(p -> "/api/image?path=" + URLEncoder.encode(p.getImageUrl(), StandardCharsets.UTF_8))
//                .toList();
//
//        return ResponseEntity.ok(imageUrls);
//    }
//    private final FileStorageService fileStorageService;
//    @GetMapping("/image")
//    public ResponseEntity<Resource> getImage(@RequestParam String path) throws IOException {
//        Resource resource = fileStorageService.loadImageAsResource(path);
//
//        if (!resource.exists()) return ResponseEntity.notFound().build();
//
//        String contentType = Files.probeContentType(resource.getFile().toPath());
//        return ResponseEntity.ok()
//                .contentType(MediaType.parseMediaType(contentType))
//                .body(resource);
//    }

}
