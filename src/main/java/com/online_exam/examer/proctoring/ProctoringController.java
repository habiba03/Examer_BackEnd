package com.online_exam.examer.proctoring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/proctoring")
public class ProctoringController {

    @Autowired
    private ProctoringService proctoringService;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestBody ImageRequest request) {
        try {
            proctoringService.saveScreenshot(request.getStudentId(), request.getExamId(), request.getImage());

            return ResponseEntity.ok().body(Map.of("message", "تم حفظ لقطة الشاشة بنجاح"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("فشل حفظ الصورة: " + e.getMessage());
        }
    }
}
